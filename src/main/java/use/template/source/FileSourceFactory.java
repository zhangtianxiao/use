

package use.template.source;

import java.io.File;
import java.nio.charset.Charset;

/**
 FileSourceFactory 用于配置 Engine 使用 FileSource 加载模板文件

 注意：
 FileSourceFactory 为模板引擎默认配置
 */
public class FileSourceFactory extends CacheableSourceFactory {
  public static final FileSourceFactory me = new FileSourceFactory("");

  public FileSourceFactory(String base) {
    super(new File(base).getAbsolutePath());
  }

  @Override
  public ISource newSource(String fileName, Charset encoding) {
    return new FileSource(baseTemplatePath, fileName, encoding);
  }
}




