package com.example.voicerecorder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FragmentAudioRecorder extends Fragment {
    private NavController navController;
    private ImageView imageViewShowAudioList;
    private ImageView imageViewMicrophone;
    private boolean isRecording;
    private Chronometer chronometer;
    //private MediaRecorder mediaRecorder;
    private WavAudioRecorder wavRecorder =  WavAudioRecorder.getInstanse();
    private String nameOfRecordedFile;

    //private static final String workingDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_recorder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
    }

    private void initialize(View view) {
        navController = Navigation.findNavController(view);
        imageViewShowAudioList = view.findViewById(R.id.image_view_show_audio_list);
        imageViewMicrophone = view.findViewById(R.id.image_view_microphone);
        isRecording = false;
        chronometer = view.findViewById(R.id.chronometer_show_record_duration);
        imageViewShowAudioListSetOnClickListener();
        imageViewMicrophoneSetOnClickListener();
        System.loadLibrary("ECE420_final");
    }

    private void imageViewMicrophoneSetOnClickListener() {
        imageViewMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    if (checkRecordAudioPermission()) {
                        startRecording();
                    }
                } else {
                    stopRecording();
                }
            }
        });
    }

    private void imageViewShowAudioListSetOnClickListener() {
        imageViewShowAudioList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    showAlertDialogStopRecording(v);
                } else {
                    navController.navigate(R.id.action_fragmentAudioRecorder_to_fragmentAudioList);
                }
            }
        });
    }

    private void showAlertDialogStopRecording(View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(v.getContext());
        View view = layoutInflater.inflate(R.layout.alert_dialog_stop_recording, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
        alertDialogBuilder.setView(view);
        AlertDialog alertDialog = alertDialogBuilder.create();

        Button buttonNo = view.findViewById(R.id.button_alert_dialog_stop_audio_no);
        Button buttonYes = view.findViewById(R.id.button_alert_dialog_stop_audio_yes);

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void startRecording() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        imageViewMicrophone.setBackground(
                ContextCompat.getDrawable(requireContext(),
                        R.drawable.button_round_background_1));
        isRecording = true;

        String recordedFilePath = Objects.requireNonNull(requireActivity().getExternalFilesDir("/")).getAbsolutePath();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.ENGLISH);
        Date date = new Date();
        nameOfRecordedFile = simpleDateFormat.format(date) + getResources().getString(R.string._wav);
        wavRecorder.setOutputFile(recordedFilePath+"/"+nameOfRecordedFile);
        if (WavAudioRecorder.State.INITIALIZING == wavRecorder.getState()) {
            wavRecorder.prepare();
            wavRecorder.start();
        }
        // try {
        //     mediaRecorder.prepare();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
        // mediaRecorder.start();
    }

    private void stopRecording() {
        if (((int) (SystemClock.elapsedRealtime() - chronometer.getBase())) > 400) {
            chronometer.stop();
            wavRecorder.stop();
            wavRecorder.reset();
            imageViewMicrophone.setBackground(
                    ContextCompat.getDrawable(requireContext(),
                            R.drawable.button_round_background_2));
            isRecording = false;

            String recordedFilePath = Objects.requireNonNull(requireActivity().getExternalFilesDir("/")).getAbsolutePath();
            cpprepet(recordedFilePath, nameOfRecordedFile);
        }
    }

    public static native void cpprepet(String folderpath, String filename);


    private boolean checkRecordAudioPermission() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            if (wavRecorder != null) {
                stopRecording();
            }
        }
    }
}