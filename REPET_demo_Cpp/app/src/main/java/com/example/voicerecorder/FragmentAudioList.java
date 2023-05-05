package com.example.voicerecorder;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;

public class FragmentAudioList extends Fragment {
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private File fileToPlay;
    private File[] files;
    private ImageButton imageButtonAudioPlayerPlay;
    private ImageButton imageButtonAudioPlayerFastForward;
    private ImageButton imageButtonAudioPlayerSkipNext;
    private ImageButton imageButtonAudioPlayerFastRewind;
    private ImageButton imageButtonAudioPlayerSkipPrevious;
    private LinearLayout linearLayoutFileToPlay;
    private MediaPlayer mediaPlayer;
    private RecyclerView recyclerView;
    private Runnable runnable;
    private SeekBar seekBarAudioDuration;
    private TextView textViewAudioPlayerFileName;
    private Handler handler;
    private boolean isMediaPlayerPlaying;
    private int recyclerViewPosition;
    private RecordedAudioListAdaptor recordedAudioListAdaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
    }

    private void changeOneItemBackground(LinearLayout linearLayoutOneItemBackground, int drawable) {
        linearLayoutOneItemBackground.setBackground(
                ResourcesCompat.getDrawable(getResources(), drawable, null));
    }

    private void viewFindViewById(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_all_records);
        textViewAudioPlayerFileName = view.findViewById(R.id.text_view_audio_player_file_name);
        imageButtonAudioPlayerPlay = view.findViewById(R.id.image_button_audio_player_play);
        imageButtonAudioPlayerFastForward = view.findViewById(R.id.image_button_audio_player_fast_forward);
        imageButtonAudioPlayerSkipNext = view.findViewById(R.id.image_button_audio_player_skip_next);
        imageButtonAudioPlayerFastRewind = view.findViewById(R.id.image_button_audio_player_fast_rewind);
        imageButtonAudioPlayerSkipPrevious = view.findViewById(R.id.image_button_audio_player_skip_previous);
        seekBarAudioDuration = view.findViewById(R.id.seek_bar_audio_duration);
    }

    private void initialize(View view) {
        isMediaPlayerPlaying = false;
        viewFindViewById(view);
        recordedAudioListAdaptor = new RecordedAudioListAdaptor(getActivity(), new RecordedAudioListAdaptor.OnItemClickAudioListInterface() {
            @Override
            public void onItemClickAudioList(File file, int adapterPosition, LinearLayout linearLayoutOneItemBackground) {
                recyclerViewPosition = adapterPosition;
                if (linearLayoutFileToPlay != null && linearLayoutFileToPlay != linearLayoutOneItemBackground) {
                    changeOneItemBackground(linearLayoutFileToPlay, R.drawable.one_item_background);
                }
                if (isMediaPlayerPlaying && fileToPlay != file) {
                    stopMediaPlayer();
                    fileToPlay = file;
                    linearLayoutFileToPlay = linearLayoutOneItemBackground;
                    startMediaPlayer(fileToPlay, linearLayoutFileToPlay);
                    changeOneItemBackground(linearLayoutFileToPlay, R.drawable.one_item_background_pressed);
                } else if (isMediaPlayerPlaying) {
                    stopMediaPlayer();
                    startMediaPlayer(fileToPlay, linearLayoutFileToPlay);
                } else if (mediaPlayer != null) {
                    stopMediaPlayer();
                    fileToPlay = file;
                    linearLayoutFileToPlay = linearLayoutOneItemBackground;
                    startMediaPlayer(fileToPlay, linearLayoutFileToPlay);
                    changeOneItemBackground(linearLayoutFileToPlay, R.drawable.one_item_background_pressed);
                } else {
                    fileToPlay = file;
                    linearLayoutFileToPlay = linearLayoutOneItemBackground;
                    startMediaPlayer(fileToPlay, linearLayoutFileToPlay);
                    changeOneItemBackground(linearLayoutFileToPlay, R.drawable.one_item_background_pressed);
                }
            }
        });
        files = recordedAudioListAdaptor.getFiles();
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        if (files != null && files.length > 0) {
            recyclerView.scrollToPosition(files.length - 1);
        }
        recyclerView.setAdapter(recordedAudioListAdaptor);
        LinearLayout linearLayoutAudioPlayer = view.findViewById(R.id.linear_layout_audio_player);
        bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutAudioPlayer);
        bottomSheetBehaviorAddBottomSheetCallback();
        imageButtonAudioPlayerPlaySetOnClickListener();
        imageButtonAudioPlayerFastForwardSetOnClickListener();
        imageButtonAudioPlayerFastRewindSetOnClickListener();
        imageButtonAudioPlayerSkipNextSetOnClickListener();
        imageButtonAudioPlayerSkipPreviousSetOnClickListener();
    }

    private void imageButtonAudioPlayerSkipNextSetOnClickListener() {
        imageButtonAudioPlayerSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSkipNext();
            }
        });
    }

    private void imageButtonAudioPlayerSkipPreviousSetOnClickListener() {
        imageButtonAudioPlayerSkipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSkipPrevious();
            }
        });
    }

    private void imageButtonAudioPlayerPlaySetOnClickListener() {
        imageButtonAudioPlayerPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileToPlay != null && linearLayoutFileToPlay != null) {
                    if (isMediaPlayerPlaying) {
                        pauseAudio();
                    } else if (mediaPlayer == null) {
                        startMediaPlayer(fileToPlay, linearLayoutFileToPlay);
                        changeOneItemBackground(linearLayoutFileToPlay, R.drawable.one_item_background_pressed);
                    } else {
                        resumeAudio();
                    }
                } else {
                    Toast.makeText(getContext(), getResources().getString(R.string.choose_file_to_play), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void imageButtonAudioPlayerFastForwardSetOnClickListener() {
        imageButtonAudioPlayerFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFastForward();
            }
        });
    }

    private void imageButtonAudioPlayerFastRewindSetOnClickListener() {
        imageButtonAudioPlayerFastRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFastRewind();
            }
        });
    }

    private void bottomSheetBehaviorAddBottomSheetCallback() {
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void mediaPlayerSetOnCompletionListener(LinearLayout linearLayoutOneItemBackground) {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (linearLayoutFileToPlay != null) {
                    changeOneItemBackground(linearLayoutOneItemBackground, R.drawable.one_item_background);
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                handler.removeCallbacks(runnable);
                mediaPlayer.release();
                mediaPlayer = null;
                isMediaPlayerPlaying = false;
                imageButtonAudioPlayerPlay.setImageResource(R.drawable.ic_play);
                seekBarAudioDuration.setMax(0);
            }
        });
    }

    private void setSeekBar() {
        seekBarAudioDuration.setMax(mediaPlayer.getDuration());
        handler = new Handler();
        updateRunnable();
        seekBarAudioDuration.postDelayed(runnable, 0);

        seekBarAudioDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                resumeAudio();
            }
        });
    }

    private void updateRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                seekBarAudioDuration.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
            }
        };
    }

    private void startMediaPlayer(File file, LinearLayout linearLayoutOneItemBackground) {
        if (file != null && linearLayoutOneItemBackground != null && file.exists()) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            textViewAudioPlayerFileName.setText(file.getName());
            imageButtonAudioPlayerPlay.setImageResource(R.drawable.ic_pause);
            mediaPlayer = new MediaPlayer();
            mediaPlayerSetOnCompletionListener(linearLayoutOneItemBackground);
            try {
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            isMediaPlayerPlaying = true;
            setSeekBar();
        } else {
            textViewAudioPlayerFileName.setText(getResources().getString(R.string.file_name));
            Toast.makeText(getContext(), getResources().getString(R.string.choose_file_to_play), Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseAudio() {
        handler.removeCallbacks(runnable);
        mediaPlayer.pause();
        imageButtonAudioPlayerPlay.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_play));
        isMediaPlayerPlaying = false;
    }

    private void resumeAudio() {
        files = recordedAudioListAdaptor.getFiles();
        if (fileToPlay != null && linearLayoutFileToPlay != null && fileToPlay.exists()) {
            mediaPlayer.start();
            changeOneItemBackground(linearLayoutFileToPlay, R.drawable.one_item_background_pressed);
            imageButtonAudioPlayerPlay.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_pause));
            isMediaPlayerPlaying = true;
            updateRunnable();
            handler.postDelayed(runnable, 0);
        } else {
            showNoticeItemChanged();
        }
    }

    private void showNoticeItemChanged() {
        if (linearLayoutFileToPlay != null) {
            changeOneItemBackground(linearLayoutFileToPlay, R.drawable.one_item_background);
        }
        textViewAudioPlayerFileName.setText(getResources().getString(R.string.file_name));
        seekBarAudioDuration.setMax(0);
        Toast.makeText(getContext(), getResources().getString(R.string.choose_file_to_play), Toast.LENGTH_SHORT).show();
    }

    private void stopMediaPlayer() {
        if (isMediaPlayerPlaying) {
            handler.removeCallbacks(runnable);
        }
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        isMediaPlayerPlaying = false;
    }

    private void doFastForward() {
        doRewind(2000);
    }

    private void doFastRewind() {
        doRewind(-2000);
    }

    private void doRewind(int incrementOrDecrement) {
        if (isMediaPlayerPlaying) {
            pauseAudio();
            int progress = seekBarAudioDuration.getProgress();
            progress += incrementOrDecrement;
            mediaPlayer.seekTo(progress);
            resumeAudio();
        }
    }

    private void doSkipNext() {
        if (isMediaPlayerPlaying) {
            if (files.length >= 1) {
                int j = 0;
                for (int i = 0; i < files.length; i++) {
                    if (files[i] == fileToPlay) {
                        j = i;
                        j++;
                    }
                }
                if (j != 0 && j < files.length) {
                    fileToPlay = files[j];
                    stopMediaPlayer();
                    recyclerViewPosition += 1;
                    recyclerView.scrollToPosition(recyclerViewPosition);
                    changeOneItemBackgroundWhenDoSkip();
                    startMediaPlayer(fileToPlay, linearLayoutFileToPlay);
                }
            }
        }
    }

    private void doSkipPrevious() {
        if (isMediaPlayerPlaying) {
            if (files.length >= 1) {
                int j = 0;
                for (int i = (files.length - 1); i >= 0; i--) {
                    if (files[i] == fileToPlay) {
                        j = i;
                        j--;
                    }
                }
                if (j >= 0 && j < files.length) {
                    fileToPlay = files[j];
                    stopMediaPlayer();
                    recyclerViewPosition -= 1;
                    recyclerView.scrollToPosition(recyclerViewPosition);
                    changeOneItemBackgroundWhenDoSkip();
                    startMediaPlayer(fileToPlay, linearLayoutFileToPlay);
                }
            }
        }
    }

    private void changeOneItemBackgroundWhenDoSkip() {
        changeOneItemBackground(linearLayoutFileToPlay, R.drawable.one_item_background);
        RecyclerView.ViewHolder recyclerViewViewHolder = recyclerView.findViewHolderForAdapterPosition(recyclerViewPosition);
        if (recyclerViewViewHolder != null) {
            View view = recyclerViewViewHolder.itemView;
            linearLayoutFileToPlay = view.findViewById(R.id.one_item_background);
            changeOneItemBackground(linearLayoutFileToPlay, R.drawable.one_item_background_pressed);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isMediaPlayerPlaying) {
            stopMediaPlayer();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (isMediaPlayerPlaying) {
            stopMediaPlayer();
        }
    }
}