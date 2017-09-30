package com.comp30022.helium.strawberry.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Response;
import com.comp30022.helium.strawberry.R;
import com.comp30022.helium.strawberry.StrawberryApplication;
import com.comp30022.helium.strawberry.components.chat.Message;
import com.comp30022.helium.strawberry.components.chat.MessageListAdapter;
import com.comp30022.helium.strawberry.components.server.PeachServerInterface;
import com.comp30022.helium.strawberry.components.server.exceptions.InstanceExpiredException;
import com.comp30022.helium.strawberry.components.server.rest.components.StrawberryListener;
import com.comp30022.helium.strawberry.entities.User;
import com.comp30022.helium.strawberry.patterns.exceptions.NotInstantiatedException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shreyashpatodia on 15/09/17.
 */
public class ChatActivity extends AppCompatActivity {
    private static final int QUERY_TIME_SECS = 3;
    private static final String TAG = "StrawberryChat";
    private static final long RECENT_TIME = 86400000; // 1 day
    private RecyclerView mMessageRecycler;

    private Timer timer;
    private User friend;
    private User me;
    List<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        messages = new ArrayList<>();

        me = PeachServerInterface.currentUser();
        String selectedId = StrawberryApplication.getString(StrawberryApplication.SELECTED_USER_TAG);
        friend = new User(selectedId);

//        Message first = new Message("Hey how are you?", me, 100000002);
//        Message second = new Message("I'm good thank you", them, 100000003);
//        messages.add(first);
//        messages.add(second);

        //TODO: update later to STOMP
        timer = new Timer();
        timer.scheduleAtFixedRate(getChatQueryTimerTask(), 0, QUERY_TIME_SECS * 1000);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);

        MessageListAdapter mMessageAdapter = new MessageListAdapter(this, messages);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.scheduleAtFixedRate(getChatQueryTimerTask(), 0, QUERY_TIME_SECS * 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer = null;
    }

    private void queryChat() {
        try {
            PeachServerInterface.getInstance().getChatLog(friend, recentTime(), new StrawberryListener(new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray chats = new JSONArray(response);
                        Log.d(TAG, chats.toString());
                        for (int i = 0; i < chats.length(); i++) {
                            JSONObject chat = new JSONObject(chats.get(i).toString());
                            User sender;

                            if (chat.get("from").equals(me.getId()))
                                sender = me;
                            else
                                sender = friend;
                            updateMessage(new Message(chat.getString("message"), sender, chat.getLong("timestamp")));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, null));

        } catch (NotInstantiatedException | InstanceExpiredException e) {
            e.printStackTrace();
        }
    }

    public TimerTask getChatQueryTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Getting chat log");
                queryChat();
            }
        };
    }

    private void updateMessage(Message message) {
        if (!messages.contains(message)) {
            messages.add(message);

            // update view
            MessageListAdapter mMessageAdapter = new MessageListAdapter(this, messages);
            mMessageRecycler.setAdapter(mMessageAdapter);
            mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private Long recentTime() {
        return System.currentTimeMillis() - RECENT_TIME;
    }

    public void clickSend(View view) {
        EditText editText = (EditText) findViewById(R.id.edittext_chatbox);
        String message = editText.getText().toString();
        Log.d(TAG, "sending " + message);

        if (message.length() > 0) {
            try {
                PeachServerInterface.getInstance().postChat(message, friend.getId(), new StrawberryListener(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        queryChat();
                    }
                }, null));
            } catch (NotInstantiatedException | InstanceExpiredException e) {
                e.printStackTrace();
            }
            editText.getText().clear();
        }
    }
}
