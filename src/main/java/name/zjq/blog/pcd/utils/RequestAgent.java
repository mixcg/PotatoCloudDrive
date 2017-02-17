package name.zjq.blog.pcd.utils;

import javax.servlet.http.HttpServletRequest;

import eu.bitwalker.useragentutils.UserAgent;

public class RequestAgent {
	private HttpServletRequest request;
	private UserAgent agent;

	public RequestAgent(HttpServletRequest request) {
		this.request = request;
		agent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
	}

	/**
	 * 获取ip地址
	 * 
	 * @return
	 */
	public String getIp() {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 获得浏览器名称
	 * 
	 * @return
	 */
	public String getBrowserName() {
		return agent.getBrowser().getName();
	}

	/**
	 * 获取客户端信息
	 * 
	 * @return
	 */
	public String getOsName() {
		return agent.getOperatingSystem().getName();
	}
}
