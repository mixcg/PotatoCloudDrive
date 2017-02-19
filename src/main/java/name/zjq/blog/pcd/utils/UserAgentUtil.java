package name.zjq.blog.pcd.utils;

import javax.servlet.http.HttpServletRequest;

import eu.bitwalker.useragentutils.UserAgent;

public class UserAgentUtil {
    /**
     * 获取客户端真实
     *
     * @param request
     * @return
     */
    public static String getIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.equals("")) {
            ip = "unknown";
        } else {
            ip = request.getHeader("X-Real-IP");
            if (ip == null || ip.equals("")) {
                ip = "unknown";
            }
        }
        return ip;
    }

    /**
     * 获取请求头id
     *
     * @param request
     * @return
     */
    public static int getUserAgentID(HttpServletRequest request) {
        return UserAgent.parseUserAgentString(request.getHeader("User-Agent")).getId();
    }
}
