package use.kit;

import com.jsoniter.output.JsonStream;
import com.jsoniter.output.JsonStreamPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static java.lang.Character.*;

public class NettyKit {
  public static io.netty.buffer.ByteBuf byteBuf(int i) {
    return PooledByteBufAllocator.DEFAULT.buffer(i);
  }

  public static ByteBuf writeJson(Object o) {
    ByteBuf byteBuf = byteBuf(2048);
    ByteBufOutputStream bos = new ByteBufOutputStream(byteBuf);
    JsonStream stream = JsonStreamPool.borrowJsonStream();
    stream.reset(bos);
    try {
      stream.writeVal(o);
      stream.flush();
    } catch (Throwable e) {
      bos.buffer().release();
      throw Helper.asRuntimeException(e);
    } finally {
      stream.reset(null);
      JsonStreamPool.returnJsonStream(stream);
    }
    return bos.buffer();
  }

  public static io.netty.buffer.ByteBuf asNettyBuf(byte[] buf) {
    return Unpooled.wrappedBuffer(buf);
  }

  public static io.netty.buffer.ByteBuf asNettyBuf(ByteBuffer buf) {
    return Unpooled.wrappedBuffer(buf);
  }

  public static io.netty.buffer.ByteBuf asNettyBuf(CharSequence string) {
    int i = string.length() * 3;
    ByteBuf buf = byteBuf(i);
    int offset = buf.writerIndex();
    try {
      int len = encodeUtf8(buf, offset, string);
      if (len > i) {
        throw new UncheckedIOException(new UnsupportedEncodingException("超出预期范围的编码"));
      }
      buf.writerIndex(offset + len);
    } catch (Throwable e) {
      buf.release();
      throw e;
    }
    return buf;
  }

  public static int encodeUtf8(io.netty.buffer.ByteBuf buffer, int offset, CharSequence string) {
    int pos = offset;
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);

      if (c <= '\u007F') {
        buffer.setByte(pos++, (byte) c);
      }
      else {
        if (c <= '\u07FF') {
          buffer.setByte(pos, (byte) (0xC0 | c >>> 6));
          buffer.setByte(pos + 1, (byte) (0x80 | c & 0x3F));
          pos += 2;
        }
        else if (c < '\uD800' || c > '\uDFFF') {
          buffer.setByte(pos, (byte) (0xE0 | c >>> 12));
          buffer.setByte(pos + 1, (byte) (0x80 | c >> 6 & 0x3F));
          buffer.setByte(pos + 2, (byte) (0x80 | c & 0x3F));
          pos += 3;
        }
        else {
          pos += writeUtf8char4(buffer, pos, c, string, i++);
        }
      }

    }
    return pos - offset;
  }

  private static byte writeUtf8char4(io.netty.buffer.ByteBuf buf, int pos, char high, CharSequence s, int i) {
    if (isHighSurrogate(high) && i + 1 < s.length()) {
      char low = s.charAt(i + 1);
      if (isLowSurrogate(low)) {
        int cp = toCodePoint(high, low);
        if (cp >= 0x10000 && cp <= 0x10FFFF) {
          //noinspection PointlessArithmeticExpression
          buf.setByte(pos + 0, (byte) (0b11110000 | cp >>> 18));
          buf.setByte(pos + 1, (byte) (0b10000000 | cp >>> 12 & 0b00111111));
          buf.setByte(pos + 2, (byte) (0b10000000 | cp >>> 6 & 0b00111111));
          buf.setByte(pos + 3, (byte) (0b10000000 | cp & 0b00111111));
          return 4;
        }
      }
    }
    return 0;
  }
}
