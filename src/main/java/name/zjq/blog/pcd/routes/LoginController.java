package name.zjq.blog.pcd.routes;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import name.zjq.blog.pcd.auth.RequestAuth;
import name.zjq.blog.pcd.config.RSAEncrypt;
import name.zjq.blog.pcd.config.UserConfig;
import name.zjq.blog.pcd.utils.IPUtil;
import name.zjq.blog.pcd.utils.ReturnInfo;
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
	public ReturnInfo auth(HttpServletRequest request) {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (StrUtil.isNullOrEmpty(username) || StrUtil.isNullOrEmpty(password)) {
			return new ReturnInfo(-1, "参数非法【用户名或密码为空】", null);
		}
		if (UserConfig.loginAuth(RSAEncrypt.getInstance().decryptByPriKey(username),
				RSAEncrypt.getInstance().decryptByPriKey(password))) {
			return new ReturnInfo(1, "登录成功", RequestAuth.getToken(IPUtil.getIpAddress(request)));
		}
		return new ReturnInfo(-1, "用户名或密码错误", null);
	}
}
