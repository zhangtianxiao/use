package use.test.db;

import com.jsoniter.JsonIterator;
import use.jdbc.DbKit;
import use.kit.Helper;
import use.test.db.model.test.Framework;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;

public class TestModelInsert {
  public static void main(String[] args) {
    var dataSourceList = Helper.ENV.get("dataSourceList");
    var options = dataSourceList.get(1);
    var db = DbKit.newDb(options);
    //db.execute("set time zone  'PRC';");

    db.update("delete from public.framework");

    var framework = new Framework();
    // recorder.set
    framework.setId(1L).setC1(1).setC2(2L).setC3("3");
    {
      framework.setC4("4").setC5(5f).setC6(6D).setC7(7D).setC8(BigDecimal.valueOf(8))
        .setC10(LocalDate.now())
        .setC11(LocalDateTime.now().with(ChronoField.NANO_OF_SECOND, 0))
        .setC12(OffsetDateTime.now().with(ChronoField.NANO_OF_SECOND, 0))
        .setC13(LocalTime.now().with(ChronoField.NANO_OF_SECOND, 0))
        // pg驱动中没有处理 timetz这种类型
        //.setC14(OffsetTime.now())
        .setC15(Helper.createJsonObject().put("type", "json"))
        .setC16(Helper.createJsonArray().add("type").add("jsonb"))
        .setC17(false)
        .setC18(new Integer[]{18})
        .setC19(new Long[]{19L})
        .setC20(Helper.strings("20"))
        .setC21(Helper.strings("21"));
    }

    // 保存
    framework.save(db);

    // 更新
    framework.setC1(1);
    framework.update(db);

    // 映射
    var sql = "select * from framework order by ctid  desc limit 1";
    var one = db.one(sql, Framework.maker);
    var list = db.list(sql, Framework.maker, "");
    var json = one.toString();
    System.out.println(json);
    System.out.println(list);

    /*
    // json包装 对比前后结果
    var any = Any.wrap(one);
    Helper.Assert(any.get("c1").bindTo(Integer.class), framework.c1);
    Helper.Assert(any.get("c2").bindTo(Long.class), framework.c2);
    Helper.Assert(any.get("c3").bindTo(String.class), framework.c3);
    Helper.Assert(any.get("c4").bindTo(String.class), framework.c4);
    Helper.Assert(any.get("c5").bindTo(Float.class), framework.c5);
    Helper.Assert(any.get("c6").bindTo(Double.class), framework.c6);
    Helper.Assert(any.get("c7").bindTo(Double.class), framework.c7);
    Helper.Assert(any.get("c8").bindTo(BigDecimal.class), framework.c8);
    {
      // 普通的Any不是lazy的, 没法受到decoder encoder的约束
      Helper.Assert(any.get("c10").bindTo(LocalDate.class), framework.c10);
      Helper.Assert(any.get("c11").bindTo(LocalDateTime.class), framework.c11);
      Helper.Assert(any.get("c12").bindTo(OffsetDateTime.class), framework.c12);
      Helper.Assert(any.get("c13").bindTo(LocalTime.class), framework.c13);
      //Helper.Assert(any.get("c14").as(OffsetTime.class),framework.c14);
      Helper.Assert(any.get("c15").bindTo(JsonNode.class), framework.c15);
      Helper.Assert(any.get("c16").bindTo(JsonNode.class), framework.c16);
      Helper.Assert(any.get("c17").bindTo(Boolean.class), framework.c17);
      Helper.Assert(any.get("c18").bindTo(Integer[].class), framework.c18);
      Helper.Assert(any.get("c19").bindTo(Long[].class), framework.c19);
      Helper.Assert(any.get("c20").bindTo(String[].class), framework.c20);
      Helper.Assert(any.get("c21").bindTo(String[].class), framework.c21);

    }*/
    // json反序列化
    Framework deserialize = JsonIterator.deserialize(json, Framework.class);
    // 对比前后结果
    Helper.Assert(deserialize.c1, framework.c1);
    {
      Helper.Assert(deserialize.c2, framework.c2);
      Helper.Assert(deserialize.c3, framework.c3);
      Helper.Assert(deserialize.c4, framework.c4);
      Helper.Assert(deserialize.c5, framework.c5);
      Helper.Assert(deserialize.c6, framework.c6);
      Helper.Assert(deserialize.c7, framework.c7);
      Helper.Assert(deserialize.c8, framework.c8);
      Helper.Assert(deserialize.c10, framework.c10);
      Helper.Assert(deserialize.c11, framework.c11);
      Helper.Assert(deserialize.c12, framework.c12);
      Helper.Assert(deserialize.c13, framework.c13);
      //Helper.Assert(deserialize.c14, framework.c14);
      Helper.Assert(deserialize.c15, framework.c15);
      Helper.Assert(deserialize.c16, framework.c16);
      Helper.Assert(deserialize.c17, framework.c17);
      Helper.Assert(deserialize.c18, framework.c18);
      Helper.Assert(deserialize.c19, framework.c19);
      Helper.Assert(deserialize.c20, framework.c20);
      Helper.Assert(deserialize.c21, framework.c21);
    }
  }

}
