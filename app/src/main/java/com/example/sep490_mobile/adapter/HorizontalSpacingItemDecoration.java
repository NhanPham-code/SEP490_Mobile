package com.example.sep490_mobile.adapter;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int spaceInPixels;

    public HorizontalSpacingItemDecoration(int spaceInPixels) {
        this.spaceInPixels = spaceInPixels;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = state.getItemCount();

        // Thêm khoảng trống vào bên trái của item đầu tiên
        if (position == 0) {
            outRect.left = spaceInPixels;
        }

        // Thêm khoảng trống vào bên phải của item cuối cùng
        if (position == itemCount - 1) {
            outRect.right = spaceInPixels;
        }
    }
}