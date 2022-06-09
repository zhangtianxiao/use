package use.jackson;

import com.fasterxml.jackson.databind.ObjectWriter;
import use.kit.Helper;

public class Test {
  public static void main(String[] args) {
    ObjectWriter writer = Helper.getObjectMapper().writer();
  }
}
