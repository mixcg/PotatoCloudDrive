package name.zjq.blog.pcd.routes;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

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
		try {
			if (DriveFile.renameFile(path, newfilename)) {
				return new PR("文件重命名成功", null);
			} else {
				throw new CustomLogicException(500, "文件重命名失败", null);
			}
		} catch (FileNotFoundException e1) {
			throw new CustomLogicException(500, e1.getMessage(), null);
		} catch (FileAlreadyExistsException e2) {
			throw new CustomLogicException(500, e2.getMessage(), null);
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
		try {
			if (filetype.equals("directory")) {
				finishFlag = DriveFile.createNewDir(path, filename);
			} else if (filetype.equals("file")) {
				finishFlag = DriveFile.createNewFile(path, filename);
			} else {
				throw new CustomLogicException(400, "不支持的操作", null);
			}
		} catch (FileAlreadyExistsException e1) {
			throw new CustomLogicException(500, e1.getMessage(), null);
		}

		if (finishFlag) {
			return new PR("新建文件成功", null);
		} else {
			throw new CustomLogicException(500, "新建文件（文件夹）失败", null);
		}
	}

	/**
	 * 文件上传
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public PR fileUpload(HttpServletRequest request, @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		Path uploadDir = Paths.get(loginUser.getDirectory(), "uploads");
		boolean uploadDirExists = Files.exists(uploadDir, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
		if (!uploadDirExists) {
			Files.createDirectory(uploadDir);
		}
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		if (multipartResolver.isMultipart(request)) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Iterator<String> iter = multiRequest.getFileNames();

			while (iter.hasNext()) {
				MultipartFile file = multiRequest.getFile(iter.next().toString());
				if (file != null) {
					Path filepath = Paths.get(uploadDir.toAbsolutePath().toString(),
							new String(file.getOriginalFilename().getBytes("ISO-8859-1"), "UTF-8"));
					boolean fileExists = Files.exists(filepath);
					if (!fileExists) {
						file.transferTo(filepath.toFile());
					}else{
						throw new CustomLogicException(400, "文件已存在", null);
					}
				}
			}
			return new PR("上传成功", null);
		}
		throw new CustomLogicException(400, "请求错误", null);
	}
	/**
	 * 文件下载
	 * @param path
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{base64filepath}/download", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<byte[]> fileDownload(@PathVariable("base64filepath") String path,
			@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
		Path file = Paths.get(loginUser.getDirectory() + "/" + new String(Coder.decoderURLBASE64(path), "utf-8"));
		if (Files.exists(file, new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", file.getFileName().toString());
			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file.toFile()), headers,
					HttpStatus.CREATED);
		} else {
			throw new CustomLogicException(500, "文件不存在", null);
		}
	}
}
