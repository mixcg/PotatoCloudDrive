package name.zjq.blog.pcd.bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import name.zjq.blog.pcd.services.CloudFileService;
import name.zjq.blog.pcd.utils.CoderUtil;

public class CloudFile {

	private boolean isdir = false;// 是否是目录
	private String fileType = "";// 文件类型
	private String fileName;// 文件名称
	private String base64FilePath;// base64编码文件地址
	private long fileSize;// 文件大小(单位:bytes)
	private String describeFileSize = "";// 文件描述大小
	private String lastModifiedTime;// 文件最后修改时间
	private boolean isPlayOnline = false;// 是否支持在线播放

	public CloudFile(String maindir, Path fileArg, BasicFileAttributes attrs) throws IOException {
		isdir = attrs.isDirectory();
		fileName = fileArg.getFileName().toString();
		if (isdir) {
			fileType = "folder";
		} else {
			int indexOf = fileName.lastIndexOf(".");
			if (indexOf > -1) {
				fileType = fileName.substring(indexOf + 1).toLowerCase();
			}
			isPlayOnline = CloudFileService.isCanPlayOnline(fileType);
			fileSize = Files.size(fileArg);
			describeFileSize = CloudFileService.calculateDescSize(fileSize);
		}
		String filepath = fileArg.toAbsolutePath().toString().replace("\\", "/").replaceFirst(maindir, "");
		base64FilePath = CoderUtil.encoderURLBASE64((filepath).getBytes());
		lastModifiedTime = CoderUtil.FORMMATTER.format(attrs.lastModifiedTime().toMillis());
	}

	public boolean isIsdir() {
		return isdir;
	}

	public String getFileType() {
		return fileType;
	}

	public String getFileName() {
		return fileName;
	}

	public String getBase64FilePath() {
		return base64FilePath;
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getDescribeFileSize() {
		return describeFileSize;
	}

	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	public boolean isPlayOnline() {
		return isPlayOnline;
	}
	
	public static void main(String[] args) throws Exception {  
	    displayContentType("D:/jdk-7u71-windows-i586.exe");  
	    displayContentType("E:/a.sh");  
	}  
	  
	static void displayContentType(String pathText) throws Exception {  
	    Path path = Paths.get(pathText);  
	    String type = Files.probeContentType(path);  
	    System.out.println(type);  
	}  
}
