package use.ext.jsoniter;

import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Encoder;
import com.jsoniter.spi.JsonException;
import com.jsoniter.spi.JsoniterSpi;
import use.kit.Digit2UTF8;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class JdkLocalDateTimeSupport {
  public static void main(String[] args) {
    LocalDateTime now = LocalDateTime.now();
    Date date = new Date();
    // 本地变量 fmt
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    byte[] bytes = null;
    String format = null;
    for (int i = 0; i < 10000; i++) {
      bytes = LocalDateTimeBytes(now, 0);
      format = fmt.format(date);
    }
    long l, r1, r2 = 0, loop = 10000;
    l = System.nanoTime();
    for (int i = 0; i < loop; i++) {
      bytes = LocalDateTimeBytes(now, 0);
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

  public static byte[] LocalDateTimeBytes(LocalDateTime that, int quote) {
    if (quote == 0 | quote == 1) {
    }
    else throw new JsonException("quote only be 0 or 1");
    int year = that.getYear();

    int n = quote;
    int m = Digit2UTF8.stringSize(year);
    byte[] bytes = new byte[10 + 1 + 8 + quote * 2];
    for (int i = 0; i < m - 4; i++)
      bytes[n++] = '0';
    if (quote != 0) bytes[0] = '"';

    n += Digit2UTF8.toBytes(bytes, year, n);
    bytes[n++] = '-';

    int month = that.getMonthValue();
    int day = that.getDayOfMonth();
    int hour = that.getHour();
    int minute = that.getMinute();
    int second = that.getSecond();

    if (month < 10) bytes[n++] = '0';
    n += Digit2UTF8.toBytes(bytes, month, n);
    bytes[n++] = '-';

    if (day < 10) bytes[n++] = '0';
    n += Digit2UTF8.toBytes(bytes, day, n);
    bytes[n++] = ' ';


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

  static  final    Encoder.ReflectionEncoder reflectionEncoder = new Encoder.ReflectionEncoder() {
    @Override
    public void encode(Object obj, JsonStream stream) throws IOException {
      //stream.writeVal(new SimpleDateFormat().format(obj));
      //String val = year + "-" + (month < 10 ? 0 : "") + month + "-" + (day < 10 ? 0 : "") + day;
      stream.write(LocalDateTimeBytes((LocalDateTime) obj, 1));
      //stream.writeVal(val);
    }

    @Override
    public Any wrap(Object obj) {
      byte[] bytes = LocalDateTimeBytes((LocalDateTime) obj, 1);
      return Any.lazyString(bytes, 0, bytes.length);
    }
  };

  public static void enable() {
    JsoniterSpi.registerTypeEncoder(LocalDateTime.class, reflectionEncoder);
    JsoniterSpi.registerTypeDecoder(LocalDateTime.class, iter -> {
      try {
        String str = iter.readString();
        if (str == null || str.equals("")) return null;
        int i = str.indexOf('-');
        int year = Integer.parseInt(str.substring(0, i));
        int month = Integer.parseInt(str.substring(i + 1, i + 3));
        int day = Integer.parseInt(str.substring(i + 4, i + 6));
        int hour = Integer.parseInt(str.substring(i + 7, i + 9));
        int minute = Integer.parseInt(str.substring(i + 10, i + 12));
        int second = Integer.parseInt(str.substring(i + 13, i + 15));
        LocalDateTime date = LocalDateTime.of(year, month, day, hour, minute, second, 0);
        //return new SimpleDateFormat().parse(iter.readString());
        return date;
      } catch (Exception e) {
        throw new JsonException(e);
      }
    });

    JsonStream.registerNativeEncoder(LocalDateTime.class, reflectionEncoder);
  }
}
