package use.kit;

public class Digit2UTF8 {

  static final byte[] DigitTens = {
    '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
    '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
    '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
    '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
    '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
    '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
    '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
    '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
    '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
    '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
  };

  static final byte[] DigitOnes = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  };

  static int getChars(long i, int index, byte[] buf) {
    long q;
    int r;
    int charPos = index;

    boolean negative = (i < 0);
    if (!negative) {
      i = -i;
    }

    // Get 2 digits/iteration using longs until quotient fits into an int
    while (i <= Integer.MIN_VALUE) {
      q = i / 100;
      r = (int) ((q * 100) - i);
      i = q;
      buf[--charPos] = DigitOnes[r];
      buf[--charPos] = DigitTens[r];
    }

    // Get 2 digits/iteration using ints
    int q2;
    int i2 = (int) i;
    while (i2 <= -100) {
      q2 = i2 / 100;
      r = (q2 * 100) - i2;
      i2 = q2;
      buf[--charPos] = DigitOnes[r];
      buf[--charPos] = DigitTens[r];
    }

    // We know there are at most two digits left at this point.
    q2 = i2 / 10;
    r = (q2 * 10) - i2;
    buf[--charPos] = (byte) ('0' + r);

    // Whatever left is the remaining digit.
    if (q2 < 0) {
      buf[--charPos] = (byte) ('0' - q2);
    }

    if (negative) {
      buf[--charPos] = (byte) '-';
    }
    return charPos;
  }


  static int stringSize(long x) {
    int d = 1;
    if (x >= 0) {
      d = 0;
      x = -x;
    }
    long p = -10;
    for (int i = 1; i < 19; i++) {
      if (x > p)
        return i + d;
      p = 10 * p;
    }
    return 19 + d;
  }

  static int getChars(int i, int index, byte[] buf) {
    int q, r;
    int charPos = index;

    boolean negative = i < 0;
    if (!negative) {
      i = -i;
    }

    // Generate two digits per iteration
    while (i <= -100) {
      q = i / 100;
      r = (q * 100) - i;
      i = q;
      buf[--charPos] = DigitOnes[r];
      buf[--charPos] = DigitTens[r];
    }

    // We know there are at most two digits left at this point.
    q = i / 10;
    r = (q * 10) - i;
    buf[--charPos] = (byte) ('0' + r);

    // Whatever left is the remaining digit.
    if (q < 0) {
      buf[--charPos] = (byte) ('0' - q);
    }

    if (negative) {
      buf[--charPos] = (byte) '-';
    }
    return charPos;
  }

  public static int stringSize(int x) {
    int d = 1;
    if (x >= 0) {
      d = 0;
      x = -x;
    }
    int p = -10;
    for (int i = 1; i < 10; i++) {
      if (x > p)
        return i + d;
      p = 10 * p;
    }
    return 10 + d;
  }

  public static int toBytes(byte[] bytes, int v, int start) {
    int i = stringSize(v);
    getChars(v, start + i, bytes);
    return i;
  }

  public static int toBytes(byte[] bytes, long v, int start) {
    int i = stringSize(v);
    getChars(v, start + i, bytes);
    return i;
  }

  //public static final byte[] Infinity = "-Infinity".getBytes(StandardCharsets.UTF_8);

}
