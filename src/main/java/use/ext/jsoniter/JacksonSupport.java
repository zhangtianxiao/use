package use.ext.jsoniter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Decoder;
import com.jsoniter.spi.Encoder;
import com.jsoniter.spi.JsoniterSpi;
import use.kit.Helper;

import java.io.IOException;

public class JacksonSupport {

  static final Encoder.ReflectionEncoder reflectionEncoder = new Encoder.ReflectionEncoder() {
    @Override
    public void encode(Object obj, JsonStream stream) throws IOException {
      //stream.writeVal(new SimpleDateFormat().format(obj));
      //String val = year + "-" + (month < 10 ? 0 : "") + month + "-" + (day < 10 ? 0 : "") + day;
      JsonNode node = (JsonNode) obj;
      // 写jackson的node, 就不能适配jsoniter的格式化了, 这里只能writer raw
      Helper.getObjectMapper().createGenerator(stream).writeObject(node);
      //stream.writeVal(val);
    }

    @Override
    public Any wrap(Object obj) {
      JsonNode node = (JsonNode) obj;
      return Any.wrap(node.toString());
    }
  };

  /**
   unknown  jsonObject jsonArray
   */
  static final Decoder commonDecoder = iter -> {
    // 这里可以通过 head tail方式把array[]抠出来, 避免String开销
    /*Any object = iter.readAny();
    iter.readStringAsSlice()
    String str = object.toString();
    JsonNode jsonNode = Global.createJsonNode(str.getBytes());*/

    byte[] buf = iter.getBuf();
    int start = iter.getHead();
    iter.readAny();
    int tail = iter.getHead();
    JsonNode jsonNode = Helper.createJsonNode(buf, start, tail);
    return jsonNode;
  };

  public static void enable() {
    JsoniterSpi.registerTypeEncoder(JsonNode.class, reflectionEncoder);
    JsoniterSpi.registerTypeDecoder(JsonNode.class, commonDecoder);
    JsonStream.registerNativeEncoder(JsonNode.class, reflectionEncoder);

    JsoniterSpi.registerTypeEncoder(ObjectNode.class, reflectionEncoder);
    JsoniterSpi.registerTypeDecoder(ObjectNode.class, commonDecoder);
    JsonStream.registerNativeEncoder(ObjectNode.class, reflectionEncoder);

    JsoniterSpi.registerTypeEncoder(ArrayNode.class, reflectionEncoder);
    JsoniterSpi.registerTypeDecoder(ArrayNode.class, commonDecoder);
    JsonStream.registerNativeEncoder(ArrayNode.class, reflectionEncoder);
  }
}
