package name.zjq.blog.pcd.routes;

import java.io.FileNotFoundException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import name.zjq.blog.pcd.bo.DriveFile;
import name.zjq.blog.pcd.bo.User;
import name.zjq.blog.pcd.exceptionhandler.CustomLogicException;
import name.zjq.blog.pcd.interceptor.LoginUserAuth;
import name.zjq.blog.pcd.utils.Coder;
import name.zjq.blog.pcd.utils.PR;
import name.zjq.blog.pcd.utils.StrUtil;

@Controller
@RequestMapping("/files")
public class FileController {
	/**
	 * 获取全部文件列表
	 *
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public PR getFileLst(@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
		return getFileList(null, loginUser);
	}

	/**
	 * 获取指定目录文件列表或文件
	 * 
	 * @param path
	 *            文件或文件夹路径(base64编码)
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{base64filepath}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public PR getFileList(@PathVariable("base64filepath") String path,
			@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
		if (StrUtil.isNullOrEmpty(path)) {
			path = loginUser.getDirectory();
		} else {
			path = loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8");
		}
		List<DriveFile> files = DriveFile.getFileList(loginUser.getDirectory(), path);
		if (files == null) {
			throw new CustomLogicException(404, "文件不存在", null);
		}
		return new PR("查询成功", files);
	}

	/**
	 * 删除文件
	 * 
	 * @param path
	 *            待删除文件或文件夹路径(base64编码)
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{base64filepath}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public PR delFile(@PathVariable("base64filepath") String path,
			@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
		if (StrUtil.isNullOrEmpty(path)) {
			throw new CustomLogicException(400, "参数为空", null);
		} else {
			path = loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8");
		}
		try {
			if (DriveFile.delFile(path)) {
				return new PR("文件删除成功", null);
			} else {
				throw new CustomLogicException(500, "文件删除失败", null);
			}
		} catch (FileNotFoundException e) {
			throw new CustomLogicException(404, "文件不存在", e);
		}
	}

	/**
	 * 文件夹或文件重命名
	 * 
	 * @param path
	 *            文件或文件夹路径(base64编码)
	 * @param newfilename
	 *            新文件或文件夹名称(base64编码)
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{base64filepath}/{base64newfilename}", method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public PR rnameFile(@PathVariable("base64filepath") String path,
			@PathVariable("base64newfilename") String newfilename,
			@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
		if (StrUtil.isNullOrEmpty(path)) {
			throw new CustomLogicException(400, "参数为空", null);
		} else {
			path = loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8");
		}
		if (DriveFile.renameFile(path, newfilename)) {
			return new PR("文件重命名成功", null);
		} else {
			throw new CustomLogicException(500, "文件重命名失败", null);
		}
	}

	/**
	 * 在主目录新建文件或文件夹
	 * 
	 * @param filename
	 *            文件或文件夹名称(base64编码)
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{filetype}/{base64filename}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR createNewFile(@PathVariable("base64filename") String filename, @PathVariable("filetype") String filetype,
			@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
		return createNewFile(null, filetype, filename, loginUser);
	}

	/**
	 * 在指定目录新建文件或文件夹
	 * 
	 * @param path
	 *            文件或文件夹路径(base64编码)
	 * @param filename
	 *            文件或文件夹名称(base64编码)
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{base64filepath}/{filetype}/{base64filename}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR createNewFile(@PathVariable("base64filepath") String path, @PathVariable("filetype") String filetype,
			@PathVariable("base64filename") String filename, @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser)
			throws Exception {
		if (StrUtil.isNullOrEmpty(path)) {
			path = loginUser.getDirectory();
		} else {
			path = loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8");
		}

		boolean finishFlag = false;
		if (filetype.equals("directory")) {
			finishFlag = DriveFile.createNewDir(path, filename);
		} else if (filetype.equals("file")) {
			finishFlag = DriveFile.createNewFile(path, filename);
		} else {
			throw new CustomLogicException(400, "不支持的操作", null);
		}
		if (finishFlag) {
			return new PR("新建文件成功", null);
		} else {
			throw new CustomLogicException(500, "新建文件（文件夹）失败", null);
		}
	}
}
