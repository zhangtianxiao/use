
package use.template.stat;

/**
 * Location
 * 生成异常发生的位置消息
 */
public class Location {
	
	private final String templateFile;
	private final int row;
	private String msg;
	
	public Location(String templateFile, int row) {
		this.templateFile = templateFile;
		this.row = row;
		this.msg = null;
	}
	
	public String toString() {
		if (msg == null) {
			StringBuilder buf = new StringBuilder();
			if (templateFile != null) {
				buf.append("\nTemplate: \"").append(templateFile).append("\". Line: ").append(row);
			} else {
				buf.append("\nString template line: ").append(row);
			}
			msg = buf.toString();
		}
		return msg;
	}
	
	public String getTemplateFile() {
		return templateFile;
	}
	
	public int getRow() {
		return row;
	}
}






