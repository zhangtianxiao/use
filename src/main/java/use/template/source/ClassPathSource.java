

package use.template.source;

import cn.hutool.core.io.resource.ResourceUtil;
import use.template.EngineConfig;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

/**
 ClassPathSource 用于从 class path 以及 jar 包之中加载模板内容

 <pre>
 注意：
 1：如果被加载的文件是 class path 中的普通文件，则该文件支持热加载

 2：如果被加载的文件处于 jar 包之中，则该文件不支持热加载，jar 包之中的文件在运行时通常不会被修改
 在极少数情况下如果需要对 jar 包之中的模板文件进行热加载，可以通过继承 ClassPathSource
 的方式进行扩展

 3：JFinal Template Engine 开启热加载需要配置 engine.setDevMode(true)
 </pre>
 */
public class ClassPathSource implements ISource {

  protected final String finalFileName;
  protected final String fileName;
  protected final Charset encoding;

  protected final boolean isInJar;
  protected final ClassLoader classLoader;
  protected final URL url;

  protected String content;
  protected long lastModified;


  public ClassPathSource(String fileName) {
    this(null, fileName, EngineConfig.DEFAULT_ENCODING);
  }

  public ClassPathSource(String fileName, Charset encoding) {
    this(null, fileName, encoding);
  }

  public ClassPathSource(String baseTemplatePath, String fileName) {
    this(baseTemplatePath, fileName, EngineConfig.DEFAULT_ENCODING);
  }

  public ClassPathSource(String baseTemplatePath, String fileName, Charset encoding) {
    this.finalFileName = buildFinalFileName(baseTemplatePath, fileName);
    this.fileName = fileName;
    this.encoding = encoding;
    this.classLoader = getClassLoader();
    this.url = classLoader.getResource(finalFileName);
    if (url == null) {
      throw new IllegalArgumentException("File not found in CLASSPATH or JAR : \"" + finalFileName + "\"");
    }

    if ("file".equalsIgnoreCase(url.getProtocol())) {
      isInJar = false;
    } else {
      isInJar = true;
    }

    getContent();
  }


  protected ClassLoader getClassLoader() {
    ClassLoader ret = Thread.currentThread().getContextClassLoader();
    return ret != null ? ret : getClass().getClassLoader();
  }

  protected String buildFinalFileName(String baseTemplatePath, String fileName) {
    String finalFileName;
    if (baseTemplatePath != null) {
      char firstChar = fileName.charAt(0);
      if (firstChar == '/' || firstChar == '\\') {
        finalFileName = baseTemplatePath + fileName;
      } else {
        finalFileName = baseTemplatePath + "/" + fileName;
      }
    } else {
      finalFileName = fileName;
    }

    if (finalFileName.charAt(0) == '/') {
      finalFileName = finalFileName.substring(1);
    }

    return finalFileName;
  }

  public String getCacheKey() {
    return fileName;
  }

  public Charset getEncoding() {
    return encoding;
  }

  protected long getLastModified() {
    return new File(url.getFile()).lastModified();
  }

  @Override
  public String fileName() {
    return fileName;
  }

  /**
   模板文件在 jar 包文件之内则不支持热加载
   */
  public boolean isModified() {
    return isInJar ? false : lastModified != getLastModified();
  }

  public String getContent() {
    if (isModified()) {
      // 与 FileSorce 不同，ClassPathSource 在构造方法中已经初始化了 lastModified
      // 下面的代码可以去掉，在此仅为了避免继承类忘了在构造中初始化 lastModified 的防卫式代码
      if (!isInJar) {    // 如果模板文件不在 jar 包文件之中，则需要更新 lastModified 值
        lastModified = getLastModified();
      }

      /*InputStream inputStream = classLoader.getResourceAsStream(finalFileName);
      if (inputStream == null)
        throw new RuntimeException("File not found : \"" + finalFileName + "\"");
      else
        inputStream.close();*/
      content = ResourceUtil.readStr(finalFileName, encoding);
    }
    return content;
  }

  @Override
  public int hashCode() {
    return new File(finalFileName).hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o == this) return true;
    ClassPathSource o2 = (ClassPathSource) o;
    return o2.finalFileName.equals(this.fileName);
  }

  public String toString() {
    return url.toString();
  }
}


/*
	protected File getFile(URL url) {
		try {
			// return new File(url.toURI().getSchemeSpecificPart());
			return new File(url.toURI());
		} catch (URISyntaxException ex) {
			// Fallback for URLs that are not valid URIs (should hardly ever happen).
			return new File(url.getFile());
		}
	}
*/

