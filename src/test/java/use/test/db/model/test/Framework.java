package use.test.db.model.test;

import use.jdbc.graph.GraphModel;

import java.util.function.Supplier;

public class Framework extends GraphModel<Framework> {
  public static final Supplier<Framework> maker = Framework::new;

  public Framework() {
    super(0);
  }

  public Framework(int n) {
    super(n);
  }

  public Long id;
  public Integer c1;
  public Long c2;
  public String c3;
  public String c4;
  public Float c5;
  public Double c6;
  public Double c7;
  public java.math.BigDecimal c8;
  public java.time.LocalDate c10;
  public java.time.LocalDateTime c11;
  public java.time.OffsetDateTime c12;
  public java.time.LocalTime c13;
  public com.fasterxml.jackson.databind.JsonNode c15;
  public com.fasterxml.jackson.databind.JsonNode c16;
  public Boolean c17;
  public Integer[] c18;
  public Long[] c19;
  public String[] c20;
  public String[] c21;


  @Override
  protected String schema() {
    return "public";
  }

  @Override
  protected String table() {
    return "framework";
  }

  @Override
  public Long getId() {
    return id;
  }

  public Framework setId(Long v) {
    this.id = v;
    checkAttrs();
    this.attrs.put("id", v);
    return this;
  }

  public void removeId() {
    this.id = null;
    this.attrs.remove("id");
  }

  public Framework setC1(Integer v) {
    this.c1 = v;
    checkAttrs();
    this.attrs.put("c1", v);
    return this;
  }

  public void removeC1() {
    this.c1 = null;
    this.attrs.remove("c1");
  }

  public Framework setC2(Long v) {
    this.c2 = v;
    checkAttrs();
    this.attrs.put("c2", v);
    return this;
  }

  public void removeC2() {
    this.c2 = null;
    this.attrs.remove("c2");
  }

  public Framework setC3(String v) {
    this.c3 = v;
    checkAttrs();
    this.attrs.put("c3", v);
    return this;
  }

  public void removeC3() {
    this.c3 = null;
    this.attrs.remove("c3");
  }

  public Framework setC4(String v) {
    this.c4 = v;
    checkAttrs();
    this.attrs.put("c4", v);
    return this;
  }

  public void removeC4() {
    this.c4 = null;
    this.attrs.remove("c4");
  }

  public Framework setC5(Float v) {
    this.c5 = v;
    checkAttrs();
    this.attrs.put("c5", v);
    return this;
  }

  public void removeC5() {
    this.c5 = null;
    this.attrs.remove("c5");
  }

  public Framework setC6(Double v) {
    this.c6 = v;
    checkAttrs();
    this.attrs.put("c6", v);
    return this;
  }

  public void removeC6() {
    this.c6 = null;
    this.attrs.remove("c6");
  }

  public Framework setC7(Double v) {
    this.c7 = v;
    checkAttrs();
    this.attrs.put("c7", v);
    return this;
  }

  public void removeC7() {
    this.c7 = null;
    this.attrs.remove("c7");
  }

  public Framework setC8(java.math.BigDecimal v) {
    this.c8 = v;
    checkAttrs();
    this.attrs.put("c8", v);
    return this;
  }

  public void removeC8() {
    this.c8 = null;
    this.attrs.remove("c8");
  }

  public Framework setC10(java.time.LocalDate v) {
    this.c10 = v;
    checkAttrs();
    this.attrs.put("c10", v);
    return this;
  }

  public void removeC10() {
    this.c10 = null;
    this.attrs.remove("c10");
  }

  public Framework setC11(java.time.LocalDateTime v) {
    this.c11 = v;
    checkAttrs();
    this.attrs.put("c11", v);
    return this;
  }

  public void removeC11() {
    this.c11 = null;
    this.attrs.remove("c11");
  }

  public Framework setC12(java.time.OffsetDateTime v) {
    this.c12 = v;
    checkAttrs();
    this.attrs.put("c12", v);
    return this;
  }

  public void removeC12() {
    this.c12 = null;
    this.attrs.remove("c12");
  }

  public Framework setC13(java.time.LocalTime v) {
    this.c13 = v;
    checkAttrs();
    this.attrs.put("c13", v);
    return this;
  }

  public void removeC13() {
    this.c13 = null;
    this.attrs.remove("c13");
  }

  public Framework setC15(com.fasterxml.jackson.databind.JsonNode v) {
    this.c15 = v;
    checkAttrs();
    this.attrs.put("c15", v);
    return this;
  }

  public void removeC15() {
    this.c15 = null;
    this.attrs.remove("c15");
  }

  public Framework setC16(com.fasterxml.jackson.databind.JsonNode v) {
    this.c16 = v;
    checkAttrs();
    this.attrs.put("c16", v);
    return this;
  }

  public void removeC16() {
    this.c16 = null;
    this.attrs.remove("c16");
  }

  public Framework setC17(Boolean v) {
    this.c17 = v;
    checkAttrs();
    this.attrs.put("c17", v);
    return this;
  }

  public void removeC17() {
    this.c17 = null;
    this.attrs.remove("c17");
  }

  public Framework setC18(Integer[] v) {
    this.c18 = v;
    checkAttrs();
    this.attrs.put("c18", v);
    return this;
  }

  public void removeC18() {
    this.c18 = null;
    this.attrs.remove("c18");
  }

  public Framework setC19(Long[] v) {
    this.c19 = v;
    checkAttrs();
    this.attrs.put("c19", v);
    return this;
  }

  public void removeC19() {
    this.c19 = null;
    this.attrs.remove("c19");
  }

  public Framework setC20(String[] v) {
    this.c20 = v;
    checkAttrs();
    this.attrs.put("c20", v);
    return this;
  }

  public void removeC20() {
    this.c20 = null;
    this.attrs.remove("c20");
  }

  public Framework setC21(String[] v) {
    this.c21 = v;
    checkAttrs();
    this.attrs.put("c21", v);
    return this;
  }

  public void removeC21() {
    this.c21 = null;
    this.attrs.remove("c21");
  }
}


