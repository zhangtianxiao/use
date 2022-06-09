

package use.template.source;

import java.nio.charset.Charset;

/**
 * ISource 用于表示模板内容的来源
 * 由于Template.env在开发模式会存下依赖的source,
 * 为了动态更新template(仅更新ast), 需要复用旧template的env, 往env中添加source的容器就不能用list, 会累积, 改成了LinkHashSet
 * 故此ISource需要重写HashCode和Equals方法
 */
public interface ISource {

  String fileName();

  /**
   * reload template if modified on devMode
   */
  boolean isModified();


  /**
   * cache key used to cache, return null if do not cache the template
   *
   * 注意：如果不希望缓存从该 ISource 解析出来的 Template 对象
   *      让 getCacheKey() 返回 null 值即可
   */
  String getCacheKey();

  /**
   * content of ISource
   */
  String getContent();

  /**
   * encoding of content
   */
  Charset getEncoding();
}


