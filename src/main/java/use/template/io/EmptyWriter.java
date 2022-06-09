package use.template.io;


import java.math.BigDecimal;

public class EmptyWriter extends Writer {
  public static final EmptyWriter me = new EmptyWriter();

  private EmptyWriter() {
  }

  @Override
  public void write(byte[] b, int off, int len) {

  }

  @Override
  public void close() {

  }

  @Override
  public void reset() {

  }


  @Override
  public String toUTF8() {
    return null;
  }

  @Override
  public void writeVal(IWritable writable) {

  }

  @Override
  public void writeVal(Long v) {

  }

  @Override
  public void writeVal(long v) {

  }

  @Override
  public void writeVal(Integer v) {

  }

  @Override
  public void writeVal(int v) {

  }

  @Override
  public void writeVal(Float v) {

  }

  @Override
  public void writeVal(float v) {

  }

  @Override
  public void writeVal(Double v) {

  }

  @Override
  public void writeVal(double v) {

  }

  @Override
  public void writeVal(BigDecimal v) {

  }

  @Override
  public void writeVal(String v) {

  }

  @Override
  public void writeVal(Object v) {

  }
}
