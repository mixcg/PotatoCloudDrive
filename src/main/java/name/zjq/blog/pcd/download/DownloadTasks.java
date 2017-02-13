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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import name.zjq.blog.pcd.exceptionhandler.CustomLogicException;
import name.zjq.blog.pcd.utils.StrUtil;

public class DownloadTasks {
	private final static Lock lock = new ReentrantLock();
	private final static Log logger = LogFactory.getLog(DownloadTasks.class);
	private static boolean initFlag = false;
	// 用户下载目录
	private static final Map<String, Path> USER_DOWNLOAD_DIR = new HashMap<String, Path>();
	// 下载任务
	private static final Map<String, Map<String, DLInterface>> DOWNLOAD_TASKS = new HashMap<String, Map<String, DLInterface>>();

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
		DOWNLOAD_TASKS.put(username, new LinkedHashMap<String, DLInterface>());
	}

	public static void initDownloadTasks() {
		if (!initFlag) {
			new DownloadTasks().new InitDownloadTask().start();
		}
		initFlag = true;
	}

	private static synchronized Map<String, DLInterface> getTaskMapByUsername(String username) {
		if (DOWNLOAD_TASKS.containsKey(username)) {
			return DOWNLOAD_TASKS.get(username);
		} else {
			Map<String, DLInterface> userTask = new LinkedHashMap<String, DLInterface>();
			DOWNLOAD_TASKS.put(username, userTask);
			return userTask;
		}
	}

	/**
	 * 新建文件下载任务
	 * 
	 * @param username
	 * @param url
	 * @throws CustomLogicException
	 */
	public static String addDownloadTask(String username, String url) throws CustomLogicException {
		try {
			lock.lock();
			String taskID = StrUtil.getUUID();
			String userdir = USER_DOWNLOAD_DIR.get(username).toAbsolutePath().toString();
			if (url.startsWith("http") || url.startsWith("https")) {
				HttpDownload httpDownload = new HttpDownload(userdir, url);
				new Thread(httpDownload).start();
				getTaskMapByUsername(username).put(taskID, httpDownload);
			} else if (url.startsWith("ftp")) {

			} else {
				throw new CustomLogicException(400, "不支持的下载链接！", null);
			}
			return taskID;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 获取下载状态
	 * 
	 * @param taskID
	 * @return
	 */
	public static List<Map<String, String>> getDownloadStatus(String username) {
		List<Map<String, String>> tasklist = new ArrayList<Map<String, String>>();
		Map<String, DLInterface> userTask = getTaskMapByUsername(username);
		Iterator<String> taskIDs = userTask.keySet().iterator();
		while (taskIDs.hasNext()) {
			String taskID = taskIDs.next();
			tasklist.add(userTask.get(taskID).getStatus());
		}
		return tasklist;
	}

	/**
	 * 暂停下载
	 * 
	 * @param taskID
	 */
	public static boolean stopDownload(String username, String taskID) {
		try {
			lock.lock();
			Map<String, DLInterface> userTask = getTaskMapByUsername(username);
			if (userTask.containsKey(taskID)) {
				userTask.get(taskID).stop();
				return true;
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 删除下载
	 * 
	 * @param taskID
	 * @return
	 */
	public static boolean deleteDownload(String username, String taskID) {
		try {
			lock.lock();
			Map<String, DLInterface> userTask = getTaskMapByUsername(username);
			if (userTask.containsKey(taskID)) {
				userTask.get(taskID).delete();
				userTask.remove(taskID);
				return true;
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 下载重试
	 * 
	 * @param taskID
	 * @return
	 */
	public static boolean retryDownload(String username, String taskID) throws CustomLogicException {
		Map<String, DLInterface> userTask = getTaskMapByUsername(username);
		if (userTask.containsKey(taskID)) {
			String url = userTask.get(taskID).getStatus().get("url");
			deleteDownload(username, taskID);
			addDownloadTask(username, url);
			return true;
		}
		return false;
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
}
