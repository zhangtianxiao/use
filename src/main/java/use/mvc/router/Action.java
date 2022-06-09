package use.mvc.router;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import use.aop.Invocation;
import use.kit.ex.Unsupported;
import use.mvc.parabind.UploadedFile;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.util.List;

/**
 抽象类
 !
 */
public abstract class Action {
  public static final Logger logger = LoggerFactory.getLogger(Action.class);

  public Action() {
  }

  /**
   内置router实现会用到该变量, vertx这样子带路由的, 就用不到
   */
  boolean matched = false;

  void setMatched(boolean matched) {
    this.matched = matched;
  }

  /**
   mvc模块本身是独立的, 甚至不依赖任何bean容器, 不依赖任何aop框架
   但为了做async支持,  需要终止调用, 只得把inv做到Action中

   或者通过action.put解耦, 但map会降低效率
   或者新建一个MvcInvocation子类, 用以解耦
   这里有点麻烦,目前偷懒
   */
  private Invocation inv;

  public Invocation inv() {
    return inv;
  }

  public void setInv(Invocation inv) {
    if (this.inv != null)
      throw new Unsupported();
    this.inv = inv;
  }
  //public abstract void cancel();

  public abstract String url();

  abstract UrlPartParser urlParser();

  public abstract String method();

  public abstract String qs();

  /**
   query param
   application/x-www-form-urlencoded
   multipart/form-data

   http server 或许会提供参数配置, 以合并三种参数到param

   servlet则没有针对application/x-www-form-urlencoded提供getFormPara方法
   默认合并到paramMap

   在vertx当中, application/x-www-form-urlencoded和multipart/form-data都属于 formdata,
   bodyHandler提供了参数合并到request.param
   */

  public abstract String param(String name);

  public abstract List<String> params(String name);

  public abstract String form_param(String name);

  public abstract List<String> form_params(String name);

  //
  public abstract String cookie(String name);

  public abstract void cookie_remove(String name);

  public abstract void cookie(String name, String value, boolean httponly);

  public abstract void cookie(String name, int value, boolean httponly);

  public abstract void cookie(String name, long value, boolean httponly);

  public void cookie(String name, String value) {
    cookie(name, value, false);
  }


  public void cookie(String name, int value) {
    cookie(name, value, false);
  }

  public void cookie(String name, long value) {
    cookie(name, value, false);
  }

  public void cookie(String name, Long value) {
    if (value != null)
      cookie(name, value.longValue());
    else
      header_remove(name);
  }

  public void cookie(String name, Integer value) {
    if (value != null)
      cookie(name, value.intValue());
    else
      header_remove(name);
  }

  //
  public abstract String header(String name);

  public abstract void header_remove(String name);

  public abstract void header(String name, int value);

  public abstract void header(String name, long value);

  public abstract void header(String name, String value);

  public void header(String name, Integer value) {
    if (value != null)
      header(name, value.intValue());
    else
      header_remove(name);
  }

  public void header(String name, Long value) {
    if (value != null)
      header(name, value.longValue());
    else
      header_remove(name);
  }

  abstract public boolean ended();

  public final void endWithStatus(int n) {
    status(n);
    end();
  }

  public final void end(Object o) {
    if (o instanceof CharSequence s) {
      end(s);
    } else if (o instanceof ByteBuffer s) {
      end(s);
    } else if (o instanceof byte[] s) {
      end(ByteBuffer.wrap(s));
    } else if (!checkEnded())
      doEndOther(o);
  }

  abstract protected void doEndOther(Object o);

  protected boolean checkEnded() {
    boolean b = ended();
    if (b)
      logger.warn("action ended!");
    return b;
  }

  public abstract void end(String text);

  public abstract void end();

  public abstract void end(CharSequence text);

  public abstract void status(int status);

  public abstract void end(ByteBuffer buf);

  //
  public abstract void send(String file);

  public  void sendHtml(String file){
    setContentType("text/html; charset=utf-8");
    send(file);
  };

  public abstract void send(File file);

  public abstract void send(Path file);

  public abstract void send(MappedByteBuffer file);

  public abstract ByteBuffer requestBody();

  public abstract UploadedFile uploaded(String name);

  // todo
  // application/x-www-form-urlencoded

  public abstract String form_data(String name);

  public String path_variable(String name) {
    throw new Unsupported();
  }

  ;

  void putPathParameter(String key, @NotNull String value) {
    throw new Unsupported();
  }

  public abstract void put(String name, Object value);

  public abstract <T> T get(String name);


  void removePathParameter(String key) {
    throw new Unsupported();
  }

  public void setContentType(String s) {
    header("Content-Type", s);
  }
}

