
package use.template.ext.extensionmethod;

/**
 * 针对 java.lang.Short 的扩展方法
 * 
 * 用法：
 * #if(value.toInt() == 123)
 */
public class ShortExt {
	
	public Boolean toBoolean(Short self) {
		return self != 0;
	}
	
	public Integer toInt(Short self) {
		return self.intValue();
	}
	
	public Long toLong(Short self) {
		return self.longValue();
	}
	
	public Float toFloat(Short self) {
		return self.floatValue();
	}
	
	public Double toDouble(Short self) {
		return self.doubleValue();
	}
	
	public Short toShort(Short self) {
		return self;
	}
	
	public Byte toByte(Short self) {
		return self.byteValue();
	}
}



