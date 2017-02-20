package name.zjq.blog.pcd.services;

import java.util.List;

import name.zjq.blog.pcd.bean.FileShare;
import name.zjq.blog.pcd.db.DBConnection;
import name.zjq.blog.pcd.utils.StrUtil;

public class ShareFileService {
    /**
     * 生成4位随机密码
     */
    public static String generatePassword() {
        byte[] bytes = new byte[4];
        byte[] uuid = StrUtil.getUUID().getBytes();
        for (byte i = 0; i < 4; i++) {
            bytes[i] = uuid[(int) (Math.random() * 32)];
        }
        return new String(bytes);
    }

    /**
     * 添加新文件分享
     *
     * @return
     */
    public static FileShare addNewShareFile(FileShare fileshare) {
        String querysql = "select * from fileshare where owner='%s' and filepath='%s'";
        querysql = String.format(querysql, fileshare.getOwner(), fileshare.getFilePath());
        List<FileShare> shareList = DBConnection.executeQuery(querysql, FileShare.class);
        if (shareList != null && !shareList.isEmpty()) {
            fileshare = shareList.get(0);
            return fileshare;
        } else {
            String sql = "insert into fileshare values ('%s','%s','%s','%s','%s','%s', %s ,0)";
            sql = String.format(sql, fileshare.getId(), fileshare.getOwner(), fileshare.getPassword(),
                    fileshare.getFileName(), fileshare.getFileType(), fileshare.getFilePath(),
                    fileshare.getShareDate());
            if (DBConnection.executeUpdate(sql) > 0) {
                return fileshare;
            } else {
                return null;
            }
        }
    }

    /**
     * 查询所有的分享
     *
     * @return
     */
    public static List<FileShare> searchAllShare(String owner) {
        String querysql = "select * from fileshare where owner='%s'";
        querysql = String.format(querysql, owner);
        return DBConnection.executeQuery(querysql, FileShare.class);
    }

    /**
     * 取消分享
     *
     * @param id    分享文件的id
     * @param owner 所有者
     * @return
     */
    public static boolean cancelShare(String id, String owner) {
        String deleteSQL = "delete from fileshare where id ='%s' and owner='%s'";
        deleteSQL = String.format(deleteSQL, id, owner);
        return DBConnection.executeUpdate(deleteSQL) > 0;
    }

    /**
     * 通过id查询分享文件
     *
     * @param id
     * @return
     */
    public static FileShare searchShareByID(String id) {
        String deleteSQL = "select * from fileshare where id ='%s'";
        deleteSQL = String.format(deleteSQL, id);
        List<FileShare> fileShareList = DBConnection.executeQuery(deleteSQL, FileShare.class);
        if (fileShareList != null && fileShareList.size() > 0) {
            return fileShareList.get(0);
        } else {
            return null;
        }
    }
}
