package use.jsoniter;

import com.jsoniter.JsonIterator;

public class ReadBigDecimal {
  public static void main(String[] args) {
    Pojo pojo = JsonIterator.deserialize("""
      {"num":1}
      """, Pojo.class);

    Pojo pojo2 = JsonIterator.deserialize("""
      {"num":"1"}
      """, Pojo.class);
    System.out.println("");
  }
}
