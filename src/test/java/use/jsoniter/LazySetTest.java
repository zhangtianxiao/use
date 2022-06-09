package use.jsoniter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;
import use.kit.Helper;

import java.io.File;

public class LazySetTest {
  public static void main(String[] args) {
    File file = new File("env.json5");
    /*Any any = Helper.lazy(file);
    Any win = any.get("node", "win");*/
    JsonNode json = Helper.readAsJson(file);
    ObjectNode obj = (ObjectNode)json.get("node");
    JsonNode win = obj.get("win");
    obj.put("win","new value");
    // obj.set("win",(Object)"");
    System.out.println("");
  }
}
