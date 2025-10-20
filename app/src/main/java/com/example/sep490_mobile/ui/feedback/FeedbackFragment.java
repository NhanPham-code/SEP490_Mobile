package com.example.sep490_mobile.ui.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.FeedbackAdapter;
import com.example.sep490_mobile.model.Feedback;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

public class FeedbackFragment extends Fragment implements FeedbackAdapter.OnItemInteractionListener {

    private FeedbackViewModel viewModel;
    private FeedbackAdapter adapter;

    // Dữ liệu được truyền từ bên ngoài vào
    private int stadiumId;
    private int currentUserId;

    private Feedback currentUserFeedback = null;

    // Khai báo các Views
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MaterialCardView cardAddFeedback;
    private EditText editTextComment;
    private RatingBar ratingBarInput;
    private Button buttonSubmit;
    private TextView addFeedbackTitle;

    /**
     * Factory method để tạo Fragment và truyền dữ liệu một cách an toàn.
     */
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
            viewModel.loadFeedbacks(stadiumId);
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
    }

    private void setupRecyclerView() {
        adapter = new FeedbackAdapter(this, currentUserId);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        buttonSubmit.setOnClickListener(v -> submitNewOrUpdateFeedback());
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            boolean showLoading = loading != null && loading;
            progressBar.setVisibility(showLoading ? View.VISIBLE : View.GONE);
            buttonSubmit.setEnabled(!showLoading);
        });

        viewModel.feedbacks.observe(getViewLifecycleOwner(), feedbacks -> {
            if (feedbacks != null) {
                adapter.submitList(feedbacks);
                handleFeedbackFormVisibility(feedbacks);
            }
        });

        viewModel.error.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.createSuccessEvent.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                viewModel.loadFeedbacks(stadiumId);
            }
        });

        // observe update success
        viewModel.updateSuccessEvent.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Cập nhật đánh giá thành công!", Toast.LENGTH_SHORT).show();
                viewModel.loadFeedbacks(stadiumId);
            }
        });

        viewModel.deleteSuccessEvent.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Đã xóa thành công", Toast.LENGTH_SHORT).show();
                viewModel.loadFeedbacks(stadiumId);
            }
        });
    }

    private void handleFeedbackFormVisibility(List<Feedback> feedbacks) {
        if (currentUserId == -1) {
            cardAddFeedback.setVisibility(View.GONE);
            return;
        }

        cardAddFeedback.setVisibility(View.VISIBLE);
        currentUserFeedback = null;
        for (Feedback feedback : feedbacks) {
            if (feedback.getUserId() == currentUserId) {
                currentUserFeedback = feedback;
                break;
            }
        }

        if (currentUserFeedback != null) {
            addFeedbackTitle.setText("Chỉnh sửa đánh giá của bạn");
            ratingBarInput.setRating(currentUserFeedback.getRating());
            editTextComment.setText(currentUserFeedback.getComment());
            buttonSubmit.setText("Cập nhật đánh giá");
        } else {
            addFeedbackTitle.setText("Để lại đánh giá của bạn");
            ratingBarInput.setRating(0);
            editTextComment.setText("");
            buttonSubmit.setText("Gửi đánh giá");
        }
    }

    private void submitNewOrUpdateFeedback() {
        int rating = (int) ratingBarInput.getRating();
        String comment = editTextComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(getContext(), "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserFeedback != null) {
            // Gọi hàm update thực tế
            viewModel.updateFeedback(currentUserFeedback.getId(), currentUserId, stadiumId, rating, comment);
        } else {
            viewModel.createFeedback(currentUserId, stadiumId, rating, comment);
        }
    }

    @Override
    public void onDeleteClick(Feedback feedback) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đánh giá này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteFeedback(feedback.getId());
                })
                .show();
    }
}