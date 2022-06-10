
package use.template.ext.directive;

import use.template.io.Writer;
import use.template.Directive;
import use.template.Env;
import use.template.TemplateException;
import use.template.stat.ParseException;
import use.template.stat.Scope;

import java.io.IOException;

/**
 Escape 对字符串进行转义
 用法:
 #escape(value)
 */
public class EscapeDirective extends Directive {

  public void exec(Env env, Scope scope, Writer w) {
    try {
      Object value = exprList.eval(scope, env);

      if (value instanceof String) {
        escape((String) value, w);
      } else if (value instanceof Number) {
        Class<?> c = value.getClass();
        if (c == Integer.class) {
          w.writeVal((Integer) value);
        } else if (c == Long.class) {
          w.writeVal((Long) value);
        } else if (c == Double.class) {
          w.writeVal((Double) value);
        } else if (c == Float.class) {
          w.writeVal((Float) value);
        } else {
          w.writeVal(value.toString());
        }
      } else if (value != null) {
        escape(value.toString(), w);
      }
    } catch (TemplateException | ParseException e) {
      throw e;
    } catch (Exception e) {
      throw new TemplateException(e.getMessage(), location, e);
    }
  }

  private void escape(String str, Writer w) throws IOException {
    for (int i = 0, len = str.length(); i < len; i++) {
      char cur = str.charAt(i);
      switch (cur) {
        case '<':
          w.writeVal("&lt;");
          break;
        case '>':
          w.writeVal("&gt;");
          break;
        case '"':
          w.writeVal("&quot;");
          break;
        case '\'':
          // w.writeVal("&apos;");	// IE 不支持 &apos; 考虑 &#39;
          w.writeVal("&#39;");
          break;
        case '&':
          w.writeVal("&amp;");
          break;
        default:
          w.writeVal(str);
          break;
      }
    }
  }
}


