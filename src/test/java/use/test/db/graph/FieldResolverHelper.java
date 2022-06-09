package use.test.db.graph;

import com.jsoniter.any.Any;
import use.beans.Beans;
import use.beans.FieldDesc;

import java.time.LocalDateTime;
import java.util.List;

public class FieldResolverHelper {
  public static void resister() {

    Beans.me.register(FieldDesc.create(SomePojo.class, "id", (it, value) -> it.id = (Long) value, it -> it.id));


    Beans.me.register(SomePojo.class, "fid", (it, value) -> it.fid = (Long) value, it -> it.fid);
    Beans.me.register(SomePojo.class, "name", it -> it.name, (it, value) -> it.name = (String) value);
    Beans.me.register(SomePojo.class, "any", it -> it.any, (it, value) -> it.any = (Any) value);
    Beans.me.register(SomePojo.class, "children", it -> it.children, (it, value) -> it.children = (List<SomePojo>) value);
    Beans.me.register(SomePojo.class, "others", it -> it.others, (it, value) -> it.others = (List<SomePojo>) value);
    Beans.me.register(SomePojo.class, "now", it -> it.now, (it, value) -> it.now = (LocalDateTime) value);
    Beans.me.register(SomePojo.class, "o1", it -> it.o1, (it, value) -> it.o1 = (SomePojo2) value);
    Beans.me.register(SomePojo.class, "o2", it -> it.o2, (it, value) -> it.o2 = (SomePojo2) value);
    Beans.me.register(SomePojo.class, "o3", it -> it.o3, (it, value) -> it.o3 = (SomePojo2) value);
    Beans.me.register(SomePojo.class, "o4", it -> it.o4, (it, value) -> it.o4 = (SomePojo2) value);
    Beans.me.register(SomePojo.class, "o5", it -> it.o5, (it, value) -> it.o5 = (SomePojo2) value);

    Beans.me.register(SomePojo2.class, "id", it -> it.id, (it, value) -> it.id = (Long) value);
    Beans.me.register(SomePojo2.class, "fid", (it, value) -> it.fid = (Long) value, it -> it.fid);
    Beans.me.register(SomePojo2.class, "name", it -> it.name, (it, value) -> it.name = (String) value);
    Beans.me.register(SomePojo2.class, "any", it -> it.any, (it, value) -> it.any = (Any) value);
    Beans.me.register(SomePojo2.class, "now", it -> it.now, (it, value) -> it.now = (LocalDateTime) value);

    Beans.me.register(入库单.class, "id", it -> it.id, (it, value) -> it.id = (Long) value);
    Beans.me.register(入库单.class, "fid", (it, value) -> it.fid = (Long) value, it -> it.fid);
    Beans.me.register(入库单.class, "name", it -> it.name, (it, value) -> it.name = (String) value);
    Beans.me.register(入库单.class, "any", it -> it.any, (it, value) -> it.any = (Any) value);
    Beans.me.register(入库单.class, "货物品类列表", it -> it.货物品类列表, (it, value) -> it.货物品类列表 = (List<货物品类>) value);

    Beans.me.register(货物品类.class, "id", it -> it.id, (it, value) -> it.id = (Long) value);
    Beans.me.register(货物品类.class, "c1", it -> it.c1, (it, value) -> it.c1 = (Integer) value);
    Beans.me.register(货物品类.class, "货物明细列表", it -> it.货物明细列表, (it, value) -> it.货物明细列表 = (List<货物明细>) value);

    Beans.me.register(货物明细.class, "id", it -> it.id, (it, value) -> it.id = (Long) value);
    Beans.me.register(货物明细.class, "c1", it -> it.c1, (it, value) -> it.c1 = (Integer) value);
    Beans.me.register(货物明细.class, "c2", it -> it.c2, (it, value) -> it.c2 = (Integer) value);
  }

}
