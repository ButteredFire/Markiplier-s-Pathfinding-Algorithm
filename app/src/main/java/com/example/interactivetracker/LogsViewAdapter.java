package com.example.interactivetracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LogsViewAdapter extends RecyclerView.Adapter<LogsViewAdapter.ViewHolder> {
    Context context;
    ArrayList<LogsModel> logsModels;

    boolean setDefaultColor = false;
    ColorStateList defaultColor = null;

    public LogsViewAdapter(Context context, ArrayList<LogsModel> logsModels) {
        this.context = context;
        this.logsModels = logsModels;
    }

    @NonNull
    @Override
    public LogsViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.log_item, parent, false);

        return new LogsViewAdapter.ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull LogsViewAdapter.ViewHolder holder, int position) {
        if(!setDefaultColor) {
            defaultColor = holder.TV_logMessage.getTextColors();
            setDefaultColor = true;
        }

        holder.TV_callSign.setText(logsModels.get(position).getLogCallSign());

        holder.TV_logMessage.setText(logsModels.get(position).getLogMessage());
        String msgColor = logsModels.get(position).getMsgColor();
        switch (msgColor) {
            case "warning":
                holder.TV_logMessage.setTextColor(R.color.yellow);
                break;

            case "success":
                holder.TV_logMessage.setTextColor(R.color.green);

            case "":
                holder.TV_logMessage.setTextColor(defaultColor);

            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return logsModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView TV_callSign, TV_logMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            TV_callSign = itemView.findViewById(R.id.TV_callSign);
            TV_logMessage = itemView.findViewById(R.id.TV_logMessage);
        }
    }
}
