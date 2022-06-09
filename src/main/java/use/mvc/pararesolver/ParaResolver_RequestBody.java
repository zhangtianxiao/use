package use.mvc.pararesolver;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import use.mvc.router.Action;
import use.mvc.parabind.RequestBody;
import use.kit.ex.Unsupported;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.function.Function;

class ParaResolver_RequestBody extends ParaResolver {
  public static final ParaResolver_RequestBody me = new ParaResolver_RequestBody();
  final Function<Action, Object> converter;

  ParaResolver_RequestBody() {
    super(null);
    converter = null;
  }


  ParaResolver_RequestBody(Function<Action, Object> converter) {
    super(null);
    this.converter = converter;
  }

  @Override
  public Object resolve(Action action) {
    assert converter != null;
    return converter.apply(action);
  }

  @Override
  public ParaResolver resolve(Parameter p, Type type, int i) {
    final Function<Action, Object> converter;
    if (ParaResolver_Query.me.match(p, type, i)) {
      Function<String, Object> fun = ParaResolver_Query.match(type);
      converter = action -> {
        ByteBuffer buffer = action.requestBody();
        int position = buffer.position();
        //buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        // keep position
        buffer.position(position);
        String packet = new String(bytes);
        return fun.apply(packet);
      };
    }
    else if (type == ByteBuffer.class) {
      converter = Action::requestBody;
    }
    else if (type == byte[].class) {
      converter = action -> {
        ByteBuffer buffer = action.requestBody();
        int position = buffer.position();
        //buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        // keep position
        buffer.position(position);
        return bytes;
      };
    }
    else if (InputStream.class.isAssignableFrom((Class<?>) type)) {
      converter = action -> new ByteBufferBackedInputStream(action.requestBody());
    }
    else if (File.class == type) {
      converter = action -> action.get(RequestBody.AS_FILE);
    }
    else {
      throw new Unsupported();
    }
    return new ParaResolver_RequestBody(converter);
  }

  @Override
  public boolean match(Parameter p, Type type, int i) {
    // 该注解和string int decimal等类型一起出现
    RequestBody annotation = p.getAnnotation(RequestBody.class);
    if (annotation != null) {
      // 常规
      if (ParaResolver_Query.me.match(p, type, i)) {
        return true;
      }
      return false;
    }

    return type == ByteBuffer.class || type == byte[].class || InputStream.class.isAssignableFrom((Class<?>) type) || File.class == type;
  }
}
