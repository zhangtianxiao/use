package use.mvc.pararesolver;

public class ParaResolverDefaults {
  public static final ParaResolver[] paraResolvers = new ParaResolver[]{
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
