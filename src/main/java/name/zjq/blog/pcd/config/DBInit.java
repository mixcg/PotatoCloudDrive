package name.zjq.blog.pcd.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import name.zjq.blog.pcd.bo.FileShare;

public class DBInit {
	private static final Log logger = LogFactory.getLog(DBInit.class);
	private static String path;

	public static void init() {
		path = DBInit.class.getClassLoader().getResource("").getPath();
		Path file = Paths.get(path.replaceFirst("/", ""), "pcd.db");
		path = file.toAbsolutePath().toString();
		boolean flag = Files.exists(file, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
		if (!flag) {
			try {
				Files.createFile(file);
			} catch (IOException e) {
				logger.error("数据库初始化失败", e);
				System.exit(1);
			}
			executeUpdate(FileShare.getTableSQL());
		}
	}

	public static int executeUpdate(String... sqls) {
		int i = 0;
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s", path));
			Statement stat = conn.createStatement();
			for (String sql : sqls) {
				i += stat.executeUpdate(sql);
			}
			stat.close();
			conn.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return i;
	}

	public static <T> List<T> executeQuery(String sql, Class<T> classz) {
		try {
			List<T> list = new ArrayList<T>();
			Field[] fields = classz.getDeclaredFields();
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s", path));
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				T t = classz.newInstance();
				for (Field field : fields) {
					field.setAccessible(true);
					field.set(t, rs.getObject(field.getName()));
				}
				list.add(t);
			}
			stat.close();
			conn.close();
			return list;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
}
