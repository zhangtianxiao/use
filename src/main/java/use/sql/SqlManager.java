package use.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import use.template.Engine;
import use.template.source.ISource;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 不是线程安全的, 应在程序启动时最好初始化,
 与db无关
 */
public abstract class SqlManager {
  public final Engine engine;
  final HashMap<String, HashSet<String>> keys = new HashMap<>();
  final HashMap<String, SqlTemplate> sqlTemplateMap = new HashMap<>();
  final List<ISource> sourceList = new ArrayList<>();

  protected SqlManager(Engine engine) {
    this.engine = engine;
    engine.config.addDirective("write", WriterDirective.class);
  }

  public abstract void add(String file);

  public abstract void onChange(ISource source);

  @NotNull
  public SqlTemplate get(String key) {
    return Objects.requireNonNull(getOrNull(key));
  }

  @Nullable
  public SqlTemplate getOrNull(String key) {
    return sqlTemplateMap.get(key);
  }

  public void watch() {
    for (ISource iSource : sourceList) {
      if (iSource.isModified())
        onChange(iSource);
    }
  }

  static final Pattern pattern = Pattern.compile(":([\\da-zA-Z]+)");

  protected String normalize(String text) {
    Matcher matcher = pattern.matcher(text);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      //String full = matcher.group(0);
      int start = matcher.start();
      if (start > 0 && text.charAt(start - 1) == ':') {
        continue;
      }
      String part = matcher.group(1);
      matcher.appendReplacement(sb, "#(" + part + ")");
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
