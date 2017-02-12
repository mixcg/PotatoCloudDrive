package name.zjq.blog.pcd.download;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import name.zjq.blog.pcd.exceptionhandler.CustomLogicException;
import name.zjq.blog.pcd.utils.StrUtil;

public class DownloadTasks {
	private final Log logger = LogFactory.getLog(DownloadTasks.class);
	private static boolean initFlag = false;
	// 用户下载目录
	private static final Map<String, Path> USER_DOWNLOAD_DIR = new HashMap<String, Path>();
	// 下载任务
	private static final Map<String, DLInterface> DOWNLOAD_TASKS = new HashMap<String, DLInterface>();

	/**
	 * 添加用户下载目录
	 * 
	 * @param username
	 *            用户名
	 * @param userdir
	 *            用户主目录
	 * @throws IOException
	 */
	public static void putUserDownloadDir(String username, String userdir) throws IOException {
		Path userDir = Paths.get(userdir, username + "_Downloads");
		if (!Files.exists(userDir, new LinkOption[] { LinkOption.NOFOLLOW_LINKS })) {
			Files.createDirectory(userDir);
		}
		USER_DOWNLOAD_DIR.put(username, userDir);
	}

	public static void initDownloadTasks() {
		if (!initFlag) {
			new DownloadTasks().new InitDownloadTask().start();
		}
		initFlag = true;
	}

	/**
	 * 新建文件下载任务
	 * 
	 * @param username
	 * @param url
	 * @throws CustomLogicException
	 */
	public static String addDownloadTask(String username, String url) throws CustomLogicException {
		String taskID = StrUtil.getUUID();
		String userdir = USER_DOWNLOAD_DIR.get(username).toAbsolutePath().toString();
		if (url.startsWith("http") || url.startsWith("https")) {
			HttpDownload httpDownload = new HttpDownload(userdir, url);
			new Thread(httpDownload).start();
			DOWNLOAD_TASKS.put(taskID, httpDownload);
		} else if (url.startsWith("ftp")) {

		} else {
			throw new CustomLogicException(400, "不支持的下载链接！", null);
		}
		return taskID;
	}

	class InitDownloadTask extends Thread {
		public void run() {
			Iterator<String> userlist = USER_DOWNLOAD_DIR.keySet().iterator();
			while (userlist.hasNext()) {
				String username = userlist.next();
				Path path = USER_DOWNLOAD_DIR.get(username);
				try {
					Files.walkFileTree(path, new HashSet<FileVisitOption>(), 1, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if (!attrs.isDirectory() && file.getFileName().toString().endsWith(".pcd.dl.cfg")) {
								List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
								if (lines != null && !lines.isEmpty()) {
									String url = lines.get(0).trim();
									if (!"".equals(url)) {
										try {
											DownloadTasks.addDownloadTask(username, url);
										} catch (CustomLogicException e) {
											logger.error(e);
										}
									}
								}
							}
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}
	
	public static void main(String args[]) throws IOException{
		DownloadTasks.putUserDownloadDir("admin","D:/");
		DownloadTasks.initDownloadTasks();
	}
}
