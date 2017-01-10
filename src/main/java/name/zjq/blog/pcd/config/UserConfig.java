package name.zjq.blog.pcd.config;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserConfig {
	private static final Log logger = LogFactory.getLog(StartupListener.class);

	private static final Properties prop = new Properties();
	private static boolean initFlag = false;

	public static void init() {
		if (!initFlag) {
			try {
				prop.load(UserConfig.class.getResourceAsStream("/config.properties"));
			} catch (Exception e) {
				logger.error("配置文件加载异常！", e);
				System.exit(1);
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
		String _username = prop.getProperty("username", "admin");
		String _password = prop.getProperty("password", "admin");
		return _username.equals(username) && _password.equals(password);
	}

}
