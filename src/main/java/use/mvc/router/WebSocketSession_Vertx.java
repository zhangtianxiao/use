package use.mvc.router;

import io.vertx.core.http.ServerWebSocket;
import use.kit.VertxKit;

import java.nio.ByteBuffer;

public class WebSocketSession_Vertx extends WebSocketSession {

  final ServerWebSocket socket;

  public WebSocketSession_Vertx(ServerWebSocket socket) {
    this.socket = socket;
  }

  @Override
  public void sendText(String text) {
    socket.writeFinalTextFrame(text);
  }

  @Override
  public void send(ByteBuffer buf) {
    socket.writeFinalBinaryFrame(VertxKit.asBuffer(buf));
  }

  public void close() {
    socket.close();
  }

}
