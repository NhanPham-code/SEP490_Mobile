package com.example.sep490_mobile.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.example.sep490_mobile.data.dto.BiometricTokenResponseDTO;
import com.example.sep490_mobile.data.dto.GoogleApiLoginRequestDTO;
import com.example.sep490_mobile.data.dto.LoginRequestDTO;
import com.example.sep490_mobile.data.dto.LoginResponseDTO;
import com.example.sep490_mobile.data.dto.LogoutRequestDTO;
import com.example.sep490_mobile.data.dto.LogoutResponseDTO;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.dto.RegisterResponseDTO;
import com.example.sep490_mobile.data.dto.ResetPasswordRequestDTO;
import com.example.sep490_mobile.data.dto.ResetPasswordResponseDTO;
import com.example.sep490_mobile.data.dto.UpdateUserProfileDTO;
import com.example.sep490_mobile.data.dto.VerifyOtpResponseDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class UserRepository {
    private final ApiService apiService;
    private final Context context;

    public UserRepository(Context context) {
        this.apiService = ApiClient.getInstance(context).getApiService();
        this.context = context;
    }

    public Call<ResetPasswordResponseDTO> forgotPassword(ResetPasswordRequestDTO request) {
        return apiService.resetPassword(request);
    }

    public Call<Void> deleteBiometricToken() {
        return apiService.deleteBiometricToken();
    }

    public Call<LoginResponseDTO> loginWithBiometricToken(String biometricToken) {
        return apiService.loginWithBiometricToken(biometricToken);
    }

    public Call<BiometricTokenResponseDTO> getBiometricToken() {
        return apiService.getBiometricToken();
    }

    public Call<PrivateUserProfileDTO> updateAvatar(int userId, Uri avatarUri) {
        RequestBody userIdPart = createPartFromString(String.valueOf(userId));

        MultipartBody.Part avatarPart = null;
        if (avatarUri != null) {
            try {
                // Tên "Avatar" phải khớp với DTO của server
                avatarPart = prepareFilePart("Avatar", avatarUri);
            } catch (IOException e) {
                // Xử lý lỗi file ở đây nếu cần
                e.printStackTrace();
            }
        }

        return apiService.updateAvatar(userIdPart, avatarPart);
    }

    public Call<PrivateUserProfileDTO> updateUserProfile(UpdateUserProfileDTO request) {
        return apiService.updateUserProfile(request);
    }

    public Call<PrivateUserProfileDTO> getUserInfo() {
        return apiService.getUserInfo();
    }

    public Call<LoginResponseDTO> loginWithGoogle(GoogleApiLoginRequestDTO request) {
        return apiService.loginWithGoogle(request);
    }

    public Call<LoginResponseDTO> login(LoginRequestDTO request) {
        return apiService.login(request);
    }

    public  Call<LogoutResponseDTO> logout(LogoutRequestDTO request) {
        return apiService.logout(request);
    }

    public Call<Boolean> checkEmailExists(String email) {
        return apiService.checkEmailExists(email);
    }

    public Call<Void> sendOTP(String email) {
        return apiService.sendOTP(new com.example.sep490_mobile.data.dto.SendOtpRequestDTO(email));
    }

    public Call<VerifyOtpResponseDTO> verifyOTP(String email, String code) {
        return apiService.verifyOTP(new com.example.sep490_mobile.data.dto.VerifyOtpRequestDTO(email, code));
    }

    public Call<RegisterResponseDTO> customerRegister(
            String fullName, String email, String password, String address, String phoneNumber,
            String gender, String dateOfBirth, Uri avatarUri) {

        // 1. Chuyển đổi các trường String thành RequestBody
        RequestBody fullNamePart = createPartFromString(fullName);
        RequestBody emailPart = createPartFromString(email);
        RequestBody passwordPart = createPartFromString(password);
        RequestBody addressPart = createPartFromString(address);
        RequestBody phoneNumberPart = createPartFromString(phoneNumber);
        RequestBody genderPart = createPartFromString(gender);
        RequestBody dateOfBirthPart = createPartFromString(dateOfBirth);

        // 2. Chuẩn bị file avatar (nếu có)
        MultipartBody.Part avatarPart = null;
        if (avatarUri != null) {
            try {
                // Tên "Avatar" phải khớp với DTO của server
                avatarPart = prepareFilePart("Avatar", avatarUri);
            } catch (IOException e) {
                // Xử lý lỗi file ở đây nếu cần
                e.printStackTrace();
            }
        }

        // 4. Gọi API với tất cả các part đã chuẩn bị
        return apiService.customerRegister(
                fullNamePart, emailPart, passwordPart, addressPart, phoneNumberPart,
                genderPart, dateOfBirthPart, avatarPart
        );
    }

    /**
     * Hàm tiện ích để tạo RequestBody từ một chuỗi String.
     * @param descriptionString Chuỗi đầu vào.
     * @return Đối tượng RequestBody.
     */
    private RequestBody createPartFromString(String descriptionString) {
        if (descriptionString == null) {
            descriptionString = "";
        }
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    /**
     * Hàm tiện ích để tạo MultipartBody.Part từ một Uri của file.
     * @param partName Tên của trường trong form-data (phải khớp với DTO server).
     * @param fileUri Uri của file.
     * @return Đối tượng MultipartBody.Part.
     */
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) throws IOException {
        File file = getFileFromUri(fileUri, context);
        ContentResolver contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(fileUri);
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);

        // Tạo MultipartBody.Part với tên tham số, tên file và request body
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    /**
     * Lấy File từ content Uri. Retrofit cần đối tượng File để gửi đi.
     * @param uri Uri của file (vd: content://...).
     * @param context Context để truy cập ContentResolver.
     * @return Đối tượng File tạm thời.
     */
    private static File getFileFromUri(Uri uri, Context context) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        String fileName = getFileName(uri, context);
        String[] splitName = splitFileName(fileName);
        File tempFile = File.createTempFile(splitName[0], splitName[1]);

        tempFile.deleteOnExit();
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[4 * 1024]; // 4k buffer
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return tempFile;
    }

    private static String getFileName(Uri uri, Context context) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "unknown_file";
    }

    private static String[] splitFileName(String fileName) {
        String name = fileName;
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i != -1) {
            name = fileName.substring(0, i);
            extension = fileName.substring(i);
        }
        return new String[]{name, extension};
    }
}
