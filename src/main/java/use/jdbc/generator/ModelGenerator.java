

package use.jdbc.generator;

import use.jdbc.DbConfig;
import use.kit.JavaKeyword;
import use.kit.Kv;
import use.template.Template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 Base model 生成器 */
public class ModelGenerator {

  protected final String modelPackageName;
  protected final String modelOutputDir;
  protected boolean generateChainSetter = false;
  protected boolean graph;

  protected JavaKeyword javaKeyword = JavaKeyword.me;

  /**
   针对 Model 中七种可以自动转换类型的 getter 方法，调用其具有确定类型返回值的 getter 方法
   享用自动类型转换的便利性，例如 getInt(String)、getStr(String)
   其它方法使用泛型返回值方法： get(String)
   注意：jfinal 3.2 及以上版本 Model 中的六种 getter 方法才具有类型转换功能
   */
  @SuppressWarnings("serial")
  protected Map<String, String> getterTypeMap = new HashMap<String, String>() {{
    put("java.lang.String", "getStr");
    put("java.lang.Integer", "getInt");
    put("java.lang.Long", "getLong");
    put("java.lang.Double", "getDouble");
    put("java.lang.Float", "getFloat");
    put("java.lang.Short", "getShort");
    put("java.lang.Byte", "getByte");

    // 新增两种可自动转换类型的 getter 方法
    put("java.util.Date", "getDate");
    put("java.time.LocalDateTime", "getLocalDateTime");
  }};

  public ModelGenerator(String modelPackageName, String modelOutputDir, boolean graph) {
    if (modelPackageName.contains("/") || modelPackageName.contains("\\")) {
      throw new IllegalArgumentException("modelPackageName error : " + modelPackageName);
    }

    this.modelPackageName = modelPackageName;
    this.modelOutputDir = modelOutputDir;
    this.graph = graph;
  }

  public void setGenerateChainSetter(boolean generateChainSetter) {
    this.generateChainSetter = generateChainSetter;
  }

  public void generate(List<TableMeta> tableMetas, Template template, DbConfig dbConfig) {
    System.out.println("Generate base model ...");
    System.out.println("Base Model Output Dir: " + new File(modelOutputDir).getAbsolutePath());

    for (TableMeta tableMeta : tableMetas) {
      genModelContent(tableMeta, template, dbConfig.dialect.ignore_schema);
    }
    writeToFile(tableMetas);
  }

  protected void genModelContent(TableMeta tableMeta, Template template, boolean ignore_schema) {
    Kv data = Kv.by("modelPackageName", modelPackageName);
    data.set("generateChainSetter", generateChainSetter);
    data.set("tableMeta", tableMeta);
    data.set("getterTypeMap", getterTypeMap);
    data.set("javaKeyword", javaKeyword);
    data.set("graph", graph);
    data.set("ignore_schema", ignore_schema);

    tableMeta.modelContent = template.renderToString(data);
  }

  protected void writeToFile(List<TableMeta> tableMetas) {
    try {
      for (TableMeta tableMeta : tableMetas) {
        writeToFile(tableMeta);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   base model 覆盖写入
   */
  protected void writeToFile(TableMeta tableMeta) throws IOException {
    File dir = new File(modelOutputDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    String target = modelOutputDir + File.separator + tableMeta.modelName + ".java";
    try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(target), "UTF-8")) {
      osw.write(tableMeta.modelContent);
    }
  }

  public String getModelPackageName() {
    return modelPackageName;
  }

  public String getModelOutputDir() {
    return modelOutputDir;
  }
}






