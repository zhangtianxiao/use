package use.test.db.graph;

import com.jsoniter.any.Any;
import use.jdbc.DbKit;
import use.jdbc.graph.Ref;
import use.kit.Helper;
import use.test.db.model.db1.T1;
import use.test.db.model.db2.T2;

import java.util.List;
import java.util.Map;

import static use.jdbc.DbKit.newDb;
import static use.kit.Helper.Assert;


public class GraphModelTest {
  public static void main(String[] args) {
    FieldResolverHelper.resister();

    var dataSourceList = Helper.ENV.get("dataSourceList");
    int[] arr = {1, 3, 4};
    for (int i : arr) {
      newDb(dataSourceList.get(i));
    }
    var db = DbKit.db("test");
    var db1 = DbKit.db("db1");
    var db2 = DbKit.db("db2");

    // 初始化c为值
    db1.update("update t1 set c1 = 1");
    db2.update("update t2 set c1 = 2");

    // graph
    var one = db.graph.one(入库单.class);

    // 循环多次 确认无连接泄露
    for (int i = 0; i < 10; i++) {
      long start = System.currentTimeMillis();
      // 协同事务
      Throwable txErr = one.tx(() -> {
        System.out.println();
        入库单 model = one.get(Map.of());
        Assert(model.货物品类列表 != null);
        for (货物品类 it : model.货物品类列表) {
          // 懒加载
          Assert(it.货物明细列表 == null);
          it.加载_货物明细列表();
          Assert(it.货物明细列表 != null);
          for (货物明细 t2 : it.货物明细列表) {
            t2.setC1(t2.c1 + 1);
            t2.update();
          }
        }

        for (货物品类 t1 : model.货物品类列表) {
          t1.setC1(t1.c1 + 1);
          t1.update();
        }
        one.streamAsJson(Map.of(), System.out);
        System.out.println("\n===");
        // 始终回滚
        return false;
      });
      if(txErr!=null){
        txErr.printStackTrace();
        break;
      }
      long elapsed = System.currentTimeMillis() - start;
      System.out.println("elapsed: " + elapsed);
    }
    // 断言
    Assert(db1.queryInt("select c1 from t1") == 1);
    Assert(db2.queryInt("select c1 from t2") == 2);
    System.out.println("ok");
  }
}

@Ref("pojo.find")
class 入库单 {
  Long id;
  Long fid;
  String name;
  Any any;

  @Ref(value = "graph.db1.find", db = "db1", keys = {"id", "fid"})
  List<货物品类> 货物品类列表;
}

class 货物品类 extends T1 {
  public 货物品类() {
    super(1);
  }

  @Ref(value = "graph.db2.find", db = "db2", lazy = true)
  List<货物明细> 货物明细列表;

  public List<货物明细> 货物明细列表() {
    super.load(0); // 美中不足, 下标需要自己写, 暂时没想到解决方法, 也许仿照lombok能实现
    return 货物明细列表;
  }

  public void 加载_货物明细列表() {
    super.doLoad(0);
  }
}

class 货物明细 extends T2 {
}

/*
  rsGetterForMerge
   内存有几记录, 有id的,
   select temp.id, t1.* from t1  left join values(?,?),(?,?),(?,?) temp(id,version) on t.v = temp.v
   where t1.id in(?,?,?)
   版本不一致, 那就拿自己的覆盖别人的, rsGetterForMerge, if不为null就跳过
  * */

/*
 graph api设计之初是用来做自动化聚合查询的, 不是为了自动化更新, 虽然也能这么用,
 更新场景本身比较复杂

 1.
 比如子表有3条记录,
 客户端提交了2条修改过的记录, 可能会有行版本不一致的情况(version)
 建议操作时是对2行数据做单独的更新, 如果需要获取整个子表的信息做计算, 调用load一次
 这没什么问题, 好比前端保存一条单据返回列表页就重查一次

 2.
 嵌套型json虽然可以映射成graph bean, 但这对数据的校验是费劲的,
 除非你的程序面向的人群, 不太在意数据校验这回事,
 互联网上的表单, 大都是一个主体元素, 几乎没有嵌套的情况,

 3.
 若动态javac支持单文件编译多个class, 那么就可以把逻辑放在一个class中,
 当作一个功能模块, 更方便实现动态更新,
 因为不用动旧的类, 包名不冲突就行

 */
