
package use.template.ext.extensionmethod;

/**
 * 针对 java.lang.Double 的扩展方法
 * 
 * 用法：
 * #if(value.toInt() == 123)
 */
public class DoubleExt {
	
	public Boolean toBoolean(Double self) {
		return self != 0;
	}
	
	public Integer toInt(Double self) {
		return self.intValue();
	}
	
	public Long toLong(Double self) {
		return self.longValue();
	}
	
	public Float toFloat(Double self) {
		return self.floatValue();
	}
	
	public Double toDouble(Double self) {
		return self;
	}
	
	public Short toShort(Double self) {
		return self.shortValue();
	}
	
	public Byte toByte(Double self) {
		return self.byteValue();
	}
}



