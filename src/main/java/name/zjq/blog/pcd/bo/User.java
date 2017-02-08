package name.zjq.blog.pcd.bo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import name.zjq.blog.pcd.utils.Coder;
import name.zjq.blog.pcd.utils.RequestAgent;
import name.zjq.blog.pcd.utils.StrUtil;

public class User {
	private static final Log logger = LogFactory.getLog(User.class);
	public static final Properties prop = new Properties();
	private static boolean initFlag = false;
	private static Map<String, User> userlist = new HashMap<String, User>();

	/**
	 * 初始化配置文件
	 */
	public static void init() {
		if (!initFlag) {
			try {
				prop.load(User.class.getResourceAsStream("/config.properties"));
			} catch (Exception e) {
				logger.error("配置文件加载异常！", e);
				System.exit(1);
			}
			String usersplit = prop.getProperty("userlist");
			if (StrUtil.isNullOrEmpty(usersplit)) {
				logger.error("配置文件配置有误，请检查");
				System.exit(1);
			}
			String[] usernames = usersplit.split(",");
			for (String username : usernames) {
				String password = prop.getProperty(String.format("%s_password", username));
				String directory = prop.getProperty(String.format("%s_directory", username));
				if (password == null || directory == null) {
					logger.error("配置文件配置有误，请检查");
					System.exit(1);
				}
				directory = directory.replace("\\", "/");
				userlist.put(username, new User(username, password, directory));
			}
			initFlag = true;
		}
	}

	public static String getDBPath() {
		String dbPath = prop.getProperty("database");
		if (dbPath == null || !dbPath.endsWith(".db")) {
			logger.error("数据库配置有误，请检查");
			System.exit(1);
		}
		return prop.getProperty("database");
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
		token = Coder.MD5(cilentinfo, x);
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
			return Coder.MD5(token, u.md5times).equals(_token) ? u : null;
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
