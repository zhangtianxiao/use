package use.mvc.mi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class MappingInfo {
  public final String path;

  public final HttpMethod method;

  public final boolean websocket;

  public final boolean async;

  public final long timeout;

  public final long max;

  public final boolean multipart;

  public final boolean form_urlencoded;

  public final boolean to_disk;

  public final Class<? extends ExceptionHandler>[] onError;

  public MappingInfo(String path, HttpMethod method, boolean websocket, boolean async, long timeout, long max, boolean multipart, boolean form_urlencoded, boolean to_disk, Class<? extends ExceptionHandler>[] onError) {
    this.path = path;
    this.method = method;
    this.websocket = websocket;
    this.async = async;
    this.timeout = timeout;
    this.max = max;
    this.multipart = multipart;
    this.form_urlencoded = form_urlencoded;
    this.to_disk = to_disk;
    this.onError = onError;
  }

  public static boolean canMapping(Method method) {
    List<Class<? extends Annotation>> classes = Arrays.asList(Mapping.class, GET.class, POST.class, OPTIONS.class);
    for (Class<? extends Annotation> annotation : classes) {
      if (method.isAnnotationPresent(annotation)) return true;
    }
    return false;
  }

  public static MappingInfo from(Method method) {
    Mapping annotation = method.getAnnotation(Mapping.class);
    if (annotation != null)
      return from(annotation);
    GET get = method.getAnnotation(GET.class);
    if (get != null)
      return from(get);
    POST post = method.getAnnotation(POST.class);
    if (post != null)
      return from(post);
    OPTIONS options = method.getAnnotation(OPTIONS.class);
    if (options != null)
      return from(options);
    return null;
  }

  static MappingInfo from(GET it) {
    return new MappingInfo(it.value(), HttpMethod.GET, it.websocket(), it.async(), it.timeout(), -1, false, false, false, it.onError());
  }

  static MappingInfo from(POST it) {
    return new MappingInfo(it.value(), HttpMethod.POST, false, it.async(), it.timeout(), it.max(), it.multipart(), it.form_urlencoded(), it.to_disk(), it.onError());
  }

  static MappingInfo from(OPTIONS it) {
    return new MappingInfo(it.value(), HttpMethod.OPTIONS, false, it.async(), it.timeout(), -1, false, false, false, it.onError());
  }

  static MappingInfo from(Mapping it) {
    return new MappingInfo(it.value(), HttpMethod.GET, it.websocket(), it.async(), it.timeout(), it.max(), it.multipart(), it.form_urlencoded(), it.to_disk(), it.onError());
  }
}
