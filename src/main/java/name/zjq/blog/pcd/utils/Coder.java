package name.zjq.blog.pcd.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 编码util
 *
 */
public class Coder {

	/**
	 * BASE64解码
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static final byte[] decoderBASE64(String key) {
		return Base64.getDecoder().decode(key);
	}

	/**
	 * BASE64编码
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static final String encoderBASE64(byte[] key) {
		return Base64.getEncoder().encodeToString(key);
	}

	/**
	 * base64解码（url）
	 * 
	 * @param key
	 * @return
	 */
	public static final byte[] decoderURLBASE64(String key) {
		return Base64.getUrlDecoder().decode(key);
	}

	/**
	 * base64编码（url）
	 * 
	 * @param key
	 * @return
	 */
	public static final String encoderURLBASE64(byte[] key) {
		return Base64.getUrlEncoder().encodeToString(key);
	}

	/**
	 * byte数组转换成十六进制字符串
	 * 
	 * @param byte[]
	 * @return HexString
	 */
	public static final String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 把16进制字符串转换成字节数组
	 * 
	 * @param hexString
	 * @return byte[]
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static int toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/**
	 * MD5加密
	 * 
	 * @param str
	 *            待加密字符
	 * @param i
	 *            加密次数
	 * @return
	 * @throws Exception
	 */
	public static String MD5(String str, int i) {
		try {
			i = i == 0 ? 10 : i;
			MessageDigest md = MessageDigest.getInstance("MD5");
			int x = 0;
			while (x < i) {
				md.update(str.getBytes());
				str = new BigInteger(1, md.digest()).toString(16);
				x++;
			}
		} catch (Exception e) {

		}
		return str;
	}
}
