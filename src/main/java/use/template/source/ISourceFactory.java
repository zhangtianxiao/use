
package use.template.source;

import java.io.File;
import java.nio.charset.Charset;

/**
 ISourceFactory 用于为 engine 切换不同的 ISource 实现类

 FileSourceFactory 用于从指定的目录中加载模板文件
 ClassPathSourceFactory 用于从 class path 以及 jar 文件中加载模板文件

 配置示例：
 engine.setSourceFactory(new ClassPathSourceFactory());
 */
@FunctionalInterface
public interface ISourceFactory {
  ISource getSource(String fileName, Charset encoding);

  default String baseTemplatePath() {
    return new File("").getAbsolutePath();
  }
}




