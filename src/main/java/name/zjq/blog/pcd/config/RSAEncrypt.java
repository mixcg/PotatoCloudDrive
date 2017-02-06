package name.zjq.blog.pcd.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import name.zjq.blog.pcd.utils.Coder;

public class RSAEncrypt {
	private final Log logger = LogFactory.getLog(RSAEncrypt.class);

	private final String KEY_ALGORITHM = "RSA";
	private byte[] pubKey;// 公钥
	private byte[] priKey;// 私钥
	private String keyFilePath;// 密钥文件

	private RSAEncrypt() {
		keyFilePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + "rsakeypair";
		if (!getKeyFromFile()) {
			initKeyPair();
			writeKeyFile();
		}
	}

	/**
	 * 获取公钥
	 * 
	 * @return
	 */
	public String getPubKey() {
		try {
			return Coder.encoderBASE64(pubKey);
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * 从密钥文件中获取密钥对
	 * 
	 * @return
	 */
	public boolean getKeyFromFile() {
		File file = new File(keyFilePath);
		if (file.exists() && file.isFile()) {
			try {
				Reader reader = new InputStreamReader(new FileInputStream(file));
				int tempchar;
				StringBuilder sb = new StringBuilder();
				while ((tempchar = reader.read()) != -1) {
					if (((char) tempchar) != '\r') {
						sb.append((char) tempchar);
					}
				}
				reader.close();
				String s = sb.toString().replace(" ", "");
				if (!"".equals(s) && s.indexOf(",") > -1) {
					pubKey = Coder.decoderBASE64(s.split(",")[0]);
					priKey = Coder.decoderBASE64(s.split(",")[1]);
					return checkKey();
				}
			} catch (Exception e) {

			}
		}
		logger.info("密钥文件不存在，重新生成密钥文件");
		return false;
	}

	/**
	 * 初始生成密钥对
	 */
	private void initKeyPair() {
		try {
			// 随机生成密钥对
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
			keyPairGen.initialize(1024);
			KeyPair keyPair = keyPairGen.generateKeyPair();
			// 公钥
			RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
			this.pubKey = publicKey.getEncoded();

			// 私钥
			RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
			this.priKey = privateKey.getEncoded();
			logger.error("初始化密钥成功！");
		} catch (Exception e) {
			logger.error("初始化密钥异常！", e);
			System.exit(1);
		}
	}

	/**
	 * 将密钥写入文件
	 */
	private void writeKeyFile() {
		try {
			File file = new File(keyFilePath);
			if (file.exists()) {
				file.delete();
			} else {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file);
			fw.write(Coder.encoderBASE64(this.pubKey) + "," + Coder.encoderBASE64(this.priKey));
			fw.flush();
			fw.close();
		} catch (Exception e) {
			logger.error("密钥写入文件异常！", e);
		}
	}

	/**
	 * 使用公钥进行数据加密
	 * 
	 * @param data
	 * @return String (已由base64编码过)
	 */
	public String encryptByPubKey(String data) {
		try {
			// 取得公钥
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(this.pubKey);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			Key publicKey = keyFactory.generatePublic(x509KeySpec);

			// 对数据加密
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);

			byte[] encryptedData = data.getBytes("utf-8");
			int inputLen = encryptedData.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			// 对数据分段解密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > 117) {
					cache = cipher.doFinal(encryptedData, offSet, 117);
				} else {
					cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * 117;
			}
			byte[] decryptedData = out.toByteArray();
			out.close();
			return Coder.encoderBASE64(decryptedData);
		} catch (Exception e) {
			logger.error("公钥加密异常!", e);
		}
		return null;
	}

	/**
	 * 使用私钥进行数据解密
	 * 
	 * @param data
	 *            (已由base64编码过)
	 * @return String
	 */
	public String decryptByPriKey(String data) {
		try {
			// 取得私钥
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(this.priKey);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

			// 对数据解密
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			byte[] encryptedData = Coder.decoderBASE64(data);

			int inputLen = encryptedData.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			// 对数据分段解密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > 128) {
					cache = cipher.doFinal(encryptedData, offSet, 128);
				} else {
					cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * 128;
			}
			byte[] decryptedData = out.toByteArray();
			out.close();
			return new String(decryptedData, "utf-8");
		} catch (Exception e) {
			logger.error("私钥解密异常!", e);
		}
		return null;
	}

	/**
	 * 检测密钥对是否合法
	 * 
	 * @return
	 */
	private boolean checkKey() {
		return "123456".equals(decryptByPriKey(encryptByPubKey("123456")));
	}

	private static RSAEncrypt encrypt = null;

	public static RSAEncrypt getInstance() {
		if (encrypt == null) {
			encrypt = new RSAEncrypt();
		}
		return encrypt;
	}
}
