package use.sql;

import cn.hutool.core.text.finder.CharFinder;
import cn.hutool.core.text.split.SplitIter;
import use.template.io.Writer;
import use.template.Directive;
import use.template.Env;
import use.template.stat.Scope;

import java.io.IOException;

public class WhereDirective extends Directive {
  private static final String WHERE_NAME = "where";
  private final String placeholder;
  private final char quotationMark;

  public WhereDirective(String placeholder, char quotationMark) {
    this.placeholder = placeholder;
    this.quotationMark = quotationMark;
  }

  private static int checkIndex(int i) {
    if (i == -1) throw new IllegalArgumentException();
    return i;
  }

  /**
   name标识符, 只允许数字, 字母, 强转符号
   */
  private static String checkIdentifier(String str) {
    final int len = str.length();
    if (len == 0)
      throw new IllegalArgumentException();

    for (int i = 0; i < len; i++) {
      int c = str.charAt(i);
      // 数字字母, ::强转
      boolean b = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == ':';
      if (!b)
        throw new IllegalArgumentException();
    }
    return str;
  }

  protected Object convertValue(String type, String value) {
    return switch (type) {
      case "string", "str" -> value;
      case "int" -> Integer.parseInt(value);
      case "long" -> Long.parseLong(value);
      case "float" -> Float.parseFloat(value);
      case "double" -> Double.parseDouble(value);
      default -> throw new IllegalArgumentException();
    };
  }

  /**
   允许的中缀操作符
   如果是pg, 则可以自行扩展更多操作符, 将type视为类型强转
   */
  protected void handleOp(String op, String name, String value, String type, Writer writer) {
    switch (op) {
      // 常规中缀操作符
      case "=", "!=", "<", "<=", ">", ">=", "like", "not like", "ilike", "not ilike" -> {
        writer.writeVal(quotationMark);
        writer.writeVal(name);
        writer.writeVal(quotationMark);
        writer.writeVal(op);
        writer.writeVar(value);
        writer.writeVal("::");
        writer.writeVal(type);
      }
      // 常用函数
      case "ends_with", "starts_with", "array_has_intersection" -> {
        writer.writeVal(op);
        writer.writeVal('(');
        writer.writeVal(quotationMark);
        writer.writeVal(name);
        writer.writeVal(quotationMark);
        writer.writeVal(',');
        writer.writeVal(value);
        writer.writeVal(')');
      }
      default -> throw new IllegalArgumentException("非法的操作符");
    }

  }

  @Override
  public void exec(Env env, Scope scope, Writer writer) {
    writer.writeVal(placeholder);
    final String text = scope.get(WHERE_NAME);
    if (text == null) return;
    final SplitIter iter = new SplitIter(text, new CharFinder('\n', false), -1, true);
    while (iter.hasNext()) {
      final String next = iter.next();
      final int $ = checkIndex(next.indexOf("$"));
      final int $2 = checkIndex(next.indexOf("$", $ + 1));
      final int $3 = checkIndex(next.indexOf("$", $2 + 1));
      // and name  =
      //
      final String type = checkIdentifier(next.substring(0, $));
      final String name = checkIdentifier(next.substring($ + 1, $2));
      final String op = next.substring($2 + 1, $3);
      //final String value = convertValue(type, next.substring($3 + 1));
      final String value = next.substring($3 + 1);
      writer.writeVal(" and ");
      handleOp(op, name, value, type, writer);
    }
  }
}
