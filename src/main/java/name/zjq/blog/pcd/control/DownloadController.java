package name.zjq.blog.pcd.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import name.zjq.blog.pcd.bean.User;
import name.zjq.blog.pcd.download.DownloadTasks;
import name.zjq.blog.pcd.exceptionhandler.CustomLogicException;
import name.zjq.blog.pcd.interceptor.LoginUserAuthInterceptor;
import name.zjq.blog.pcd.utils.CoderUtil;
import name.zjq.blog.pcd.utils.PR;

import javax.servlet.http.HttpServletResponse;

@RequestMapping("/download")
@Controller
public class DownloadController {
	/**
	 * 获取下载任务列表
	 * 
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public PR getDownloadList(@RequestAttribute(LoginUserAuthInterceptor.LOGIN_USER) User loginUser) throws Exception {
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
			@RequestAttribute(LoginUserAuthInterceptor.LOGIN_USER) User loginUser) throws Exception {
		String taskID = DownloadTasks.addDownloadTask(loginUser.getUsername(), new String(CoderUtil.decoderURLBASE64(url)));
		return new PR("添加下载任务成功", taskID);
	}

	/**
	 * 停止下载
	 * 
	 * @param taskid
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{taskid}", method = RequestMethod.PATCH, produces = "application/json")
	@ResponseBody
	public PR stopDownload(@PathVariable("taskid") String taskid,
			@RequestAttribute(LoginUserAuthInterceptor.LOGIN_USER) User loginUser) throws Exception {
		if (DownloadTasks.stopDownload(loginUser.getUsername(), taskid)) {
			return new PR("停止下载成功", null);
		} else {
			throw new CustomLogicException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "停止下载失败", null);
		}
	}

	/**
	 * 下载重试
	 * 
	 * @param taskid
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{taskid}", method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public PR retryDownload(@PathVariable("taskid") String taskid,
			@RequestAttribute(LoginUserAuthInterceptor.LOGIN_USER) User loginUser) throws Exception {
		if (DownloadTasks.retryDownload(loginUser.getUsername(), taskid)) {
			return new PR("重新下载成功", null);
		} else {
			throw new CustomLogicException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "重新下载失败", null);
		}
	}

	/**
	 * 删除下载
	 * 
	 * @param taskid
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{taskid}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public PR deleteDownload(@PathVariable("taskid") String taskid,
			@RequestAttribute(LoginUserAuthInterceptor.LOGIN_USER) User loginUser) throws Exception {
		if (DownloadTasks.deleteDownload(loginUser.getUsername(), taskid)) {
			return new PR("删除下载成功", null);
		} else {
			throw new CustomLogicException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "删除下载失败", null);
		}
	}
}
