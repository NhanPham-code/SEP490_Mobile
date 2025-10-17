package com.example.sep490_mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GifPickerDialogFragment extends DialogFragment implements GifAdapter.OnGifSelectedListener {

    private static final String TAG = "GifPickerDialog";

    // --- THAY ĐỔI 1: Danh sách các API key ---
    // Thay thế một key duy nhất bằng một danh sách để có thể chuyển đổi.
    private final List<String> GIPHY_API_KEYS = new ArrayList<>(Arrays.asList(
            "7zx1zwTHnSpD2C0JSsGcQmSwGY2ZEgXC", // Key gốc
            "nPzPdEpyFDnvTmqh1yL2BDJWHrf6Ttoi", // Key dự phòng 1
            "dxZv8BlFA4DRLE2v5ywI0mTdRQhYdiYb",
            "Y20zvCCO5kRsUQHTOFn15TgSlpvSG53N"// Key dự phòng 2
    ));
    // Biến để theo dõi key hiện tại đang được sử dụng
    private int currentApiKeyIndex = 0;

    private RecyclerView gifRecyclerView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private ImageButton closeButton;
    private GifAdapter adapter;
    private List<String> gifUrls = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_gif_picker, container, false);
        gifRecyclerView = view.findViewById(R.id.gifRecyclerView);
        progressBar = view.findViewById(R.id.gifProgressBar);
        searchView = view.findViewById(R.id.gifSearchView);
        closeButton = view.findViewById(R.id.closeButton);

        gifRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new GifAdapter(getContext(), gifUrls, this);
        gifRecyclerView.setAdapter(adapter);

        setupSearchView();
        closeButton.setOnClickListener(v -> dismiss());

        fetchTrendingGifs();

        getDialog().setCanceledOnTouchOutside(true);
        return view;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    searchGifs(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.trim().isEmpty()) {
                    fetchTrendingGifs();
                }
                return true;
            }
        });
    }

    // --- THAY ĐỔI 2: Đặt lại chỉ số key cho mỗi yêu cầu mới ---
    private void fetchTrendingGifs() {
        currentApiKeyIndex = 0; // Bắt đầu lại với key đầu tiên
        fetchGifs("https://api.giphy.com/v1/gifs/trending?limit=20&rating=g");
    }

    private void searchGifs(String query) {
        currentApiKeyIndex = 0; // Bắt đầu lại với key đầu tiên
        fetchGifs("https://api.giphy.com/v1/gifs/search?q=" + query + "&limit=20&rating=g");
    }

    // --- THAY ĐỔI 3: Logic tìm nạp GIF được cập nhật để thử lại ---
    private void fetchGifs(String baseUrl) {
        // Kiểm tra xem đã hết key để thử chưa
        if (currentApiKeyIndex >= GIPHY_API_KEYS.size()) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load GIFs. All API keys failed.", Toast.LENGTH_LONG).show();
                });
            }
            return; // Dừng lại nếu tất cả các key đều lỗi
        }

        progressBar.setVisibility(View.VISIBLE);
        String currentKey = GIPHY_API_KEYS.get(currentApiKeyIndex);
        String urlWithKey = baseUrl + "&api_key=" + currentKey;
        Request request = new Request.Builder().url(urlWithKey).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to load GIFs. Network error.", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    // Yêu cầu thành công, xử lý dữ liệu JSON
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray data = jsonObject.getJSONArray("data");

                        gifUrls.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject gif = data.getJSONObject(i);
                            String gifUrl = gif.getJSONObject("images").getJSONObject("fixed_height").getString("url");
                            gifUrls.add(gifUrl);
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                            });
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Error parsing GIFs.", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                } else {
                    // Yêu cầu không thành công (ví dụ: key không hợp lệ), thử key tiếp theo
                    Log.w(TAG, "Request failed with code: " + response.code() + ". Trying next API key.");
                    currentApiKeyIndex++; // Chuyển sang key tiếp theo
                    fetchGifs(baseUrl);   // Gọi lại hàm để thử với key mới
                }
            }
        });
    }

    @Override
    public void onGifSelected(String gifUrl) {
        if (getActivity() instanceof GifAdapter.OnGifSelectedListener) {
            ((GifAdapter.OnGifSelectedListener) getActivity()).onGifSelected(gifUrl);
        }
        dismiss();
    }
}