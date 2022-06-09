
package use.template.stat;

/**
 ParaToken
 */
public class ParaToken extends Token {

  // 接管父类的 value，content 可能为 null
  private String content;

  public ParaToken(String content, int row) {
    super(Symbol.PARA, row);
    this.content = content;
  }

  public String value() {
    return content;
  }

  public String getContent() {
    return content;
  }

  public String toString() {
    return content != null ? content.toString() : "null";
  }

  public void print() {
    System.out.print("[");
    System.out.print(row);
    System.out.print(", PARA, ");
    System.out.print(toString());
    System.out.println("]");
  }
}

