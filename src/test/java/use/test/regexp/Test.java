package use.test.regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
  public static void main(String[] args) {
    // select :a
    //var str = ":age insert into t values(:id);select :a,b::int";
    /*Pattern compile = Pattern.compile("(?<x>.)(?!\\k<x>)\\w+");
    Matcher matcher = compile.matcher(str);
    while (matcher.find()) {
      System.out.println(matcher.group(0));
      System.out.println(matcher.group(1));
    }*/

    /* Pattern compile = Pattern.compile("[\b,^:\\w+]{0,}?:([\\da-zA-Z]+)");
    Matcher matcher = compile.matcher(str);
    while (matcher.find()) {
      System.out.println(matcher.group(0));
      System.out.println(matcher.group(1));
    }*/

    var str = "123!e !!b ";
    Pattern compile = Pattern.compile("!([\\da-zA-Z]+)");
    Matcher matcher = compile.matcher(str);
    var sb = new StringBuilder();
    while (matcher.find()) {
      //String full = matcher.group(0);
      int start = matcher.start();
      if (start > 0 && str.charAt(start - 1) == '!') {
        continue;
      }

      String part = matcher.group(1);
      matcher.appendReplacement(sb, "#(" + part + ")");
      System.out.println(part);
      // sb.append("#(");
      //  sb.append(")");
    }
    matcher.appendTail(sb);
    System.out.println(sb);
  }
}
