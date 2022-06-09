package use.mvc.router;

import io.vertx.core.MultiMap;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import use.aop.Invocation;
import use.kit.VertxKit;
import use.mvc.parabind.UploadedFile;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class Action_Vertx extends Action {
  public final RoutingContext ctx;

  public Action_Vertx(RoutingContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public String url() {
    return ctx.request().absoluteURI();
  }

  @Override
  UrlPartParser urlParser() {
    return null;
  }

  @Override
  public String method() {
    return ctx.request().method().name();
  }

  @Override
  public String qs() {
    return ctx.request().query();
  }

  @Override
  public String param(String name) {
    return ctx.request().params().get(name);
  }

  @Override
  public List<String> params(String name) {
    return ctx.request().params().getAll(name);
  }

  @Override
  public String form_param(String name) {
    return ctx.request().formAttributes().get(name);
  }

  @Override
  public List<String> form_params(String name) {
    return ctx.request().formAttributes().getAll(name);
  }

  @Override
  public String path_variable(String name) {
    return null;
  }

  @Override
  void removePathParameter(String key) {
    ctx.pathParams().remove(key);
  }

  @Override
  public String cookie(String name) {
    Cookie cookie = ctx.request().getCookie(name);
    if (cookie == null)
      return null;
    return cookie.getValue();
  }

  @Override
  public void cookie_remove(String name) {
    ctx.request().response().removeCookie(name);
  }

  @Override
  public void cookie(String name, String value, boolean http) {
    ctx.request().response().addCookie(Cookie.cookie(name, value).setHttpOnly(http));
  }

  @Override
  public void cookie(String name, int value, boolean http) {
    cookie(name, value + "", http);
  }

  @Override
  public void cookie(String name, long value, boolean http) {
    cookie(name, value + "", http);
  }

  @Override
  public String header(String name) {
    return ctx.request().getHeader(name);
  }

  @Override
  public void header_remove(String name) {
    ctx.request().response().headers().remove(name);
  }

  @Override
  public void header(String name, int value) {
    header(name, value + "");
  }

  @Override
  public void header(String name, long value) {
    header(name, value + "");
  }

  @Override
  public void header(String name, String value) {
    ctx.request().response().putHeader(name, value);
  }

  @Override
  public boolean ended() {
    return ctx.response().ended();
  }

  @Override
  protected void doEndOther(Object o) {
    if (o == null)
      end();
    else
      ctx.end(VertxKit.writeJson(o));
  }

  @Override
  public void end(String text) {
    if (!checkEnded())
      ctx.end(VertxKit.asBuffer(text));
  }

  @Override
  public void end() {
    Invocation.pool.recycle(inv());
    ctx.end();
  }

  @Override
  public void end(CharSequence text) {
    if (!checkEnded())
      ctx.response().end(VertxKit.asBuffer(text));
  }

  @Override
  public void status(int status) {
    if (!checkEnded())
      ctx.response().setStatusCode(status);
  }

  @Override
  public void end(ByteBuffer buf) {
    if (!checkEnded())
      ctx.response().end(VertxKit.asBuffer(buf));
  }

  @Override
  public void send(String file) {
    if (!checkEnded()) {
      HttpServerResponse response = ctx.request().response();
      response.sendFile(file);
    }
  }

  @Override
  public void send(File file) {
    send(file.getAbsolutePath());
  }

  @Override
  public void send(Path file) {
    send(file.toAbsolutePath().toString());
  }

  @Override
  public void send(MappedByteBuffer file) {
    if (!checkEnded())
      ctx.request().response().end(VertxKit.asBuffer(file));
  }

  @Override
  public ByteBuffer requestBody() {
    return ctx.getBody().getByteBuf().nioBuffer();
  }

  @Override
  public UploadedFile uploaded(String name) {
    Set<FileUpload> fileUploads = ctx.fileUploads();
    for (FileUpload fileUpload : fileUploads) {
      if (fileUpload.name().equals(name))
        return new UploadedFile(new File(fileUpload.uploadedFileName()), null, fileUpload.fileName());
    }
    return null;
  }

  @Override
  public String form_data(String name) {
    MultiMap entries = ctx.request().formAttributes();
    return entries.get(name);
  }

  @Override
  public void put(String name, Object value) {
    ctx.put(name, value);
  }

  @Override
  public <T> T get(String name) {
    return ctx.get(name);
  }
}
