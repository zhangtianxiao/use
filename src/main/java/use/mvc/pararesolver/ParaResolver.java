package use.mvc.pararesolver;

import use.mvc.router.Action;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public abstract class ParaResolver {
  final String name;

  protected ParaResolver(String name) {
    this.name = name;
  }

  protected ParaResolver(String name, String primary) {
    this.name = primary == null || primary.isEmpty() ? name : primary;
  }

  public abstract Object resolve(Action action);

  public abstract ParaResolver resolve(Parameter p, Type type, int i);

  public abstract boolean match(Parameter p, Type type, int i);


  private static final ParaResolver[] paraResolvers = new ParaResolver[]{
    // 前边几个无关乎优先级
    ParaResolver_Action.me,
    ParaResolver_File.me,
    ParaResolver_Header.me,
    ParaResolver_Cookie.me,
    ParaResolver_PathVariable.me,
    ParaResolver_RequestBody.me,

    // 中间或可扩展

    // 优先级最低的
    ParaResolver_Query.me,
    ParaResolver_Null.me,
  };

}




