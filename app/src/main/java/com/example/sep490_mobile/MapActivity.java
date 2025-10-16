package com.example.sep490_mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sep490_mobile.ui.map.MapFragment;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thiết lập layout cho Activity này. Layout này nên chứa một FragmentContainerView.
        setContentView(R.layout.activity_map);

        if (savedInstanceState == null) {
            // Tạo và thêm MapFragment vào container
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MapFragment())
                    .commit();
        }
    }
}