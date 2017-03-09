package name.zjq.blog.pcd.bean;

import name.zjq.blog.pcd.services.CloudFileService;
import name.zjq.blog.pcd.utils.CoderUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * 云端文件
 */
public class CloudFile {

    private boolean isDir;// 是否是目录
    private String fileType = "";// 文件类型
    private String fileName;// 文件名称
    private String base64FilePath;// base64编码文件地址
    private long fileSize;// 文件大小(单位:bytes)
    private String describeFileSize = "";// 文件描述大小
    private String lastModifiedTime;// 文件最后修改时间
    private boolean isPlayOnline;// 是否支持在线播放
    private boolean isCanPreview;//是否支持预览

    /**
     * @param mainDir 用户主目录
     * @param fileArg 文件
     * @param attrs   文件属性
     * @throws IOException
     */
    public CloudFile(String mainDir, Path fileArg, BasicFileAttributes attrs) throws IOException {
        mainDir = mainDir.replace("\\", "/");
        String relativeFilePath = fileArg.toAbsolutePath().toString().replace("\\", "/").replaceFirst(mainDir, "");
        base64FilePath = CoderUtil.encoderURLBASE64((relativeFilePath).getBytes());
        fileName = fileArg.getFileName().toString();
        lastModifiedTime = CoderUtil.FORMMATTER.format(attrs.lastModifiedTime().toMillis());
        if (isDir = attrs.isDirectory()) {
            fileType = "folder";
        } else {
            int indexOf = fileName.lastIndexOf(".");
            if (indexOf > -1) {
                fileType = fileName.substring(indexOf + 1).toLowerCase();
            }
            isPlayOnline = CloudFileService.isCanPlayOnline(fileType);
            isCanPreview = CloudFileService.isCanPreview(fileType);
            fileSize = Files.size(fileArg);
            describeFileSize = CloudFileService.calculateDescSize(fileSize);
        }
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

    public boolean isCanPreview() {
        return isCanPreview;
    }
}
