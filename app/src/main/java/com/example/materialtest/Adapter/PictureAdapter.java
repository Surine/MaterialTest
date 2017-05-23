package com.example.materialtest.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.materialtest.Bean.Picture;
import com.example.materialtest.R;
import com.example.materialtest.UI.PictureVp_Activity;

import java.util.List;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder>{

    private static final String TAG = "PictureAdapter";

    private Context mContext;
    private Activity mActivity;
    private List<Picture> mFruitList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView CardView;
        ImageView Image;
        TextView Name;

        public ViewHolder(View view) {
            super(view);
            CardView = (CardView) view;
            Image = (ImageView) view.findViewById(R.id.small_image);
            Name = (TextView) view.findViewById(R.id.fruit_name);
        }
    }

    public PictureAdapter(List<Picture> fruitList, Activity activity) {
        mFruitList = fruitList;
        mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        final View view = LayoutInflater.from(mContext).inflate(R.layout.item_picture, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the click postion
                int position = holder.getAdapterPosition();
                Picture fruit = mFruitList.get(position);

                //set intent
                Intent intent = PictureVp_Activity.newIntent(mContext,fruit.getUrl(),fruit.getDesc(),mFruitList);

                //Shared elements of animation
                if (android.os.Build.VERSION.SDK_INT > 21) {
                    mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(mActivity, view, "transitionImg").toBundle());
                } else {
                    mContext.startActivity(intent);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picture fruit = mFruitList.get(position);
        holder.Name.setText(fruit.getDesc());
        //use glide to set picture
        Glide.with(mContext).load(fruit.getUrl()).into(holder.Image);
    }

    @Override
    public int getItemCount() {
        return mFruitList.size();
    }

}
