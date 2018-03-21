package com.github.at.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.at.R;
import com.github.at.model.User;

import java.util.List;

/**
 * Created by zlove on 2018/3/20.
 */

public class SelectFriendsAdapter extends RecyclerView.Adapter<SelectFriendsAdapter.SelectFriendsViewHolder> {

    private Context mContext;
    private List<User> mUserList;
    private OnItemClickListener mListener;

    public SelectFriendsAdapter(Context context, List<User> userList, OnItemClickListener listener) {
        this.mContext = context;
        this.mUserList = userList;
        this.mListener = listener;
    }

    @Override
    public SelectFriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_friends, parent, false);
        return new SelectFriendsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SelectFriendsViewHolder holder, int position) {
        User user = mUserList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    class SelectFriendsViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView tvName;
        TextView tvAge;

        public SelectFriendsViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvName = (TextView) itemView.findViewById(R.id.name);
            tvAge = (TextView) itemView.findViewById(R.id.age);
        }

        public void bind(final User user) {
            tvName.setText(user.getUserName());
            tvAge.setText(String.valueOf(user.getUserAge()));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(user);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }
}
