package name.zjq.blog.pcd.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import name.zjq.blog.pcd.bo.User;
import name.zjq.blog.pcd.utils.StrUtil;

public class LoginUserAuth implements HandlerInterceptor {
	public static final String TOKEN_NAME = "pcdtoken";
	public static final String LOGIN_USER = "loginUser";

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
				if (c.getName().equals(TOKEN_NAME)) {
					token = c.getValue();
					break;
				}
			}
			if (StrUtil.isNullOrEmpty(token)) {
				arg1.sendError(401, "认证失败！请重新登录");
				return false;
			}
		}
		User u = User.checkToken(token, arg0);
		if (u == null) {
			arg1.sendError(401, "认证失败！请重新登录");
			return false;
		}
		arg0.setAttribute(LOGIN_USER, u);
		return true;
	}
}
