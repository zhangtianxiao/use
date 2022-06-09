package use.vjson;

import cn.hutool.core.io.FileUtil;
import vjson.CharStream;
import vjson.JSON;
import vjson.cs.UTF8ByteArrayCharStream;
import vjson.parser.CompositeParser;
import vjson.parser.ObjectParser;
import vjson.parser.ParserOptions;
import vjson.parser.ParserUtils;
import vjson.util.ObjectBuilder;

import java.io.File;

import static use.kit.Helper.as;

public class vjsonTest {
  public static void main(String[] args) {
    byte[] bytes = FileUtil.readBytes(new File("json5.json5"));
    ParserOptions parserOptions = new ParserOptions();
    parserOptions.setStringSingleQuotes(true);
    parserOptions.setKeyNoQuotes(true);

    var cs = new UTF8ByteArrayCharStream(bytes);
    JSON.Object json = as(ParserUtils.buildFrom(cs, parserOptions));

    JSON.Object node = as(json.get("node"));
    JSON.String win = as(node.get("win"));
    String s = win.toJavaObject();
    System.out.println("");

    ObjectBuilder objectBuilder = new ObjectBuilder();
    // objectBuilder.put

  }
}
