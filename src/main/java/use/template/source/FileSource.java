

package use.template.source;

import cn.hutool.core.io.FileUtil;
import use.kit.ex.Unsupported;
import use.template.EngineConfig;

import java.io.File;
import java.nio.charset.Charset;

/**
 FileSource 用于从普通文件中加载模板内容
 */
public class FileSource implements ISource {

  public final File file;
  private final String fileName;
  private final Charset encoding;
  private String content;

  private long lastModified;

  public FileSource(String baseTemplatePath, String fileName, Charset encoding) {
    File base = new File(baseTemplatePath).getAbsoluteFile();
    File file = new File(base, fileName).getAbsoluteFile();
    if (file.toPath().startsWith(base.toPath())) {
    } else
      throw new Unsupported("文件路径非法: " + file);
    this.file = file;
    this.fileName = fileName;
    this.encoding = encoding;
    getContent();
  }


  @Override
  public String fileName() {
    return fileName;
  }

  public boolean isModified() {
    long it = file.lastModified();
    return lastModified != it;
  }

  public String getCacheKey() {
    return fileName;
  }

  public Charset getEncoding() {
    return encoding;
  }

  public String getContent() {
    if (!file.exists()) {
      throw new RuntimeException("File not found : \"" + file.getAbsolutePath() + "\"");
    }
    if (isModified()) {
      // 极为重要，否则在开发模式下 isModified() 一直返回 true，缓存一直失效（原因是 lastModified 默认值为 0）
      this.lastModified = file.lastModified();

      content = FileUtil.readString(file, encoding);
    }
    return content;
  }


  public String toString() {
    return file.toString();
  }

  @Override
  public int hashCode() {
    return file.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o == this) return true;
    FileSource o2 = (FileSource) o;
    return o2.file.equals(this.file);
  }
}




