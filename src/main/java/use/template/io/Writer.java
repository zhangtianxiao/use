package use.template.io;

import use.kit.ex.Unsupported;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

/**
 暂不提供内置的byte writer实现
 ! */
public abstract class Writer extends OutputStream {

  public static EmptyWriter empty() {
    return EmptyWriter.me;
  }

  @Override
  public void write(int b)  {
    throw new Unsupported();
  }

  @Override
  public abstract void write(byte b[], int off, int len) ;

  @Override
  public abstract void close() ;

  public abstract void reset();

  public abstract String toUTF8();

  public abstract void writeVal(IWritable writable) ;

  public abstract void writeVal(Long v) ;

  public abstract void writeVal(long v) ;

  public abstract void writeVal(Integer v) ;

  public abstract void writeVal(int v) ;

  public void writeVal(char v) {
  }

  public abstract void writeVal(Float v) ;

  public abstract void writeVal(float v) ;

  public abstract void writeVal(Double v) ;

  public abstract void writeVal(double v) ;

  public abstract void writeVal(BigDecimal v) ;

  public abstract void writeVal(String v) ;

  public abstract void writeVal(Object v) ;

  public void writeVar(Object v)  {
    writeVal(v);
  }

  public void writeNull() {
  }

  public int length(){
    throw new Unsupported();
  };


}
