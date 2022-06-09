
package use.kit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 JavaKeyword.
 */
public class JavaKeyword {

  private static final String[] keywordArray = {
    "abstract",
    "assert",
    "boolean",
    "break",
    "byte",
    "case",
    "catch",
    "char",
    "class",
    "const",
    "continue",
    "default",
    "do",
    "double",
    "else",
    "enum",
    "extends",
    "final",
    "finally",
    "float",
    "for",
    "goto",
    "if",
    "implements",
    "import",
    "instanceof",
    "int",
    "interface",
    "long",
    "native",
    "new",
    "package",
    "private",
    "protected",
    "public",
    "return",
    "strictfp",
    "short",
    "static",
    "super",
    "switch",
    "synchronized",
    "this",
    "throw",
    "throws",
    "transient",
    "try",
    "void",
    "volatile",
    "while"
  };

  private final Set<String> set;

  public static final JavaKeyword me = new JavaKeyword();

  public JavaKeyword() {
    HashSet<String> set = new HashSet<>();
    set.addAll(Arrays.asList(keywordArray));
    this.set = set;
  }

  public JavaKeyword(Set<String> set) {
    this.set = set;
  }

  public JavaKeyword addKeyword(String keyword) {
    if (StrKit.notBlank(keyword)) {
      set.add(keyword);
    }
    return this;
  }

  public JavaKeyword removeKeyword(String keyword) {
    set.remove(keyword);
    return this;
  }

  public JavaKeyword copy() {
    return new JavaKeyword(new HashSet<>(this.set));
  }

  public boolean contains(String str) {
    return set.contains(str);
  }
}






