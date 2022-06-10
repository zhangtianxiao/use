package use.ext.jsoniter;

import com.jsoniter.CodegenAccess;
import com.jsoniter.JsonIterator;
import com.jsoniter.spi.Decoder;
import com.jsoniter.spi.JsoniterSpi;

import java.io.IOException;
import java.math.BigDecimal;

public class MaybeStringBigDecimalDecoder implements Decoder {
  private static final MaybeStringBigDecimalDecoder me = new MaybeStringBigDecimalDecoder();

  private MaybeStringBigDecimalDecoder() {
  }

  public static void enable() {
    JsoniterSpi.registerTypeDecoder(BigDecimal.class, MaybeStringBigDecimalDecoder.me);
  }

  @Override
  public Object decode(JsonIterator iter) throws IOException {
    byte c = CodegenAccess.nextToken(iter);
    if (c != '"') {
      CodegenAccess.unreadByte(iter);
      return iter.readBigDecimal();
    }
    BigDecimal val = iter.readBigDecimal();
    c = CodegenAccess.nextToken(iter);
    if (c != '"') {
      throw iter.reportError("StringLongDecoder", "expect \", but found: " + (char) c);
    }
    return val;
  }
}
