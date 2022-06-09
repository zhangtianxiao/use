package use.mvc.router;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class WebSocketSession {
  private WebSocketSession delegate;

  void setDelegate(WebSocketSession delegate) {
    this.delegate = delegate;
    this.onConnected();
  }

  /**
   * 内置子类主要实现 sendText和 send 两个
   * 业务子类实现onXXX方法
   */
  public void sendText(String text) {
    delegate.sendText(text);
  }

  public void send(ByteBuffer buf) {
    delegate.send(buf);
  }

  public void close() {
    delegate.close();
  }

  public void send(byte[] buf) {
    send(ByteBuffer.wrap(buf));
  }

  public void send(String text) {
    send(ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8)));
  }


  public void send(byte[] buf, int head, int count) {
    send(ByteBuffer.wrap(buf, head, count));
  }

  public void onConnected() {
    delegate.onConnected();
  }

  public void onClosed() {
    delegate.onConnected();
  }

  public void onError(Throwable e) {
    delegate.onError(e);
  }

  public void onText(String msg) {
    delegate.onText(msg);
  }

  public void onBinary(ByteBuffer buf) {
    delegate.onBinary(buf);
  }

}

