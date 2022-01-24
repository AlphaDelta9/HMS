package com.pmc.huawei;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private JSONArray items;
    private OnItemClick listener;

    public ItemAdapter(JSONArray items, OnItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ItemViewHolder itemViewHolder, int i) {
        final int h = i;
        try {
            itemViewHolder.textView.setText(new JSONObject(items.getString(i)).getString("string1"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        itemViewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickView(h);
            }
        });
        itemViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickButton(h);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.length();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button button;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            button = itemView.findViewById(R.id.button);
        }
    }

    interface OnItemClick{
        void onClickView(int item);
        void onClickButton(int item);
    }
}
