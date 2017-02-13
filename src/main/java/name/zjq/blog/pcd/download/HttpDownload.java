package name.zjq.blog.pcd.download;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpDownload implements Runnable, DLInterface {
	private final Log logger = LogFactory.getLog(HttpDownload.class);

	private String userdir = "";// 用户目录
	private String url;// url地址
	private String filename;// 文件名称
	private long urlFileSize;// 下载文件的大小
	private long localFileSize;// 本地文件大小
	private boolean continueTrans;// 是否支持断点续传
	private String message;// 消息
	private DLSTATUSENUM downloadStatus;// 下载状态

	private boolean stopFlag = false;
	private Path localfiletmp;// 本地已下载缓存文件
	private Path cfgpath;// 下载配置文件
	private int responseCode;// 响应状态
	private CloseableHttpResponse response = null;

	private int downloadSpeed;// 下载速度

	public HttpDownload(String userdir, String url) {
		this.userdir = userdir;
		this.url = url;
	}

	private boolean checkDownloadFile() {
		try {
			response = download(0);
			responseCode = response.getStatusLine().getStatusCode();
			switch (responseCode) {
			case 206:
				continueTrans = true;
				break;
			case 200:
				continueTrans = false;
				break;
			default:
				this.downloadStatus = DLSTATUSENUM.ERROR;
				break;
			}
			urlFileSize = Integer.valueOf(response.getFirstHeader("Content-Length").getValue());
			getFileName();
			closeHttpResponse();
			return true;
		} catch (IOException e) {
			logger.error("download [" + url + "] ERROR", e);
			this.downloadStatus = DLSTATUSENUM.ERROR;
			return false;
		}
	}

	/**
	 * 检查本地已下载文件
	 * 
	 * @throws IOException
	 */
	private boolean checkLocalFile() throws IOException {
		Path localfile = Paths.get(userdir, filename);// 本地文件
		if (Files.exists(localfile, new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
			this.downloadStatus = DLSTATUSENUM.FINISH;
			this.message = "用户downloads目录下存在相同命名文件，下载已停止";
			return false;
		} else {
			localfiletmp = Paths.get(localfile.toAbsolutePath().toString() + ".tmp");
			if (Files.exists(localfiletmp, new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
				localFileSize = localfiletmp.toFile().length();
			} else {
				Files.createFile(localfiletmp);
			}
			cfgpath = Paths.get(localfile.toAbsolutePath().toString() + ".pcd.dl.cfg");// 本地已下载缓存文件
			if (!Files.exists(cfgpath, new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
				Files.createFile(cfgpath);
			}
			FileWriter fw = new FileWriter(cfgpath.toFile());
			fw.write(url);
			fw.flush();
			fw.close();
			return true;
		}
	}

	public void run() {
		this.downloadStatus = DLSTATUSENUM.READY;
		if (checkDownloadFile()) {
			try {
				if (checkLocalFile()) {
					startDownloadFile();
				}
			} catch (IOException e) {
				logger.error("download [" + url + "] ERROR", e);
				this.downloadStatus = DLSTATUSENUM.ERROR;
				return;
			}
		}
	}

	private void startDownloadFile() {
		if (urlFileSize - localFileSize <= 0) {
			moveFile();
			downloadStatus = DLSTATUSENUM.FINISH;
			logger.info("下载文件" + filename + "完成");
			return;
		}
		try {
			response = download(localFileSize);
			downloadStatus = DLSTATUSENUM.PROCESS;
			InputStream is = response.getEntity().getContent();
			FileOutputStream fi = new FileOutputStream(localfiletmp.toFile());
			FileChannel channel = fi.getChannel();
			byte[] readBuffer = new byte[1048576];
			ByteBuffer writebuffer = ByteBuffer.allocate(1048576);
			long startTime = System.currentTimeMillis();
			int writeSize = 0;
			int readLength = -1;
			while ((readLength = is.read(readBuffer)) != -1 && !stopFlag) {
				writeSize += readLength;
				long endTime = System.currentTimeMillis();
				if (endTime - startTime > 1024) {
					downloadSpeed = writeSize;
					startTime = endTime;
					writeSize = 0;
				}
				if (continueTrans) {
					channel.position(localFileSize);
				}
				writebuffer.clear();
				writebuffer.put(readBuffer, 0, readLength);
				writebuffer.flip();
				while (writebuffer.hasRemaining()) {
					channel.write(writebuffer);
				}
				localFileSize += readLength;
			}
			writebuffer.clear();
			channel.close();
			fi.close();
			is.close();
			closeHttpResponse();
			startDownloadFile();
		} catch (IOException e) {
			logger.error("download [" + url + "] ERROR", e);
			this.downloadStatus = DLSTATUSENUM.ERROR;
			return;
		}
	}

	/**
	 * 重命名文件
	 */
	private void moveFile() {
		try {
			Files.move(localfiletmp, Paths.get(userdir, filename), StandardCopyOption.REPLACE_EXISTING);
			Files.deleteIfExists(cfgpath);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private CloseableHttpResponse download(long startMark) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Range", String.format("bytes=%s-", startMark));
		return httpclient.execute(httpGet);
	}

	/**
	 * 从响应信息中获取文件名
	 */
	private void getFileName() {
		Header contentHeader = response.getFirstHeader("Content-Disposition");
		this.filename = null;
		if (contentHeader != null) {
			HeaderElement[] values = contentHeader.getElements();
			if (values.length == 1) {
				NameValuePair param = values[0].getParameterByName("filename");
				if (param != null) {
					try {
						filename = param.getValue();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (this.filename == null) {
			this.filename = url.substring(url.lastIndexOf("/") + 1);
		}
	}

	/**
	 * 关闭CloseableHttpResponse
	 */
	private void closeHttpResponse() {
		if (response != null) {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	/**
	 * 获取下载完成百分比
	 * 
	 * @return
	 */
	private String getPercentComplete() {
		if(urlFileSize == 0 || localFileSize == 0){
			return "0";
		}
		return new BigDecimal(localFileSize).divide(new BigDecimal(urlFileSize), 2, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(100))
				.toString();
	}

	@Override
	public void stop() {
		stopFlag = true;
	}

	@Override
	public Map<String, String> getStatus() {
		Map<String, String> dlstatus = new HashMap<String, String>();
		dlstatus.put("url", url);
		dlstatus.put("filename", filename);
		dlstatus.put("filesize", calculateDescSize(urlFileSize));
		dlstatus.put("downloadSpeed", calculateDescSize(downloadSpeed)+"/S");
		dlstatus.put("percentcomplete", getPercentComplete());
		dlstatus.put("continuetrans", String.valueOf(continueTrans));
		dlstatus.put("status", downloadStatus.toString());
		dlstatus.put("message", message);
		return dlstatus;
	}

	@Override
	public void delete() {
		stopFlag = true;
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		try {
			Files.deleteIfExists(localfiletmp);
			Files.deleteIfExists(cfgpath);
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
