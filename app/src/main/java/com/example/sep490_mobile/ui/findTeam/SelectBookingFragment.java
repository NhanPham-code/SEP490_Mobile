package com.example.sep490_mobile.ui.findTeam;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.SelectBookingAdapter;
import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.data.dto.SelectBookingDTO;
import com.example.sep490_mobile.databinding.FragmentSelectBookingBinding;
import com.example.sep490_mobile.ui.home.OnItemClickListener;
import com.example.sep490_mobile.ui.schedule.ScheduleViewModel;
import com.example.sep490_mobile.viewmodel.FindTeamViewModel;

import java.util.List;


public class SelectBookingFragment extends Fragment implements OnItemClickListener{


    private FindTeamViewModel model;
    private SelectBookingDTO selectBookingDTO;
    private int count;
    private FragmentSelectBookingBinding binding;
    private RecyclerView recyclerView;
    private SelectBookingAdapter adapter;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentSelectBookingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        showLoading();
        model = new ViewModelProvider(this).get(FindTeamViewModel.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            model.fetchBooking(this.getContext());
        }
        observeBookingListResponse();
        return root;
    }
    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewBookings.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerViewBookings.setVisibility(View.VISIBLE);
    }

    private void observeBookingListResponse() {
        adapter = new SelectBookingAdapter(this.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        model.selectBooking.observe(getViewLifecycleOwner(), response -> {
            selectBookingDTO = response;
            if(response.getScheduleBookingDTOS().size() > 0){
                adapter.setSelectBookingList(selectBookingDTO, this);
                adapter.notifyDataSetChanged();
                hideLoading();
            }else{
                binding.layoutNoBookings.setVisibility(View.VISIBLE);
            }

        });
    }

    @Override
    public void onItemClick(int item) {

    }
}