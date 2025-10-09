package com.example.sep490_mobile.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.Adapter.StadiumAdapter;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.databinding.FragmentHomeBinding;
import com.example.sep490_mobile.viewmodel.StadiumViewModel;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private StadiumAdapter adapter;
    private StadiumViewModel viewModel;
    private ODataResponse<StadiumDTO> stadiumList;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(StadiumViewModel.class);


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        recyclerView = root.findViewById(R.id.my_recycler_view);
//
//        stadiumList = viewModel.fetchStadium();
//
//        adapter = new StadiumAdapter(getContext(), stadiumList.getItems());
//        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}