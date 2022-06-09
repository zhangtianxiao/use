

package use.jdbc.generator;

import use.jdbc.DbConfig;
import use.template.Template;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

public class Generator {
  protected final DbConfig dbConfig;
  protected final MetaBuilder metaBuilder;
  protected final ModelGenerator modelGenerator;
  protected final DataDictionaryGenerator dataDictionaryGenerator;
  public boolean generateDataDictionary = false;

  public Generator(DbConfig dbConfig, String modelPackageName, File base) {
    this(dbConfig, new MetaBuilder(dbConfig), modelPackageName, base);
  }

  public Generator(DbConfig dbConfig, String modelPackageName, String modelOutputDir) {
    this(dbConfig, new MetaBuilder(dbConfig), modelPackageName, modelOutputDir);
  }

  public Generator(DbConfig dbConfig, MetaBuilder metaBuilder, String modelPackageName, File base) {
    this(dbConfig, metaBuilder, modelPackageName, new File(base, modelPackageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator))).getAbsolutePath());
  }

  public Generator(DbConfig dbConfig, MetaBuilder metaBuilder, String modelPackageName, String modelOutputDir) {
    this(dbConfig, metaBuilder, new ModelGenerator(modelPackageName, modelOutputDir, dbConfig.graph));
  }

  public Generator(DbConfig dbConfig, MetaBuilder metaBuilder, ModelGenerator modelGenerator) {
    this.dbConfig = dbConfig;
    this.metaBuilder = metaBuilder;
    this.modelGenerator = modelGenerator;
    this.dataDictionaryGenerator = new DataDictionaryGenerator(dbConfig, modelGenerator.modelOutputDir);
  }

  public void generate(Template templateForModel) {

    long start = System.currentTimeMillis();
    List<TableMeta> tableMetas = metaBuilder.build();
    if (tableMetas.size() == 0) {
      System.out.println("TableMeta 数量为 0，不生成任何文件");
      return;
    }

    modelGenerator.generate(tableMetas, templateForModel, dbConfig);

    if (dataDictionaryGenerator != null && generateDataDictionary) {
      dataDictionaryGenerator.generate(tableMetas);
    }

    long usedTime = (System.currentTimeMillis() - start) / 1000;
    System.out.println("Generate complete in " + usedTime + " seconds.");
  }
}



