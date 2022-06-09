package use.sql;

import org.jetbrains.annotations.Nullable;
import use.kit.ex.Unsupported;
import use.template.io.EmptyWriter;
import use.template.io.TextWriter;
import use.template.io.Writer;
import use.template.io.WriterWrapper;
import use.template.stat.ast.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SqlParaWriter extends WriterWrapper {
  public static final Consumer<SqlParaWriter> clearer = SqlParaWriter::reset;

  @Override
  public void close() {
    throw new Unsupported();
  }

  public final List<Object> paras = new ArrayList<>();


  /*
  // 传入实际的writer
  public SqlParaWriter(Writer writer) {
    super(writer);
  }*/

  public SqlParaWriter(TextWriter writer) {
    super(writer);
  }

  public SqlParaWriter(EmptyWriter writer) {
    super(writer);
  }

  public abstract String toString();

  public abstract String toSQL();

  @Override
  public void reset() {
    super.reset();
    paras.clear();
  }

  public static class Dynamic extends SqlParaWriter {

    public Dynamic() {
      super(new TextWriter());
    }

    @Override
    public String toString() {
      return delegate.toUTF8();
    }

    @Override
    public String toSQL() {
      return delegate.toUTF8();
    }

    public static class Fast extends Dynamic {
      public static final Supplier<SqlParaWriter> maker = Fast::new;

      @Override
      public void writeVar(@Nullable Object any) {
        writeVal('?');
        paras.add(any);
      }
    }
  }

  /**
   仅用于承载参数, 不做字符串拼接
   */
  public static class Immutable extends SqlParaWriter {

    public Immutable() {
      super(Writer.empty());
    }

    @Override
    public String toString() {
      throw new Unsupported();
    }

    @Override
    public String toSQL() {
      throw new Unsupported();
    }

    public static class Fast extends Dynamic {
      public static final Supplier<SqlParaWriter> maker = Fast::new;

      @Override
      public void writeVar(@Nullable Object any) {
        paras.add(any);
      }
    }
  }
}
