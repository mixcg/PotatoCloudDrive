package name.zjq.blog.pcd.routes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import name.zjq.blog.pcd.bean.FileShare;
import name.zjq.blog.pcd.bean.User;
import name.zjq.blog.pcd.exceptionhandler.CustomLogicException;
import name.zjq.blog.pcd.interceptor.LoginUserAuth;
import name.zjq.blog.pcd.services.ShareFileService;
import name.zjq.blog.pcd.utils.PR;
import name.zjq.blog.pcd.utils.StrUtil;

@Controller
@RequestMapping("/share")
public class ShareController {
	/**
	 * 文件分享
	 * 
	 * @param path
	 *            文件路径（base64）
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{path}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR shareFiles(@PathVariable("path") String path, @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser)
			throws Exception {
		if (StrUtil.isNullOrEmpty(path)) {
			throw new CustomLogicException(400, "参数为空", null);
		} else {
			FileShare f = new FileShare(loginUser, path);
			f = ShareFileService.addNewShareFile(f);
			if (f != null) {
				return new PR("文件分享成功", f);
			} else {
				throw new CustomLogicException(500, "文件分享失败", null);
			}
		}
	}

	/**
	 * 查询所有分享
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public PR getAllShare(@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
		return new PR("查询成功", ShareFileService.searchAllShare(loginUser.getUsername()));
	}

	/**
	 * 取消文件分享
	 * 
	 * @param id
	 *            分享的文件的id
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public PR cancelShare(@PathVariable String id, @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser)
			throws Exception {
		if (ShareFileService.cancelShare(id, loginUser.getUsername())) {
			return new PR("取消分享成功", null);
		}
		throw new CustomLogicException(500, "取消分享失败", null);
	}
}
