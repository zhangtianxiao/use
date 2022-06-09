
package use.template.expr.ast;

import use.template.TemplateException;
import use.template.expr.ast.SharedMethodKit.SharedMethodInfo;
import use.template.stat.Location;
import use.template.stat.ParseException;
import use.template.stat.Scope;

/**
 * SharedMethod
 * 
 * 用法：
 * engine.addSharedMethod(new StrKit());
 * engine.addSharedStaticMethod(MyKit.class);
 * 
 * #if (notBlank(para))
 *     ....
 * #end
 * 
 * 上面代码中的 notBlank 方法来自 StrKit
 */
public class SharedMethod extends Expr {
	
	private final SharedMethodKit sharedMethodKit;
	private final String methodName;
	private final ExprList exprList;
	
	public SharedMethod(SharedMethodKit sharedMethodKit, String methodName, ExprList exprList, Location location) {
		if (MethodKit.isForbiddenMethod(methodName)) {
			throw new ParseException("Forbidden method: " + methodName, location); 
		}
		this.sharedMethodKit = sharedMethodKit;
		this.methodName = methodName;
		this.exprList = exprList;
		this.location = location;
	}
	
	public Object eval(Scope scope) {
		Object[] argValues = exprList.evalExprList(scope);
		
		try {
			SharedMethodInfo sharedMethodInfo = sharedMethodKit.getSharedMethodInfo(methodName, argValues);
			if (sharedMethodInfo != null) {
				return sharedMethodInfo.invoke(argValues);
			} else {
				// ShareMethod 相当于是固定的静态的方法，不支持 null safe，null safe 只支持具有动态特征的用法
				throw new TemplateException(Method.buildMethodNotFoundSignature("Shared method not found: ", methodName, argValues), location);
			}
			
		} catch (TemplateException | ParseException e) {
			throw e;
		} catch (Exception e) {
			throw new TemplateException(e.getMessage(), location, e);
		}
	}
}




