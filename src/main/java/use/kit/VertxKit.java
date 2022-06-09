package use.kit;

import io.netty.buffer.Unpooled;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class VertxKit {
  public static Buffer asBuffer(CharSequence string) {
    return Buffer.buffer(NettyKit.asNettyBuf(string));
  }

  public static Buffer asBuffer(ByteBuffer buf) {
    return Buffer.buffer(NettyKit.asNettyBuf(buf));
  }

  public static Buffer writeJson(Object o) {
    return Buffer.buffer(NettyKit.writeJson(o));
  }

  public static <T> T await(Future<T> f) {
    try {
      return f.toCompletionStage().toCompletableFuture().get();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }


  @NotNull
  public static <T> T get(Object key) {
    T local = (T) Vertx.currentContext().getLocal(key);
    Objects.requireNonNull(local);
    return local;
  }

  public static void put(Object key, Object value) {
    Vertx.currentContext().putLocal(key, value);
  }

  public static Vertx current() {
    return Vertx.currentContext().owner();
  }
}
