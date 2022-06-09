package use.kit;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.system.SystemUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jsoniter.any.Any;
import com.jsoniter.extra.JdkDatetimeSupport;
import com.jsoniter.output.JsonStream;
import com.jsoniter.output.JsonStreamPool;
import com.jsoniter.spi.JsonException;
import com.jsoniter.spi.JsoniterSpi;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import use.ext.jsoniter.*;
import use.template.Engine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Predicate;

public class Helper {
  public static final Charset CHARSET_JNU = Charset.forName(System.getProperty("sun.jnu.encoding"));
  public static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

  public static Any lazyObject(String str) {
    return lazyObject(str.getBytes(CHARSET_UTF8));
  }

  public static Any lazyArray(String str) {
    return lazyArray(str.getBytes(CHARSET_UTF8));
  }

  public static Any lazyObject(byte[] bytes) {
    return Any.lazyObject(bytes, 0, bytes.length);
  }

  public static Any lazyArray(byte[] bytes) {
    return Any.lazyArray(bytes, 0, bytes.length);
  }

  public static Any lazyObject(File file) {
    byte[] bytes = FileUtil.readBytes(file);
    return lazyObject(bytes);
  }

  public static Any lazyArray(File file) {
    byte[] bytes = FileUtil.readBytes(file);
    return lazyArray(bytes);
  }

  public static Any lazy(File file) {
    byte[] bytes = FileUtil.readBytes(file);
    int i = 0;
    while (true) {
      byte aByte = bytes[i];
      if (aByte == '\n' || aByte == '\r' || aByte == ' ') {
      } else {
        if (aByte == '[')
          return Any.lazyArray(bytes, 0, bytes.length);
        else if (aByte == '{') {
          return Any.lazyObject(bytes, 0, bytes.length);
        }
      }
      i++;
    }
  }

  public static final Any ENV = lazyObject(new File("env.json"));


  public static final Engine engine = Engine.byClassPath("global");

  public static final Logger logger = Helper.getLogger(Helper.class);

  public static Logger getLogger(Class<?> c) {
    return LoggerFactory.getLogger(c);
  }


  public static Logger getLogger(String c) {
    return LoggerFactory.getLogger(c);
  }

  public static <T> T as(Object o) {
    return (T) o;
  }

  public static Object[] objects(Object... arr) {
    return arr;
  }

  public static Long[] longs(Long... arr) {
    return arr;
  }

  public static String[] strings(String... arr) {
    return arr;
  }

  private static final ObjectMapper defaultObjectMapper = new ObjectMapper();
  private static ObjectMapper objectMapper = defaultObjectMapper;

  public static void setObjectMapper(ObjectMapper mapper) {
    if (objectMapper != defaultObjectMapper) {
      logger.warn("objectMapper仅能修改一次");
    }
    objectMapper = mapper;
  }

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public static ObjectNode createJsonObject() {
    return objectMapper.createObjectNode();
  }

  public static ArrayNode createJsonArray() {
    return objectMapper.createArrayNode();
  }

  public static JsonNode readAsJson(File file) {
    byte[] bytes = FileUtil.readBytes(file);
    return createJsonNode(bytes);
  }

  public static JsonNode createJsonNode(byte[] b) {
    return createJsonNode(b, 0, b.length);
  }

  public static JsonNode createJsonNode(byte[] b, int head, int tail) {
    try {
      return objectMapper.createParser(b, head, tail - head).readValueAs(JsonNode.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  // ZoneOffset没有直接给默认的offset, 需要通过其他api获取
  public static final ZonedDateTime INIT_AT = ZonedDateTime.now();
  public static final ZoneOffset zoneOffset = INIT_AT.getOffset();

  public static boolean close(@Nullable DataSource ds) {
    if (ds == null) return false;
    if (ds instanceof AutoCloseable it) {
      try {
        it.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return true;
    }
    logger.warn("无法close数据源: {}", ds);
    // throw new Unsupported("无法close数据源: "+ds})
    return false;
  }

  public static RuntimeException asRuntimeException(Throwable e) {
    RuntimeException ret = e instanceof RuntimeException e2 ? e2 : new RuntimeException(e);
    return ret;
  }

  public static final Predicate<Field> NORMAL_FIELD = it -> {
    int m = it.getModifiers();
    if (Modifier.isTransient(m))
      return false;
    if (Modifier.isStatic(m))
      return false;
    return true;
  };

  public static boolean isList(Class<?> type) {
    return Collection.class.isAssignableFrom(type);
  }

  public static StringBuilder jvminfo() {
    StringBuilder sb = new StringBuilder();
    sb.append("pid: ").append(SystemUtil.getCurrentPID()).append("   ").append(Thread.currentThread()).append("\n\n");
    sb.append(SystemUtil.getJvmInfo()).append("\n");

    sb.append("total memory: ").append(DataSizeUtil.format(SystemUtil.getTotalMemory())).append("\n");
    sb.append("free  memory: ").append(DataSizeUtil.format(SystemUtil.getFreeMemory())).append("\n");
    sb.append("max   memory: ").append(DataSizeUtil.format(SystemUtil.getMaxMemory())).append("\n\n");

    ArrayList<Thread> userThreads = new ArrayList<Thread>();
    ArrayList<Thread> systemThreads = new ArrayList<Thread>();
    Thread.getAllStackTraces().keySet().stream().sorted((a, b) -> (int) (a.getId() - b.getId())).filter(thread -> !(thread.getName().contains("Coroutines") || thread.getName().contains("Ctrl-Break") || thread.getThreadGroup().getName().equals("InnocuousThreadGroup")))
      .forEach(thread -> {
        if (thread.getThreadGroup().getName().equals("system"))
          systemThreads.add(thread);
        else
          userThreads.add(thread);
      });
    sb.append("user threads: \n");
    for (int i = 0; i < userThreads.size(); i++) {
      Thread thread = userThreads.get(i);
      sb.append(i + 1).append(" ").append(thread.getThreadGroup().getName()).append(" ").append(thread.getName()).append(" ").append(thread.getState().name().toLowerCase()).append("\n");
    }

    sb.append("\n");

    sb.append("system threads: \n");
    for (int i = 0; i < systemThreads.size(); i++) {
      Thread thread = systemThreads.get(i);
      sb.append(i + 1).append(" ").append(thread.getThreadGroup().getName()).append(" ").append(thread.getName()).append(" ").append(thread.getState().name().toLowerCase()).append("\n");
    }

    // 手动制造内存泄漏
    // 由于没有调用recycle, pool items不会增加,
    // 由于没有调用recycle, created 和 unrecycledBufs 一直在增加
  /*ByteBufPool.allocate(1).let {
      println(System.identityHashCode(it))
      //   it.recycle()
    }
    ByteBufPool.allocate(10).let {
      println(System.identityHashCode(it))
      // it.recycle()
    }*/
    sb.append("\n");
    /*sb.append("ByteBufs:\n");
    //sb.append(ByteBufPool.getStats().poolItemsString)
    //sb.append("\n")
    sb.append("getCreatedItems: ").append(ByteBufPool.getStats().getCreatedItems()).append("\n");
    sb.append("getPoolItems: ").append(ByteBufPool.getStats().getPoolItems()).append("\n");
    sb.append("getPoolSizeKB: ").append(ByteBufPool.getStats().getPoolSizeKB()).append("\n");
    sb.append("getReusedItems: ").append(ByteBufPool.getStats().getReusedItems()).append("\n");
    sb.append("getUnrecycledBufs: ").append(ByteBufPool.getStats().getUnrecycledBufs().size()).append("\n");*/
    // 这两项没看明白具体意思
    // sb.append("totalEvicted: ${ByteBufPool.getStats().totalEvicted}\n")
    // sb.append("totalSlabMins: ${ByteBufPool.getStats().totalSlabMins}\n")
    return sb;
  }


  public static void Asset(boolean b, String s) {
    if (!b) throw new AssertionError(s);
  }

  public static boolean Assert(boolean b) {
    if (!b)
      throw new RuntimeException("预期之外的结果");
    return true;
  }

  public static boolean Assert(Object a, Object b) {
    if (a == null && b == null)
      return Assert(true);
    else if (a == null || b == null) {
      return Assert(false);
    } else if (a instanceof Comparable a1 && b instanceof Comparable b1) {
      return Assert(a1.compareTo(b1) == 0);
    } else if (a.getClass().isArray() && b.getClass().isArray()) {
      return Assert(ArrayUtil.equals(a, b));
    } else {
      return Assert(a.equals(b));
    }
  }

  static final com.jsoniter.spi.Config JsoniterConfigForPrint;

  static {
    JdkDatetimeSupport.enable("yyyy-MM-dd HH:mm:ss");

    JdkLocalTimeSupport.enable();
    JdkLocalDateSupport.enable();
    JdkLocalDateTimeSupport.enable();

    JdkOffsetTimeSupport.enable();
    JdkOffsetDateTimeSupport.enable();

    JacksonSupport.enable();

    com.jsoniter.spi.Config currentConfig = JsoniterSpi.getCurrentConfig();
    com.jsoniter.spi.Config.Builder builderForPrint = currentConfig.copyBuilder();
    builderForPrint.escapeUnicode(false);
    builderForPrint.indentionStep(2);
    JsoniterConfigForPrint = builderForPrint.build();

    com.jsoniter.spi.Config.Builder builder = currentConfig.copyBuilder();
    builder.escapeUnicode(false);
    com.jsoniter.spi.Config config = builder.build();
    JsoniterSpi.setCurrentConfig(config);
    JsoniterSpi.setDefaultConfig(config);

  }

  public static void printAsJson(Object obj) {
    printAsJson(obj, true);
  }

  public static void printAsJson(Object obj, boolean pretty) {
    // 修改当前配置
    com.jsoniter.spi.Config current = JsoniterSpi.getCurrentConfig();
    JsoniterSpi.setCurrentConfig(pretty ? JsoniterConfigForPrint : current);
    try {
      JsonStream stream = JsonStreamPool.borrowJsonStream();
      try {
        try {
          stream.reset(System.out);
          stream.writeVal(obj);
          // 就很奇怪, 默认实现中, 仅仅在finally中调用了close, 而没有提前调用flush
          stream.flush();
          System.out.write('\n');
        } finally {
          stream.reset(null);
          // close也很不合理
          //stream.close();
        }
      } catch (IOException e) {
        throw new JsonException(e);
      } finally {
        JsonStreamPool.returnJsonStream(stream);
      }
    } finally {
      // 重置为默认配置
      JsoniterSpi.clearCurrentConfig();
    }

    //JsonStream.serialize(pretty ? JsoniterConfigForPrint : current, obj, System.out);
  }


}
