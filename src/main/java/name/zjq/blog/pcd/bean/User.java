package name.zjq.blog.pcd.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import name.zjq.blog.pcd.utils.CoderUtil;
import name.zjq.blog.pcd.utils.RequestAgent;
import name.zjq.blog.pcd.utils.StrUtil;

public class User {
	private static Map<String, User> userlist = new HashMap<String, User>();

	/**
	 * 用户注册
	 * @param username 用户名
	 * @param password 密码
	 * @param directory 目录
	 */
	public static void registerUser(String username, String password, String directory) {
		userlist.put(username, new User(username, password, directory));
	}

	/**
	 * 登录验证
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public static User loginAuth(String username, String password) {
		if (userlist.containsKey(username)) {
			User u = userlist.get(username);
			if (u.username.equals(username) && u.password.equals(password)) {
				return u;
			}
		}
		return null;
	}

	/**
	 * 创建token
	 * 
	 * @return
	 */
	public synchronized String createToken(HttpServletRequest request) {
		if (userlist.containsKey(token)) {
			userlist.remove(token);
		}
		uuid = StrUtil.getUUID();
		RequestAgent ra = new RequestAgent(request);
		String cilentinfo = new StringBuilder().append(username).append(ra.getIp()).append(ra.getBrowserName())
				.append(ra.getOsName()).append(uuid).toString();
		int x = (int) (Math.random() * 100);
		token = CoderUtil.MD5(cilentinfo, x);
		md5times = x;
		this.expirationtime = new Date().getTime() + 30 * 60 * 1000;
		userlist.put(token, this);
		return token;
	}

	/**
	 * 校验token
	 * 
	 * @param _token
	 * @param request
	 * @return
	 */
	public static User checkToken(String _token, HttpServletRequest request) {
		if (userlist.containsKey(_token)) {
			User u = userlist.get(_token);
			if (u.expirationtime < new Date().getTime()) {
				return null;
			}
			RequestAgent ra = new RequestAgent(request);
			String token = new StringBuilder().append(u.username).append(ra.getIp()).append(ra.getBrowserName())
					.append(ra.getOsName()).append(u.uuid).toString();
			if (CoderUtil.MD5(token, u.md5times).equals(_token)) {
				u.setExpirationtime(new Date().getTime() + 30 * 60 * 1000);
				return u;
			} else {
				return null;
			}
		}
		return null;
	}

	private String username;// 用户名
	private String password;// 密码
	private String directory;// 目录

	private int md5times;// md5加密次数
	private String uuid;// 随机id
	private String token;
	private long expirationtime;// 过期时间

	private User(String username, String password, String directory) {
		this.username = username;
		this.password = password;
		this.directory = directory;
	}

	public void setExpirationtime(long expirationtime) {
		this.expirationtime = expirationtime;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getDirectory() {
		return directory;
	}

	public String getUsername() {
		return username;
	}
}
