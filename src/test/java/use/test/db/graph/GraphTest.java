package use.test.db.graph;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import use.jdbc.DbKit;
import use.jdbc.graph.Graph;
import use.jdbc.graph.Ref;
import use.kit.Helper;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Ref("pojo.find")
 class SomePojo {
  Long id;
  Long fid;
  String name;
  Any any;
  //JsonNode any;

  @Ref(value = "pojo.children", keys = "id")
  public List<SomePojo> children;

  @Ref(value = "pojo.others", keys = {"id"}, breakCond = "id > 5")
  public List<SomePojo> others;

  @Ref(value = "now")
  //public OffsetDateTime now;
  public LocalDateTime now;

  @Ref(value = "pojo.findOne")
  public SomePojo2 o1;
  @Ref(value = "pojo.findOne", db = "")
  public SomePojo2 o2;
  @Ref(value = "pojo.findOne")
  public SomePojo2 o3;
  @Ref(value = "pojo.findOne")
  public SomePojo2 o4;
  @Ref(value = "pojo.findOne")
  public SomePojo2 o5;

}

class SomePojo2 {
  Long id;
  Long fid;
  String name;
  Any any;
  //JsonNode any;

  @Ref(value = "now")
  //public OffsetDateTime now;
  public LocalDateTime now;
}

public class GraphTest {
  public static void main(String[] args) {
    FieldResolverHelper.resister();

    var dataSourceList = Helper.ENV.get("dataSourceList");
    var options = dataSourceList.get(1);
    var db = DbKit.newDb(options);

    var bos = new ByteArrayOutputStream(30 * 1020);
    var bos2 = new ByteArrayOutputStream(30 * 1020);
    var timer = DateUtil.timer(true);

    var listGraph = db.graph.list(SomePojo.class);
    var oneGraph = db.graph.one(SomePojo.class);

    test(listGraph, bos, bos2, timer);
    test(oneGraph, bos, bos2, timer);

  }

  static void test(Graph<?> finder, ByteArrayOutputStream bos, ByteArrayOutputStream bos2, TimeInterval timer) {
    Object ret = null;
    for (int i = 0; i < 1; i++) {
      timer.restart();

      bos.reset();
      ret = finder.get(Map.of());
      JsonStream.serialize(ret, bos);
      long t1 = timer.intervalRestart();

      bos2.reset();
      finder.streamAsJson(Map.of(), bos2);
      long t2 = timer.intervalRestart();

      System.out.println(t1 + " asBean ");
      System.out.println(t2 + " asJsonDirectly ");
      System.out.println();

      //printAsJson(ret, false);
    }

    System.out.println(bos);
    System.out.println(bos2);
    Helper.Assert(bos.size(), bos2.size());
  }

}
