package use.test.db.graph;


import com.jsoniter.any.Any;
import use.jdbc.DbKit;
import use.jdbc.graph.GraphModel;
import use.jdbc.graph.Ref;
import use.kit.Helper;

import java.util.List;

import static use.kit.Helper.printAsJson;


@Ref("pojo.find")
class SomeGraphModel extends GraphModel<SomeGraphModel> {
  private static final int RefFieldCount = refFieldCount(SomeGraphModel.class);

  Long id;
  Long fid;
  String name;
  Any any;

  public SomeGraphModel() {
    super(RefFieldCount);
  }

  @Ref(value = "graph.lazy", lazy = true, keys = "id")
  public List<SomeGraphModel> list;

  public List<SomeGraphModel> getList() {
    if (list == null) super.load(0);
    return list;
  }

  @Override
  protected String schema() {
    return "public";
  }

  @Override
  protected String table() {
    return "test";
  }

  /**
   只负责primitive字段
   */
  public SomeGraphModel setFid(Long fid) {
    this.fid = fid;
    return this;
  }

  public SomeGraphModel removeFid() {
    this.fid = null;
    attrs.remove("fid");
    return this;
  }


  @Override
  public Long getId() {
    return id;
  }
}


public class GraphLazyloadTest {
  public static void main(String[] args) {
    var dataSourceList = Helper.ENV.get("dataSourceList");
    var options = dataSourceList.get(1);
    var db = DbKit.newDb(options);
    var graph = db.graph.list(SomeGraphModel.class);

    printAsJson(graph.get(null));

    var list = graph.get(null);
    for (var it : list) {
      // lazy
      Helper.Assert(it.list == null);
      Helper.Assert(!it.getList().isEmpty());
    }
    printAsJson(list);
  }
}
