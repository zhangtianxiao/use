package use.jdbc;

import com.jsoniter.any.Any;
import org.slf4j.Logger;
import use.jdbc.dialect.PostgresqlDialect;
import use.jdbc.generator.Generator;
import use.jdbc.generator.MetaBuilder;
import use.kit.Helper;
import use.kit.ObjectPool;
import use.kit.ResourceMode;
import use.kit.StrKit;
import use.sql.SqlManager;
import use.sql.SqlManager_XML;
import use.sql.SqlParaWriter;
import use.template.Engine;
import use.template.EngineConfig;
import use.template.Template;
import use.template.io.TextWriter;
import use.template.source.FileSourceFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DbKit {
  private static final Logger logger = Helper.getLogger(DbKit.class);

  private static final HashMap<String, Db> dbMap = new HashMap<>();

  public static Db db(String id) {
    Db db = dbMap.get(id);
    Objects.nonNull(db);
    return db;
  }

  public static Db newDb(Any options) {

    String id = options.toString("id");
    if (dbMap.containsKey(id)) {
      logger.warn("db id重复:" + id);
    }
    boolean showSql = options.toBoolean("showSql");
    boolean ignore_schema = options.toBoolean("ignore_schema");
    Any ns = options.get("ns");
    final SqlManager ntm;
    if (ns.toBoolean()) {
      String path = ns.toString("path");
      Any files = ns.get("files");
      boolean watch = ns.toBoolean("watch");

      EngineConfig engineConfig = new EngineConfig("db-engine-" + id, new FileSourceFactory(path));
      Engine engine = new Engine(engineConfig).setDevMode(true);
      ntm = new SqlManager_XML(engine);

      for (Any any : files) {
        String file = any.toString();
        ntm.add(file);
      }
    } else {
      ntm = null;
    }

    ResourceMode mode = ResourceMode.valueOf(options.toString("resourceMode"));
    boolean graph = options.toBoolean("graph", "enable");
    DbConfig config = new DbConfig(id, newConnPool(options), new PostgresqlDialect(ignore_schema), graph, ntm, showSql, 4
      , ConnectionHolder.create(mode)
      , ObjectPool.create("Db-SqlParaWriter.Dynamic-" + id, SqlParaWriter.Dynamic.Fast.maker, SqlParaWriter.clearer, -1, mode)
      , ObjectPool.create("Db-SqlParaWriter.Immutable-" + id, SqlParaWriter.Immutable.Fast.maker, SqlParaWriter.clearer, -1, mode)
      , ObjectPool.create("Db-TextWriter.Immutable-" + id, TextWriter.maker, TextWriter.clearer, -1, mode));
    Db db = new Db(config);
    dbMap.putIfAbsent(id, db);
    //System.out.println(db.get("find").sql());
    return db;
  }


  public static ObjectPool<Connection> newConnPool(Any JDBC) {
    ResourceMode resourceMode = ResourceMode.valueOf(JDBC.toString("resourceMode"));
    return newConnPool(JDBC, resourceMode);
  }

  public static ObjectPool<Connection> newConnPool(Any JDBC, ResourceMode resourceMode) {
    String id = JDBC.toString("id");
    String url = JDBC.toString("url");
    String username = JDBC.toString("username");
    String password = JDBC.toString("password");
    int max = JDBC.toInt("max");
    return newConnPool(id, url, username, password, max, resourceMode);
  }

  public static ObjectPool<Connection> newConnPool(String id, String url, String username, String password, int max, ResourceMode resourceMode) {
    Supplier<Connection> supplier = () -> {
      try {
        return DriverManager.getConnection(url, username, password);
      } catch (SQLException e) {
        throw ActiveRecordException.wrap(e);
      }
    };
    ObjectPool<Connection> pool = ObjectPool.create("jdbc-datasource-" + id, supplier, (conn) -> true, max, resourceMode);
    return pool;
  }

  public static ObjectPool<Connection> newConnPool(String id, DataSource ds, ResourceMode resourceMode) {
    Supplier<Connection> supplier = () -> {
      try {
        return ds.getConnection();
      } catch (SQLException e) {
        throw ActiveRecordException.wrap(e);
      }
    };
    Consumer<Connection> disposer = it -> {
      try {
        it.close();
      } catch (SQLException e) {
        throw ActiveRecordException.wrap(e);
      }
    };
    ObjectPool<Connection> pool = ObjectPool.create("jdbc-datasource-" + id, supplier, disposer, -1, resourceMode);
    return pool;
  }




  public static void generateModel(Db db, Template template, File baseDir, String basePackage) {
    MetaBuilder metaBuilder = new MetaBuilder(db.config);
    //metaBuilder.skip((a, name) -> {return !name.equals("framework");});
    //Generator gen = new Generator(db.config, "use.test.db.model", new File("src\\test\\java"));
    Generator gen = new Generator(db.config, metaBuilder, basePackage + "." + db.config.id, baseDir);
    gen.generate(template);
  }

  public static void generateModel(Db db, String basePackage) {
    Engine engine = Engine.byClassPath("");
    engine.config.addSharedMethod(StrKit.class);
    Template template = engine.getTemplate("use/jdbc/generator/model_template.jf");
    generateModel(db, template, new File("src\\test\\java"), basePackage);
  }

}
