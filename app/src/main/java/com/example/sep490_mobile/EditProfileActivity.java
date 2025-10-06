package com.example.sep490_mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.databinding.ActivityEditProfileBinding;
import com.example.sep490_mobile.utils.ImageUtils;
import com.example.sep490_mobile.viewmodel.EditProfileViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.Manifest;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private EditProfileViewModel viewModel;

    // --- BIẾN MỚI ĐỂ THEO DÕI THAY ĐỔI ---
    private boolean isProfileChanged = false;

    // --- Các Launcher cho việc chọn ảnh và xin quyền ---
    private Uri tempPhotoUri; // Dùng để lưu Uri của ảnh đang chụp
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestStoragePermissionLauncher;
    private ActivityResultLauncher<String> pickFromGalleryLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        // hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupGenderSpinner();
        initializeLaunchers();
        setupClickListeners();
        setupObservers();

        viewModel.fetchUserProfile();
    }

    @Override
    public void finish() {
        if (isProfileChanged) {
            setResult(Activity.RESULT_OK);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        super.finish();
    }

    private void setupGenderSpinner() {
        String[] genders = new String[]{"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        binding.inputGender.setAdapter(adapter);
    }

    // --- Logic chọn ảnh được mang từ CompleteRegisterActivity sang ---
    private void initializeLaunchers() {
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) dispatchTakePictureIntent();
            else Toast.makeText(this, "Bạn đã từ chối quyền sử dụng camera.", Toast.LENGTH_SHORT).show();
        });

        requestStoragePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) dispatchPickFromGalleryIntent();
            else Toast.makeText(this, "Bạn đã từ chối quyền truy cập ảnh.", Toast.LENGTH_SHORT).show();
        });

        pickFromGalleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                viewModel.updateAvatar(uri); // Gọi ViewModel để cập nhật
            }
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
            if (isSuccess && tempPhotoUri != null) {
                viewModel.updateAvatar(tempPhotoUri); // Gọi ViewModel để cập nhật
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnEditAvatar.setOnClickListener(v -> showImageSourceDialog()); // Thay đổi ở đây
        binding.inputDob.setOnClickListener(v -> showDatePickerDialog());
        binding.btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(this)
                .setTitle("Thay đổi ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndTakePhoto();
                    else checkStoragePermissionAndPickImage();
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
            tempPhotoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photoFile);
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
    // --- Kết thúc logic chọn ảnh ---

    private void setupObservers() {
        viewModel.isLoading.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!isLoading);
        });

        viewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.userProfile.observe(this, this::populateUI);

        viewModel.updateSuccess.observe(this, isSuccess -> {
            if (isSuccess) {
                isProfileChanged = true; // Đánh dấu có thay đổi

                // save new profile to shared preferences

                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(PrivateUserProfileDTO profile) {
        if (profile == null) return;

        String fullAvatarUrl = ImageUtils.getFullUrl(profile.getAvatarUrl());
        Glide.with(this)
                .load(fullAvatarUrl)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(binding.imgAvatar);

        binding.inputFullname.setText(profile.getFullName());
        binding.inputEmail.setText(profile.getEmail());
        binding.inputPhone.setText(profile.getPhoneNumber());
        binding.inputAddress.setText(profile.getAddress());
        binding.inputDob.setText(profile.getDateOfBirth());
        binding.inputGender.setText(profile.getGender(), false);
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = year1 + "-" + String.format(Locale.getDefault(), "%02d", monthOfYear + 1) + "-" + String.format(Locale.getDefault(), "%02d", dayOfMonth);
            binding.inputDob.setText(selectedDate);
        }, year, month, day).show();
    }

    private void saveProfileChanges() {
        String fullName = binding.inputFullname.getText().toString().trim();
        String address = binding.inputAddress.getText().toString().trim();
        String phone = binding.inputPhone.getText().toString().trim();
        String dob = binding.inputDob.getText().toString().trim();
        String gender = binding.inputGender.getText().toString().trim();

        if (fullName.isEmpty()) {
            binding.inputLayoutFullname.setError("Họ và tên không được để trống");
            return;
        } else {
            binding.inputLayoutFullname.setError(null);
        }

        viewModel.updateUserProfile(fullName, address, phone, gender, dob);
    }
}