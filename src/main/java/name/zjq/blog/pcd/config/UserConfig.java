package name.zjq.blog.pcd.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import name.zjq.blog.pcd.bo.User;
import name.zjq.blog.pcd.utils.RequestInfo;
import name.zjq.blog.pcd.utils.StrUtil;

public class UserConfig {

	private static Lock lock = new ReentrantLock();
	private static final Log logger = LogFactory.getLog(StartupListener.class);
	private static final Properties prop = new Properties();
	private static boolean initFlag = false;

	private static Map<String, User> userlist = new HashMap<String, User>();

	public static void init() {
		if (!initFlag) {
			try {
				prop.load(UserConfig.class.getResourceAsStream("/config.properties"));
			} catch (Exception e) {
				logger.error("配置文件加载异常！", e);
				System.exit(1);
			}
			String usersplit = prop.getProperty("USERLIST");
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
				userlist.put(username, new User(username, password, directory));
			}
			initFlag = true;
		}
	}

	/**
	 * 登录验证
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public static boolean loginAuth(String username, String password) {
		if (userlist.containsKey(username)) {
			return userlist.get(username).loginAuth(username, password);
		}
		return false;
	}

	/**
	 * 生成token
	 * 
	 * @param request
	 * @return
	 */
	public static String createToken(String username, HttpServletRequest request) {
		RequestInfo ri = new RequestInfo(request);
		String _token = String.format("%s#%s#%s#%s#%s", username, ri.getIp(), ri.getBrowserName(), ri.getOsName(), StrUtil.getUUID());
		return RSAEncrypt.getInstance().encryptByPubKey(_token);
	}

	/**
	 * 设置token信息
	 * 
	 * @param username
	 * @param token
	 */
	public static void setToken(String username, String token) {
		lock.lock();
		long expired = new Date().getTime() + 30 * 60 * 1000;
		if (userlist.containsKey(username)) {
			User u = userlist.get(username);
			u.setToken(token);
			u.setExpirationtime(expired);
		}
		lock.unlock();
	}

	/**
	 * token认证
	 * 
	 * @param tokenArg
	 * @param request
	 * @return
	 */
	public static User checkToken(String tokenArg, HttpServletRequest request) {
		String _token = RSAEncrypt.getInstance().decryptByPriKey(tokenArg);
		if (_token == null) {
			return null;
		}
		String[] tokenArray = _token.split("#");
		if (tokenArray.length != 5) {
			return null;
		}
		String username = tokenArray[0];
		User u = userlist.get(username);
		String utoken = u.getToken();
		if (utoken.equals(tokenArg)) {
			RequestInfo ri = new RequestInfo(request);
			if (ri.getIp().equals(tokenArray[1]) && ri.getBrowserName().equals(tokenArray[2]) && ri.getOsName().equals(tokenArray[3])) {
				return u;
			}
		}
		return null;
	}
}
