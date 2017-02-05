package name.zjq.blog.pcd.routes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import name.zjq.blog.pcd.bo.DriveFile;
import name.zjq.blog.pcd.bo.FileShare;
import name.zjq.blog.pcd.bo.User;
import name.zjq.blog.pcd.config.DBInit;
import name.zjq.blog.pcd.interceptor.TokenAuthInterceptor;
import name.zjq.blog.pcd.utils.Coder;
import name.zjq.blog.pcd.utils.PR;
import name.zjq.blog.pcd.utils.StrUtil;

@Controller
public class FileController {
	/**
	 * 获取文件列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/file/", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR getFileLst(@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		return getFileList(null, loginUser);
	}

	/**
	 * 获取指定目录文件列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/file/{path}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR getFileList(@PathVariable("path") String path,
			@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		if (StrUtil.isNullOrEmpty(path)) {
			path = loginUser.getDirectory();
		} else {
			try {
				path = loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8");
			} catch (Exception e) {
				return new PR(200, "加载失败", null);
			}
		}
		List<DriveFile> files = DriveFile.getFileList(loginUser.getDirectory(), path);
		if (files == null) {
			return new PR(200, "文件不存在", null);
		}
		return new PR(200, "查询成功", files);
	}

	/**
	 * 删除文件
	 * 
	 * @return
	 */
	@RequestMapping(value = "/file/del/{path}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR delFile(@PathVariable("path") String path,
			@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		if (StrUtil.isNullOrEmpty(path)) {
			return new PR(400, "文件不存在", null);
		} else {
			try {
				path = loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8");
			} catch (Exception e) {
				return new PR(500, "文件删除失败", null);
			}
		}
		try {
			if (DriveFile.delFile(path)) {
				return new PR(200, "文件删除成功", null);
			} else {
				return new PR(500, "文件删除失败", null);
			}
		} catch (FileNotFoundException e) {
			return new PR(412, e.getMessage(), null);
		}
	}

	/**
	 * 文件夹或文件重命名
	 * 
	 * @return
	 */
	@RequestMapping(value = "/file/rename/{path}/{newfilename}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR rnameFile(@PathVariable("path") String path, @PathVariable("newfilename") String newfilename,
			@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		if (StrUtil.isNullOrEmpty(path)) {
			return new PR(400, "文件不存在", null);
		} else {
			try {
				path = loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8");
			} catch (Exception e) {
				return new PR(500, "文件重命名失败", null);
			}
		}
		try {
			if (DriveFile.renameFile(path, newfilename)) {
				return new PR(200, "文件重命名成功", null);
			} else {
				return new PR(500, "文件重命名失败", null);
			}
		} catch (IOException e) {
			return new PR(412, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/file/newfile/{newfilename}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR createNewFile(@PathVariable("newfilename") String newfilename,
			@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		return createNewFile(null, newfilename, loginUser);
	}

	/**
	 * 新建文件
	 * 
	 * @return
	 */
	@RequestMapping(value = "/file/newfile/{path}/{newfilename}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR createNewFile(@PathVariable("path") String path, @PathVariable("newfilename") String newfilename,
			@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		if (StrUtil.isNullOrEmpty(path)) {
			path = loginUser.getDirectory();
		} else {
			try {
				path = loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8");
			} catch (Exception e) {
				return new PR(500, "新建文件失败", null);
			}
		}
		try {
			if (DriveFile.createNewFile(path, newfilename)) {
				return new PR(200, "新建文件成功", null);
			} else {
				return new PR(500, "新建文件失败", null);
			}
		} catch (IOException e) {
			return new PR(412, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/file/newdir/{newfilename}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR createNewDir(@PathVariable("newfilename") String newfilename,
			@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		return createNewDir(null, newfilename, loginUser);
	}

	/**
	 * 新建文件夹
	 * 
	 * @return
	 */
	@RequestMapping(value = "/file/newdir/{path}/{newfilename}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR createNewDir(@PathVariable("path") String path, @PathVariable("newfilename") String newfilename,
			@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		if (StrUtil.isNullOrEmpty(path)) {
			path = loginUser.getDirectory();
		} else {
			try {
				path = loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8");
			} catch (Exception e) {
				return new PR(500, "新建文件夹失败", null);
			}
		}
		try {
			if (DriveFile.createNewDir(path, newfilename)) {
				return new PR(200, "新建文件夹成功", null);
			} else {
				return new PR(500, "新建文件夹失败", null);
			}
		} catch (IOException e) {
			return new PR(412, e.getMessage(), null);
		}
	}

	/**
	 * 新建文件夹
	 * 
	 * @return
	 */
	@RequestMapping(value = "/file/share/{path}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR shareFiles(@PathVariable("path") String path,
			@RequestAttribute(TokenAuthInterceptor.ATTRIBUTE_NAME) User loginUser) {
		if (StrUtil.isNullOrEmpty(path)) {
			return new PR(400, "文件不存在", null);
		} else {
			byte[] bytes = new byte[4];
			byte[] uuid = StrUtil.getUUID().getBytes();
			for (byte i = 0; i < 4; i++) {
				bytes[i] = uuid[(int) (Math.random() * 32)];
			}
			String password = new String(bytes);
			FileShare f = new FileShare(loginUser.getUsername(), password, path);
			if (f.addNewShare()) {
				return new PR(200, "文件分享成功", f);
			} else {
				return new PR(500, "文件分享失败", null);
			}
		}
	}
}
