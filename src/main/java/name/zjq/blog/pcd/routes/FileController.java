package name.zjq.blog.pcd.routes;

import name.zjq.blog.pcd.bean.User;
import name.zjq.blog.pcd.exceptionhandler.CustomLogicException;
import name.zjq.blog.pcd.interceptor.LoginUserAuth;
import name.zjq.blog.pcd.services.CloudFileService;
import name.zjq.blog.pcd.utils.CoderUtil;
import name.zjq.blog.pcd.utils.PR;
import name.zjq.blog.pcd.utils.StrUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Iterator;

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
    public PR getFileLst(@RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser,
                         @RequestParam(value = "accepttype", required = false) String filtertype,
                         @RequestParam(value = "filterfile", required = false) String filterfile) throws Exception {
        return getFileList(null, loginUser, filtertype, filterfile);
    }

    /**
     * 获取指定目录全部文件
     *
     * @param path       文件或文件夹路径(base64编码)
     * @param accepttype 按照参数返回指定类型的文件，可传值类型 folder ,file
     * @param filterfile 过滤文件，文件或文件夹路径(base64编码)，不返回此文件及文件下子文件
     * @param loginUser
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{base64filepath}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public PR getFileList(@PathVariable("base64filepath") String path,
                          @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser,
                          @RequestParam(value = "accepttype", required = false) String accepttype,
                          @RequestParam(value = "filterfile", required = false) String filterfile) throws Exception {
        if (StrUtil.isNullOrEmpty(path)) {
            path = loginUser.getDirectory();
        } else {
            path = loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(path), "utf-8");
        }
        if (filterfile != null) {
            filterfile = loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(filterfile), "utf-8");
            filterfile = Paths.get(filterfile).toAbsolutePath().toString();
        }
        return new PR("查询成功", CloudFileService.getFileList(loginUser.getDirectory(), path, accepttype, filterfile));
    }

    /**
     * 删除文件
     *
     * @param path      待删除文件或文件夹路径(base64编码)
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
            path = loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(path), "utf-8");
        }
        try {
            if (CloudFileService.delFile(path)) {
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
     * @param path        文件或文件夹路径(base64编码)
     * @param newfilename 新文件或文件夹名称(base64编码)
     * @param loginUser
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{base64filepath}/{base64newfilename}", method = RequestMethod.PATCH, produces = "application/json")
    @ResponseBody
    public PR rnameFile(@PathVariable("base64filepath") String path,
                        @PathVariable("base64newfilename") String newfilename,
                        @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
        if (StrUtil.isNullOrEmpty(path)) {
            throw new CustomLogicException(400, "参数为空", null);
        } else {
            path = loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(path), "utf-8");
        }
        try {
            if (CloudFileService.renameFile(path, newfilename)) {
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
     * @param filename  文件或文件夹名称(base64编码)
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
     * @param path      文件或文件夹路径(base64编码)
     * @param filename  文件或文件夹名称(base64编码)
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
            path = loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(path), "utf-8");
        }

        boolean finishFlag = false;
        try {
            if (filetype.equals("directory")) {
                finishFlag = CloudFileService.createNewDir(path, filename);
            } else if (filetype.equals("file")) {
                finishFlag = CloudFileService.createNewFile(path, filename);
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
     * 移动文件到主目录
     *
     * @param filepath   文件或文件夹路径(base64编码)
     * @param loginUser
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{base64filepath}/", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public PR moveFile(@PathVariable("base64filepath") String filepath,
                       @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
        return moveFile(filepath, null, loginUser);
    }

    /**
     * 移动文件
     *
     * @param filepath   文件或文件夹路径(base64编码)
     * @param newdirpath 新文件或文件夹名称(base64编码)
     * @param loginUser
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{base64filepath}/{newdirpath}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public PR moveFile(@PathVariable("base64filepath") String filepath, @PathVariable("newdirpath") String newdirpath,
                       @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
        filepath = loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(filepath), "utf-8");
        if (StrUtil.isNullOrEmpty(newdirpath)) {
            newdirpath = loginUser.getDirectory();
        } else {
            newdirpath = loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(newdirpath), "utf-8");
        }
        Path sourceFile = Paths.get(filepath);
        Path targetFile = Paths.get(newdirpath);
        CloudFileService.moveFile(sourceFile, targetFile);
        return new PR("移动文件成功", null);
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
        Path uploadDir = Paths.get(loginUser.getDirectory(), "uploads");
        boolean uploadDirExists = Files.exists(uploadDir, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
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
                    Path filePath = Paths.get(uploadDir.toAbsolutePath().toString(),
                            new String(file.getOriginalFilename()));
                    boolean fileExists = Files.exists(filePath);
                    if (!fileExists) {
                        Path tmpFile = Paths.get(filePath.toAbsolutePath().toString() + ".upload.tmp");
                        Files.deleteIfExists(tmpFile);
                        Files.createFile(tmpFile);
                        byte[] readBuffer = new byte[10485760];
                        InputStream is = file.getInputStream();
                        int hasReadSize;
                        while ((hasReadSize = is.read(readBuffer)) != -1) {
                            Files.write(tmpFile, Arrays.copyOf(readBuffer, hasReadSize), StandardOpenOption.APPEND);
                        }
                        Files.move(tmpFile, filePath, StandardCopyOption.REPLACE_EXISTING);
                    } else {
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
     *
     * @param path
     * @param loginUser
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{base64filepath}/download")
    public void fileDownload(@PathVariable("base64filepath") String path,
                             @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser, HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        Path file = Paths.get(loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(path), "utf-8"));
        CloudFileService.fileDownload(file, request, response);
    }

    /**
     * 视频播放
     *
     * @param path
     * @param loginUser
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{base64filepath}/play", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<byte[]> filePlay(@PathVariable("base64filepath") String path,
                                           @RequestAttribute(LoginUserAuth.LOGIN_USER) User loginUser) throws Exception {
        Path file = Paths.get(loginUser.getDirectory() + "/" + new String(CoderUtil.decoderURLBASE64(path), "utf-8"));
        if (Files.exists(file, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})
                || Files.isDirectory(file, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
            String filetype = file.getFileName().toString();
            int indexOf = filetype.lastIndexOf(".");
            if (indexOf > -1) {
                filetype = filetype.substring(indexOf + 1).toLowerCase();
            }
            if (!CloudFileService.isCanPlayOnline(filetype)) {
                throw new CustomLogicException(500, "文件不支持在线播放", null);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", Files.probeContentType(file));
            headers.setContentDispositionFormData("attachment", file.getFileName().toString());
            return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file.toFile()), headers,
                    HttpStatus.CREATED);
        } else {
            throw new CustomLogicException(500, "文件不存在", null);
        }
    }
}
