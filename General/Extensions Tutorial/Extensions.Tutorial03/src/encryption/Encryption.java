package encryption;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.junit.Test;

import utilities.Encoder;
import utilities.Message;

//New Line of Code
public class Encryption {
	private static Encryption _encryption = null;
	byte[] wrappedKey;
	Key unwrappedKey;
	SecretKey passwordKey;
	PBEParameterSpec paramSpec;
	Key sharedKey;
	Cipher cipher;
	PBEKeySpec keySpec;

	public static Encryption getInstance() {
		if (_encryption == null)
			try {
				_encryption = new Encryption();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return _encryption;
	}

	public Encryption() throws Exception {
		KeyGenerator kg = KeyGenerator.getInstance("DESede");
		sharedKey = kg.generateKey();
		String password = "password";
		byte[] salt = "salt1234".getBytes();
		paramSpec = new PBEParameterSpec(salt, 20); // Parameter based
													// encryption
		keySpec = new PBEKeySpec(password.toCharArray());

	}

	public byte[] Encrypt(Message data, Key _sharedKey) {
		try {
			SecretKeyFactory kf = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			passwordKey = kf.generateSecret(keySpec);
			cipher = Cipher.getInstance("PBEWithMD5AndDES");
			cipher.init(Cipher.WRAP_MODE, passwordKey, paramSpec);
			wrappedKey = cipher.wrap(sharedKey);
			cipher = Cipher.getInstance("DESede");
			cipher = Cipher.getInstance("PBEWithMD5AndDES");
			cipher.init(Cipher.UNWRAP_MODE, passwordKey, paramSpec);
			unwrappedKey = cipher.unwrap(wrappedKey, "DESede",
					Cipher.SECRET_KEY);
			cipher = Cipher.getInstance("DESede");
			cipher.init(Cipher.ENCRYPT_MODE, _sharedKey);
			byte[] input = Encoder.encode(data);
			byte[] encrypted = cipher.doFinal(input);
			return encrypted;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Message Decrypt(byte[] encrypted, Key _unwrappedKey) {
		try {
			SecretKeyFactory kf = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			passwordKey = kf.generateSecret(keySpec);
			cipher = Cipher.getInstance("PBEWithMD5AndDES");
			cipher.init(Cipher.WRAP_MODE, passwordKey, paramSpec);
			wrappedKey = cipher.wrap(sharedKey);
			cipher = Cipher.getInstance("DESede");
			cipher = Cipher.getInstance("PBEWithMD5AndDES");
			cipher.init(Cipher.UNWRAP_MODE, passwordKey, paramSpec);
			unwrappedKey = cipher.unwrap(wrappedKey, "DESede",
					Cipher.SECRET_KEY);
			cipher = Cipher.getInstance("DESede");
			cipher.init(Cipher.DECRYPT_MODE, _unwrappedKey);
			Message data = (Message) Encoder.decode(cipher.doFinal(encrypted));

			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Key getUnwrappedKey() {
		return unwrappedKey;
	}

	public void setUnwrappedKey(Key unwrappedKey) {
		this.unwrappedKey = unwrappedKey;
	}

	public Key getSharedKey() {
		return sharedKey;
	}

	public void setSharedKey(Key sharedKey) {
		this.sharedKey = sharedKey;
	}

	public Cipher getCipher() {
		return cipher;
	}

	public void setCipher(Cipher c) {
		this.cipher = c;
	}

	@Test
	public void EncryptTest01() {
		KeyRequest _request = new KeyRequest("Ali", "abcde");
		Encryption.getInstance().Encrypt(_request, sharedKey);
	}

	@Test
	public void DecryptTest01() {
		KeyRequest _request = new KeyRequest("Ali", "abcdef");
		byte[] encrypted = (byte[]) Encryption.getInstance().Encrypt(_request,sharedKey);
		Encryption.getInstance().Decrypt(encrypted, sharedKey);
	}

}
