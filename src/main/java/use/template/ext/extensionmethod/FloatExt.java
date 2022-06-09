
package use.template.ext.extensionmethod;

/**
 * 针对 java.lang.Float 的扩展方法
 * 
 * 用法：
 * #if(value.toInt() == 123)
 */
public class FloatExt {
	
	public Boolean toBoolean(Float self) {
		return self != 0;
	}
	
	public Integer toInt(Float self) {
		return self.intValue();
	}
	
	public Long toLong(Float self) {
		return self.longValue();
	}
	
	public Float toFloat(Float self) {
		return self;
	}
	
	public Double toDouble(Float self) {
		return self.doubleValue();
	}
	
	public Short toShort(Float self) {
		return self.shortValue();
	}
	
	public Byte toByte(Float self) {
		return self.byteValue();
	}
}



