package use.kit;

import com.jsoniter.output.JsonStream;
import com.jsoniter.output.JsonStreamPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import use.template.io.IWritable;
import use.template.io.Writer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;

public class NettyWriter extends Writer {
  final JsonStream stream;
  final ByteBufOutputStream nettyOutput;

  public NettyWriter() {
    JsonStream stream = JsonStreamPool.borrowJsonStream();
    ByteBuf byteBuf = NettyKit.byteBuf(2048);
    this.nettyOutput = new ByteBufOutputStream(byteBuf);
    stream.reset(nettyOutput);
    this.stream = stream;
  }

  public ByteBuf buffer() {
    return nettyOutput.buffer();
  }

  @Override
  public void write(byte[] b, int off, int len) {
    try {
      stream.write(b, off, len);
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  @Override
  public void close() {
    try {
      stream.flush();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    JsonStreamPool.returnJsonStream(stream);
  }

  @Override
  public void reset() {
    stream.reset(null);
  }


  @Override
  public String toUTF8() {
    return stream.buffer().toString();
  }

  @Override
  public void writeVal(IWritable writable) {
    byte[] v = writable.getBytes();
    try {
      stream.write(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(Long v) {
    try {
      stream.writeVal(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(long v) {
    try {
      stream.writeVal(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(Integer v) {
    try {
      stream.writeVal(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(int v) {
    try {
      stream.writeVal(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(Float v) {
    try {
      stream.writeVal(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(float v) {
    try {
      stream.writeVal(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(Double v) {
    try {
      stream.writeVal(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(double v) {
    try {
      stream.writeVal(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(BigDecimal v) {
    try {
      stream.writeVal(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(String v) {
    try {
      stream.writeRaw(v);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void writeVal(Object v) {
    writeVal(v.toString());
  }
}
