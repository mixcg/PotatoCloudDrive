package name.zjq.blog.pcd.exceptionhandler;

public class CustomLogicException extends Exception {
	private static final long serialVersionUID = 6465464L;
	private int httpStatus;

	public CustomLogicException(int httpStatus, String paramString, Throwable paramThrowable) {
		super(paramString, paramThrowable);
		this.httpStatus = httpStatus;
	}

	public int getHttpStatus() {
		return httpStatus;
	}
}
