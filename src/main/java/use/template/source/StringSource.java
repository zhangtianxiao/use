package use.template.source;

import use.kit.HashKit;
import use.kit.StrKit;
import use.kit.ex.Unsupported;
import use.template.EngineConfig;

import java.nio.charset.Charset;

/**
 * StringSource 用于从 String 变量中加载模板内容
 */
public class StringSource implements ISource {

  private final String cacheKey;
  private final String content;

  /**
   * 构造 StringSource
   * @param content 模板内容
   * @param cache true 则缓存 Template，否则不缓存
   */
  public StringSource(String content, boolean cache) {
    this(content, cache ? HashKit.md5(content) : null);
  }

  /**
   * 构造 StringSource
   * @param content 模板内容
   * @param cacheKey 缓存 Template 使用的 key，值为 null 时不缓存
   */
  public StringSource(String content, String cacheKey) {
    if (StrKit.isBlank(content)) {
      throw new IllegalArgumentException("content can not be blank");
    }
    this.content = content;
    this.cacheKey = cacheKey;
  }

 /* public StringSource(StringBuilder content, boolean cache) {
    this(content, cache && content != null ? HashKit.md5(content.toString()) : null);
  }

  public StringSource(StringBuilder content, String cacheKey) {
    if (content == null || content.length() == 0) {
      throw new IllegalArgumentException("content can not be blank");
    }
    this.content = content;
    this.cacheKey = cacheKey;      // cacheKey 值为 null 时不缓存
  }*/

  @Override
  public String fileName() {
    throw new Unsupported();
  }

  public boolean isModified() {
    return false;
  }


  public String getCacheKey() {
    return cacheKey;
  }

  public String getContent() {
    return content;
  }

  public Charset getEncoding() {
    return EngineConfig.DEFAULT_ENCODING;
  }

  public String toString() {
    return content.toString();
  }

  @Override
  public boolean equals(Object o) {
    throw new Unsupported();
  }

  @Override
  public int hashCode() {
    throw new Unsupported();
  }
}







