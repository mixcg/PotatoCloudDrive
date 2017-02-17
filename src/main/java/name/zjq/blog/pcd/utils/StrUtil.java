package name.zjq.blog.pcd.utils;

import java.util.UUID;

public class StrUtil {
	/**
	 * 对象是否为null或空字符串
	 * 
	 * @param a
	 * @return
	 */
	public static boolean isNullOrEmpty(String a) {
		return a == null || "".equals(a);
	}

	/**
	 * 获取uuid
	 * @return
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
