package com.example.sep490_mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

import javax.crypto.Cipher;

public class BiometricHelper {

    private static final String PREFS_NAME = "BiometricPrefs";
    private static final String PREF_BIOMETRIC_ENABLED = "is_biometric_enabled";
    private static final String PREF_ENCRYPTED_BIOMETRIC_TOKEN = "encrypted_biometric_token"; // Đổi tên
    private static final String PREF_ENCRYPTION_IV = "encryption_iv";
    private static final String KEY_ALIAS = "my_biometric_key";

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final CryptographyManager cryptographyManager;
    private final BiometricCallback callback;

    public interface BiometricCallback {
        void onAuthenticationSuccess(BiometricPrompt.AuthenticationResult result);
        void onAuthenticationError(String errorMessage);
    }

    public BiometricHelper(Context context, BiometricCallback callback) {
        this.context = context;
        this.callback = callback;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.cryptographyManager = new CryptographyManager();
    }

    public boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public boolean isBiometricLoginEnabled() {
        return sharedPreferences.getBoolean(PREF_BIOMETRIC_ENABLED, false);
    }

    public void authenticate(BiometricPrompt.CryptoObject cryptoObject) {
        Executor executor = ContextCompat.getMainExecutor(context);
        BiometricPrompt biometricPrompt = new BiometricPrompt((AppCompatActivity) context, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onAuthenticationSuccess(result);
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.onAuthenticationError(errString.toString());
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực sinh trắc học")
                .setSubtitle("Sử dụng vân tay hoặc khuôn mặt của bạn")
                .setNegativeButtonText("Hủy")
                .build();

        biometricPrompt.authenticate(promptInfo, cryptoObject);
    }

    // --- Các hàm cho việc BẬT tính năng ---
    public void setupForEncryption() {
        try {
            Cipher cipher = cryptographyManager.getCipherForEncryption(KEY_ALIAS);
            BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);
            authenticate(cryptoObject);
        } catch (Exception e) {
            callback.onAuthenticationError("Không thể thiết lập mã hóa: " + e.getMessage());
        }
    }

    public void onEncryptionSuccess(BiometricPrompt.AuthenticationResult result, String tokenToEncrypt) {
        try {
            Cipher cipher = result.getCryptoObject().getCipher();
            CryptographyManager.EncryptedData encryptedData = cryptographyManager.encryptDataWithCipher(tokenToEncrypt, cipher);

            sharedPreferences.edit()
                    .putString(PREF_ENCRYPTED_BIOMETRIC_TOKEN, Base64.encodeToString(encryptedData.ciphertext, Base64.DEFAULT)) // Dùng tên biến mới
                    .putString(PREF_ENCRYPTION_IV, Base64.encodeToString(encryptedData.initializationVector, Base64.DEFAULT))
                    .putBoolean(PREF_BIOMETRIC_ENABLED, true)
                    .apply();
        } catch (Exception e) {
            callback.onAuthenticationError("Lỗi khi lưu dữ liệu đã mã hóa.");
        }
    }

    // --- Các hàm cho việc TẮT tính năng ---
    public void disableBiometricLogin() {
        sharedPreferences.edit()
                .remove(PREF_ENCRYPTED_BIOMETRIC_TOKEN) // Dùng tên biến mới
                .remove(PREF_ENCRYPTION_IV)
                .putBoolean(PREF_BIOMETRIC_ENABLED, false)
                .apply();
    }

    // --- Các hàm cho việc ĐĂNG NHẬP ---
    public void setupForDecryption() {
        try {
            String ivString = sharedPreferences.getString(PREF_ENCRYPTION_IV, null);
            if (ivString == null) {
                callback.onAuthenticationError("Không tìm thấy dữ liệu sinh trắc học.");
                return;
            }
            byte[] iv = Base64.decode(ivString, Base64.DEFAULT);
            Cipher cipher = cryptographyManager.getCipherForDecryption(KEY_ALIAS, iv);
            BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);
            authenticate(cryptoObject);
        } catch (Exception e) {
            callback.onAuthenticationError("Không thể thiết lập giải mã: " + e.getMessage());
        }
    }

    public String onDecryptionSuccess(BiometricPrompt.AuthenticationResult result) {
        try {
            String encryptedTokenString = sharedPreferences.getString(PREF_ENCRYPTED_BIOMETRIC_TOKEN, null); // Dùng tên biến mới
            if (encryptedTokenString == null) return null;

            byte[] encryptedToken = Base64.decode(encryptedTokenString, Base64.DEFAULT);
            Cipher cipher = result.getCryptoObject().getCipher();
            return cryptographyManager.decryptDataWithCipher(encryptedToken, cipher);
        } catch (Exception e) {
            callback.onAuthenticationError("Lỗi khi giải mã dữ liệu.");
            return null;
        }
    }
}