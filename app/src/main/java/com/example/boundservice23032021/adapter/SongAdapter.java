package com.example.boundservice23032021.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boundservice23032021.R;
import com.example.boundservice23032021.interfaces.OnItemClick;
import com.example.boundservice23032021.model.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    List<Song> mListSong;
    OnItemClick mOnItemClick;

    public SongAdapter(List<Song> mListSong) {
        this.mListSong = mListSong;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.layout_item_song, parent, false);
        return new SongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = mListSong.get(position);
        holder.txtIndex.setText(position + 1 + "");
        holder.txtTiTle.setText(song.getTitle());
        long minus = (song.getDuration() / 60000);
        long second = (song.getDuration() % 60000) / 1000;
        holder.txtDuration.setText("0" + minus + " : " + (second >= 10 ? second : "0" + second));
    }

    @Override
    public int getItemCount() {
        return mListSong != null ? mListSong.size() : 0;
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        TextView txtIndex, txtTiTle, txtDuration;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            txtIndex = itemView.findViewById(R.id.textViewIndex);
            txtTiTle = itemView.findViewById(R.id.textViewTitle);
            txtDuration = itemView.findViewById(R.id.textViewDuration);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClick != null) {
                        mOnItemClick.onClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public void setOnItemClick(OnItemClick mOnItemClick) {
        this.mOnItemClick = mOnItemClick;
    }
}
