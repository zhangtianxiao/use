package use.kit;

public class SimpleArray {
  int pos = 0;
  private Object[] array;

  public SimpleArray(int n) {
    array = new Object[n];
  }

  public void add(Object o) {
    if (pos == array.length) {
      // 一次扩容三个元素
      Object[] newArray = new Object[pos + 4];
      System.arraycopy(array, 0, newArray, 0, pos);
      this.array = newArray;
    }
    this.array[pos] = o;
    pos++;
  }

  public void clear() {
    for (int i = 0; i < pos; i++) {
      this.array[i] = null;
    }
    this.pos = 0;
  }

  public Object get(int index) {
    //if (index > pos) throw new ArrayIndexOutOfBoundsException();
    return array[index];
  }


  public void setArg(int index, Object value) {
    if (index >= array.length)
      throw new ArrayIndexOutOfBoundsException();
    array[index] = value;
  }
}
