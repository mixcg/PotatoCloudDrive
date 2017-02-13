package name.zjq.blog.pcd.routes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import name.zjq.blog.pcd.bo.User;
import name.zjq.blog.pcd.download.DownloadTasks;
import name.zjq.blog.pcd.interceptor.LoginUserAuth;
import name.zjq.blog.pcd.utils.Coder;
import name.zjq.blog.pcd.utils.PR;

@RequestMapping("/download")
@Controller
public class DownloadController {
	/**
	 * 获取下载任务列表
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public PR getDownloadList(@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
		return new PR("查询成功", DownloadTasks.getDownloadStatus(loginUser.getUsername()));
	}

	/**
	 * 新建下载任务
	 * 
	 * @param url
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{url}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR addDownloadTask(@PathVariable("url") String url,
			@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
		String taskID = DownloadTasks.addDownloadTask(loginUser.getUsername(), new String(Coder.decoderURLBASE64(url)));
		return new PR("添加下载任务成功", taskID);
	}
}
