package use.test.mb;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Set;

public class Test {
  public Long id;
  public String guid;
  public boolean del;
  public Long c1;
  public Long c2;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getGuid() {
    return guid;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public boolean isDel() {
    return del;
  }

  public void setDel(boolean del) {
    this.del = del;
  }

  public Long getC1() {
    return c1;
  }

  public void setC1(Long c1) {
    this.c1 = c1;
  }

  public Long getC2() {
    return c2;
  }

  public void setC2(Long c2) {
    this.c2 = c2;
  }

  private static class InnerMap extends AbstractMap{
    @NotNull
    @Override
    public Set<Entry> entrySet() {
      return null;
    }
  }
}
