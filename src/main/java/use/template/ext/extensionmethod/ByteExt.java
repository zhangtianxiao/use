
package use.template.ext.extensionmethod;

/**
 * 针对 java.lang.Byte 的扩展方法
 * 
 * 用法：
 * #if(value.toInt() == 123)
 */
public class ByteExt {
	
	public Boolean toBoolean(Byte self) {
		return self != 0;
	}
	
	public Integer toInt(Byte self) {
		return self.intValue();
	}
	
	public Long toLong(Byte self) {
		return self.longValue();
	}
	
	public Float toFloat(Byte self) {
		return self.floatValue();
	}
	
	public Double toDouble(Byte self) {
		return self.doubleValue();
	}
	
	public Short toShort(Byte self) {
		return self.shortValue();
	}
	
	public Byte toByte(Byte self) {
		return self;
	}
}



