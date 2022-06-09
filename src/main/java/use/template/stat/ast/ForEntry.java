
package use.template.stat.ast;

import java.util.Map.Entry;

/**
 * ForEntry 包装 HashMap、LinkedHashMap 等 Map 类型的 Entry 对象
 */
public class ForEntry implements Entry<Object, Object> {
	
	private Entry<Object, Object> entry;
	
	public void init(Entry<Object, Object> entry) {
		this.entry = entry;
	}
	
	public Object getKey() {
		return entry.getKey();
	}
	
	public Object getValue() {
		return entry.getValue();
	}
	
	public Object setValue(Object value) {
		return entry.setValue(value);
	}
}



