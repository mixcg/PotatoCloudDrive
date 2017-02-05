package name.zjq.blog.pcd.utils;

public class PR {
	private int status;
	private String resultdesc;
	private Object result;

	public PR(int status, String resultdesc, Object result) {
		this.status = status;
		this.resultdesc = resultdesc;
		this.result = result;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getResultdesc() {
		return resultdesc;
	}

	public void setResultdesc(String resultdesc) {
		this.resultdesc = resultdesc;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

}
