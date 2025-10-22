package com.example.sep490_mobile.ui.findTeam;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.UpdateTeamPostDTO;
import com.example.sep490_mobile.databinding.FragmentUpdatePostBinding;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.HtmlConverter;
import com.example.sep490_mobile.utils.PriceFormatter;
import com.example.sep490_mobile.viewmodel.FindTeamViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UpdatePostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdatePostFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FindTeamDTO findTeamDTO;
    private FragmentUpdatePostBinding binding;
    private FindTeamViewModel findTeamViewModel;
    private Map<String, String> odataUrl = new HashMap<>();

    // Store currently loaded ReadTeamPostDTO so we can reuse fields like joinedPlayers when building update payload
    private ReadTeamPostDTO currentReadTeamPostDTO;

    private String mParam1;
    private String mParam2;

    public UpdatePostFragment() {
        // Required empty public constructor
    }

    public static UpdatePostFragment newInstance(String param1, String param2) {
        UpdatePostFragment fragment = new UpdatePostFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUpdatePostBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        findTeamViewModel = new ViewModelProvider(this).get(FindTeamViewModel.class);

        odataUrl.put("$expand", "TeamMembers");
        odataUrl.put("$filter", "Id eq " + mParam1);
        odataUrl.put("$count", "true");
        findTeamViewModel.fetchFindTeamList(odataUrl);
        observeDataResponse();
        setButton();
        // Inflate the layout for this fragment
        return view;
    }

    private void setButton(){
        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateTeamPostDTO dto = collectUpdateData();
                if(dto != null){
                    // TODO: call your ViewModel/network method to send update request.
                    findTeamViewModel.updateTeamPost(dto);
                    findTeamViewModel.teamPostData.observe(getViewLifecycleOwner(), readTeamPostDTO -> {
                        if(readTeamPostDTO != null){
                            Map<String, String> stringStringMap = new HashMap<>();
                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
                            int myId  = sharedPreferences.getInt("user_id", 0);
                            if(mParam2.equalsIgnoreCase("MyPostManagerFragment")){
                                stringStringMap.put("$expand", "TeamMembers");
                                stringStringMap.put("$filter", "CreatedBy eq " + myId);
                                stringStringMap.put("$orderby", "CreatedAt desc");
                                stringStringMap.put("$count", "true");
                                stringStringMap.put("$top", "10");
                                stringStringMap.put("$skip", "0");
                            }else{
                                stringStringMap.put("$expand", "TeamMembers");
                                stringStringMap.put("$filter", "TeamMembers/any(x: x/UserId eq " + myId + ")");
                                stringStringMap.put("$orderby", "CreatedAt desc");
                                stringStringMap.put("$count", "true");
                                stringStringMap.put("$top", "10");
                                stringStringMap.put("$skip", "0");
                            }
                            findTeamViewModel.fetchFindTeamList(stringStringMap);
                            findTeamViewModel.findTeam.observe(getViewLifecycleOwner(), findTeamDTO1 -> {
                                if(findTeamDTO1 != null){
                                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                    fragmentTransaction.addToBackStack(mParam2);
                                    getParentFragmentManager().popBackStack(mParam2, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                }
                            });
                        }
                    });
                }
            }
        });
        binding.backButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.addToBackStack(mParam2);
                getParentFragmentManager().popBackStack(mParam2, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    /**
     * Thu thập dữ liệu từ các input controls và tạo một UpdateTeamPostDTO.
     * Trả về null nếu có lỗi/validate không hợp lệ.
     */
    private UpdateTeamPostDTO collectUpdateData(){
        // id
        int id = -1;
        if (!TextUtils.isEmpty(mParam1)) {
            try {
                id = Integer.parseInt(mParam1);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Id bài đăng không hợp lệ", Toast.LENGTH_LONG).show();
                return null;
            }
        } else {
            Toast.makeText(getContext(), "Không có id bài đăng", Toast.LENGTH_LONG).show();
            return null;
        }

        // title
        String title = binding.updateTitleEditText.getText() != null ? binding.updateTitleEditText.getText().toString().trim() : "";
        if(TextUtils.isEmpty(title)){
            Toast.makeText(getContext(), "Vui lòng nhập tiêu đề", Toast.LENGTH_LONG).show();
            return null;
        }

        // needed players
        int neededPlayers = 0;
        String neededStr = binding.updateNeededPlayersEditText.getText() != null ? binding.updateNeededPlayersEditText.getText().toString().trim() : "";
        if(!TextUtils.isEmpty(neededStr)){
            try {
                neededPlayers = Integer.parseInt(neededStr);
                if(neededPlayers < 0){
                    Toast.makeText(getContext(), "Số thành viên cần tìm không thể âm", Toast.LENGTH_LONG).show();
                    return null;
                }
                if(neededPlayers < currentReadTeamPostDTO.getJoinedPlayers()){
                    Toast.makeText(getContext(), "Số thành viên cần tìm không thể nhỏ hơn số thành viên đã tham gia", Toast.LENGTH_LONG).show();
                    return null;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số thành viên cần tìm không hợp lệ", Toast.LENGTH_LONG).show();
                return null;
            }
        } else {
            Toast.makeText(getContext(), "Vui lòng nhập số thành viên cần tìm", Toast.LENGTH_LONG).show();
            return null;
        }

        // price per person (allow empty -> 0)
        double pricePerPerson = 0.0;
        String priceStr = binding.updatePricePerPersonEditText.getText() != null ? binding.updatePricePerPersonEditText.getText().toString().trim() : "";
        if(!TextUtils.isEmpty(priceStr)){
            try {
                pricePerPerson = PriceFormatter.parsePriceToDouble(priceStr);
            }catch (NumberFormatException e){

            }
            // Remove non-digit except dot

        }else{
            Toast.makeText(getContext(), "Không được để trống giá tiền", Toast.LENGTH_LONG).show();
        }

        // description - convert to HTML if you keep Markdown in EditText.
        String descriptionRaw = binding.updateDescriptionEditText.getText() != null ? HtmlConverter.convertSpannedToEscapedHtml(binding.updateDescriptionEditText) : "";
        String descriptionToSend = descriptionRaw.trim();
        // If you have a helper to convert Markdown -> HTML, use it here.
        // Example: descriptionToSend = HtmlConverter.convertMarkdownToHtml(descriptionRaw);
        // If that method doesn't exist, we will send plain text (or you can wrap into basic <p>).

        // joinedPlayers: preserve existing joinedPlayers if we loaded the post, otherwise keep 0
        int joinedPlayers = 0;
        if(currentReadTeamPostDTO != null){
            joinedPlayers = currentReadTeamPostDTO.getJoinedPlayers();
        }

        // updatedAt: set to current ISO datetime or leave null - backend may set updatedAt itself.
        String updatedAt = null;

        UpdateTeamPostDTO dto = new UpdateTeamPostDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setNeededPlayers(neededPlayers);
        dto.setPricePerPerson(pricePerPerson);
        dto.setDescription(descriptionToSend);
        dto.setJoinedPlayers(joinedPlayers);
        dto.setUpdatedAt(updatedAt);

        return dto;
    }

    private void setDataUpdate(FindTeamDTO dataUpdate){
        if(dataUpdate != null){
            ReadTeamPostDTO readTeamPostDTO = dataUpdate.getTeamPostDTOS().get(0);
            // store current post DTO so collectUpdateData can read joinedPlayers
            this.currentReadTeamPostDTO = readTeamPostDTO;

            StadiumDTO firstStadium = dataUpdate.getStadiums().get(readTeamPostDTO.getStadiumId());

            CourtsDTO[] courtsDTOS;

            if (firstStadium != null ) {
                if (firstStadium.getCourts() != null) {
                    courtsDTOS = firstStadium.getCourts().toArray(new CourtsDTO[0]);
                    System.out.println("Đã chuyển đổi thành công. Số lượng sân: " + courtsDTOS.length);
                } else {
                    System.err.println("Lỗi: Danh sách Courts là NULL.");
                    courtsDTOS = new CourtsDTO[0];
                }
            } else {
                System.err.println("Lỗi: Danh sách Stadiums (value) là RỖNG hoặc NULL.");
                courtsDTOS = new CourtsDTO[0];
            }

            binding.updateTitleEditText.setText(readTeamPostDTO.getTitle());
            HtmlConverter.convertHtmlToMarkdown(readTeamPostDTO.getDescription().trim(), binding.updateDescriptionEditText);
            binding.updateLocationEditText.setText(readTeamPostDTO.getLocation());
            if (courtsDTOS.length > 0 && courtsDTOS[0] != null) {
                binding.updateSport.setText(courtsDTOS[0].getSportType());
            }
            binding.updatePricePerPersonEditText.setText(PriceFormatter.formatPriceDouble(readTeamPostDTO.getPricePerPerson()));
            binding.updateNeededPlayersEditText.setText(readTeamPostDTO.getNeededPlayers() + "");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.updatePlayDateEditText.setText(DurationConverter.convertIsoPlayDate(readTeamPostDTO.getPlayDate(), "dd/MM/yyyy - HH:mm"));
            }
            binding.updateTimePlaySpinner.setText(DurationConverter.convertDuration(readTeamPostDTO.getTimePlay(), 1));
        }else{
            Toast.makeText(getContext(), "Không có dữ liệu để cập nhật", Toast.LENGTH_LONG).show();
            if(getParentFragmentManager() != null){
                getParentFragmentManager().popBackStack();
            }
        }

    }


    private void observeDataResponse(){
        findTeamViewModel.findTeam.observe(getViewLifecycleOwner(), findTeamDTO1 -> {
            if(findTeamDTO1 != null){
                findTeamDTO = findTeamDTO1;
                setDataUpdate(findTeamDTO1);
            }
        });
    }
}