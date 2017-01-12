package name.zjq.blog.pcd.enums;

public enum TokenENUM {
	/**
	 * 该用户不存在token
	 */
	NOT_EXIST,
	/**
	 * 过期
	 */
	EXPIRED,
	/**
	 * token不相符
	 */
	ERROR_TOKEN,
	/**
	 * 认证失败
	 */
	AUTH_FAIL
}
