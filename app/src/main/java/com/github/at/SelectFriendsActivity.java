package com.github.at;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.at.adapter.DividerItemDecoration;
import com.github.at.adapter.SelectFriendsAdapter;
import com.github.at.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlove on 2018/3/20.
 */

public class SelectFriendsActivity extends AppCompatActivity implements SelectFriendsAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private SelectFriendsAdapter adapter;

    private String[] userNameArray;
    private List<User> mUserList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friends);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mUserList = new ArrayList<>();
        userNameArray = getResources().getStringArray(R.array.user_name);
        for (int i = 0; i < userNameArray.length; i++) {
            User user = new User(i, userNameArray[i], i + 20);
            mUserList.add(user);
        }

        adapter = new SelectFriendsAdapter(this, mUserList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(User user) {
        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_DATA, user);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
