
package use.template.expr.ast;

import java.lang.reflect.Method;

/**
 * MethodInfoExt 辅助实现 extension method 功能
 */
public class MethodInfoExt extends MethodInfo {
	
	protected Object objectOfExtensionClass;
	
	public MethodInfoExt(Object objectOfExtensionClass, Long key, Class<?> clazz, Method method) {
		super(key, clazz, method);
		this.objectOfExtensionClass = objectOfExtensionClass;
		
		// 将被 mixed 的类自身添加入参数类型数组的第一个位置
		// Class<?>[] newParaTypes = new Class<?>[paraTypes.length + 1];
		// newParaTypes[0] = clazz;	// 第一个参数就是被 mixed 的类它自己
		// System.arraycopy(paraTypes, 0, newParaTypes, 1, paraTypes.length);
		// this.paraTypes = newParaTypes;
	}
	
	public Object invoke(Object target, Object... args) throws ReflectiveOperationException {
		Object[] finalArgs = new Object[args.length + 1];
		finalArgs[0] = target;
		
		if (args.length > 0) {
			System.arraycopy(args, 0, finalArgs, 1, args.length);
		}
		
		if (isVarArgs) {
			return invokeVarArgsMethod(objectOfExtensionClass, finalArgs);
		} else {
			return method.invoke(objectOfExtensionClass, finalArgs);
		}
	}
}







