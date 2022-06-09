

package use.template.stat.ast;

import org.jetbrains.annotations.NotNull;
import use.template.io.IWritable;
import use.template.io.Writer;
import use.template.EngineConfig;
import use.template.Env;
import use.template.stat.Compressor;
import use.template.stat.Scope;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 Text 输出纯文本块以及使用 "#[[" 与 "]]#" 定义的原样输出块
 */
public class Text extends Stat implements IWritable {

  // content、bytes、chars 三者必有一者不为 null
  // 在 OutputStream、Writer 混合模式下 bytes、chars 同时不为null
  private final String content;

  public final byte[] bytes;
  public final char[] chars;

  // content 初始值在 Lexer 中已确保不为 null
  public Text(@NotNull StringBuilder content, EngineConfig ec) {
    Compressor c = ec.getCompressor();
    this.content = (c != null ? c.compress(content).toString() : content.toString());
    this.bytes = this.content.getBytes(StandardCharsets.UTF_8);
    this.chars = this.content.toCharArray();
  }

  public void exec(Env env, Scope scope, Writer writer) {
    writer.writeVal(this);
  }

  public boolean isEmpty() {
    return content.length() == 0;
  }

  @Override
  public String toString() {
    return content;
  }

  @Override
  public byte[] getBytes() {
    return bytes;
  }

  @Override
  public char[] getChars() {
    return chars;
  }
}



