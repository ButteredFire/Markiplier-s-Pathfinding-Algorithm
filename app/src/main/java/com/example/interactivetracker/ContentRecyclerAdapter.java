package com.example.interactivetracker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ContentRecyclerAdapter extends RecyclerView.Adapter<ContentRecyclerAdapter.ViewHolder> {
    private final ContentViewInterface contentInterface;
    public final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ArrayList<ContentModel> contentList;
    public final String callSign = "ContentRecyclerAdapter";
    Context context;

    public ContentRecyclerAdapter(Context context, ArrayList<ContentModel> contentList, ContentViewInterface contentInterface) {
        this.context = context;
        this.contentList = contentList;
        this.contentInterface = contentInterface;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView IV_ContentThumbnail, IV_fadeGradient;
        private TextView TV_ContentTitle, TV_ContentStatistics, TV_VideoID;

        public ViewHolder(final View itemView, ContentViewInterface contentInterface) {
            super(itemView);

            IV_fadeGradient = itemView.findViewById(R.id.IV_fadeGradient);

            TV_VideoID = itemView.findViewById(R.id.TV_VideoID);
            IV_ContentThumbnail = itemView.findViewById(R.id.IV_ContentThumbnail);
            TV_ContentTitle = itemView.findViewById(R.id.TV_ContentTitle);
            TV_ContentStatistics = itemView.findViewById(R.id.TV_ContentStatistics);

            itemView.setOnClickListener(view -> {
                if(contentInterface != null) {
                    int pos = getAdapterPosition();

                    if(pos != RecyclerView.NO_POSITION) {
                        contentInterface.onItemClick(pos);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ContentRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.content_item, parent, false);
        return new ContentRecyclerAdapter.ViewHolder(itemView, contentInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentRecyclerAdapter.ViewHolder holder, int position) {
        int fadeGradientID = contentList.get(position).getFadeGradient();
        holder.IV_fadeGradient.setBackgroundResource(fadeGradientID);

        String vID = contentList.get(position).getVideoID();
        String holderText = "Video ID: " + vID;
        holder.TV_VideoID.setText(holderText);

        String title = contentList.get(position).getContentTitle();
        holder.TV_ContentTitle.setText(title);

        String stats = contentList.get(position).getContentStatistics();
        holder.TV_ContentStatistics.setText(stats);

        String thumbnailURL = contentList.get(position).getContentThumbnail();
        Bitmap thumbnail = null;

        List<Future<Bitmap>> futures = new ArrayList<>();
        final Future<Bitmap> future = executor.submit(new APIObject(
                null, null, null, null).new getImageBitmap<>(
                thumbnailURL
        ));
        futures.add(future);

        for (Future<Bitmap> f : futures) {
            try {
                thumbnail = f.get();

            } catch (InterruptedException | ExecutionException ignored) { }
        }

        holder.IV_ContentThumbnail.setImageBitmap(thumbnail);
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}
