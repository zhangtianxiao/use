package use.test.db;

import cn.hutool.core.thread.ThreadUtil;
import use.jdbc.DbKit;

import static use.kit.Helper.ENV;

public class TestConnection {
  public static void main(String[] args) {
    var dataSourceList = ENV.get("dataSourceList");
    var options = dataSourceList.get(1);
    var db = DbKit.newDb(options);

    String sql = "select c1 from test where id = 1";

    new Thread(() -> {
      db.holdConnection((conn) -> {
        // 两个线程各自一条连接
        ThreadUtil.sleep(500);

        Integer old = db.queryInt(sql);
        System.out.println("old: " + old);
        int update = db.update(conn, "update test set c1 = " + (old + 1) + " where id = 1");
        Integer updated = db.queryInt(conn, sql);
        System.out.println("updated: " + updated);
        return null;
      });
    }).start();

    new Thread(() -> {
      db.holdConnection((conn) -> {
        // 两个线程各自一条连接
        ThreadUtil.sleep(500);
        // 等待第一个线程更新完毕
        ThreadUtil.sleep(500);

        Integer latest = db.queryInt(conn, sql);
        System.out.println("latest: " + latest);
        return null;
      });
    }).start();
  }
}
