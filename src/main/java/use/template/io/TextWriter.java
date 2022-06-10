package use.template.io;

import use.kit.ObjectPool;
import use.kit.ex.Unsupported;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TextWriter extends Writer {
  public static final Supplier<TextWriter> maker = TextWriter::new;
  public static final Function<TextWriter, Boolean> clearer = it -> {
    it.reset();
    return true;
  };
  public static final ObjectPool<TextWriter> pool = ObjectPool.create("TextWriter", TextWriter.maker, TextWriter.clearer, -1);
  private final StringBuilder sb = new StringBuilder();

  private TextWriter() {
  }

  public void writeWithQuote(String v) {
    writeVal("\"");
    writeVal(v);
    writeVal("\"");
  }


  @Override
  public void write(byte[] b, int off, int len) {
    throw new Unsupported();
  }

  @Override
  public void close() {
    reset();
  }

  @Override
  public void reset() {
    sb.setLength(0);
  }

  @Override
  public String toUTF8() {
    return sb.toString();
  }

  public void writeVal(IWritable writable) {
    sb.append(writable.getChars());
  }

  @Override
  public void writeVal(Long v) {
    sb.append(v);
  }

  @Override
  public void writeVal(long v) {
    sb.append(v);
  }

  @Override
  public void writeVal(Integer v) {
    sb.append(v);
  }

  @Override
  public void writeVal(int v) {
    sb.append(v);
  }

  @Override
  public void writeVal(char v) {
    sb.append(v);
  }

  @Override
  public void writeVal(Float v) {
    sb.append(v);
  }

  @Override
  public void writeVal(float v) {
    sb.append(v);
  }

  @Override
  public void writeVal(Double v) {
    sb.append(v);
  }

  @Override
  public void writeVal(double v) {
    sb.append(v);
  }

  @Override
  public void writeVal(BigDecimal v) {
    sb.append(v);
  }

  @Override
  public void writeVal(String v) {
    sb.append(v);
  }

  public void writeVal(StringBuilder v) {
    sb.append(v);
  }

  @Override
  public void writeVal(Object i) {
    if (i == null)
      writeNull();
    else
      writeVal(i.toString());
  }


  public int length() {
    return sb.length();
  }

  public void writeTo(TextWriter other) {
    other.writeVal(sb);
  }
}
