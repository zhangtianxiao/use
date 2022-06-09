package use.test.db;

import use.jdbc.Db;
import use.jdbc.DbKit;
import use.kit.Helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


public class TestPrepare {
  public static void main(String[] args) {
    var dataSourceList = Helper.ENV.get("dataSourceList");
    var options = dataSourceList.get(1);
    var db = DbKit.newDb(options);
    db.tx(() -> {
      var s1 = "s1";
      var s2 = "s2"; // "s1"/"s2"
      test5(db, s1);
      test5(db, s2);
      test5(db, s1);
      test5(db, s2);
      return true;
    });


  }

  /**
   参考:  https://jdbc.postgresql.org/documentation/head/server-prepare.html

   可在pgstream的 395行  370行, 打上断点 并填充condition语句, 以观察
   System.out.println("debug: "+new String(buf));
   return false;

   PGProperty.java中有三个 预编译相关的参数 都是会话级别的 不同conn不互通

   1. PREPARED_STATEMENT_CACHE_QUERIES
   2. PREPARED_STATEMENT_CACHE_SIZE_MIB
   预编译LRU cached的过期条件

   3. PREPARE_THRESHOLD
   同一个sql执行几次后, 会产生server side预编译信息
   */
  private static void test3(Connection conn) throws SQLException {
    // conn.setAutoCommit(false);

    /**
     * 通过测试, 跟踪源码, 可以看到, 一个查询 对应一个 preparedStatement,  不可共享
     *
     * */
    test2(conn);


    /**
     * 然而 预编译视图, 却查不出个结果
     * */
    showPrepareStatementView(conn);

    /**
     * 当一个会话中 某个sql执行次数超过阈值, 会产生server side prepared info
     * */
    for (int i = 0; i < 5; i++) {
      test2(conn);
    }

    /**
     此时, 便能查到记录
     * */
    showPrepareStatementView(conn);

    /**
     *  手动执行第二条语句
     *  这个用法在实际使用中是没啥用处的
     * */
    ResultSet rs = conn.createStatement().executeQuery("execute \"S_2\"(1);");
    while (rs.next()) {
      System.out.println("execute: " + rs.getString(1));
    }
    rs.close();


    /**
     * */
    //conn.createStatement().execute("prepare x as select 1;");
    //showPrepareStatementView(conn);

  }

  private static void test1(Connection conn) throws SQLException {
    var pst = conn.prepareStatement("select * from test");
    var rs = pst.executeQuery();
    while (rs.next()) {
      System.out.println(rs.getInt(1));

      var rs2 = pst.executeQuery();
      while (rs2.next()) {
        System.out.println(rs2.getInt(1));
      }
      rs2.close();
    }

    rs.close();
    pst.close();
  }

  /**
   多result
   */
  private static void test2(Connection conn) throws SQLException {
    var pst = conn.prepareStatement("select * from test where id > ?");
    var pst2 = conn.prepareStatement("select * from test");
    pst.setInt(1, 0);
    var rs = pst.executeQuery();
    while (rs.next()) {
      System.out.println(rs.getInt(1));

      var rs2 = pst2.executeQuery();
      while (rs2.next()) {
        System.out.println(rs2.getInt(1));
      }
    }
    pst.close();
    pst2.close();
  }

  private static void test4(Connection conn) throws SQLException {
    for (int i = 0; i < 10; i++) {
      var pst = conn.prepareStatement("select * from test where id > ?");
      pst.setInt(1, 0);
      var rs = pst.executeQuery();
      while (rs.next()) {
        System.out.println(rs.getInt(1));
      }
      pst.close();
    }

    showPrepareStatementView(conn);
  }

  /**
   不同schema下, prepare statement没有失效
   */
  private static void test5(Db db, String s) throws SQLException {
    // 1.
    //conn.setSchema(s);
    // 2. 或者这样
    // search_path的变动, 会导致驱动层面 prepared cache失效
    db.execute("set search_path = " + s);
    System.out.println("\nsearch_path: " + db.queryString("show search_path ;"));
    var sql = "select c1 from t1 where id = ?";
    var sqlForUpdate = "update t1 set c1 = c1+1 where id = ?";
    for (int i = 0; i < 10; i++) {
      System.out.println(db.queryInt(sql, 0));
      db.update(sqlForUpdate, new Object[]{0});
      System.out.println(db.queryInt(sql, 0));
    }
    showPrepareStatementView(db.config.getConnection());
  }

  /**
   @see org.postgresql.core.v3.QueryExecutorImpl#sendParse
   */
  private static void showPrepareStatementView(Connection conn) throws SQLException {
    System.out.println("\npg_prepared_statements: ");
    var pst = conn.createStatement();
    ResultSet rs3 = pst.executeQuery("select * from pg_prepared_statements;");
    ResultSetMetaData md = rs3.getMetaData();
    int c = md.getColumnCount();
    while (rs3.next()) {
      for (int i = 0; i < c; i++) {
        String name = md.getColumnName(i + 1);
        Object value = rs3.getObject(i + 1);
        System.out.println(name + ": " + value);
      }
    }
    rs3.close();
    pst.close();
  }
}
