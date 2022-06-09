package use.template.source;

import io.netty.util.collection.LongObjectHashMap;
import use.beans.FieldKeyBuilder;

import java.nio.charset.Charset;
import java.util.Map;

public abstract class CacheableSourceFactory implements ISourceFactory {
  protected final Map<Long,ISource> cache = new LongObjectHashMap<>();
  public final String baseTemplatePath;

  @Override
  public String baseTemplatePath() {
    return baseTemplatePath;
  }

  public CacheableSourceFactory(String baseTemplatePath) {
    this.baseTemplatePath = baseTemplatePath;
  }

  public abstract ISource newSource(String fileName, Charset encoding);

  public ISource getSource(String fileName, Charset encoding) {
    // 注意, baseTemplatePath必须是绝对路径 才能作为正确的key
    long key = FieldKeyBuilder.me.build(baseTemplatePath, fileName);
    ISource iSource = cache.get(key);
    if (iSource == null) {
      iSource = newSource(fileName, encoding);
      cache.put(key, iSource);
    }
    return iSource;
  }
}
