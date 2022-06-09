package use.ext.jsoniter;

import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Encoder;
import com.jsoniter.spi.JsonException;
import com.jsoniter.spi.JsoniterSpi;
import use.kit.Digit2UTF8;
import use.kit.Helper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetTime;
import java.util.Date;

public class JdkOffsetTimeSupport {
  public static void main(String[] args) {
    OffsetTime now = OffsetTime.now();
    Date date = new Date();
    // 本地变量 fmt
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    byte[] bytes = null;
    String format = null;
    for (int i = 0; i < 10000; i++) {
      bytes = OffsetTimeBytes(now, 0);
      format = fmt.format(date);
    }
    long l, r1, r2 = 0, loop = 10000;
    l = System.nanoTime();
    for (int i = 0; i < loop; i++) {
      bytes = OffsetTimeBytes(now, 0);
    }
    r1 = System.nanoTime() - l;

    l = System.nanoTime();
    for (int i = 0; i < loop; i++) {
      format = fmt.format(date);
    }
    r2 = System.nanoTime() - l;
    System.out.println(new String(bytes));
    System.out.println(format);
    System.out.println("循环" + loop + "次, 快了: " + (r2 - r1) + "ns");
  }

  private static byte[] OffsetTimeBytes(OffsetTime that, int quote) {
    if (quote == 0 | quote == 1) {
    }
    else throw new JsonException("quote only be 0 or 1");

    int n = quote;
    byte[] bytes = new byte[8 + quote * 2];
    if (quote != 0) bytes[0] = '"';


    int hour = that.getHour();
    int minute = that.getMinute();
    int second = that.getSecond();

    if (hour < 10) bytes[n++] = '0';
    n += Digit2UTF8.toBytes(bytes, hour, n);
    bytes[n++] = ':';

    if (minute < 10) bytes[n++] = '0';
    n += Digit2UTF8.toBytes(bytes, minute, n);
    bytes[n++] = ':';

    if (second < 10) bytes[n++] = '0';
    n += Digit2UTF8.toBytes(bytes, second, n);
    if (quote != 0) bytes[n] = '"';
    return bytes;
  }

  static final Encoder.ReflectionEncoder reflectionEncoder = new Encoder.ReflectionEncoder() {
    @Override
    public void encode(Object obj, JsonStream stream) throws IOException {
      //stream.writeVal(new SimpleDateFormat().format(obj));
      //String val = year + "-" + (month < 10 ? 0 : "") + month + "-" + (day < 10 ? 0 : "") + day;
      stream.write(OffsetTimeBytes((OffsetTime) obj, 1));
      //stream.writeVal(val);
    }

    @Override
    public Any wrap(Object obj) {
      byte[] bytes = OffsetTimeBytes((OffsetTime) obj, 1);
      return Any.lazyString(bytes, 0, bytes.length);
    }
  };

  public static void enable() {
    JsoniterSpi.registerTypeEncoder(OffsetTime.class, reflectionEncoder);
    JsoniterSpi.registerTypeDecoder(OffsetTime.class, iter -> {
      try {
        String str = iter.readString();
        if (str == null || str.equals("")) return null;
        int i = str.indexOf('-');
        int hour = Integer.parseInt(str.substring(i + 7, i + 9));
        int minute = Integer.parseInt(str.substring(i + 10, i + 12));
        int second = Integer.parseInt(str.substring(i + 13, i + 15));
        OffsetTime ret = OffsetTime.of(hour, minute, second, 0, Helper.zoneOffset);
        //return new SimpleDateFormat().parse(iter.readString());
        return ret;
      } catch (Exception e) {
        throw new JsonException(e);
      }
    });
    JsonStream.registerNativeEncoder(OffsetTime.class, reflectionEncoder);
  }
}
