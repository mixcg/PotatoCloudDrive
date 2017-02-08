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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import name.zjq.blog.pcd.bo.FileShare;

public class DBConnection {
	private static final Log logger = LogFactory.getLog(DBConnection.class);
	private static Connection conn = null;

	private static String path;

	public static void init() {
		path = DBConnection.class.getClassLoader().getResource("").getPath();
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
		Statement stat = null;
		try {
			stat = getConnection().createStatement();
			for (String sql : sqls) {
				i += stat.executeUpdate(sql);
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				if (stat != null) {
					stat.close();
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return i;
	}

	public static <T> List<T> executeQuery(String sql, Class<T> classz) {
		Statement stat = null;
		try {
			List<T> list = new ArrayList<T>();
			Field[] fields = classz.getDeclaredFields();

			stat = getConnection().createStatement();
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				T t = classz.newInstance();
				for (Field field : fields) {
					field.setAccessible(true);
					FieldAlias inject = field.getAnnotation(FieldAlias.class);
					if (inject != null) {
						String alias = inject.alias();
						if (!alias.equals("")) {
							field.set(t, rs.getObject(alias));
						} else {
							field.set(t, rs.getObject(field.getName()));
						}
					}
				}
				list.add(t);
			}
			stat.close();
			return list;
		} catch (Exception e) {
			logger.error(e);
			return null;
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}
		}
	}

	/**
	 * 获得数据库连接
	 * 
	 * @return
	 */
	private static Connection getConnection() {
		if (conn == null) {
			try {
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s", path));
			} catch (ClassNotFoundException | SQLException e) {
				logger.error("数据库连接失败", e);
				System.exit(1);
			}
		}
		return conn;
	}

	/**
	 * 销毁数据库连接
	 */
	public static void destoryConnection() {
		logger.info("关闭数据库连接");
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
	}
}
