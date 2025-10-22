package com.example.sep490_mobile.ui.findTeam;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.ListMemberAdapter;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.CreateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.data.dto.PublicProfileDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.TeamMemberDetailDTO;
import com.example.sep490_mobile.data.dto.UpdateTeamMemberDTO;
import com.example.sep490_mobile.data.remote.OnItemClickListener;
import com.example.sep490_mobile.databinding.FragmentPostDetailBinding;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.HtmlConverter;
import com.example.sep490_mobile.utils.ImageUtils;
import com.example.sep490_mobile.viewmodel.FindTeamViewModel;

import java.util.HashMap;
import java.util.Map;

public class PostDetailFragment extends Fragment implements OnItemClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private FragmentPostDetailBinding binding;
    private FindTeamViewModel findTeamViewModel;
    private FindTeamDTO findTeamDTO;
    private ListMemberAdapter adapter;
    private int teamMemberId;
    private final Map<String, String> odata = new HashMap<>();

    public PostDetailFragment() {
        // Required empty public constructor
    }

    public static PostDetailFragment newInstance(String param1, String param2) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findTeamViewModel = new ViewModelProvider(this).get(FindTeamViewModel.class);

        setupRecyclerView();
        setupButtons();
        setupOData();
        observeViewModel();

        loadData();
    }

    private void setupOData() {
        odata.put("$expand", "teamMembers");
        odata.put("$filter", "Id eq " + mParam1);
        odata.put("$count", "true");
    }

    private void loadData() {
        if (mParam1 != null) {
            findTeamViewModel.getTeamMember(Integer.parseInt(mParam1));
            findTeamViewModel.fetchFindTeamList(odata);
        }
    }

    private void setupRecyclerView() {
        adapter = new ListMemberAdapter(requireContext());
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMembers.setAdapter(adapter);
    }

    private void setupButtons() {
        binding.btnJoin.setOnClickListener(v -> {
            String tag = binding.btnJoin.getText().toString();
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", requireContext().MODE_PRIVATE);
            int myId = sharedPreferences.getInt("user_id", 0);

            if (tag.equalsIgnoreCase("Tham Gia")) {
                joinTeam(findTeamDTO.getTeamPostDTOS().get(0).getId(), myId);
            } else { // "Rời Nhóm"
                removeMember(teamMemberId, findTeamDTO.getTeamPostDTOS().get(0).getId(), "member");
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack(mParam2, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private void observeViewModel() {
        // Observer for post details
        findTeamViewModel.findTeam.observe(getViewLifecycleOwner(), findTeamDTO1 -> {
            if (findTeamDTO1 != null) {
                findTeamDTO = findTeamDTO1;
                updatePostDetailsView();
            }
        });

        // Observer for member list
        findTeamViewModel.teamMemberDetail.observe(getViewLifecycleOwner(), teamMemberDetailDTO -> {
            if (teamMemberDetailDTO != null) {
                adapter.setListMemberAdapter(teamMemberDetailDTO, this);
                adapter.notifyDataSetChanged();
            }
        });

        // Observer for create member action
        findTeamViewModel.created.observe(getViewLifecycleOwner(), isCreated -> {
            if (isCreated) {
                Toast.makeText(getContext(), "Đã tham gia, và hãy chờ duyệt", Toast.LENGTH_LONG).show();
                loadData();
            }
        });

        // Observer for update/delete member actions
        findTeamViewModel.success.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                Toast.makeText(getContext(), "Hành động thành công!", Toast.LENGTH_SHORT).show();
                loadData();
            }
        });
    }

    private void joinTeam(int postId, int currentUserId) {
        CreateTeamMemberDTO createTeamMemberDTO = new CreateTeamMemberDTO();
        createTeamMemberDTO.setTeamPostId(postId);
        createTeamMemberDTO.setUserId(currentUserId);
        createTeamMemberDTO.setRole("Waiting");
        createTeamMemberDTO.setJoinedAt(DurationConverter.createCurrentISOString());

        findTeamViewModel.createMember(createTeamMemberDTO);
    }

    private void acceptMember(int id, int postId) {
        ReadTeamPostDTO post = findPostById(postId);
        if (post != null) {
            UpdateTeamMemberDTO updateDto = new UpdateTeamMemberDTO(id, "Member");
            findTeamViewModel.updateTeamMember(updateDto, post);
        }
    }

    private void removeMember(int id, int postId, String type) {
        ReadTeamPostDTO post = findPostById(postId);
        if (post != null) {
            findTeamViewModel.deleteMember(id, postId, post, type);
        }
    }

    private ReadTeamPostDTO findPostById(int postId) {
        if (findTeamDTO != null && findTeamDTO.getTeamPostDTOS() != null) {
            for (ReadTeamPostDTO post : findTeamDTO.getTeamPostDTOS()) {
                if (post.getId() == postId) {
                    return post;
                }
            }
        }
        return null;
    }


    private void updatePostDetailsView() {
        if (findTeamDTO == null || findTeamDTO.getTeamPostDTOS().isEmpty()) return;

        ReadTeamPostDTO readTeamPostDTO = findTeamDTO.getTeamPostDTOS().get(0);
        StadiumDTO stadiumDTO = findTeamDTO.getStadiums().get(readTeamPostDTO.getStadiumId());
        PublicProfileDTO publicProfileDTO = findTeamDTO.getUsers().get(readTeamPostDTO.getCreatedBy());

        if (publicProfileDTO != null) {
            Glide.with(requireContext()).load(ImageUtils.getFullUrl(publicProfileDTO.getAvatarUrl())).centerCrop().into(binding.playerAvatar);
            binding.tvFullName.setText(publicProfileDTO.getFullName());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.tvCreatedAt.setText(DurationConverter.convertIsoPlayDate(readTeamPostDTO.getCreatedAt(), "dd/MM/yyyy - HH:mm"));
            binding.tvContentDateTime.setText(DurationConverter.convertIsoPlayDate(readTeamPostDTO.getPlayDate(), "dd/MM/yyyy"));
        } else {
            // Consider adding a fallback for older APIs if needed
        }

        if (stadiumDTO != null && stadiumDTO.getCourts() != null && !stadiumDTO.getCourts().isEmpty()) {
            CourtsDTO[] courtsDTOS = stadiumDTO.getCourts().toArray(new CourtsDTO[0]);
            binding.tvSportType.setText(courtsDTOS[0].getSportType());
        }

        binding.tvContentLocation.setText(readTeamPostDTO.getLocation());
        binding.tvContentPlayerCount.setText("Đã tham gia " + readTeamPostDTO.getJoinedPlayers() + "/" + readTeamPostDTO.getNeededPlayers() + " người chơi");
        HtmlConverter.convertHtmlToMarkdown(readTeamPostDTO.getDescription(), binding.tvDescription);

        updateJoinButtonState(readTeamPostDTO);
    }

    private void updateJoinButtonState(ReadTeamPostDTO readTeamPostDTO) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", requireContext().MODE_PRIVATE);
        int myId = sharedPreferences.getInt("user_id", 0);

        boolean isMatchOver = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate localPlayDate = java.time.OffsetDateTime
                        .parse(readTeamPostDTO.getPlayDate())
                        .atZoneSameInstant(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                isMatchOver = localPlayDate.isBefore(today);
            } else {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
                java.util.Date playDate = sdf.parse(readTeamPostDTO.getPlayDate().substring(0, 19));
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                java.util.Date today = cal.getTime();
                isMatchOver = playDate.before(today);
            }
        } catch (Exception e) {
            e.printStackTrace();
            isMatchOver = true; // Safety default
        }

        if (isMatchOver) {
            binding.btnJoin.setVisibility(View.GONE);
            return;
        }

        int userStatus = -1; // -1: Not joined, 0: Joined (Member/Waiting), 1: Leader
        for (ReadTeamMemberDTO member : readTeamPostDTO.getTeamMembers()) {
            if (member.getUserId() == myId) {
                teamMemberId = member.getId();
                if (member.getRole().equalsIgnoreCase("Leader")) {
                    userStatus = 1;
                } else {
                    userStatus = 0;
                }
                break;
            }
        }

        switch (userStatus) {
            case 1: // Leader
                binding.btnJoin.setVisibility(View.GONE);
                break;
            case 0: // Member or Waiting
                binding.btnJoin.setVisibility(View.VISIBLE);
                binding.btnJoin.setText("Rời Nhóm");
                binding.btnJoin.setBackgroundColor(requireContext().getResources().getColor(R.color.error_red));
                break;
            default: // Not joined
                binding.btnJoin.setVisibility(View.VISIBLE);
                binding.btnJoin.setText("Tham Gia");
                binding.btnJoin.setBackgroundColor(requireContext().getResources().getColor(R.color.gradient_start));
                break;
        }
    }


    @Override
    public void onItemClick(int item) {
    }

    @Override
    public void onItemClick(int item, String type) {
    }

    @Override
    public void onItemClickRemoveMember(int id, int postId, String type) {
        if (type.equalsIgnoreCase("remove")) {
            removeMember(id, postId, "member");
        } else { // "accept"
            acceptMember(id, postId);
        }
    }
}