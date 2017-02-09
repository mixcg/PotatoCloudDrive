package name.zjq.blog.pcd.bo;

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import name.zjq.blog.pcd.config.DBConnection;
import name.zjq.blog.pcd.config.FieldAlias;
import name.zjq.blog.pcd.utils.Coder;
import name.zjq.blog.pcd.utils.StrUtil;

public class FileShare {
	@FieldAlias
	private String id;// 主键
	@FieldAlias
	private String owner;// 所有者
	@FieldAlias
	private String filename;// 文件名称
	@FieldAlias
	private String filetype = "";// 文件类型
	@FieldAlias
	private String password;// 分享密码
	@FieldAlias
	private String filepath;// 文件路径
	@FieldAlias
	private long sharedate;// 分享时间(毫秒数)
	@FieldAlias
	private int downloadtimes;// 下载次数

	public FileShare(User loginUser, String filepath) throws UnsupportedEncodingException {
		this.filepath = filepath;
		filepath = new String(Coder.decoderURLBASE64(filepath), "UTF-8");
		this.id = StrUtil.getUUID();
		this.sharedate = new Date().getTime();
		Path file = Paths.get(loginUser.getDirectory(), filepath);
		filename = file.getFileName().toString();
		if (Files.isDirectory(file, new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
			this.filetype = "文件夹";
		} else {
			if (filename.lastIndexOf(".") > -1) {
				this.filetype = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
			}
		}
		this.owner = loginUser.getUsername();
		generatePassword();
	}

	public FileShare() {

	}

	public FileShare(User loginUser) {
		this.owner = loginUser.getUsername();
	}

	public String getId() {
		return id;
	}

	public String getFilename() {
		return filename;
	}

	public String getFiletype() {
		return filetype;
	}

	public String getOwner() {
		return owner;
	}

	public String getPassword() {
		return password;
	}

	public String getFilepath() {
		return filepath;
	}

	public long getSharedate() {
		return sharedate;
	}

	public int getDownloadtimes() {
		return downloadtimes;
	}

	/**
	 * 生成密码
	 */
	private void generatePassword() {
		byte[] bytes = new byte[4];
		byte[] uuid = StrUtil.getUUID().getBytes();
		for (byte i = 0; i < 4; i++) {
			bytes[i] = uuid[(int) (Math.random() * 32)];
		}
		this.password = new String(bytes);
	}

	/**
	 * 添加新文件分享
	 * 
	 * @return
	 */
	public boolean addNewShare() {
		String querysql = "select * from fileshare where owner='%s' and filepath='%s'";
		querysql = String.format(querysql, this.owner, this.filepath);
		List<FileShare> shareList = DBConnection.executeQuery(querysql, FileShare.class);
		if (shareList != null && !shareList.isEmpty()) {
			FileShare fileshare = shareList.get(0);
			this.id = fileshare.getId();
			this.owner = fileshare.getOwner();
			this.filename = fileshare.getFilename();
			this.filetype = fileshare.getFiletype();
			this.password = fileshare.getPassword();
			this.filepath = fileshare.getFilepath();
			this.sharedate = fileshare.getSharedate();
			this.downloadtimes = fileshare.getDownloadtimes();
			return true;
		} else {
			String sql = "insert into fileshare values ('%s','%s','%s','%s','%s','%s', %s ,0)";
			sql = String.format(sql, this.id, this.owner, this.password, this.filename, this.filetype, this.filepath,
					this.sharedate);
			return DBConnection.executeUpdate(sql) > 0;
		}
	}

	/**
	 * 查询所有的分享
	 * 
	 * @return
	 */
	public static List<FileShare> searchAllShare(String owner) {
		String querysql = "select * from fileshare where owner='%s'";
		querysql = String.format(querysql, owner);
		return DBConnection.executeQuery(querysql, FileShare.class);
	}

	/**
	 * 取消分享
	 * @param id 分享文件的id
	 * @param owner 所有者
	 * @return
	 */
	public static boolean cancelShare(String id, String owner) {
		String deleteSQL = "delete from fileshare where id ='%s' and owner='%s'";
		deleteSQL = String.format(deleteSQL, id, owner);
		return DBConnection.executeUpdate(deleteSQL) > 0;
	}

	/**
	 * 获取建表语句
	 * 
	 * @return
	 */
	public static final String getTableSQL() {
		return "create table fileshare(id varchar(50), owner varchar(50), password varchar(50), filename varchar(1000),filetype varchar(200),filepath varchar(1000),sharedate int,downloadtimes int);";
	}
}
