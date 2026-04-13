package com.example.cementtablet;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CementTableAdapter extends RecyclerView.Adapter<CementTableAdapter.ViewHolder> {

    private List<CementLog> dataList;
    private List<String> groupSums = new ArrayList<>();

    public CementTableAdapter(List<CementLog> dataList) {
        this.dataList = dataList;
    }

    public void setGroupSums(List<String> groupSums) {
        this.groupSums = groupSums;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cement_row, parent, false);
        return new ViewHolder(view, new CustomEditTextListener());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CementLog item = dataList.get(position);

        holder.myCustomEditTextListener.updatePosition(position);

        holder.etDGather.setText(item.getDGather());
        
        if (holder.spCLine.getAdapter() == null) {
            String[] lines = {"P101","P102","P103","P104","P105","P106","P107","P108","P109","P110",
                              "P111","P112","P113","P114","P201","P202","P203","P204","P205","P206",
                              "P207","P208","P209","P210","P211","P212","P213","P214"};
            ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(holder.itemView.getContext(),
                    android.R.layout.simple_spinner_item, lines);
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spCLine.setAdapter(spinAdapter);
        }
        
        if (item.getCLine() != null && !item.getCLine().isEmpty()) {
            ArrayAdapter<String> currentAdapter = (ArrayAdapter<String>) holder.spCLine.getAdapter();
            int pos = currentAdapter.getPosition(item.getCLine());
            if (pos >= 0) holder.spCLine.setSelection(pos, false);
        } else {
            holder.spCLine.setSelection(0, false);
            item.setCLine("P101"); // Auto set default to model
        }

        holder.spCLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int sposition, long id) {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < dataList.size()) {
                    dataList.get(holder.getAdapterPosition()).setCLine((String) parent.getItemAtPosition(sposition));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (holder.spGroupSum.getAdapter() == null || holder.spGroupSum.getAdapter().getCount() != groupSums.size()) {
            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(holder.itemView.getContext(),
                    android.R.layout.simple_spinner_item, groupSums);
            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spGroupSum.setAdapter(groupAdapter);
        }

        if (item.getCSample() != null && !item.getCSample().isEmpty()) {
            ArrayAdapter<String> currentGrpAdapter = (ArrayAdapter<String>) holder.spGroupSum.getAdapter();
            int pos = currentGrpAdapter.getPosition(item.getCSample());
            if (pos >= 0) holder.spGroupSum.setSelection(pos, false);
        } else if (groupSums.size() > 0) {
            holder.spGroupSum.setSelection(0, false);
            item.setCSample(groupSums.get(0));
        }

        holder.spGroupSum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int sposition, long id) {
                if (holder.getAdapterPosition() >= 0 && holder.getAdapterPosition() < dataList.size()) {
                    dataList.get(holder.getAdapterPosition()).setCSample((String) parent.getItemAtPosition(sposition));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        holder.etMeasurement.setText(item.getMeasurement());

        if (item.isSaved()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light Green
            holder.etMeasurement.setEnabled(false);
            holder.spCLine.setEnabled(false);
            holder.spGroupSum.setEnabled(false);
            holder.btnDeleteRow.setVisibility(View.INVISIBLE);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.etMeasurement.setEnabled(true);
            holder.spCLine.setEnabled(true);
            holder.spGroupSum.setEnabled(true);
            holder.btnDeleteRow.setVisibility(View.VISIBLE);
        }

        holder.btnDeleteRow.setOnClickListener(v -> {
            if (holder.getAdapterPosition() != RecyclerView.NO_POSITION && !dataList.get(holder.getAdapterPosition()).isSaved()) {
                int currentPos = holder.getAdapterPosition();
                dataList.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, dataList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText etDGather, etMeasurement;
        Spinner spCLine, spGroupSum;
        ImageButton btnDeleteRow;
        CustomEditTextListener myCustomEditTextListener;

        public ViewHolder(View itemView, CustomEditTextListener listener) {
            super(itemView);
            etDGather = itemView.findViewById(R.id.etDGather);
            spCLine = itemView.findViewById(R.id.spCLine);
            spGroupSum = itemView.findViewById(R.id.spGroupSum);
            etMeasurement = itemView.findViewById(R.id.etMeasurement);
            btnDeleteRow = itemView.findViewById(R.id.btnDeleteRow);
            myCustomEditTextListener = listener;

            etDGather.addTextChangedListener(new FieldTextWatcher(1, listener));
            etMeasurement.addTextChangedListener(new FieldTextWatcher(6, listener));
        }
    }

    private static class FieldTextWatcher implements TextWatcher {
        private int fieldType;
        private CustomEditTextListener listener;

        public FieldTextWatcher(int fieldType, CustomEditTextListener listener) {
            this.fieldType = fieldType;
            this.listener = listener;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            listener.onTextChanged(fieldType, s.toString());
        }
    }

    private class CustomEditTextListener {
        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        public void onTextChanged(int fieldType, String text) {
            if (position < 0 || position >= dataList.size()) return;
            CementLog log = dataList.get(position);
            switch (fieldType) {
                case 1: log.setDGather(text); break;
                case 6: log.setMeasurement(text); break;
            }
        }
    }
}
