package name.zjq.blog.pcd.control;

import name.zjq.blog.pcd.interceptor.LoginUserAuthInterceptor;
import name.zjq.blog.pcd.interceptor.ShareAccessAuthInterceptor;
import name.zjq.blog.pcd.services.CloudFileService;
import name.zjq.blog.pcd.utils.CoderUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import name.zjq.blog.pcd.bean.FileShare;
import name.zjq.blog.pcd.bean.User;
import name.zjq.blog.pcd.exceptionhandler.CustomLogicException;
import name.zjq.blog.pcd.services.ShareFileService;
import name.zjq.blog.pcd.utils.PR;
import name.zjq.blog.pcd.utils.StrUtil;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/share")
public class ShareController {
    /**
     * 文件分享
     *
     * @param path      文件路径（base64）
     * @param loginUser
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{path}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public PR shareFiles(@PathVariable("path") String path, @RequestAttribute(LoginUserAuthInterceptor.LOGIN_USER) User loginUser)
            throws Exception {
        if (StrUtil.isNullOrEmpty(path)) {
            throw new CustomLogicException(400, "参数为空", null);
        } else {
            FileShare f = new FileShare(loginUser, new String(CoderUtil.decoderURLBASE64(path), "utf-8"));
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
    public PR getAllShare(@RequestAttribute(LoginUserAuthInterceptor.LOGIN_USER) User loginUser) throws Exception {
        return new PR("查询成功", ShareFileService.searchAllShare(loginUser.getUsername()));
    }

    /**
     * 取消文件分享
     *
     * @param id        分享的文件的id
     * @param loginUser
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public PR cancelShare(@PathVariable String id, @RequestAttribute(LoginUserAuthInterceptor.LOGIN_USER) User loginUser)
            throws Exception {
        if (ShareFileService.cancelShare(id, loginUser.getUsername())) {
            return new PR("取消分享成功", null);
        }
        throw new CustomLogicException(500, "取消分享失败", null);
    }

    /**
     * 查询分享文件
     *
     * @param fileShare 分享文件
     * @param parentDir 父目录
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/access", produces = "application/json")
    @ResponseBody
    public PR getShareFileByID(@RequestAttribute(ShareAccessAuthInterceptor.SHARE_FILE) FileShare fileShare, @RequestParam(value = "parent", required = false) String parentDir) throws Exception {
        String mainDir = User.getUserDirectory(fileShare.getOwner());
        if (mainDir == null) {
            ShareFileService.cancelShare(fileShare.getId(), fileShare.getOwner());
            throw new CustomLogicException(404, "文件已被取消分享", null);
        }
        Path mainPath = Paths.get(mainDir, fileShare.getFilePath());
        if (!Files.exists(mainPath, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
            throw new CustomLogicException(404, "文件已被删除", null);
        }
        String parent = "";
        if (parentDir != null) {
            parent = new String(CoderUtil.decoderURLBASE64(parentDir), "utf-8");
        }
        String mainPathString = mainPath.toAbsolutePath().toString();
        return new PR("查询成功", CloudFileService.getFileList(mainPathString, mainPathString += parent, null, null));
    }
}
