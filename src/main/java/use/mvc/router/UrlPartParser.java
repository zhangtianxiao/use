package use.mvc.router;


import cn.hutool.core.util.CharUtil;
import org.jetbrains.annotations.NotNull;
import use.kit.ex.Unsupported;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

@SuppressWarnings("WeakerAccess")
public final class UrlPartParser {

  public static final byte SLASH = '/';

  private static final Charset CHARSET = ISO_8859_1;


  private final byte[] raw;
  private final short limit;
  private final short offset;


  private final short pathEnd;
  short pos = 0;

  // region creators
  private UrlPartParser(byte[] raw, short offset, short limit) {
    this.raw = raw;
    this.offset = offset;
    this.limit = limit;
    this.pathEnd = limit;
  }

  public static @NotNull UrlPartParser of(@NotNull String url) {
    return of(url.getBytes(ISO_8859_1), 0, url.length());
  }

  public static @NotNull UrlPartParser of(byte[] url, int offset, int limit) {
    UrlPartParser httpUrl = createParser(url, offset, limit);
    return httpUrl;
  }

  public static @NotNull UrlPartParser parse(@NotNull String url) {
    return parse(url.getBytes(ISO_8859_1), 0, url.length());
  }

  public static @NotNull UrlPartParser parse(byte[] url, int offset, int limit) {
    UrlPartParser httpUrl = createParser(url, offset, limit);
    return httpUrl;
  }
  // endregion

  private static UrlPartParser createParser(byte[] url, int offset, int limit) {
    if (limit <= Short.MAX_VALUE) {
      return new UrlPartParser(url, (short) offset, (short) limit);
    }
    int urlLength = limit - offset;
    if (urlLength > Short.MAX_VALUE) {
      throw new Unsupported("URL length exceeds " + Short.MAX_VALUE + " bytes");
    }
    byte[] urlBytes = new byte[urlLength];
    System.arraycopy(url, offset, urlBytes, 0, urlLength);
    return new UrlPartParser(urlBytes, (short) 0, (short) urlLength);
  }

  /**
   暂时不做uri decode
   */
  String pollUrlPart() {
    if (pos < pathEnd) {
      int start = pos + 1;
      int nextSlash = indexOf(SLASH, start);
      pos = nextSlash > pathEnd ? pathEnd : (short) nextSlash;
      boolean needDecode = indexOf((byte) '%', start) != -1;
      String part;
      if (pos == -1) {
        part = needDecode ? new String(decode(raw, start, pathEnd, true), StandardCharsets.UTF_8) : new String(raw, start, pathEnd - start, CHARSET);
        pos = limit;
      } else {
        part = needDecode ? new String(decode(raw, start, pos, true), StandardCharsets.UTF_8) : new String(raw, start, pos - start, CHARSET);
      }
    /*  if (pos == -1) {
        part = new String(raw, start, pathEnd - start, CHARSET);
        pos = limit;
      } else {
        part = new String(raw, start, pos - start, CHARSET);
      }*/
      return part;
      //return part;
    } else {
      return "";
    }
  }

  private int indexOf(byte b, int from) {
    for (int i = from; i < limit; i++) {
      if (raw[i] == b) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public String toString() {
    return new String(raw, offset, limit - offset, CHARSET);
  }

  public static byte[] decode(byte[] bytes, int start, int end, boolean isPlusToSpace) {
    if (bytes == null) {
      return null;
    }
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream(bytes.length);
    int b;
    for (int i = start; i < end; i++) {
      b = bytes[i];
      if (b == '+') {
        buffer.write(isPlusToSpace ? CharUtil.SPACE : b);
      } else if (b == '%') {
        if (i + 1 < bytes.length) {
          final int u = CharUtil.digit16(bytes[i + 1]);
          if (u >= 0 && i + 2 < bytes.length) {
            final int l = CharUtil.digit16(bytes[i + 2]);
            if (l >= 0) {
              buffer.write((char) ((u << 4) + l));
              i += 2;
              continue;
            }
          }
        }
        // 跳过不符合规范的%形式
        buffer.write(b);
      } else {
        buffer.write(b);
      }
    }
    return buffer.toByteArray();
  }
}
