package use.beans;

import use.kit.HashKit;

public class FieldKeyBuilder {
  /**
   * 约定为全局唯一, 只应修改一次
   */
  public static FieldKeyBuilder me = new FieldKeyBuilder() {
  };

  public long fieldHash(String second) {
    return HashKit.fnv1a64(second);
  }

  public long build(String first, long second) {
    return first.hashCode() ^ second;
  }

  public long build(String first, String second) {
    return build(first, fieldHash(second));
  }
}
