package name.zjq.blog.pcd.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import name.zjq.blog.pcd.bo.User;

public class StartupListener implements ApplicationContextAware {

	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		RSAEncrypt.getInstance();
		User.init();
		DBConnection.init();
	}

}
