package name.zjq.blog.pcd.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import name.zjq.blog.pcd.bo.User;
import name.zjq.blog.pcd.config.UserConfig;
import name.zjq.blog.pcd.utils.StrUtil;

public class TokenAuthInterceptor implements HandlerInterceptor {
	public static final String TOKEN_NAME = "token";
	public static final String ATTRIBUTE_NAME = "AuthUser";

	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {

	}

	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {

	}

	/**
	 * 权限认证
	 */
	public boolean preHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2) throws Exception {
		String token = arg0.getParameter(TOKEN_NAME);
		if (StrUtil.isNullOrEmpty(token)) {
			Cookie[] cookies = arg0.getCookies();
			for (Cookie c : cookies) {
				if (c.getName().equals("pcdtoken")) {
					token = c.getValue();
					break;
				}
			}
			if (StrUtil.isNullOrEmpty(token)) {
				arg1.setStatus(401);
				return false;
			}
		}
		User u = UserConfig.checkToken(token, arg0);
		if (u == null) {
			arg1.setStatus(401);
			return false;
		}
		arg0.setAttribute(ATTRIBUTE_NAME, u);
		return true;
	}
}
