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

	@ExceptionHandler(value = { CustomizeLogicException.class })
	public void logicExceptionInterception(CustomizeLogicException ex, HttpServletResponse response) {
		response.setContentType("text/plain;charset=UTF-8");
		response.setStatus(ex.getHttpStatus());
		try {
			response.getWriter().write(ex.getMessage());
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@ExceptionHandler(value = { RuntimeException.class, Exception.class })
	public void dealRunException(Throwable paramThrowable, HttpServletResponse response) {
		logger.error(paramThrowable);
		response.setContentType("text/plain;charset=UTF-8");
		response.setStatus(500);
		try {
			response.getWriter().write("啊哦，出错了！");
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
