package name.zjq.blog.pcd.download;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

	private String url;// url地址
	private String filename;// 文件名称
	private long urlFileSize;// 下载文件的大小
	private long localFileSize;// 本地文件大小
	private DLSTATUSENUM downloadStatus;// 下载状态
	private String userdir = "";// 用户目录
	private boolean continueTrans;// 是否支持断点续传
	private String message;// 消息

	private Path localfiletmp;
	private int responseCode;// 响应状态
	private CloseableHttpResponse response = null;

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
	private void checkLocalFile() throws IOException {
		Path localfile = Paths.get(userdir, filename);// 本地文件
		if (Files.exists(localfile, new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
			this.downloadStatus = DLSTATUSENUM.FINISH;
			this.message = "downloads目录下存在相同命名文件，下载已停止";
		} else {
			localfiletmp = Paths.get(localfile.toAbsolutePath().toString() + ".tmp");// 本地已下载缓存文件
			if (Files.exists(localfiletmp, new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
				localFileSize = localfiletmp.toFile().length();
			} else {
				Files.createFile(localfiletmp);
			}
			Path cfgpath = Paths.get(localfile.toAbsolutePath().toString() + ".pcd.dl.cfg");// 本地已下载缓存文件
			if (!Files.exists(cfgpath, new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
				Files.createFile(cfgpath);
			}
			FileWriter fw = new FileWriter(cfgpath.toFile());
			fw.write(url);
			fw.flush();
			fw.close();
		}
	}

	public void run() {
		this.downloadStatus = DLSTATUSENUM.READY;
		if (checkDownloadFile()) {
			try {
				checkLocalFile();
			} catch (IOException e) {
				logger.error("download [" + url + "] ERROR", e);
				this.downloadStatus = DLSTATUSENUM.ERROR;
				return;
			}
			startDownloadFile();
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
			int readLength = -1;
			while ((readLength = is.read(readBuffer)) != -1) {
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

	public static void main(String args[]) {
		new HttpDownload("D:/", "http://dl.zjq.name/Shadowsocks-3.4.3.zip").run();
	}
}
