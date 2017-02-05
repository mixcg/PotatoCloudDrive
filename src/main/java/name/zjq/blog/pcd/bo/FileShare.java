package name.zjq.blog.pcd.bo;

import java.util.Date;
import java.util.List;

import name.zjq.blog.pcd.config.DBInit;
import name.zjq.blog.pcd.utils.StrUtil;

public class FileShare {
	private String id;// 主键
	private String owner;// 所有者
	private String password;// 分享密码
	private String filepath;// 文件路径(base64 url)
	private long sharedate;// 分享时间(毫秒数)
	private int downloadtimes;// 下载次数

	public FileShare(String owner, String password, String filepath) {
		this.id = StrUtil.getUUID();
		this.sharedate = new Date().getTime();
		this.owner = owner;
		this.password = password;
		this.filepath = filepath;
	}

	public FileShare() {

	}

	public FileShare(String id) {

	}

	public String getId() {
		return id;
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
	 * 添加新分享
	 * 
	 * @return
	 */
	public boolean addNewShare() {
		String querysql = "select * from fileshare where owner='%s' and filepath='%s'";
		querysql = String.format(querysql, this.owner, this.filepath);
		List<FileShare> shareList = DBInit.executeQuery(querysql, FileShare.class);
		if (shareList != null && !shareList.isEmpty()) {
			FileShare fileshare = shareList.get(0);
			this.id = fileshare.getId();
			this.owner = fileshare.getOwner();
			this.password = fileshare.getPassword();
			this.filepath = fileshare.getFilepath();
			this.sharedate = fileshare.getSharedate();
			this.downloadtimes = fileshare.getDownloadtimes();
			return true;
		} else {
			String sql = "insert into fileshare values ('%s','%s','%s','%s',%s,0)";
			sql = String.format(sql, this.id, this.owner, this.password, this.filepath, this.sharedate);
			return DBInit.executeUpdate(sql) > 0;
		}
	}

	/**
	 * 获取建表语句
	 * 
	 * @return
	 */
	public static final String getTableSQL() {
		return "create table fileshare(id varchar(50), owner varchar(50), password varchar(50), filepath varchar(1000),sharedate int,downloadtimes int);";
	}
}
