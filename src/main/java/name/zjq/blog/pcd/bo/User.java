package name.zjq.blog.pcd.bo;

public class User {
	private String username;// 用户名
	private String password;// 密码
	private String directory;// 目录

	private String token;
	private long expirationtime;// 过期时间

	public User(String username, String password, String directory) {
		this.username = username;
		this.password = password;
		this.directory = directory;
	}

	/**
	 * 登录验证
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean loginAuth(String username, String password) {
		return this.username.equals(username) && this.password.equals(password);
	}

	public long getExpirationtime() {
		return expirationtime;
	}

	public void setExpirationtime(long expirationtime) {
		this.expirationtime = expirationtime;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getDirectory() {
		return this.directory;
	}
}
