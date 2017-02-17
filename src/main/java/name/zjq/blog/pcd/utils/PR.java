package name.zjq.blog.pcd.utils;

public class PR {
	private String resultdesc;
	private Object result;

	public PR(String resultdesc, Object result) {
		this.resultdesc = resultdesc;
		this.result = result;
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
