package com.group46.infosys1d;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterDialogFragment extends BottomSheetDialogFragment {

    private Spinner categorySpinner;

    public interface FilterDialogListener {
        void onFilterApplied(String selectedCategory);
    }

    private FilterDialogListener listener;

    public static FilterDialogFragment newInstance() {
        return new FilterDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FilterDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FilterDialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.filter_dialog_layout, container, false);

        categorySpinner = view.findViewById(R.id.categorySpinner);

        // Populate spinner with categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.Categories, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        categorySpinner.setAdapter(adapter);

        Button applyButton = view.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass selected category to listener
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                listener.onFilterApplied(selectedCategory);
                dismiss(); // Dismiss the bottom sheet dialog
            }
        });

        return view;
    }
}
