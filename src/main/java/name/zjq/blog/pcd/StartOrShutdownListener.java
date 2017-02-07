package name.zjq.blog.pcd;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import name.zjq.blog.pcd.bo.User;
import name.zjq.blog.pcd.config.DBConnection;
import name.zjq.blog.pcd.config.RSAEncrypt;

public class StartOrShutdownListener implements ServletContextListener {
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		DBConnection.destoryConnection();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		RSAEncrypt.getInstance();
		User.init();
		DBConnection.init();
	}

}
