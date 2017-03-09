package name.zjq.blog.pcd.interceptor;


import name.zjq.blog.pcd.bean.FileShare;
import name.zjq.blog.pcd.bean.User;
import name.zjq.blog.pcd.services.ShareFileService;
import name.zjq.blog.pcd.utils.StrUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ShareAccessAuthInterceptor implements HandlerInterceptor {
    private static final String LINK_PARAMETER = "shareid";
    private static final String PWD_PARAMETER = "password";
    public static final String SHARE_FILE = "fileshare";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String shareID = request.getParameter(LINK_PARAMETER);
        if (StrUtil.isNullOrEmpty(shareID)) {
            constructResponse(response, HttpServletResponse.SC_BAD_REQUEST, "无效参数：shareid为空");
            return false;
        }
        String password = request.getParameter(PWD_PARAMETER);
        if (StrUtil.isNullOrEmpty(password)) {
            constructResponse(response, HttpServletResponse.SC_BAD_REQUEST, "无效参数：password为空");
            return false;
        }
        FileShare fileShare = ShareFileService.searchShareByID(shareID);
        if (fileShare == null) {
            constructResponse(response, HttpServletResponse.SC_NOT_FOUND, "文件已取消分享");
            return false;
        } else if (!fileShare.getPassword().equals(password)) {
            constructResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "密码错误！");
            response.getWriter().write("密码错误！");
            return false;
        }
        request.setAttribute(SHARE_FILE, fileShare);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    private static void constructResponse(HttpServletResponse response, int responseCode, String msg) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(msg);
    }
}
