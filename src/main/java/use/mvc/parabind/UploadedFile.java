package use.mvc.parabind;

import java.io.File;

public class UploadedFile {
  public UploadedFile(File file, byte[] temp, String originName) {
    this.file = file;
    this.temp = temp;
    this.originName = originName;
  }

  public final File file;
  public final byte[] temp;
  public final String originName;

  public long size() {
    return file == null ? temp.length : file.length();
  }
}
