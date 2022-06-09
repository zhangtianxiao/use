package use.mvc.router;

import org.jetbrains.annotations.NotNull;
import use.mvc.parabind.UploadedFile;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.Path;
import java.util.List;

public class Action_Wrapper extends Action {
  private final Action self;

  public Action_Wrapper(Action self) {
    this.self = self;
  }

  @Override
  public String url() {
    return self.url();
  }

  @Override
  UrlPartParser urlParser() {
    return self.urlParser();
  }

  @Override
  public String method() {
    return self.method();
  }

  @Override
  public String qs() {
    return self.qs();
  }

  @Override
  public String param(String name) {
    return self.param(name);
  }

  @Override
  public List<String> params(String name) {
    return self.params(name);
  }

  @Override
  public String form_param(String name) {
    return self.form_param(name);
  }

  @Override
  public List<String> form_params(String name) {
    return self.form_params(name);
  }

  @Override
  public String path_variable(String name) {
    return self.path_variable(name);
  }

  @Override
  void removePathParameter(String key) {
    self.removePathParameter(key);
  }

  @Override
  public String cookie(String name) {
    return self.cookie(name);
  }

  @Override
  public void cookie_remove(String name) {
    self.cookie_remove(name);
  }

  @Override
  public void cookie(String name, String value, boolean httponly) {
    self.cookie(name, value, httponly);
  }

  @Override
  public void cookie(String name, int value, boolean httponly) {
    self.cookie(name, value, httponly);
  }

  @Override
  public void cookie(String name, long value, boolean httponly) {
    self.cookie(name, value, httponly);
  }

  @Override
  public void cookie(String name, String value) {
    self.cookie(name, value);
  }

  @Override
  public void cookie(String name, int value) {
    self.cookie(name, value);
  }

  @Override
  public void cookie(String name, long value) {
    self.cookie(name, value);
  }

  @Override
  public String header(String name) {
    return self.header(name);
  }

  @Override
  public void header_remove(String name) {
    self.header_remove(name);
  }

  @Override
  public void header(String name, int value) {
    self.header(name, value);
  }

  @Override
  public void header(String name, long value) {
    self.header(name, value);
  }

  @Override
  public void header(String name, String value) {
    self.header(name, value);
  }

  @Override
  public boolean ended() {
    return self.ended();
  }

  @Override
  protected void doEndOther(Object o) {
    self.doEndOther(o);
  }

  @Override
  public void end(String text) {
    self.end(text);
  }

  @Override
  public void end() {
    self.end();
  }

  @Override
  public void end(CharSequence text) {
    self.end(text);
  }

  @Override
  public void status(int status) {
    self.status(status);
  }

  @Override
  public void end(ByteBuffer buf) {
    self.end(buf);
  }

  @Override
  public void send(String file) {
    self.send(file);
  }

  @Override
  public void send(File file) {
    self.send(file);
  }

  @Override
  public void send(Path file) {
    self.send(file);
  }

  @Override
  public void send(MappedByteBuffer file) {
    self.send(file);
  }

  @Override
  public ByteBuffer requestBody() {
    return self.requestBody();
  }

  @Override
  public UploadedFile uploaded(String name) {
    return self.uploaded(name);
  }

  @Override
  public String form_data(String name) {
    return self.form_data(name);
  }

  @Override
  void putPathParameter(String key, @NotNull String value) {
    self.putPathParameter(key, value);
  }

  @Override
  public void put(String name, Object value) {
    self.put(name, value);
  }

  @Override
  public <T> T get(String name) {
    return self.get(name);
  }
}
