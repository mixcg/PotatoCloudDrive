package name.zjq.blog.pcd.routes;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import name.zjq.blog.pcd.bo.DriveFile;
import name.zjq.blog.pcd.bo.User;
import name.zjq.blog.pcd.interceptor.TokenAuthInterceptor;
import name.zjq.blog.pcd.utils.RInfo;
import name.zjq.blog.pcd.utils.StrUtil;

@Controller
@RequestMapping("/file")
public class FileController {
	/**
	 * 获取文件列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/{path}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public RInfo getFileList(String path, @RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		if (StrUtil.isNullOrEmpty(path)) {
			path = loginUser.getDirectory();
		}
		List<DriveFile> files = DriveFile.getFileList(path);
		if (files == null) {
			return new RInfo(200, "文件不存在", null);
		}
		return new RInfo(200, "查询成功", files);
	}
}
