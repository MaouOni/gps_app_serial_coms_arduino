package com.example.proyecto_3;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

public class LoadingDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading_dialog, container, false);
        TextView textView = view.findViewById(R.id.loading_text);
        assert getArguments() != null;
        String message = getArguments().getString(ARG_MESSAGE);
        textView.setText(message);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false); // Prevent dismissing the dialog by touching outside
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent); // Transparent background
        return dialog;
    }
}
