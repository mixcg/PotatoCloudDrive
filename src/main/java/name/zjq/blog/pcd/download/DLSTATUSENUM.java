package name.zjq.blog.pcd.download;
/**
 * 下载状态
 * @author ZhaoJQ
 *
 */
public enum DLSTATUSENUM {
	/**
	 * 出错
	 */
	ERROR(-1),
	/**
	 * 准备开始
	 */
	READY(0),
	/**
	 * 进行中
	 */
	PROCESS(1),
	/**
	 * 下载完成
	 */
	FINISH(2),
	/**
	 * 已停止
	 */
	STOP(3);
	private int dlcode;

	private DLSTATUSENUM(int code) {
		this.dlcode = code;
	}

	@Override
	public String toString() {
		return String.valueOf(this.dlcode);
	}
}
