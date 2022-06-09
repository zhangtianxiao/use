package use.test.db.model.db1;

import use.jdbc.graph.GraphModel;

import java.util.function.Supplier;

public class T1 extends GraphModel<T1> {
  public static final Supplier<T1> maker = T1::new;

  public T1() {
    super(0);
  }

  public T1(int n) {
    super(n);
  }

  public Long id;
  public Integer c1;


  @Override
  protected String schema() {
    return "public";
  }

  @Override
  protected String table() {
    return "t1";
  }

  @Override
  public Long getId() {
    return id;
  }

  public T1 setId(Long v) {
    this.id = v;
    checkAttrs();
    this.attrs.put("id", v);
    return this;
  }

  public void removeId() {
    this.id = null;
    this.attrs.remove("id");
  }

  public T1 setC1(Integer v) {
    this.c1 = v;
    checkAttrs();
    this.attrs.put("c1", v);
    return this;
  }

  public void removeC1() {
    this.c1 = null;
    this.attrs.remove("c1");
  }
}


