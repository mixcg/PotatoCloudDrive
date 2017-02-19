package name.zjq.blog.pcd.bean;

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import name.zjq.blog.pcd.db.FieldAlias;
import name.zjq.blog.pcd.exceptionhandler.CustomLogicException;
import name.zjq.blog.pcd.services.ShareFileService;
import name.zjq.blog.pcd.utils.CoderUtil;
import name.zjq.blog.pcd.utils.StrUtil;

public class FileShare {
    public static final String CREATE_TABLE_SQL = "create table fileshare(id varchar(50), owner varchar(50), password varchar(50), filename varchar(1000),filetype varchar(200),filepath varchar(1000),sharedate int,downloadtimes int);";

    @FieldAlias
    private String id;// 主键
    @FieldAlias
    private String owner;// 所有者
    @FieldAlias(alias = "filename")
    private String fileName;// 文件名称
    @FieldAlias(alias = "filetype")
    private String fileType = "";// 文件类型
    @FieldAlias
    private String password;// 分享密码
    @FieldAlias(alias = "filepath")
    private String filePath;// 文件路径
    @FieldAlias(alias = "sharedate")
    private long shareDate;// 分享时间(毫秒数)
    @FieldAlias(alias = "downloadtimes")
    private int downloadTimes;// 下载次数

    public FileShare(User loginUser, String filepath) throws Exception {
        this.id = StrUtil.getUUID();
        this.shareDate = new Date().getTime();
        this.filePath = filepath;
        this.owner = loginUser.getUsername();
        this.password = ShareFileService.generatePassword();
        filepath = new String(CoderUtil.decoderURLBASE64(filepath), "UTF-8");
        Path file = Paths.get(loginUser.getDirectory(), filepath);
        if (!Files.exists(file, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
            throw new CustomLogicException(400, "要分享的文件不存在", null);
        }
        fileName = file.getFileName().toString();
        if (Files.isDirectory(file, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
            this.fileType = "folder";
        } else {
            int indexOf = fileName.lastIndexOf(".");
            if (indexOf > -1) {
                this.fileType = fileName.substring(indexOf + 1).toLowerCase();
            }
        }
    }

    public FileShare() {

    }

    public FileShare(User loginUser) {
        this.owner = loginUser.getUsername();
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getOwner() {
        return owner;
    }

    public String getPassword() {
        return password;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getShareDate() {
        return shareDate;
    }

    public int getDownloadTimes() {
        return downloadTimes;
    }

    public String getDiscShareDate() {
        return CoderUtil.FORMMATTER.format(new Date(shareDate));
    }
}
