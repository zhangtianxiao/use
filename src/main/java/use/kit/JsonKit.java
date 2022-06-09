package use.kit;

import com.jsoniter.output.JsonStream;

import java.io.OutputStream;

public class JsonKit {
  public static void asJson(Object o, OutputStream stream) {
    JsonStream.serialize(o,stream);
  }
}
