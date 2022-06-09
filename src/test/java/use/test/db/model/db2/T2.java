package use.test.db.model.db2;

import use.jdbc.graph.GraphModel;

import java.util.function.Supplier;

public class T2 extends GraphModel<T2> {
  public static final Supplier<T2> maker = T2::new;

  public T2() {
    super(0);
  }

  public T2(int n) {
    super(n);
  }

  public Long id;
  public Integer c1;
  public Integer c2;


  @Override
  protected String schema() {
    return "public";
  }

  @Override
  protected String table() {
    return "t2";
  }

  @Override
  public Long getId() {
    return id;
  }

  public T2 setId(Long v) {
    this.id = v;
    checkAttrs();
    this.attrs.put("id", v);
    return this;
  }

  public void removeId() {
    this.id = null;
    this.attrs.remove("id");
  }

  public T2 setC1(Integer v) {
    this.c1 = v;
    checkAttrs();
    this.attrs.put("c1", v);
    return this;
  }

  public void removeC1() {
    this.c1 = null;
    this.attrs.remove("c1");
  }

  public T2 setC2(Integer v) {
    this.c2 = v;
    checkAttrs();
    this.attrs.put("c2", v);
    return this;
  }

  public void removeC2() {
    this.c2 = null;
    this.attrs.remove("c2");
  }
}


