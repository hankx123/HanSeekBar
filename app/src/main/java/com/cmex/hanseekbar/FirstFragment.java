package com.cmex.hanseekbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cmex.hanseekbar.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        HanSeekBar seekbar = view.findViewById(R.id.seekbar);
        float[] pos = {0, 0.1f, 0.3f, 0.5f, 1};
        String[] titles = {"0", "10", "30", "50", "100"};
        seekbar.setSelections(pos, titles);
        seekbar.setIsCustomBubbleText();
        seekbar.setOnProgressUpdate(new HanSeekBar.ProgressCallback() {
            @Override
            public void onProgressChanged(float currentValue) {
                seekbar.setBubbleText((int)currentValue + "X");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}