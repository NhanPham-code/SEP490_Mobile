package com.example.sep490_mobile.ui.feedback;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.FeedbackAdapter;
import com.example.sep490_mobile.model.Feedback;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class FeedbackFragment extends Fragment {

    private FeedbackViewModel viewModel;
    private FeedbackAdapter adapter;
    private int stadiumId;
    private int currentUserId;
    private Feedback currentUserFeedback = null;

    // Views
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MaterialCardView cardAddFeedback;
    private EditText editTextComment;
    private RatingBar ratingBarInput;
    private Button buttonSubmit;
    private TextView addFeedbackTitle;
    private Button buttonDeleteMyFeedback;
    private View deleteButtonSpace;
    private LinearLayoutManager layoutManager;

    // Views cho phân trang
    private LinearLayout paginationControls;
    private LinearLayout pagesContainer;
    private Button buttonPrevPage, buttonNextPage;

    // Views and variables for image handling
    private ImageView imagePreview;
    private Button buttonSelectImage;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri = null;
    // LƯU Ý: Sửa lại IP này thành 10.0.2.2 nếu bạn dùng máy ảo Android
    private static final String BASE_URL = "https://localhost:7136";

    public static FeedbackFragment newInstance(int stadiumId, int currentUserId) {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        args.putInt("STADIUM_ID", stadiumId);
        args.putInt("CURRENT_USER_ID", currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stadiumId = getArguments().getInt("STADIUM_ID");
            currentUserId = getArguments().getInt("CURRENT_USER_ID", -1);
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imagePreview.setVisibility(View.VISIBLE);
                        Glide.with(requireContext()).load(uri).into(imagePreview);
                        buttonSelectImage.setText("Đổi ảnh");
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();

        viewModel = new ViewModelProvider(this).get(FeedbackViewModel.class);
        observeViewModel();

        if (stadiumId > 0) {
            viewModel.setStadiumId(stadiumId);
            viewModel.loadFeedbacksForPage(1);
            if (currentUserId != -1) {
                viewModel.findUserFeedback(stadiumId, currentUserId);
            }
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_feedbacks);
        progressBar = view.findViewById(R.id.feedback_progress_bar);
        cardAddFeedback = view.findViewById(R.id.card_add_feedback);
        editTextComment = view.findViewById(R.id.edit_text_comment);
        ratingBarInput = view.findViewById(R.id.rating_bar_input);
        buttonSubmit = view.findViewById(R.id.button_submit);
        addFeedbackTitle = view.findViewById(R.id.add_feedback_title);
        buttonDeleteMyFeedback = view.findViewById(R.id.button_delete_my_feedback);
        deleteButtonSpace = view.findViewById(R.id.delete_button_space);
        paginationControls = view.findViewById(R.id.pagination_controls);
        pagesContainer = view.findViewById(R.id.pages_container);
        buttonPrevPage = view.findViewById(R.id.button_prev_page);
        buttonNextPage = view.findViewById(R.id.button_next_page);
        imagePreview = view.findViewById(R.id.image_preview);
        buttonSelectImage = view.findViewById(R.id.button_select_image);
    }

    private void setupRecyclerView() {
        adapter = new FeedbackAdapter(currentUserId);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        buttonSubmit.setOnClickListener(v -> submitNewOrUpdateFeedback());

        buttonDeleteMyFeedback.setOnClickListener(v -> {
            if (currentUserFeedback != null) {
                showDeleteConfirmationDialog(currentUserFeedback);
            }
        });

        buttonPrevPage.setOnClickListener(v -> {
            Integer currentPage = viewModel.currentPage.getValue();
            if (currentPage != null && currentPage > 1) {
                viewModel.loadFeedbacksForPage(currentPage - 1);
            }
        });

        buttonNextPage.setOnClickListener(v -> {
            Integer currentPage = viewModel.currentPage.getValue();
            Integer totalPages = viewModel.totalPages.getValue();
            if (currentPage != null && totalPages != null && currentPage < totalPages) {
                viewModel.loadFeedbacksForPage(currentPage + 1);
            }
        });

        buttonSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            buttonSubmit.setEnabled(!loading);
            buttonDeleteMyFeedback.setEnabled(!loading);
        });

        viewModel.feedbacks.observe(getViewLifecycleOwner(), feedbacks -> adapter.submitList(feedbacks));

        viewModel.error.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.forceRefreshEvent.observe(getViewLifecycleOwner(), refresh -> {
            if (refresh != null && refresh) {
                Integer currentPage = viewModel.currentPage.getValue();
                viewModel.loadFeedbacksForPage(currentPage != null ? currentPage : 1);
                if (currentUserId != -1) {
                    viewModel.findUserFeedback(stadiumId, currentUserId);
                }
            }
        });

        viewModel.totalPages.observe(getViewLifecycleOwner(), totalPages -> {
            Integer currentPage = viewModel.currentPage.getValue();
            if (currentPage != null && totalPages != null) {
                updatePaginationControls(currentPage, totalPages);
            }
        });

        viewModel.currentPage.observe(getViewLifecycleOwner(), currentPage -> {
            Integer totalPages = viewModel.totalPages.getValue();
            if (totalPages != null && currentPage != null) {
                updatePaginationControls(currentPage, totalPages);
            }
        });

        viewModel.userFeedback.observe(getViewLifecycleOwner(), feedback -> {
            currentUserFeedback = feedback;
            handleFeedbackFormVisibility();
        });

        viewModel.createSuccessEvent.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.updateSuccessEvent.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Cập nhật đánh giá thành công!", Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.deleteSuccessEvent.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Đã xóa thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePaginationControls(int currentPage, int totalPages) {
        if (totalPages <= 1) {
            paginationControls.setVisibility(View.GONE);
            return;
        }
        paginationControls.setVisibility(View.VISIBLE);
        pagesContainer.removeAllViews();

        buttonPrevPage.setEnabled(currentPage > 1);
        buttonNextPage.setEnabled(currentPage < totalPages);

        for (int i = 1; i <= totalPages; i++) {
            Button pageButton = new Button(requireContext(), null, android.R.attr.borderlessButtonStyle);
            pageButton.setText(String.valueOf(i));
            final int page = i;
            pageButton.setOnClickListener(v -> viewModel.loadFeedbacksForPage(page));

            if (i == currentPage) {
                pageButton.setTypeface(null, Typeface.BOLD);
                pageButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
            } else {
                pageButton.setTypeface(null, Typeface.NORMAL);
                pageButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            }
            pagesContainer.addView(pageButton);
        }
    }

    private void handleFeedbackFormVisibility() {
        if (currentUserId == -1) {
            cardAddFeedback.setVisibility(View.GONE);
            return;
        }

        cardAddFeedback.setVisibility(View.VISIBLE);
        selectedImageUri = null; // Reset selection

        if (currentUserFeedback != null) {
            addFeedbackTitle.setText("Chỉnh sửa đánh giá của bạn");
            ratingBarInput.setRating(currentUserFeedback.getRating());
            editTextComment.setText(currentUserFeedback.getComment());
            buttonSubmit.setText("Cập nhật");
            buttonDeleteMyFeedback.setVisibility(View.VISIBLE);
            deleteButtonSpace.setVisibility(View.VISIBLE);

            if (currentUserFeedback.getImagePath() != null && !currentUserFeedback.getImagePath().isEmpty()) {
                imagePreview.setVisibility(View.VISIBLE);
                String fullUrl = BASE_URL + currentUserFeedback.getImagePath();
                Glide.with(requireContext()).load(fullUrl).into(imagePreview);
                buttonSelectImage.setText("Đổi ảnh");
            } else {
                imagePreview.setVisibility(View.GONE);
                buttonSelectImage.setText("Thêm ảnh");
            }
        } else {
            addFeedbackTitle.setText("Để lại đánh giá của bạn");
            ratingBarInput.setRating(0);
            editTextComment.setText("");
            buttonSubmit.setText("Gửi đánh giá");
            buttonDeleteMyFeedback.setVisibility(View.GONE);
            deleteButtonSpace.setVisibility(View.GONE);
            imagePreview.setVisibility(View.GONE);
            buttonSelectImage.setText("Chọn ảnh");
        }
    }

    private void submitNewOrUpdateFeedback() {
        int rating = (int) ratingBarInput.getRating();
        String comment = editTextComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(getContext(), "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        File imageFile = null;
        if (selectedImageUri != null) {
            imageFile = getFileFromUri(requireContext(), selectedImageUri);
        }

        if (currentUserFeedback != null) {
            viewModel.updateFeedback(currentUserFeedback.getId(), currentUserId, stadiumId, rating, comment, imageFile);
        } else {
            viewModel.createFeedback(currentUserId, stadiumId, rating, comment, imageFile);
        }
    }

    private void showDeleteConfirmationDialog(Feedback feedback) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đánh giá này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteFeedback(feedback.getId());
                })
                .show();
    }

    private File getFileFromUri(Context context, Uri uri) {
        if (uri == null) return null;
        File tempFile = null;
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            tempFile = new File(context.getCacheDir(), "temp_image.jpg");
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = Objects.requireNonNull(inputStream).read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}