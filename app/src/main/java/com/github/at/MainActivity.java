package com.github.at;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.at.model.User;
import com.github.at.widget.MentionEditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_DATA = "extra_data";
    private static final int REQ_CODE_SELECT_FRIENDS = 1011;

    private ImageView ivAt;
    private MentionEditText commentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivAt = (ImageView) findViewById(R.id.at);
        commentEditText = (MentionEditText) findViewById(R.id.comment_edit);
        ivAt.setOnClickListener(this);

        commentEditText.setMentionTextColor(getResources().getColor(R.color.s3));
        commentEditText.setOnMentionInputListener(new MentionEditText.OnMentionInputListener() {
            @Override
            public void onMentionCharacterInput() {
                goSelectFriends();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == ivAt) {
            goSelectFriends();
        }
    }

    private void goSelectFriends() {
        if (commentEditText.getMentionTextCount() < 5) {
            Intent intent = new Intent(MainActivity.this, SelectFriendsActivity.class);
            startActivityForResult(intent, REQ_CODE_SELECT_FRIENDS);
        } else {
            Toast.makeText(this, R.string.max_at_limit, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SELECT_FRIENDS && data != null) {
            if (data.hasExtra(EXTRA_DATA)) {
                User user = (User) data.getSerializableExtra(EXTRA_DATA);
                if (user != null) {
                    if (!commentEditText.isContains(user.getUserId())) {
                        commentEditText.addMentionText(user.getUserId(), user.getUserName());
                    } else {
                        Toast.makeText(this, R.string.already_ated, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
