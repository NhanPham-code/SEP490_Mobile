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
import android.widget.Toast;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.FindTeamAdapter;
import com.example.sep490_mobile.adapter.SelectBookingAdapter;
import com.example.sep490_mobile.adapter.StadiumAdapter;
import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.databinding.FragmentCreatePostBinding;
import com.example.sep490_mobile.databinding.FragmentSelectBookingBinding;
import com.example.sep490_mobile.ui.schedule.ScheduleViewModel;
import com.example.sep490_mobile.viewmodel.FindTeamViewModel;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreatePostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreatePostFragment extends Fragment {

    private FindTeamViewModel model;
    private List<ScheduleBookingDTO> bookingList;
    private int count;
    private FragmentSelectBookingBinding binding;
    private RecyclerView recyclerView;
    private SelectBookingAdapter adapter;


    public CreatePostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreatePostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreatePostFragment newInstance(String param1, String param2) {
        CreatePostFragment fragment = new CreatePostFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
        model = new ViewModelProvider(this).get(FindTeamViewModel.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            model.fetchBooking(this.getContext());
        }
        return root;
    }


}