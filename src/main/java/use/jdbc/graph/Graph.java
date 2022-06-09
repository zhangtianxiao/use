package use.jdbc.graph;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import use.jdbc.Db;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 聚合实体越多,  即java对象越多, stream write优势越大
 !
 */

public abstract class Graph<T> {
  private static final Logger logger = LoggerFactory.getLogger(Graph.class);
  public final List<Db> dbs;
  public final Db main;

  public Graph(Db main, LinkedHashSet<Db> dbs) {
    this.main = main;
    this.dbs = dbs.stream().toList();
  }

  /**
   用于业务处理
   */
  public abstract T get(Map map);


  /**
   优点是不产生中间对象
   */
  public abstract void streamAsJson(@NotNull Map map, OutputStream out);

  /**
   重要: 当发生 MergedRollbackException时,
   意味着出现了执行正常, 但commit/rollback却发生了异常的极端情况,
   多数据源下可能存在db1事务成功提交, db2事务失败的可能

   @return MergedRollbackException/null
   */
  public Throwable tx(Supplier<Boolean> atom) {
    main.config.begin();
    boolean[] flags = new boolean[dbs.size()];
    final boolean success;
    Throwable ret = null;
    try {
      success = atom.get();
      for (int i = 0; i < dbs.size(); i++) {
        Db db = dbs.get(i);
        if (success)
          db.config.commit();
          // 返回false时 回滚
        else
          db.config.rollback();
        flags[i] = true;
      }
    } catch (Throwable e) {
      List<RollbackException> errors = null;
      // 发生异常时,回滚
      for (int i = 0; i < flags.length; i++) {
        Db db = dbs.get(i);
        // 存在已提交的事务
        if (flags[i]) {
          logger.error("存在已提交的事务, db: " + db.config.id);
          if (errors == null) errors = new ArrayList<>(dbs.size());
          errors.add(new AlreadyCommitException(db));
        }
        //
        else {
          try {
            db.config.rollback();
          } catch (Throwable re) {
            // 合并回滚的异常
            if (errors == null) errors = new ArrayList<>(dbs.size());
            errors.add(new RollbackException(db, re));
          }
        }
      }
      // 正常回滚所有事物
      if (errors == null)
        ret = e;
      else {
        ret = new MergedRollbackException(e, errors);
      }
    }
    return ret;
  }

}