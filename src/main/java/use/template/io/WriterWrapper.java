package use.template.io;

import java.math.BigDecimal;

public class WriterWrapper extends Writer {
  public final Writer delegate;

  public WriterWrapper(Writer delegate) {
    this.delegate = delegate;
  }

  @Override
  public void write(byte[] b, int off, int len) {
    delegate.write(b, off, len);
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public void reset() {
    delegate.reset();
  }

  @Override
  public String toUTF8() {
    return delegate.toUTF8();
  }

  @Override
  public void writeVal(IWritable v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(Long v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(long v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(char v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(Integer v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(int v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(Float v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(float v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(Double v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(double v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(BigDecimal v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(String v) {
    delegate.writeVal(v);
  }

  @Override
  public void writeVal(Object v) {
    delegate.writeVal(v);
  }

  @Override
  public int length() {
    return delegate.length();
  }
}
