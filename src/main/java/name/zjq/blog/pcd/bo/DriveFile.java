package name.zjq.blog.pcd.bo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DriveFile {
	private boolean isDir;// 是否是目录
	private String fileType;// 文件类型
	private String fileName;// 文件名称
	private long fileSize;// 文件大小(单位:k)
	private String descSize;// 描述大小
	private String lastModifiedTime;// 文件最后修改时间

	public DriveFile(File fileArg) {
		this.isDir = fileArg.isDirectory();
		this.fileName = fileArg.getName();
		this.fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
		this.fileSize = fileArg.length();
		this.descSize = "1G";
		this.lastModifiedTime = formatTime(fileArg.lastModified());
	}

	public boolean isDir() {
		return isDir;
	}

	public String getFileType() {
		return fileType;
	}

	public String getFileName() {
		return fileName;
	}

	public String getDescSize() {
		return descSize;
	}

	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	private static String formatTime(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(new Date(time));
	}

	/**
	 * 获取文件列表
	 * @param filepath
	 * @return
	 */
	public static List<DriveFile> getFileList(String filepath) {
		File file = new File(filepath);
		if (file.exists()) {
			return null;
		}
		List<DriveFile> filelsit = new ArrayList<DriveFile>();
		if (file.isFile()) {
			filelsit.add(new DriveFile(file));
			return filelsit;
		} else {
			File[] files = file.listFiles();
			for (File f : files) {
				filelsit.add(new DriveFile(f));
				return filelsit;
			}
		}
		return null;
	}
}
