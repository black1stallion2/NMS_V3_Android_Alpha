package com.apps.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.interfaces.RecyclerClickListener;
import com.apps.nowmusicstream.R;
import com.squareup.picasso.Picasso;

public class AdapterNavigation extends RecyclerView.Adapter<AdapterNavigation.ViewHolder> {

    private Activity activity;
    private String[] name;
    private Integer[] image;
    private int row_index = 0;
    private RecyclerClickListener recyclerClickListener;

    public AdapterNavigation(Activity activity, String[] name, Integer[] image, RecyclerClickListener recyclerClickListener) {
        this.activity = activity;
        this.name = name;
        this.image = image;
        this.recyclerClickListener = recyclerClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.layout_navigation_child, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Picasso.with(activity).load(image[position]).into(holder.imageView);
        holder.textView_Name.setText(name[position]);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerClickListener.onClick(holder.getAdapterPosition());
                if(holder.getAdapterPosition() < 6) {
                    row_index = holder.getAdapterPosition();
                    notifyDataSetChanged();
                }
            }
        });

        if (row_index == position) {
            holder.linearLayout.setBackgroundColor(activity.getResources().getColor(R.color.bg_navi_selected));
            holder.textView_Name.setTextColor(activity.getResources().getColor(R.color.bg_navi_text_selected));
            holder.imageView.setColorFilter(activity.getResources().getColor(R.color.bg_navi_image_selected));
        } else {
            holder.linearLayout.setBackgroundColor(activity.getResources().getColor(R.color.bg_navi_unselected));
            holder.textView_Name.setTextColor(activity.getResources().getColor(R.color.bg_navi_text_unselected));
            holder.imageView.setColorFilter(0);
        }
    }

    @Override
    public int getItemCount() {
        return name.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView textView_Name;
        private LinearLayout linearLayout;


        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView_main_adapter);
            textView_Name = (TextView) itemView.findViewById(R.id.textView_main_adapter);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout_main_adapter);
        }
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

}
