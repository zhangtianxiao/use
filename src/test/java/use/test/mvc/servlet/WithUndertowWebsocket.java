package use.test.mvc.servlet;

import io.undertow.websockets.jsr.UndertowContainerProvider;

import javax.websocket.WebSocketContainer;
import javax.websocket.server.ServerContainer;

public class WithUndertowWebsocket {
  /*
   servlet-websocket规范本身的api比较弱,
   spring有做中间桥接,
   自己做的话 会比较麻烦 需要适配不同的容器
  * */
  public static void main(String[] args) {
  }
}
