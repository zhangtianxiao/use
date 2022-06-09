package use.mvc.pararesolver;

import use.mvc.router.Action;
import use.mvc.parabind.UploadedFile;
import use.mvc.mi.POST;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

 class ParaResolver_File extends ParaResolver {
  public static final ParaResolver_File me = new ParaResolver_File(null);
  final String name;

  public ParaResolver_File(String name) {
    super(name);
    this.name = name;
  }

  @Override
  public Object resolve(Action action) {
    return action.uploaded(name);
  }

  @Override
  public ParaResolver resolve(Parameter p, Type type, int i) {
    return new ParaResolver_File(p.getName());
  }

  @Override
  public boolean match(Parameter p, Type type, int i) {
    boolean toDisk;
    POST annotation = p.getDeclaringExecutable().getAnnotation(POST.class);
    if (annotation != null)
      toDisk = annotation.to_disk();
    else
      toDisk = false;
    return type == UploadedFile.class && !toDisk;
  }
}