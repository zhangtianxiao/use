package use.test.db;

import com.jsoniter.any.Any;
import use.jdbc.Db;
import use.jdbc.DbKit;
import use.kit.Helper;


public class TestModelGenerator {
  public static void main(String[] args) {
    Any dataSourceList = Helper.ENV.get("dataSourceList");
    // postgres, db1, db2
    int[] arr = {1, 3, 4};
    for (int i : arr) {
      Any options = dataSourceList.get(i);
      Db db = DbKit.newDb(options);
      DbKit.generateModel(db, "use.test.db.model");
    }
   /* Engine engine = new Engine("");
    engine.config.addSharedMethod(StrKit.class);
    Template template = engine.getTemplate("use/db/generator/model_template.jf");

    MetaBuilder metaBuilder = new MetaBuilder(db.config);
    metaBuilder.skip((a, name) -> {
      return !name.equals("framework");
    });
    //Generator gen = new Generator(db.config, "use.test.db.model", new File("src\\test\\java"));
    Generator gen = new Generator(db.config, metaBuilder, "use.test.db.model." + db.config.id, new File("src\\test\\java"));
    gen.generate(template);*/

  }
}
