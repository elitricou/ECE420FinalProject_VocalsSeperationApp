package com.example.voicerecorder;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class RecordedAudioListAdaptor extends RecyclerView.Adapter<RecordedAudioListAdaptor.RecordedAudioListAdaptorViewHolder> {
    private File[] files;
    private final OnItemClickAudioListInterface onItemClickAudioListInterface;

    public RecordedAudioListAdaptor(Context context, OnItemClickAudioListInterface onItemClickAudioListInterface) {
        this.files = initialize(context);
        this.onItemClickAudioListInterface = onItemClickAudioListInterface;
    }

    public File[] getFiles() {
        return files;
    }

    private File[] initialize(Context context) {
        String stringPathToFilesDirectory = Objects.requireNonNull(context.getExternalFilesDir("/")).getAbsolutePath();
        return new File(stringPathToFilesDirectory).listFiles();
    }

    public interface OnItemClickAudioListInterface {
        void onItemClickAudioList(File file, int adapterPosition, LinearLayout linearLayoutOneItemBackground);
    }

    @NonNull
    @Override
    public RecordedAudioListAdaptorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.one_item_layout, parent, false);
        return new RecordedAudioListAdaptorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordedAudioListAdaptorViewHolder holder, int position) {
        holder.textViewFileName.setText(files[position].getName());
        holder.linearLayoutOneItemBackground.setBackgroundResource(R.drawable.one_item_background);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        StringBuilder stringBuilder = new StringBuilder()
                .append(simpleDateFormat.format(files[position].lastModified()))
                .append(" ")
                .append(getFileLength(files[position]));
        holder.textViewFileTimeCreated.setText(stringBuilder);
    }

    private String getFileLength(File file) {
        long fileLength = file.length();
        if (fileLength < 1024) {
            return fileLength + "B";
        } else if (fileLength < (1024 * 1024)) {
            return (fileLength / 1024) + "KB";
        } else {
            return fileLength / 1024 / 1024 + "MB";
        }
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    public class RecordedAudioListAdaptorViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewFileName;
        private final TextView textViewFileTimeCreated;
        private final LinearLayout linearLayoutOneItemBackground;

        public RecordedAudioListAdaptorViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFileName = itemView.findViewById(R.id.text_view_file_name);
            textViewFileTimeCreated = itemView.findViewById(R.id.text_view_file_time_created);
            linearLayoutOneItemBackground = itemView.findViewById(R.id.one_item_background);
            LinearLayout linearLayoutOneItem = itemView.findViewById(R.id.linear_layout_one_item);
            ImageButton imageButtonMenu = itemView.findViewById(R.id.image_button_menu);

            linearLayoutOneItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    onItemClickAudioListInterface.onItemClickAudioList(
                            files[adapterPosition],
                            adapterPosition,
                            linearLayoutOneItemBackground
                    );
                }
            });

            imageButtonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    setPopUpMenu(v, adapterPosition);
                }
            });
        }
    }

    private void setPopUpMenu(View v, int adapterPosition) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        try {
            Method method = popupMenu
                    .getMenu()
                    .getClass()
                    .getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            method.setAccessible(true);
            method.invoke(popupMenu.getMenu(), true);
        } catch (Exception e) {
            Log.d("Exception invoke popup:", Log.getStackTraceString(e));
        }
        popupMenu.inflate(R.menu.menu_pop_up);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_rename) {
                    showAlertDialogRename(v, adapterPosition);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete) {
                    showAlertDialogDelete(v, adapterPosition);
                    return true;
                } else {
                    return false;
                }
            }
        });
        popupMenu.show();
    }

    private void showAlertDialogRename(View v, int adapterPosition) {
        LayoutInflater layoutInflater = LayoutInflater.from(v.getContext());
        View view = layoutInflater.inflate(R.layout.alert_dialog_rename, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
        alertDialogBuilder.setView(view);
        AlertDialog alertDialog = alertDialogBuilder.create();

        Button buttonCancel = view.findViewById(R.id.button_alert_dialog_cancel);
        Button buttonRename = view.findViewById(R.id.button_alert_dialog_rename);
        TextView textViewFileNameOld = view.findViewById(R.id.text_view_alert_dialog_file_name_old_name);
        String fileName = files[adapterPosition].getName();
        String fileNameOld = fileName.substring(0, fileName.length() - 4);
        textViewFileNameOld.setText(fileNameOld);
        EditText editText = view.findViewById(R.id.edit_text_alert_dialog_file_name_new);

        CustomKeyboard customKeyboard = view.findViewById(R.id.custom_keyboard);
        InputConnection inputConnection = editText.onCreateInputConnection(new EditorInfo());
        customKeyboard.setInputConnection(inputConnection);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        buttonRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileNameNew = editText.getText().toString().trim();
                if (fileNameNew.equals("")) {
                    Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.new_file_name_is_empty), Toast.LENGTH_SHORT).show();
                } else {
                    String stringPathToFilesDirectory = Objects.requireNonNull(v.getContext().getExternalFilesDir("/")).getAbsolutePath();
                    File fileOld = new File(stringPathToFilesDirectory, fileName);
                    File fileNew = new File(stringPathToFilesDirectory, fileNameNew.concat(v.getContext().getResources().getString(R.string._wav)));
                    boolean result = fileOld.renameTo(fileNew);
                    if (result) {
                        Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.file_renamed), Toast.LENGTH_SHORT).show();
                        files = initialize(v.getContext());
                        notifyItemChanged(adapterPosition);
                        notifyItemRangeChanged(0, getItemCount());
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.not_renamed), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        alertDialog.show();
        setAlertDialogSizeParameters(alertDialog, v);
        preventSoftKeyboardBeShownWhenEditTextIsClicked(editText);
    }

    private void setAlertDialogSizeParameters(AlertDialog alertDialog, View v) {
        int width = (int) (v.getResources().getDisplayMetrics().widthPixels * 0.99);
        int height = (int) (v.getResources().getDisplayMetrics().heightPixels * 0.85);
        Objects.requireNonNull(alertDialog.getWindow()).setLayout(width, height);
    }

    private void preventSoftKeyboardBeShownWhenEditTextIsClicked(EditText editText) {
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setTextIsSelectable(true);
    }

    private void showAlertDialogDelete(View v, int adapterPosition) {
        LayoutInflater layoutInflater = LayoutInflater.from(v.getContext());
        View view = layoutInflater.inflate(R.layout.alert_dialog_delete, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
        alertDialogBuilder.setView(view);
        AlertDialog alertDialog = alertDialogBuilder.create();

        TextView textViewFileName = view.findViewById(R.id.text_view_alert_dialog_file_name);
        Button buttonNo = view.findViewById(R.id.button_alert_dialog_delete_no);
        Button buttonYes = view.findViewById(R.id.button_alert_dialog_delete_yes);
        textViewFileName.setText(files[adapterPosition].getName());

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stringPathToFilesDirectory = Objects.requireNonNull(v.getContext().getExternalFilesDir("/")).getAbsolutePath();
                File file = new File(stringPathToFilesDirectory, String.valueOf(textViewFileName.getText()));
                if (file.exists()) {
                    boolean result = file.delete();
                    if (result) {
                        files = initialize(v.getContext());
                        notifyItemRemoved(adapterPosition);
                        notifyItemRangeChanged(0, getItemCount());
                    } else {
                        Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.file_was_not_deleted), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.file_not_exists), Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
