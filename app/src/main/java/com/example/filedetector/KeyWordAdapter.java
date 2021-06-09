package com.example.filedetector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class KeyWordAdapter extends RecyclerView.Adapter<KeyWordAdapter.KeywordViewHolder> {
    private Context mContext;
    private ClickEventHandler myClickEventHandler;
    private List<String> keyWordList;

    KeyWordAdapter(Context context, ClickEventHandler clickEventHandler, ArrayList<String> keyWordList) {
        mContext = context;
        myClickEventHandler = clickEventHandler;
        this.keyWordList = keyWordList;
        if (keyWordList.size() == 0) {
            keyWordList.add(0, "NO KEYWORD");
        }
    }

    interface ClickEventHandler {
        void onClickItemHandler(int moviePosition);
    }

    @NonNull
    @Override
    public KeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View movieItemView = layoutInflater.inflate(R.layout.keyword_item, parent, false);
        return new KeywordViewHolder(movieItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull KeywordViewHolder holder, int position) {
        if (position == 0 && keyWordList.get(0).equals("NO KEYWORD")) {
            holder.deleteKeyword.setVisibility(View.INVISIBLE);
        }
        else {
            holder.deleteKeyword.setVisibility(View.VISIBLE);
        }
        holder.numberView.setText(keyWordList.get(position));
    }

    @Override
    public int getItemCount() {
        if (keyWordList == null) {
            return 0;
        }
        return keyWordList.size();
    }

    public class KeywordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView numberView;
        Button deleteKeyword;

        private KeywordViewHolder(View itemView) {
            super(itemView);
            numberView = itemView.findViewById(R.id.textview_keyword);
            deleteKeyword = itemView.findViewById(R.id.button_delete_keyword);
            deleteKeyword.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            myClickEventHandler.onClickItemHandler(position);
        }
    }
}
