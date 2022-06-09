

package use.jdbc.generator;

import use.jdbc.DbConfig;
import use.kit.StrKit;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 MetaBuilder
 */
public class MetaBuilder {
  protected final DbConfig dbConfig;

  protected BiPredicate<String, String> tableSkip = null;

  protected Connection conn = null;
  protected DatabaseMetaData dbMeta = null;

  protected String[] removedTableNamePrefixes = null;


  protected boolean generateRemarks = false;  // 是否生成备注
  protected boolean generateView = false;    // 是否生成 view

  public MetaBuilder(DbConfig dbConfig) {
    this.dbConfig = dbConfig;
  }

  public void setGenerateRemarks(boolean generateRemarks) {
    this.generateRemarks = generateRemarks;
  }

  public void setGenerateView(boolean generateView) {
    this.generateView = generateView;
  }

  /**
   设置需要被移除的表名前缀，仅用于生成 modelName 与  baseModelName
   例如表名  "osc_account"，移除前缀 "osc_" 后变为 "account"
   */
  public void setRemovedTableNamePrefixes(String... removedTableNamePrefixes) {
    this.removedTableNamePrefixes = removedTableNamePrefixes;
  }

  public List<TableMeta> build() {
    System.out.println("Build TableMeta ...");
    conn = dbConfig.connPool.get();
    try {
      dbMeta = conn.getMetaData();

      List<TableMeta> ret = new ArrayList<TableMeta>();
      buildTableNames(ret);
      for (TableMeta tableMeta : ret) {
        buildPrimaryKey(tableMeta);
        buildColumnMetas(tableMeta);
      }
      removeNoPrimaryKeyTable(ret);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      dbConfig.connPool.recycle(conn);
    }
  }

  // 移除没有主键的 table
  protected void removeNoPrimaryKeyTable(List<TableMeta> ret) {
    for (java.util.Iterator<TableMeta> it = ret.iterator(); it.hasNext(); ) {
      TableMeta tm = it.next();
      if (StrKit.isBlank(tm.primaryKey)) {
        if (generateView) {
          tm.primaryKey = dbConfig.dialect.getDefaultPrimaryKey();
          System.out.println("Set primaryKey \"" + tm.primaryKey + "\" for " + tm.name);
        } else {
          it.remove();
          System.err.println("Skip table " + tm.name + " because there is no primary key");
        }
      }
    }
  }

  /**
   通过继承并覆盖此方法，跳过一些不希望处理的 table，定制更加灵活的 table 过滤规则

   @return 返回 true 时将跳过当前 tableName 的处理
   */
  protected boolean isSkipTable(String tableName) {
    return false;
  }

  /**
   跳过不需要生成器处理的 table

   由于 setMetaBuilder 将置换掉 MetaBuilder，所以 Generator.addExcludedTable(...)
   需要放在 setMetaBuilder 之后调用，否则 addExcludedTable 将无效

   示例：
   Generator gen = new Generator(...);
   gen.setMetaBuilder(new MetaBuilder(dataSource).skip(
   tableName -> {
   return tableName.startsWith("SYS_");
   })
   );
   gen.addExcludedTable("error_log");	// 注意这行代码要放在上面的之后调用
   gen.generate();
   */
  public MetaBuilder skip(BiPredicate<String, String> tableSkip) {
    this.tableSkip = tableSkip;
    return this;
  }

  /**
   构造 modelName，mysql 的 tableName 建议使用小写字母，多单词表名使用下划线分隔，不建议使用驼峰命名
   oracle 之下的 tableName 建议使用下划线分隔多单词名，无论 mysql还是 oralce，tableName 都不建议使用驼峰命名
   */
  protected String buildModelName(String tableName) {
    // 移除表名前缀仅用于生成 modelName、baseModelName，而 tableMeta.name 表名自身不能受影响
    if (removedTableNamePrefixes != null) {
      for (String prefix : removedTableNamePrefixes) {
        if (tableName.startsWith(prefix)) {
          tableName = tableName.replaceFirst(prefix, "");
          break;
        }
      }
    }

    // 将 oralce 大写的 tableName 转成小写，再生成 modelName
    if (dbConfig.dialect.isOracle()) {
      tableName = tableName.toLowerCase();
    }

    return StrKit.firstCharToUpperCase(StrKit.toCamelCase(tableName));
  }

  /**
   使用 modelName 构建 baseModelName
   */
  protected String buildBaseModelName(String modelName) {
    return "Base" + modelName;
  }

  /**
   不同数据库 dbMeta.getTables(...) 的 schemaPattern 参数意义不同
   1：oracle 数据库这个参数代表 dbMeta.getUserName()
   2：postgresql 数据库中需要在 jdbcUrl中配置 schemaPatter，例如：
   jdbc:postgresql://localhost:15432/djpt?currentSchema=public,sys,app
   最后的参数就是搜索schema的顺序，DruidPlugin 下测试成功
   3：开发者若在其它库中发现工作不正常，可通过继承 MetaBuilder并覆盖此方法来实现功能
   */
  protected ResultSet getTablesResultSet() throws SQLException {
    String schemaPattern = dbConfig.dialect.isOracle() ? dbMeta.getUserName() : null;
    if (generateView) {
      return dbMeta.getTables(conn.getCatalog(), schemaPattern, null, new String[]{"TABLE", "VIEW"});
    } else {
      return dbMeta.getTables(conn.getCatalog(), schemaPattern, null, new String[]{"TABLE"});  // 不支持 view 生成
    }
  }

  protected void buildTableNames(List<TableMeta> ret) throws SQLException {
    ResultSet rs = getTablesResultSet();
    while (rs.next()) {
      String tableSchem = rs.getString("TABLE_SCHEM");
      String tableName = rs.getString("TABLE_NAME");

      // jfinal 4.3 新增过滤 table 机制
      if (tableSkip != null && tableSkip.test(tableSchem, tableName)) {
        System.out.println("Skip table :" + tableName);
        continue;
      }

      TableMeta tableMeta = new TableMeta();
      tableMeta.schema = tableSchem;
      tableMeta.name = tableName;
      tableMeta.remarks = rs.getString("REMARKS");

      tableMeta.modelName = buildModelName(tableName);
      ret.add(tableMeta);
    }
    rs.close();
  }

  protected void buildPrimaryKey(TableMeta tableMeta) throws SQLException {
    ResultSet rs = dbMeta.getPrimaryKeys(conn.getCatalog(), tableMeta.schema, tableMeta.name);

    String primaryKey = "id";
    tableMeta.primaryKey = primaryKey;
    rs.close();
  }

  /**
   文档参考：
   http://dev.mysql.com/doc/connector-j/en/connector-j-reference-type-conversions.html

   JDBC 与时间有关类型转换规则，mysql 类型到 java 类型如下对应关系：
   DATE				java.sql.Date
   DATETIME			java.sql.Timestamp
   TIMESTAMP[(M)]	java.sql.Timestamp
   TIME				java.sql.Time

   对数据库的 DATE、DATETIME、TIMESTAMP、TIME 四种类型注入 new java.util.Date()对象保存到库以后可以达到“秒精度”
   为了便捷性，getter、setter 方法中对上述四种字段类型采用 java.util.Date，可通过定制 TypeMapping 改变此映射规则
   */
  protected void buildColumnMetas(TableMeta tableMeta) throws SQLException {
    String sql = dbConfig.dspwFactory.use(writer -> {
      dbConfig.dialect.forTableBuilderDoBuild(tableMeta.schema, tableMeta.name, writer);
      return writer.toUTF8();
    });
    Statement stm = conn.createStatement();
    ResultSet rs = stm.executeQuery(sql);
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();


    Map<String, ColumnMeta> columnMetaMap = new HashMap<>();
    if (generateRemarks) {
      ResultSet colMetaRs = null;
      try {
        colMetaRs = dbMeta.getColumns(conn.getCatalog(), null, tableMeta.name, null);
        while (colMetaRs.next()) {
          ColumnMeta columnMeta = new ColumnMeta();
          columnMeta.name = colMetaRs.getString("COLUMN_NAME");
          columnMeta.remarks = colMetaRs.getString("REMARKS");
          columnMetaMap.put(columnMeta.name, columnMeta);
        }
      } catch (Exception e) {
        System.out.println("无法生成 REMARKS");
      } finally {
        if (colMetaRs != null) {
          colMetaRs.close();
        }
      }
    }

    for (int i = 1; i <= columnCount; i++) {

      ColumnMeta cm = new ColumnMeta();
      cm.name = rsmd.getColumnName(i);

      String typeStr = dbConfig.dialect.handleJavaType(rsmd, i);
      cm.javaType = typeStr;

      // 构造字段对应的属性名 attrName
      cm.attrName = buildAttrName(cm.name);

      // 备注字段赋值
      if (generateRemarks && columnMetaMap.containsKey(cm.name)) {
        cm.remarks = columnMetaMap.get(cm.name).remarks;
      }

      tableMeta.columnMetas.add(cm);
    }

    rs.close();
    stm.close();
  }


  /*protected String handleJavaType(String typeStr, ResultSetMetaData rsmd, int column,int type,String ct) throws SQLException {
    // 当前实现只处理 Oracle
    if (!config.dialect.isOracle()) {
      return typeStr;
    }

    // 默认实现只处理 BigDecimal 类型
    if ("java.math.BigDecimal".equals(typeStr)) {
      int scale = rsmd.getScale(column);      // 小数点右边的位数，值为 0 表示整数
      int precision = rsmd.getPrecision(column);  // 最大精度
      if (scale == 0) {
        if (precision <= 9) {
          typeStr = "java.lang.Integer";
        } else if (precision <= 18) {
          typeStr = "java.lang.Long";
        } else {
          typeStr = "java.math.BigDecimal";
        }
      } else {
        // 非整数都采用 BigDecimal 类型，需要转成 double 的可以覆盖并改写下面的代码
        typeStr = "java.math.BigDecimal";
      }
    }

    return typeStr;
  }*/

  /**
   构造 colName 所对应的 attrName，mysql 数据库建议使用小写字段名或者驼峰字段名
   Oralce 反射将得到大写字段名，所以不建议使用驼峰命名，建议使用下划线分隔单词命名法
   */
  protected String buildAttrName(String colName) {
    if (dbConfig.dialect.isOracle()) {
      colName = colName.toLowerCase();
    }
    return StrKit.toCamelCase(colName);
  }
}







