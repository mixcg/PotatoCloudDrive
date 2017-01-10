package name.zjq.blog.pcd.auth;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import name.zjq.blog.pcd.config.RSAEncrypt;
import name.zjq.blog.pcd.utils.StrUtil;

public class RequestAuth {
	private static Map<String, String> keep = new HashMap<String, String>();
	static {
		
	}
	private static Lock lock = new ReentrantLock();

	public static String getToken(String ip) {
		long time = new Date().getTime() + 30 * 60 * 1000;
		return RSAEncrypt.getInstance().encryptByPubKey(ip + "#" + StrUtil.getUUID() + "#" + time);
	}
}
