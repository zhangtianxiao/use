package use.mvc.router;

import cn.hutool.core.util.ArrayUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 用于后续定制基于任何ioc/httpserver的, mvc框架
 rem: spring-web的path variable格式为 /home/{id}, 但本类实现为/home/:id, 与vertx内置实现一置
 !
 */
public final class ActionRouter implements RouteHandler {
  private static final String[] HTTP_METHODS = {"GET", "PUT", "POST", "HEAD", "DELETE", "CONNECT", "OPTIONS", "TRACE", "PATCH", "SEARCH", "COPY", "MOVE", "LOCK", "UNLOCK", "MKCOL", "PROPFIND", "PROPPATCH"};

  private static final String ROOT = "/";
  private static final String STAR = "*";
  private static final String WILDCARD = "/" + STAR;

  private static final int WS_ORDINAL = HTTP_METHODS.length;
  private static final int ANY_HTTP_ORDINAL = WS_ORDINAL + 1;

  private final RouteHandler[] rootHandlers = new RouteHandler[ANY_HTTP_ORDINAL + 1];
  private final RouteHandler[] fallbackHandlers = new RouteHandler[ANY_HTTP_ORDINAL + 1];

  private final Map<String, ActionRouter> routes = new HashMap<>();
  private final Map<String, ActionRouter> parameters = new HashMap<>();

  private ActionRouter() {
  }

  public static ActionRouter create() {
    return new ActionRouter();
  }


  public static void checkArgument(boolean expression, Object message) {
    if (!expression) {
      throw new IllegalArgumentException(String.valueOf(message));
    }
  }

  public ActionRouter route(@NotNull String path, @NotNull RouteHandler handler) {
    return route(null, path, handler);
  }

  @Contract("_, _, _ -> this")
  public ActionRouter route(@Nullable String method, @NotNull String path, @NotNull RouteHandler handler) {
    return doMap(method == null ? ANY_HTTP_ORDINAL : ArrayUtil.indexOf(HTTP_METHODS, method), path, handler);
  }

  @Contract("_, _, _ -> this")
  private ActionRouter doMap(int ordinal, @NotNull String path, @NotNull RouteHandler handler) {
    checkArgument(path.startsWith(ROOT) && (path.endsWith(WILDCARD) || !path.contains(STAR)), "Invalid path: " + path);
    if (path.endsWith(WILDCARD)) {
      makeSubtree(path.substring(0, path.length() - 2)).mapFallback(ordinal, handler);
    } else {
      makeSubtree(path).route(ordinal, handler);
    }
    return this;
  }

  public void visit(Visitor visitor) {
    visit(ROOT, visitor);
  }

  private void visit(String prefix, Visitor visitor) {
    for (int i = 0; i < rootHandlers.length; i++) {
      RouteHandler rootHandler = rootHandlers[i];
      if (rootHandler != null) {
        String method = i == WS_ORDINAL || i == ANY_HTTP_ORDINAL ? null : HTTP_METHODS[i];
        visitor.accept(method, prefix, rootHandler);
      }
    }
    for (int i = 0; i < fallbackHandlers.length; i++) {
      RouteHandler fallbackHandler = fallbackHandlers[i];
      if (fallbackHandler != null) {
        String method = i == WS_ORDINAL || i == ANY_HTTP_ORDINAL ? null : HTTP_METHODS[i];
        visitor.accept(method, prefix, fallbackHandler);
      }
    }
    routes.forEach((route, subtree) -> subtree.visit(prefix + route + "/", visitor));
    parameters.forEach((route, subtree) -> subtree.visit(prefix + ":" + route + "/", visitor));
  }

  public @Nullable
  ActionRouter getSubtree(String path) {
    return getOrCreateSubtree(path, (handler, name) ->
      name.startsWith(":") ?
        handler.parameters.get(name.substring(1)) :
        handler.routes.get(name));
  }

  @Contract("_ -> new")
  public ActionRouter merge(ActionRouter handler) {
    return merge(ROOT, handler);
  }

  @Contract("_, _ -> new")
  public ActionRouter merge(String path, ActionRouter handler) {
    ActionRouter merged = new ActionRouter();
    mergeInto(merged, this);
    mergeInto(merged.makeSubtree(path), handler);
    return merged;
  }

  @Override
  public @NotNull
  void doHandle(Action ctx) {
    tryServe(ctx);
  }

  private void route(int ordinal, @NotNull RouteHandler handler) {
    doMerge(rootHandlers, ordinal, handler);
  }

  private void mapFallback(int ordinal, @NotNull RouteHandler handler) {
    doMerge(fallbackHandlers, ordinal, handler);
  }

  private void doMerge(RouteHandler[] handlers, int ordinal, RouteHandler handler) {
    if (handlers[ordinal] != null) {
      throw new IllegalArgumentException("Already mapped");
    }
    handlers[ordinal] = handler;
  }

  @Nullable
  private void tryServe(Action action) {
    UrlPartParser parser = action.urlParser();
    short introPosition = parser.pos;
    String urlPart = parser.pollUrlPart();
    // int ordinal =  ctx.method .ordinal();
    int ordinal = ArrayUtil.indexOf(HTTP_METHODS, action.method());

    if (urlPart.isEmpty()) {
      // 最后一个节点
      RouteHandler handler = getOrDefault(rootHandlers, ordinal);
      if (handler != null) {
        action.setMatched(true);
        handler.doHandle(action);
        return;
      }
    } else {
      // 获取下一个节点
      short position = parser.pos;
      ActionRouter transit = routes.get(urlPart);
      if (transit != null) {
        transit.tryServe(action);
        if (action.matched) {
          return;
        }
        parser.pos = position;
      }
      for (Map.Entry<String, ActionRouter> entry : parameters.entrySet()) {
        String key = entry.getKey();
        action.putPathParameter(key, urlPart);
        entry.getValue().tryServe(action);
        if (action.matched) {
          return;
        }
        action.removePathParameter(key);
        parser.pos = position;
      }
    }
    // 无匹配
    RouteHandler handler = getOrDefault(fallbackHandlers, ordinal);
    if (handler != null) {
      parser.pos = introPosition;
      action.setMatched(true);
      handler.doHandle(action);
    }
  }

  private ActionRouter makeSubtree(String path) {
    return getOrCreateSubtree(path, (handler, name) ->
      name.startsWith(":") ?
        handler.parameters.computeIfAbsent(name.substring(1), $ -> new ActionRouter()) :
        handler.routes.computeIfAbsent(name, $ -> new ActionRouter()));
  }

  private ActionRouter getOrCreateSubtree(@NotNull String path, BiFunction<ActionRouter, String, ActionRouter> childGetter) {
    if (path.isEmpty() || path.equals(ROOT)) {
      return this;
    }
    ActionRouter sub = this;
    int slash = path.indexOf('/', 1);
    String remainingPath = path;
    while (true) {
      String urlPart = remainingPath.substring(1, slash == -1 ? remainingPath.length() : slash);

      if (urlPart.isEmpty()) {
        return sub;
      }
      sub = childGetter.apply(sub, urlPart);

      if (slash == -1 || sub == null) {
        return sub;
      }
      remainingPath = remainingPath.substring(slash);
      slash = remainingPath.indexOf('/', 1);
    }
  }

  public static ActionRouter merge(ActionRouter first, ActionRouter second) {
    ActionRouter merged = new ActionRouter();
    mergeInto(merged, first);
    mergeInto(merged, second);
    return merged;
  }

  public static ActionRouter merge(ActionRouter... handlers) {
    ActionRouter merged = new ActionRouter();
    for (ActionRouter handler : handlers) {
      mergeInto(merged, handler);
    }
    return merged;
  }

  private static void mergeInto(ActionRouter into, ActionRouter from) {
    for (int i = 0; i < from.rootHandlers.length; i++) {
      RouteHandler rootHandler = from.rootHandlers[i];
      if (rootHandler != null) {
        into.route(i, rootHandler);
      }
    }
    for (int i = 0; i < from.fallbackHandlers.length; i++) {
      RouteHandler fallbackHandler = from.fallbackHandlers[i];
      if (fallbackHandler != null) {
        into.mapFallback(i, fallbackHandler);
      }
    }
    from.routes.forEach((key, value) ->
      into.routes.merge(key, value, (s1, s2) -> {
        mergeInto(s1, s2);
        return s1;
      }));
    from.parameters.forEach((key, value) ->
      into.parameters.merge(key, value, (s1, s2) -> {
        mergeInto(s1, s2);
        return s1;
      }));
  }

  private static @Nullable
  RouteHandler getOrDefault(RouteHandler[] handlers, int ordinal) {
    RouteHandler maybeResult = handlers[ordinal];
    if (maybeResult != null || ordinal == WS_ORDINAL) {
      return maybeResult;
    }
    return handlers[ANY_HTTP_ORDINAL];
  }

  @FunctionalInterface
  public interface Visitor {
    void accept(@Nullable String method, String path, RouteHandler handler);
  }
}
