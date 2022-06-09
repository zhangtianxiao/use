package use.test.nettybuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Test {
  public static void main(String[] args) {
    ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4});
    byteBuf.refCnt();
    System.out.println("");
  }
}
