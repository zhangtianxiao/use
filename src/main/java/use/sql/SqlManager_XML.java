package use.sql;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import use.kit.ex.Unsupported;
import use.template.Engine;
import use.template.Env;
import use.template.source.ISource;
import use.template.stat.Parser;
import use.template.stat.ast.StatList;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class SqlManager_XML extends SqlManager {
  public SqlManager_XML(Engine engine) {
    super(engine);
  }

  public void add(String file) {
    ISource source = engine.config.sourceFactory.getSource(file, StandardCharsets.UTF_8);
    Document doc = Jsoup.parse(source.getContent());
    Elements elements = doc.select("insert,update,delete,select,sql");
    HashSet<String> keys = new HashSet<>();
    for (Element element : elements) {
      // 防止在所有的资源文件中, 出现重复的sqlKey
      String id = element.id();
      SqlTemplate old = this.sqlTemplateMap.get(id);
      if (old != null) {
        throw new Unsupported("出现重复的SQLKey");
      }
      keys.add(id);
      String text = element.text();
      String normalized = normalize(text);

      String tag = element.tagName();
      final SQL_TYPE sql_type;
      if (tag.equals("sql"))
        sql_type = SQL_TYPE.execute;
      else
        sql_type = SQL_TYPE.valueOf(tag);
      boolean isReturning = text.contains("returning");

      Env env = new Env(engine.config);
      StatList statList = new Parser(env, null).parse(normalized);
      this.sqlTemplateMap.put(id, new SqlTemplate(env, statList, sql_type, isReturning));
    }
    HashSet<String> put = this.keys.put(file, keys);
    if (put == null)
      this.sourceList.add(source);
  }

  public void onChange(ISource source) {
    String file = source.fileName();
    Document doc = Jsoup.parse(source.getContent());
    Elements elements = doc.select("insert,update,delete,select,sql");
    HashSet<String> keys = new HashSet<>();
    for (Element element : elements) {
      String tag = element.tagName();
      final SQL_TYPE sql_type;
      if (tag.equals("sql"))
        sql_type = SQL_TYPE.execute;
      else
        sql_type = SQL_TYPE.valueOf(tag);
      String id = element.id();
      SqlTemplate old = this.sqlTemplateMap.get(id);
      keys.add(id);

      String text = element.text();
      String normalized = normalize(text);
      boolean isReturning = text.contains("returning");

      Env env = new Env(engine.config);
      StatList statList = new Parser(env, null).parse(normalized);

      if (old == null)
        this.sqlTemplateMap.put(id, new SqlTemplate(env, statList, sql_type, isReturning));
      else {
        old.init(env, statList, sql_type, isReturning);
      }
    }
    this.keys.put(file, keys);
  }
}
