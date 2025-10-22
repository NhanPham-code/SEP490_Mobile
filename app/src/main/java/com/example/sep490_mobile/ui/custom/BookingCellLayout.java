// File: ui/custom/BookingCellLayout.java
package com.example.sep490_mobile.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sep490_mobile.R;

public class BookingCellLayout extends FrameLayout {

    // Định nghĩa các hằng số trạng thái để dễ sử dụng trong Adapter
    public static final int STATE_AVAILABLE = 0;
    public static final int STATE_BOOKED = 1;
    public static final int STATE_RELATED = 2;

    // Mảng chứa các thuộc tính trạng thái tùy chỉnh từ attrs.xml
    // BÂY GIỜ CÁC THUỘC TÍNH NÀY SẼ TỒN TẠI
    private static final int[] STATE_BOOKED_ARRAY = {R.attr.state_booked};
    private static final int[] STATE_RELATED_ARRAY = {R.attr.state_related};

    // Sử dụng các biến boolean riêng cho từng trạng thái
    private boolean mIsBooked = false;
    private boolean mIsRelated = false;

    public BookingCellLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBookingState(int state) {
        // Dựa vào state nhận từ Adapter, cập nhật các biến boolean nội bộ
        this.mIsBooked = (state == STATE_BOOKED);
        this.mIsRelated = (state == STATE_RELATED);
        // Yêu cầu vẽ lại view với trạng thái mới
        refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // Tăng extraSpace để chứa đủ các trạng thái tùy chỉnh có thể có
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);

        // Hợp nhất (merge) các trạng thái nếu chúng đang được kích hoạt
        if (mIsBooked) {
            mergeDrawableStates(drawableState, STATE_BOOKED_ARRAY);
        }
        if (mIsRelated) {
            mergeDrawableStates(drawableState, STATE_RELATED_ARRAY);
        }

        return drawableState;
    }
}