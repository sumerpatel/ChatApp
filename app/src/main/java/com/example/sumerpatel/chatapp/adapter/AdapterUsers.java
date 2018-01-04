package com.example.sumerpatel.chatapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sumerpatel.chatapp.R;
import com.example.sumerpatel.chatapp.interfaces.RecyclerViewClickListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sumerpatel on 1/4/2018.
 */

public class AdapterUsers extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<String> usersList;
    private Context context;
    private RecyclerViewClickListener mListener;

    public AdapterUsers(Context context, ArrayList<String> usersList, RecyclerViewClickListener listener){
        this.context = context;
        this.usersList = usersList;
        this.mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_users,parent,false);
        return new ItemViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String strUsername = usersList.get(position);
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            strUsername = strUsername.substring(0,1).toUpperCase() + strUsername.substring(1).toLowerCase();
            viewHolder.tvUsername.setText(strUsername);
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private RecyclerViewClickListener mListener;
        TextView tvUsername;
        CircleImageView ivProfilePic;
        ImageView ivAddImage;
        public ItemViewHolder(View view,  RecyclerViewClickListener listener) {
            super(view);
            mListener = listener;
            tvUsername = (TextView) view.findViewById(R.id.user_name);
            ivProfilePic = (CircleImageView) view.findViewById(R.id.iv_profile_pic);
            ivAddImage = (ImageView) view.findViewById(R.id.iv_change_pic);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}
