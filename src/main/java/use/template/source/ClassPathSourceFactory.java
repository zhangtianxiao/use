
package use.template.source;

import java.nio.charset.Charset;

/**
 ClassPathSourceFactory 用于配置 Engine 使用 ClassPathSource 加载模板文件

 配置示例：
 engine.baseTemplatePath(null);	// 清掉 base path
 engine.setSourceFactory(new ClassPathSourceFactory());
 */
public class ClassPathSourceFactory extends CacheableSourceFactory {
  public static final ClassPathSourceFactory me = new ClassPathSourceFactory();

  private ClassPathSourceFactory() {
    super("");
  }

  @Override
  public ISource newSource(String fileName, Charset encoding) {
    return new ClassPathSource(baseTemplatePath, fileName, encoding);
  }
}



