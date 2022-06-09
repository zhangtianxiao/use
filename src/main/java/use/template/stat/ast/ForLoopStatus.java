
package use.template.stat.ast;

/**
 * ForLoopStatus
 * 封装 #for( init; cond; update) 循环的状态，便于模板中获取
 * 
 * 如下表达式可从模板中获取循环状态：
 * for.index 从 0 下始的下标
 * for.count 从 1 开始的计数器
 * for.first 是否第一个元素
 * for.odd 是否第奇数个元素
 * for.even 是否第偶数个元素
 * for.outer 获取外层 for 对象，便于获取外层 for 循环状态
 *           例如: for.outer.index
 * 
 * 注意：比迭代型循环语句少支持两个状态取值表达式：for.size、for.last
 */
public class ForLoopStatus {
	
	private Object outer;
	private int index;
	
	public ForLoopStatus(Object outer) {
		this.outer = outer;
		this.index = 0;
	}
	
	void nextState() {
		index++;
	}
	
	public Object getOuter() {
		return outer;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getCount() {
		return index + 1;
	}
	
	public boolean getFirst() {
		return index == 0;
	}
	
	public boolean getOdd() {
		return index % 2 == 0;
	}
	
	public boolean getEven() {
		return index % 2 != 0;
	}
}



