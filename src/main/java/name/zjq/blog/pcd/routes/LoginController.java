package name.zjq.blog.pcd.routes;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import name.zjq.blog.pcd.bo.User;
import name.zjq.blog.pcd.config.RSAEncrypt;
import name.zjq.blog.pcd.config.UserConfig;
import name.zjq.blog.pcd.interceptor.TokenAuthInterceptor;
import name.zjq.blog.pcd.utils.PR;
import name.zjq.blog.pcd.utils.StrUtil;

@Controller
@RequestMapping("/login")
public class LoginController {

	/**
	 * 获取公钥
	 * 
	 * @return
	 */
	@RequestMapping("/getpubkey")
	@ResponseBody
	public String getpubkey() {
		return RSAEncrypt.getInstance().getPubKey();
	}

	/**
	 * 登录验证
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/auth", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR auth(HttpServletRequest request) {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (StrUtil.isNullOrEmpty(username) || StrUtil.isNullOrEmpty(password)) {
			return new PR(400, "参数非法【用户名或密码为空】", null);
		}
		username = RSAEncrypt.getInstance().decryptByPriKey(username);
		password = RSAEncrypt.getInstance().decryptByPriKey(password);
		if (UserConfig.loginAuth(username, password)) {
			String token = UserConfig.createToken(username, request);
			UserConfig.setToken(username, token);
			return new PR(200, "登录成功", token);
		}
		return new PR(403, "用户名或密码错误", null);
	}

	/**
	 * 退出登录
	 * 
	 * @return
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR logout(@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		loginUser.setToken(null);
		loginUser.setExpirationtime(-1);
		return new PR(200, "ok", null);
	}
}
