package name.zjq.blog.pcd.utils;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {
    /**
     * 对象是否为null或空字符串
     *
     * @param a
     * @return
     */
    public static boolean isNullOrEmpty(String a) {
        return a == null || "".equals(a);
    }

    /**
     * 获取uuid
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

//    public static String StringFilter(String str) {
//        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
//        Pattern p = Pattern.compile(regEx);
//        Matcher m = p.matcher(str);
//        return m.replaceAll("").trim();
//    }
}
