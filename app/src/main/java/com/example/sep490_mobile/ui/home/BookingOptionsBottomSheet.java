package com.example.sep490_mobile.ui.home; // Adjust package if needed

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.databinding.BottomSheetBookingOptionsBinding; // Make sure ViewBinding is enabled
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BookingOptionsBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetBookingOptionsBinding binding;
    private static final String ARG_STADIUM_ID = "stadium_id";
    private int stadiumId;

    // Static method to create a new instance and pass arguments
    public static BookingOptionsBottomSheet newInstance(int stadiumId) {
        BookingOptionsBottomSheet fragment = new BookingOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_STADIUM_ID, stadiumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stadiumId = getArguments().getInt(ARG_STADIUM_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetBookingOptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Click listener for Visual Booking
        binding.optionVisualBooking.setOnClickListener(v -> {
            // Navigate using the action defined in nav_graph
            HomeFragmentDirections.ActionNavigationHomeToVisuallyBookingFragment action =
                    HomeFragmentDirections.actionNavigationHomeToVisuallyBookingFragment(stadiumId);
            NavHostFragment.findNavController(this).navigate(action);
            dismiss(); // Close the bottom sheet after navigation
        });

        // Click listeners for other options (show a simple message for now)
        binding.optionListBooking.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đặt sân theo danh sách (chưa triển khai)", Toast.LENGTH_SHORT).show();
            // dismiss();
        });

        binding.optionMonthlyBooking.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đặt sân theo tháng (chưa triển khai)", Toast.LENGTH_SHORT).show();
            // dismiss();
        });

        // Cancel button
        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up binding
    }
}