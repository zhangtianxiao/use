package use.test.ntm;

import io.vertx.core.Vertx;
import use.jdbc.Db;
import use.sql.SqlManager;
import use.sql.SqlManager_XML;
import use.sql.SqlTemplate;
import use.template.Engine;
import use.template.EngineConfig;
import use.template.source.FileSourceFactory;

import java.util.Map;

public class Test {
  public static void main(String[] args) {
    EngineConfig engineConfig = new EngineConfig("test", new FileSourceFactory("file-test"));
    Engine test = new Engine(engineConfig);
    test.setDevMode(true);

    SqlManager sm = new SqlManager_XML(test);
    sm.add("test.sql.xml");
    SqlTemplate template = sm.get("查询用户");

    String sql = template.renderToString(Map.of());
    System.out.println(sql);
    System.out.println(template.renderToString(Map.of()) == template.renderToString(Map.of()));

    Db db = null;


    Vertx vertx = Vertx.vertx();
    vertx.setPeriodic(1000, id -> {
      sm.watch();
    });
  }
}
/*
#ns("",true)

#end
* */