package com.example.sep490_mobile.utils;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.nio.charset.Charset;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class CryptographyManager {

    private static final int KEY_SIZE = 256;
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM;
    private static final String ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE;
    private static final String ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;

    private final KeyStore keyStore;

    public CryptographyManager() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get KeyStore instance", e);
        }
    }

    // --- CÁC HÀM MỚI ĐỂ BIOMETRICHELPER SỬ DỤNG ---

    /**
     * Lấy một Cipher để MÃ HÓA.
     * BiometricPrompt sẽ dùng Cipher này trong CryptoObject.
     */
    public Cipher getCipherForEncryption(String keyAlias) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_BLOCK_MODE + "/" + ENCRYPTION_PADDING);
        SecretKey secretKey = getOrCreateSecretKey(keyAlias);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher;
    }

    /**
     * Lấy một Cipher để GIẢI MÃ.
     */
    public Cipher getCipherForDecryption(String keyAlias, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_BLOCK_MODE + "/" + ENCRYPTION_PADDING);
        SecretKey secretKey = getOrCreateSecretKey(keyAlias);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        return cipher;
    }

    /**
     * Mã hóa dữ liệu bằng một Cipher đã được "mở khóa" bởi BiometricPrompt.
     */
    public EncryptedData encryptDataWithCipher(String plaintext, Cipher cipher) throws Exception {
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(Charset.defaultCharset()));
        return new EncryptedData(ciphertext, cipher.getIV());
    }

    /**
     * Giải mã dữ liệu bằng một Cipher đã được "mở khóa" bởi BiometricPrompt.
     */
    public String decryptDataWithCipher(byte[] ciphertext, Cipher cipher) throws Exception {
        byte[] decryptedBytes = cipher.doFinal(ciphertext);
        return new String(decryptedBytes, Charset.defaultCharset());
    }

    // ----------------------------------------------------

    private SecretKey getOrCreateSecretKey(String keyAlias) throws Exception {
        if (!keyStore.containsAlias(keyAlias)) {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM, ANDROID_KEYSTORE);
            final KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(ENCRYPTION_BLOCK_MODE)
                    .setEncryptionPaddings(ENCRYPTION_PADDING)
                    .setKeySize(KEY_SIZE)
                    .setUserAuthenticationRequired(true);

            keyGenerator.init(builder.build());
            return keyGenerator.generateKey();
        } else {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(keyAlias, null)).getSecretKey();
        }
    }

    public static class EncryptedData {
        public final byte[] ciphertext;
        public final byte[] initializationVector;

        public EncryptedData(byte[] ciphertext, byte[] initializationVector) {
            this.ciphertext = ciphertext;
            this.initializationVector = initializationVector;
        }
    }
}