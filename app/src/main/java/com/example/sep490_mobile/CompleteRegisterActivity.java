package com.example.sep490_mobile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.sep490_mobile.databinding.ActivityCompleteRegisterBinding;
import com.example.sep490_mobile.model.RegistrationData;
import com.example.sep490_mobile.viewmodel.CompleteRegisterViewModel;
import android.Manifest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompleteRegisterActivity extends AppCompatActivity {

    private ActivityCompleteRegisterBinding binding;
    private CompleteRegisterViewModel viewModel;
    private RegistrationData registrationData;
    private Uri avatarUri; // Sẽ là null nếu người dùng không chọn
    private Uri tempPhotoUri; // Dùng để lưu Uri của ảnh đang chụp

    // --- ActivityResultLaunchers ---
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;
    private ActivityResultLauncher<String> pickFromGalleryLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompleteRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (getIntent().hasExtra("REGISTRATION_DATA")) {
            registrationData = (RegistrationData) getIntent().getSerializableExtra("REGISTRATION_DATA");
        }
        if (registrationData == null) {
            Toast.makeText(this, "Lỗi dữ liệu đăng ký.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeLaunchers();
        viewModel = new ViewModelProvider(this).get(CompleteRegisterViewModel.class);

        setupClickListeners();
        setupObservers();
    }

    private void initializeLaunchers() {
        // Launcher xin quyền Camera
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) dispatchTakePictureIntent();
            else Toast.makeText(this, "Bạn đã từ chối quyền sử dụng camera.", Toast.LENGTH_SHORT).show();
        });

        // Launcher xin quyền đọc bộ nhớ
        requestStoragePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) dispatchPickFromGalleryIntent();
            else Toast.makeText(this, "Bạn đã từ chối quyền truy cập ảnh.", Toast.LENGTH_SHORT).show();
        });

        // Launcher chọn ảnh từ gallery
        pickFromGalleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                avatarUri = uri;
                binding.imgAvatar.setImageURI(avatarUri);
            }
        });

        // Launcher chụp ảnh
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
            if (isSuccess) {
                avatarUri = tempPhotoUri;
                binding.imgAvatar.setImageURI(avatarUri);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvSelectAvatar.setOnClickListener(v -> showImageSourceDialog());
        binding.btnCompleteRegistration.setOnClickListener(v -> completeRegistration());
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Chụp ảnh
                        checkCameraPermissionAndTakePhoto();
                    } else { // Chọn từ thư viện
                        checkStoragePermissionAndPickImage();
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkStoragePermissionAndPickImage() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            dispatchPickFromGalleryIntent();
        } else {
            requestStoragePermissionLauncher.launch(permission);
        }
    }

    private void dispatchPickFromGalleryIntent() {
        pickFromGalleryLauncher.launch("image/*");
    }

    private void dispatchTakePictureIntent() {
        try {
            File photoFile = createImageFile();
            tempPhotoUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    photoFile);
            takePictureLauncher.launch(tempPhotoUri);
        } catch (IOException ex) {
            Toast.makeText(this, "Không thể tạo file ảnh.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnCompleteRegistration.setEnabled(!isLoading);
        });

        viewModel.getRegisterSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Đăng ký tài khoản thành công!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void completeRegistration() {
        viewModel.registerUser(
                registrationData.getFullName(),
                registrationData.getEmail(),
                registrationData.getPassword(),
                null, // address
                registrationData.getPhone(),
                null, // gender
                registrationData.getDateOfBirth(),
                avatarUri, // có thể là null
                null
        );
    }
}