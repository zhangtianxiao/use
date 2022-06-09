package use.test.web.test;

import org.springframework.stereotype.Component;
import use.mvc.router.WebSocketSession;

import java.nio.ByteBuffer;
import java.util.Date;

@Component
public class TestWebSocketSession extends WebSocketSession {
  @Override
  public void onConnected() {
    System.err.println("ws connected..." + this);
    sendText(new Date().toString());
  }

  @Override
  public void onClosed() {
    System.err.println("ws closed..." + this);
  }

  @Override
  public void onBinary(ByteBuffer buf) {
    System.err.println("ws binary..." + buf);
  }

  @Override
  public void onText(String msg) {
    sendText(new Date().toString());
    System.err.println("ws text..." + msg);
  }

  @Override
  public void onError(Throwable e) {
    e.printStackTrace();
  }
}
