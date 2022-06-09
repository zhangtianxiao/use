
package use.template.ext.extensionmethod;

/**
 * 针对 java.lang.Long 的扩展方法
 * 
 * 用法：
 * #if(value.toInt() == 123)
 */
public class LongExt {
	
	public Boolean toBoolean(Long self) {
		return self != 0;
	}
	
	public Integer toInt(Long self) {
		return self.intValue();
	}
	
	public Long toLong(Long self) {
		return self;
	}
	
	public Float toFloat(Long self) {
		return self.floatValue();
	}
	
	public Double toDouble(Long self) {
		return self.doubleValue();
	}
	
	public Short toShort(Long self) {
		return self.shortValue();
	}
	
	public Byte toByte(Long self) {
		return self.byteValue();
	}
}



