package use.mvc.router;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.net.multipart.MultipartFormData;
import cn.hutool.core.net.multipart.UploadFile;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.jsoniter.output.JsonStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import use.aop.Invocation;
import use.kit.ex.Unsupported;
import use.mvc.parabind.UploadedFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static use.kit.Helper.as;

public class Action_Servlet extends Action {
  public final HttpServletRequest request;
  public final HttpServletResponse response;
  ByteBuffer payload;
  MultipartFormData formData;

  boolean ended = false;
  final UrlPartParser urlPartParser;
  HashMap<String, String> path_variables;

  public Action_Servlet(ServletRequest req, ServletResponse res) {
    this.request = as(req);
    this.response = as(res);
    String requestURI = request.getRequestURI();
    this.urlPartParser = UrlPartParser.of(requestURI);
  }

  @Override
  public String url() {
    return request.getRequestURI();
  }

  @Override
  UrlPartParser urlParser() {
    return urlPartParser;
  }

  @Override
  public String method() {
    return request.getMethod();
  }

  @Override
  public String qs() {
    return request.getQueryString();
  }

  @Nullable
  @Override
  public String param(String name) {
    //return urlParser.getQueryParameter(name);
    return request.getParameter(name);
  }

  @Nullable
  @Override
  public List<String> params(String name) {
    String[] parameterValues = request.getParameterValues(name);
    if (parameterValues == null) return null;
    ArrayList<String> list = new ArrayList<>(parameterValues.length);
    list.addAll(Arrays.asList(parameterValues));
    return list;
  }

  @Nullable
  @Override
  public String form_param(String name) {
    if (formData == null)
      return null;
    return formData.getParam(name);
  }

  @Nullable
  @Override
  public List<String> form_params(String name) {
    if (formData == null)
      return null;
    return formData.getListParam(name);
  }

  @Override
  public String path_variable(String name) {
    if (path_variables == null) return null;
    return path_variables.get(name);
  }

  @Override
  void removePathParameter(String key) {
    if (path_variables != null)
      path_variables.remove(key);
  }

  @Override
  void putPathParameter(String key, @NotNull String value) {
    if (path_variables == null)
      path_variables = new HashMap<>();
    path_variables.put(key, value);
  }

  @Override
  public String cookie(String name) {
    Cookie cookie = ServletUtil.getCookie(request, name);
    if (cookie == null) return null;
    return cookie.getValue();
  }

  @Override
  public void cookie_remove(String name) {
    Cookie cookie = new Cookie(name, null);
    response.addCookie(cookie);
  }

  @Override
  public void cookie(String name, String value, boolean httponly) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(httponly);
    response.addCookie(cookie);
  }

  @Override
  public void cookie(String name, int value, boolean httponly) {
    Cookie cookie = new Cookie(name, String.valueOf(value));
    cookie.setHttpOnly(httponly);
    response.addCookie(cookie);
  }

  @Override
  public void cookie(String name, long value, boolean httponly) {
    Cookie cookie = new Cookie(name, String.valueOf(value));
    cookie.setHttpOnly(httponly);
    response.addCookie(cookie);
  }

  @Override
  public String header(String name) {
    return request.getHeader(name);
  }

  @Override
  public void header_remove(String name) {
    response.setHeader(name, null);
  }

  @Override
  public void header(String name, int value) {
    response.setHeader(name, String.valueOf(value));
  }

  @Override
  public void header(String name, long value) {
    response.setHeader(name, String.valueOf(value));
  }

  @Override
  public void header(String name, String value) {
    response.setHeader(name, String.valueOf(value));
  }

  @Override
  public boolean ended() {
    return ended;
  }

  @Override
  protected void doEndOther(Object o) {
    if (o == null)
      end();
    else {
      final ServletOutputStream outputStream;
      try {
        if (response.getContentType() == null)
          response.setContentType("text/plain; charset=utf-8");
        outputStream = response.getOutputStream();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
      JsonStream.serialize(o, outputStream);
    }
  }

  @Override
  public void end(String text) {
    this.end();
    try {
      if (response.getContentType() == null)
        response.setContentType("text/plain; charset=utf-8");
      response.getOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void end() {
    ended = true;
    Invocation.pool.recycle(inv());
  }

  @Override
  public void end(CharSequence text) {
    end(text.toString());
  }

  @Override
  public void status(int status) {
    response.setStatus(status);
  }

  @Override
  public void end(ByteBuffer buf) {
    try {
      response.getOutputStream().write(buf.array());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void send(String file) {
    send(new File(file));
  }

  @Override
  public void send(File file) {
    end();
    final ServletOutputStream out;
    try {
      if (response.getContentType() == null)
        response.setContentType("text/plain; charset=utf-8");
      out = response.getOutputStream();
      IoUtil.copy(FileUtil.getInputStream(file), out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void send(Path file) {
    send(file.toFile());
  }

  @Override
  public void send(MappedByteBuffer file) {
    throw new Unsupported();
  }

  @Override
  public ByteBuffer requestBody() {
    return payload;
  }

  @Override
  public UploadedFile uploaded(String name) {
    UploadFile file = formData.getFile(name);
    final File onDisk = as(ReflectUtil.getFieldValue(file, "tempFile"));
    final byte[] temp;
    try {
      temp = onDisk == null ? file.getFileContent() : null;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new UploadedFile(onDisk, temp, file.getFileName());
  }

  @Override
  public String form_data(String name) {
    return formData.getParam(name);
  }

  @Override
  public void put(String name, Object value) {
    request.setAttribute(name, value);
  }

  @Override
  public <T> T get(String name) {
    return (T) request.getAttribute(name);
  }
}
