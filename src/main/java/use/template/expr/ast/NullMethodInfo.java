
package use.template.expr.ast;

/**
 * NullMethodInfo
 * 
 * 1：MethodKit.getMethod(...) 消除 instanceof 判断
 * 2：Method.exec(...) 消除 null 值判断
 */
public class NullMethodInfo extends MethodInfo {
	
	public static final NullMethodInfo me = new NullMethodInfo();
	
	public boolean notNull() {
		return false;
	}
	
	public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
		throw new RuntimeException("The method invoke(Object, Object...) of NullMethodInfo should not be invoked");
	}
}

