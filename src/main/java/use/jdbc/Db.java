package use.jdbc;

import cn.hutool.core.util.ClassUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.jsoniter.output.JsonStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import use.jdbc.dialect.Dialect;
import use.jdbc.graph.ClassInfo;
import use.jdbc.graph.GraphFactory;
import use.kit.ObjectPool;
import use.sql.SqlParaWriter;
import use.sql.SqlTemplate;
import use.template.io.TextWriter;
import use.kit.Helper;
import use.kit.Kv;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static use.kit.Helper.as;

/**
 doSelect: 入参为rs
 doSelectList: 遍历每一行rs
 doSelectOne:  只遍历第一行rs
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Db {

  static final Logger log = Helper.getLogger(Db.class);
  private static final List<Object> EMPTY_PARAM = Collections.emptyList();

  public static boolean isPrimitive(Class c) {
    return c.isPrimitive() || ClassUtil.isPrimitiveWrapper(c) || c == String.class
      || Temporal.class.isAssignableFrom(c)
      || JsonNode.class.isAssignableFrom(c);
  }


  public static final RsHandler toObject = new RsHandler() {
    @Override
    public Object run(ResultSet rs, Dialect dialect, Object atta, int n) throws SQLException {
      if (rs.next())
        return dialect.convertForRead(rs, 1);
      else return null;
    }
  };

  public static final RsHandler to2dArray = new RsHandler() {
    @Override
    public Object run(ResultSet rs, Dialect dialect, Object atta, int n) throws SQLException {
      JsonStream stream = (JsonStream) atta;
      boolean next = rs.next();
      boolean first = true;
      // [ [], [] ]
      do {
        try {
          if (next) {
            ResultSetMetaData md = rs.getMetaData();
            int cn = md.getColumnCount();
            if (first) first = false;
            else stream.writeMore();
            stream.writeArrayStart();
            for (int i = 1; i <= cn; i++) {
              stream.writeArrayStart();
              Object value = dialect.convertForRead(rs, i);
              if (i != 1)
                stream.writeMore();
              stream.writeVal(value);
              stream.writeArrayEnd();
            }
            stream.writeArrayEnd();
          } else {
            stream.writeEmptyArray();
          }
        } catch (IOException e) {
          throw ActiveRecordException.wrapEx(e);
        }
      } while (rs.next());
      return null;
    }
  };


  public static RsHandler<String> toString = toObject;
  public static RsHandler<Integer> toInt = toObject;
  public static RsHandler<Long> toLong = toObject;
  public static RsHandler<Float> toFloat = toObject;
  public static RsHandler<Double> toDouble = toObject;
  public static RsHandler<BigDecimal> toBigDecimal = toObject;

  public final DbConfig config;
  public final GraphFactory graph;

  public Db(DbConfig config) {
    this.config = config;
    this.graph = new GraphFactory(this);
  }

  /**
   动态赋值的缺陷是按name, 而不是按index, 效率差点
   而binder不能提前知晓调用处的字段类型, 每个字段都需要走一遍convertForRead, 效率差点
   但, 业务处理的场景, partial select 而非全量 也是有好处的
   */
  protected RsHandler binder = new RsHandler() {
    @Override
    public Object run(ResultSet rs, Dialect dialect, Object it, int depth) throws SQLException {
      ResultSetMetaData md = rs.getMetaData();
      int columnCount = md.getColumnCount();
      for (int c = 1; c <= columnCount; c++) {
        String key = md.getColumnName(c);
        Object value = dialect.convertForRead(rs, c);
        config.sm.engine.config.beans.set(it, key, value);
      }
      return null;
    }
  };

  /**
   转换rs行 -> 任意实体, 内部不需要调用rs.next
   */
  public void setBinder(RsHandler<Object> binder) {
    this.binder = binder;
  }

  @NotNull
  public Connection getConn() {
    return config.getConnection();
  }


  @Nullable
  public Connection getConnInHolder() {
    return config.conn_holder.get();
  }

  protected PreparedStatement doPrepare(Connection connection, String sql) throws SQLException {
    return connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
  }


  public Object primitive(String sql, List<Object> paras, RsHandler handler) {
    Connection conn = getConn();
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        return handler.run(rs, config.dialect, null);
      }
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    } finally {
      config.tryClose(conn);
    }
  }

  public Object primitive(String sql, List<Object> paras) {
    return primitive(sql, paras, toObject);
  }

  public Object primitive(String sql) {
    return primitive(sql, EMPTY_PARAM);
  }

  /**
   doSelect 不做异常处理 往上抛
   理想情况下, 该方法只有可能抛出SQLException, 但Iterator的遍历就不一定了

   public修饰, 可用于外部数据源
   */
  public <T> T select(Connection conn, String sql, Iterator<Object> paras, RsHandler<T> f, Object atta) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        return f.run(rs, config.dialect, atta);
      }
    } catch (Throwable e) {
      throw ActiveRecordException.wrapEx(e);
    }
  }

  public <T> T select(Connection conn, String sql, List<Object> paras, RsHandler<T> f, Object atta) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        return f.run(rs, config.dialect, atta);
      }
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  public <T> T select(Connection conn, String sql, Object[] paras, RsHandler<T> f, Object atta) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        return f.run(rs, config.dialect, atta);
      }
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  public <T> T select(String sql, List<Object> paras, RsHandler<T> f, Object atta) {
    Connection conn = getConn();
    try {
      return select(conn, sql, paras, f, atta);
    } finally {
      config.tryClose(conn);
    }
  }

  public <T> T select(String sql, Iterator<Object> paras, RsHandler<T> f, Object atta) {
    Connection conn = getConn();
    try {
      return select(conn, sql, paras, f, atta);
    } finally {
      config.tryClose(conn);
    }
  }

  public <T> T select(String sql, Object[] paras, RsHandler<T> f, Object atta) {
    Connection conn = getConn();
    try {
      return select(conn, sql, paras, f, atta);
    } finally {
      config.tryClose(conn);
    }
  }

  public <T> T select(Connection conn, String sql, List<Object> paras, RsHandler<T> f) {
    return select(conn, sql, paras, f, null);
  }

  public <T> T select(Connection conn, String sql, Iterator<Object> paras, RsHandler<T> f) {
    return select(conn, sql, paras, f, null);
  }

  public <T> T select(Connection conn, String sql, Object[] paras, RsHandler<T> f) {
    return select(conn, sql, paras, f, null);
  }

  public <T> T select(String sql, List<Object> paras, RsHandler<T> f) {
    return select(sql, paras, f, null);
  }

  public <T> T select(String sql, Iterator<Object> paras, RsHandler<T> f) {
    return select(sql, paras, f, null);
  }

  public <T> T select(String sql, Object[] paras, RsHandler<T> f) {
    return select(sql, paras, f, null);
  }

  public <T> T select(String sql, RsHandler<T> f) {
    return select(sql, EMPTY_PARAM, f, null);
  }


  public <T> T select(SqlTemplate template, Map map, RsHandler<T> f, Object atta) {
    ObjectPool<SqlParaWriter> factory = template.immutable() ? config.ispwFactory : config.dspwFactory;
    SqlParaWriter writer = factory.get();
    try {
      String sql = template.renderToString(map, writer);
      List<Object> paras = writer.paras;
      return select(sql, paras, f, atta);
    } finally {
      factory.recycle(writer);
    }
  }


  public Object getField(SqlTemplate template, Map map, RsGetter r) {
    return select(template, map, r, null);
  }

  // ===
  public <T> T doSelectOne(Connection conn, String sql, List<Object> paras, RsHandler<T> f, Object atta) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next())
          return f.run(rs, config.dialect, atta);
        else
          return null;
      }
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }


  private <T> T doSelectOne(String sql, List<Object> paras, RsHandler<T> f, Object atta) {
    Connection conn = getConn();
    try {
      return doSelectOne(conn, sql, paras, f, atta);
    } finally {
      config.tryClose(conn);
    }
  }

  public <T> T one(String sql, List<Object> paras, RsHandler<T> f, Object atta) {
    return doSelectOne(sql, paras, f, atta);
  }


  @Nullable
  private <T> T doSelectOne(Connection conn, String sql, Iterator<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        // lazy
        if (rs.next()) {
          T t = f.get();
          binder.run(rs, config.dialect, t);
          return t;
        }
      }
      return null;
    } catch (Throwable e) {
      throw ActiveRecordException.wrapEx(e);
    }
  }

  @Nullable
  private <T> T doSelectOne(Connection conn, String sql, List<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        // lazy
        if (rs.next()) {
          T t = f.get();
          binder.run(rs, config.dialect, t);
          return t;
        }
      }
      return null;
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  @Nullable
  private <T> T doSelectOne(Connection conn, String sql, Object[] paras, Supplier<T> f, RsHandler<T> binder) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        // lazy
        if (rs.next()) {
          T t = f.get();
          binder.run(rs, config.dialect, t);
          return t;
        }
      }
      return null;
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  @Nullable
  private <T> T doSelectOne(Connection conn, String sql, List<Object> paras, T t, RsHandler<T> binder) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          binder.run(rs, config.dialect, t);
          return t;
        }
      }
      return null;
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  @Nullable
  private <T> T doSelectOne(Connection conn, String sql, Iterator<Object> paras, T t, RsHandler<T> binder) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      try (ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          binder.run(rs, config.dialect, t);
          return t;
        }
      }
      return null;
    } catch (Throwable e) {
      throw ActiveRecordException.wrapEx(e);
    }
  }

  public <T> T one(Connection conn, String sql, Iterator<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    return doSelectOne(conn, sql, paras, f, binder);
  }

  public <T> T one(Connection conn, String sql, List<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    return doSelectOne(conn, sql, paras, f, binder);
  }

  public <T> T one(Connection conn, String sql, Iterator<Object> paras, T t, RsHandler<T> binder) {
    return doSelectOne(conn, sql, paras, t, binder);
  }

  public <T> T one(Connection conn, String sql, List<Object> paras, T t, RsHandler<T> binder) {
    return doSelectOne(conn, sql, paras, t, binder);
  }


  public <T> T one(String sql, List<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      return doSelectOne(conn, sql, paras, f, binder);
    } finally {
      config.tryClose(conn);
    }
  }

  public <T> T one(String sql, Iterator<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      return doSelectOne(conn, sql, paras, f, binder);
    } finally {
      config.tryClose(conn);
    }
  }

  public <T> T one(String sql, Object[] paras, Supplier<T> f, RsHandler<T> binder) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      return doSelectOne(conn, sql, paras, f, binder);
    } finally {
      config.tryClose(conn);
    }
  }

  public <T> T one(String sql, Iterator<Object> paras, Supplier<T> f) {
    return one(sql, paras, f, as(this.binder));
  }

  public <T> T one(String sql, List<Object> paras, Supplier<T> f) {
    return one(sql, paras, f, as(this.binder));
  }

  public <T> T one(String sql, Supplier<T> f) {
    return one(sql, EMPTY_PARAM, f, as(this.binder));
  }


  public <T> T selectById(Connection conn, String schema, String table, @NotNull Long id, Supplier<T> f) {
    SqlParaWriter writer = config.dspwFactory.get();
    try {
      config.dialect.forDbFindById(schema, table, id, writer);
      RsHandler<T> binder = as(this.binder);
      return one(conn, writer.toSQL(), writer.paras, f, binder);
    } finally {
      config.dspwFactory.recycle(writer);
    }
  }

  public <T> T selectById(Connection conn, String schema, String table, @NotNull Long id, T t) {
    SqlParaWriter writer = config.dspwFactory.get();
    try {
      config.dialect.forDbFindById(schema, table, id, writer);
      RsHandler<T> binder = as(this.binder);
      return doSelectOne(conn, writer.toSQL(), writer.paras, t, binder);
    } finally {
      config.dspwFactory.recycle(writer);
    }
  }


  public <T> T selectById(String schema, String table, @NotNull Long id, Supplier<T> f) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      return selectById(conn, schema, table, id, f);
    } finally {
      config.tryClose(conn);
    }
  }


  public Kv selectById(Connection conn, String schema, String table, @NotNull Long id) {
    return selectById(conn, schema, table, id, Kv.maker);
  }


  public <T> T selectById(String schema, String table, @NotNull Long id, T t) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      return selectById(conn, schema, table, id, t);
    } finally {
      config.tryClose(conn);
    }
  }


  // ===
  private <T> List<T> doSelectList(Connection conn, String sql, Iterator<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      ArrayList<T> ret = null;
      try (ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
          // lazy
          if (ret == null)
            ret = new ArrayList<>();
          T t = f.get();
          binder.run(rs, config.dialect, t);
          ret.add(t);
        }
      }
      return ret;
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  private <T> List<T> doSelectList(Connection conn, String sql, List<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      ArrayList<T> ret = null;
      try (ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
          // lazy
          if (ret == null)
            ret = new ArrayList<>();
          T t = f.get();
          binder.run(rs, config.dialect, t);
          ret.add(t);
        }
      }
      return ret;
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  private <T> List<T> doSelectList(Connection conn, String sql, Object[] paras, Supplier<T> f, RsHandler<T> binder) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      ArrayList<T> ret = null;
      try (ResultSet rs = pst.executeQuery()) {
        while (rs.next()) {
          // lazy
          if (ret == null)
            ret = new ArrayList<>();
          T t = f.get();
          binder.run(rs, config.dialect, t);
          ret.add(t);
        }
      }
      return ret;
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  public <T> List<T> list(Connection conn, String sql, Iterator<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    return doSelectList(conn, sql, paras, f, binder);
  }

  public <T> List<T> list(Connection conn, String sql, Iterator<Object> paras, Supplier<T> f) {
    return doSelectList(conn, sql, paras, f, this.binder);
  }

  public <T> List<T> list(String sql, Iterator<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      return doSelectList(conn, sql, paras, f, binder);
    } finally {
      config.tryClose(conn);
    }
  }

  public <T> List<T> list(String sql, List<Object> paras, Supplier<T> f, RsHandler<T> binder) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      return doSelectList(conn, sql, paras, f, binder);
    } finally {
      config.tryClose(conn);
    }
  }

  public <T> List<T> list(String sql, Object[] paras, Supplier<T> f, RsHandler<T> binder) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      return doSelectList(conn, sql, paras, f, binder);
    } finally {
      config.tryClose(conn);
    }
  }


  public <T> List<T> list(String sql, Iterator<Object> paras, Supplier<T> f) {
    return list(sql, paras, f, this.binder);
  }

  public <T> List<T> list(String sql, List<Object> paras, Supplier<T> f) {
    return list(sql, paras, f, this.binder);
  }

  public <T> List<T> list(String sql, Supplier<T> f) {
    return list(sql, EMPTY_PARAM, f, this.binder);
  }

  public <T> List<T> list(String sql, Supplier<T> f, Object... args) {
    return list(sql, args, f, this.binder);
  }


  // ===
  public Integer queryInt(String sql, List<Object> paras) {
    return select(sql, paras, toInt);
  }

  public Integer queryInt(String sql, Iterator<Object> paras) {
    return select(sql, paras, toInt);
  }

  public Integer queryInt(String sql, Object... paras) {
    return select(sql, paras, toInt);
  }

  public Integer queryInt(String sql) {
    return select(sql, EMPTY_PARAM, toInt);
  }

  public Integer queryInt(Connection conn, String sql) {
    return select(conn, sql, EMPTY_PARAM, toInt);
  }

  public Long queryLong(String sql, List<Object> paras) {
    return select(sql, paras, toLong);
  }

  public Long queryLong(String sql, Iterator<Object> paras) {
    return select(sql, paras, toLong);
  }

  public Long queryLong(String sql, Object... paras) {
    return select(sql, paras, toLong);
  }

  public Long queryLong(String sql) {
    return select(sql, EMPTY_PARAM, toLong);
  }

  public Float queryFloat(String sql, List<Object> paras) {
    return select(sql, paras, toFloat);
  }

  public Float queryFloat(String sql, Iterator<Object> paras) {
    return select(sql, paras, toFloat);
  }

  public Float queryFloat(String sql, Object... paras) {
    return select(sql, paras, toFloat);
  }

  public Float queryFloat(String sql) {
    return select(sql, EMPTY_PARAM, toFloat);
  }

  public Double queryDouble(String sql, List<Object> paras) {
    return select(sql, paras, toDouble);
  }

  public Double queryDouble(String sql, Iterator<Object> paras) {
    return select(sql, paras, toDouble);
  }

  public Double queryDouble(String sql, Object... paras) {
    return select(sql, paras, toDouble);
  }


  public Double queryDouble(String sql) {
    return select(sql, EMPTY_PARAM, toDouble);
  }

  public String queryString(String sql, List<Object> paras) {
    return select(sql, paras, toString);
  }

  public String queryString(String sql, Iterator<Object> paras) {
    return select(sql, paras, toString);
  }

  public String queryString(String sql, Object... paras) {
    return select(sql, paras, toString);
  }

  public String queryString(String sql) {
    return select(sql, EMPTY_PARAM, toString);
  }

  public BigDecimal queryBigDecimal(String sql, List<Object> paras) {
    return select(sql, paras, toBigDecimal);
  }

  public BigDecimal queryBigDecimal(String sql, Iterator<Object> paras) {
    return select(sql, paras, toBigDecimal);
  }

  public BigDecimal queryBigDecimal(String sql, Object... paras) {
    return select(sql, paras, toBigDecimal);
  }

  public BigDecimal queryBigDecimal(String sql) {
    return select(sql, EMPTY_PARAM, toBigDecimal);
  }

  /*
   * 方法本身只抛出SQLException, 但不代表paras的遍历不会出现异常
   * */
  // ===
  private int doUpdate(Connection conn, String sql, List<Object> paras) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      return pst.executeUpdate();
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  private int doUpdate(Connection conn, String sql, Iterator<Object> paras) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      return pst.executeUpdate();
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }


  private int doUpdate(Connection conn, String sql, Object[] paras) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      return pst.executeUpdate();
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }


  public int update(Connection conn, String sql, List<Object> paras) {
    return doUpdate(conn, sql, paras);
  }

  public int update(Connection conn, String sql, Iterator<Object> paras) {
    return doUpdate(conn, sql, paras);
  }

  public int update(Connection conn, String sql) {
    return update(conn, sql, EMPTY_PARAM);
  }

  public int update(Connection conn, String sql, Object... paras) {
    return doUpdate(conn, sql, paras);
  }


  public final BiFunction<String, List<Object>, Integer> LAMBDA_UPDATE_BY_LIST = this::update;
  public final BiFunction<String, List<Object>, Void> LAMBDA_EXECUTE = this::execute;

  /*
   * 尝试回收连接
   * */
  public int update(String sql, Iterator<Object> paras) {
    Connection conn = getConn();
    try {
      return doUpdate(conn, sql, paras);
    } finally {
      config.tryClose(conn);
    }
  }

  public int update(String sql, List<Object> paras) {
    Connection conn = getConn();
    try {
      return doUpdate(conn, sql, paras);
    } finally {
      config.tryClose(conn);
    }
  }

  public int update(String sql, Object[] args) {
    Connection conn = getConn();
    try {
      return doUpdate(conn, sql, args);
    } finally {
      config.tryClose(conn);
    }
  }

  public int update(String sql) {
    return update(sql, EMPTY_PARAM);
  }

  // ===
  public void execute(Connection conn, String sql, List<Object> paras) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      pst.execute();
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  public void execute(Connection conn, String sql, Object... paras) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      pst.execute();
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    }
  }

  public void execute(Connection conn, String sql, Iterator<Object> paras) {
    try (PreparedStatement pst = doPrepare(conn, sql)) {
      config.dialect.fillStatement(pst, paras);
      pst.execute();
    } catch (Throwable e) {
      throw ActiveRecordException.wrapEx(e);
    }
  }

  public Void execute(String sql, List<Object> paras) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      execute(conn, sql, paras);
    } finally {
      config.tryClose(conn);
    }
    return null;
  }

  public Void execute(String sql, Iterator<Object> paras) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      execute(conn, sql, paras);
    } finally {
      config.tryClose(conn);
    }
    return null;
  }

  public Void execute(String sql, Object... paras) {
    // 尝试回收连接
    Connection conn = getConn();
    try {
      execute(conn, sql, paras);
    } finally {
      config.tryClose(conn);
    }
    return null;
  }

  public void execute(Connection conn, String sql) {
    execute(conn, sql, EMPTY_PARAM);
  }

  public void execute(String sql) {
    execute(sql, EMPTY_PARAM);
  }


  // ===
  public int save(BaseModel<?> recorder) {
    SqlParaWriter writer = config.dspwFactory.get();
    TextWriter temp = config.textWriterFactory.get();
    try {
      config.dialect.forDbSave(recorder.schema(), recorder.table(), recorder.attrs, writer, temp);
      return update(writer.toSQL(), writer.paras);
    } finally {
      config.dspwFactory.recycle(writer);
      config.textWriterFactory.recycle(temp);
    }
  }

  public int update(BaseModel<?> recorder, @NotNull Long id) {
    SqlParaWriter writer = config.dspwFactory.get();
    try {
      config.dialect.forDbUpdate(recorder.schema(), recorder.table(), recorder.attrs, id, writer);
      return update(writer.toSQL(), writer.paras);
    } finally {
      config.dspwFactory.recycle(writer);
    }
  }


  public int delete(BaseModel<?> recorder, @NotNull Long id) {
    SqlParaWriter writer = config.dspwFactory.get();
    try {
      config.dialect.forDbDeleteById(recorder.schema(), recorder.table(), id, writer);
      return update(writer.toSQL(), writer.paras);
    } finally {
      config.dspwFactory.recycle(writer);
    }
  }


  // ===
  protected boolean tx(DbConfig dbConfig, int transactionLevel, IAtom atom) {
    // 仅尝试获取当前事务中的连接
    Connection conn = dbConfig.conn_holder.get();
    if (conn != null) {  // Nested transaction support
      try {
        if (conn.getTransactionIsolation() < transactionLevel)
          conn.setTransactionIsolation(transactionLevel);
        boolean result = atom.run();
        if (result) return true;
        throw new NestedTransactionHelpException("Notice the outer transaction that the nested transaction return false");  // important:can not return false
      } catch (Throwable e) {
        throw ActiveRecordException.wrapEx(e);
      }
    }

    conn = config.begin(transactionLevel);
    try {
      boolean result = atom.run();
      if (result)
        dbConfig.commit(conn);
      else
        dbConfig.rollback(conn);
      return result;
    } catch (NestedTransactionHelpException e) {
      dbConfig.rollback(conn);
      return false;
    } catch (Throwable t) {
      dbConfig.rollback(conn);
      throw ActiveRecordException.wrapEx(t);
    }
  }


  /**
   Execute transaction with default transaction level.

   @see #tx(int, IAtom)
   */
  public boolean tx(IAtom atom) {
    return tx(config, config.getTransactionLevel(), atom);
  }

  public boolean tx(int transactionLevel, IAtom atom) {
    return tx(config, transactionLevel, atom);
  }


  /**
   该方法的用途时单测
   尽量不要和 db.tx 混用
   */
  public <R> R holdConnection(ConnSupplier<R> fn) {
    Connection conn = getConn();
    boolean tx = config.isInTransaction();
    // 如果原来没有开事务, 这里也不开事务,
    // 但是需要复用连接
    if (!tx)
      config.conn_holder.set(conn);
    R ret = null;
    try {
      ret = fn.apply(conn);
    } catch (SQLException e) {
      throw ActiveRecordException.wrap(e);
    } finally {
      // 如果原本开了事务, 则不处理
      if (!tx) {
        config.conn_holder.remove();
        config.connPool.recycle(conn);
      }
    }
    return ret;
  }

  // 支持递归
  public final ThreadLocal<HashMap<Class<?>, ClassInfo>> classInfoSingletonTl = ThreadLocal.withInitial(HashMap::new);
  // 单例缓存
  public final ConcurrentHashMap<Class<?>, ClassInfo> classInfoSingletonCache = new ConcurrentHashMap<>();

  public SqlTemplate template(String fieldSQlKey) {
    return config.sm.get(fieldSQlKey);
  }
}



