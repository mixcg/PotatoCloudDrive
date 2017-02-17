package name.zjq.blog.pcd.exceptionhandler;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Log logger = LogFactory.getLog(GlobalExceptionHandler.class);

    @ExceptionHandler(value = {CustomLogicException.class})
    public void logicExceptionInterception(CustomLogicException ex, HttpServletResponse response) {
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(ex.getHttpStatus());
        try {
            response.getWriter().write(ex.getMessage());
        } catch (IOException e) {
            logger.error(e);
        }
    }

    @ExceptionHandler(value = {Exception.class})
    public void dealRunException(Exception ex, HttpServletResponse response) {
        if (ex != null) {
            logger.error(ex);
        }
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(500);
        try {
            response.getWriter().write("啊哦，出错了！");
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
