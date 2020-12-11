package com.example.akundu.arnabsmessenger;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ListchatActivity extends AppCompatActivity {

    //region Variables
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference msgDatabaseReference, uidDatabaseRef, frndDatabaseRef, databaseReference;
    String uid, frndId, frnd_name;
    FloatingActionButton floatingActionButton;
    ListView chatListView;
    EditText msg;
    Toolbar toolbar;
    TextView toolbartext;
    String UTC_datetime;
    long auto, auto1;
    ProgressBar progressBar;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);//commnent uncomment this line for toolbar
        toolbartext = (TextView) findViewById(R.id.toolbartext);
        toolbartext.setSelected(true);
        toolbartext.requestFocus();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for showing back button
        //getSupportActionBar().setHomeButtonEnabled(true);//for show back button
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        chatListView = (ListView) findViewById(R.id.chat_listview);
        msg = (EditText) findViewById(R.id.msg);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("AllUserInfo").child(AmessengerApplication.appfirebaseUser.getUid());
        msgDatabaseReference = firebaseDatabase.getReference().child("AllUserMsg");
        msgDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                chatListView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Bundle bundle = getIntent().getExtras();
        uid = bundle.getString("u_id");
        frndId = bundle.getString("frnd_id");
        frnd_name = bundle.getString("frnd_name");
        //toolbar.setTitle(frnd_name);//set title Option Menu will not visible
        toolbartext.setText(frnd_name);
        Log.d("msg uid", uid);
        Log.d("msg frndId", frndId);
        uidDatabaseRef = msgDatabaseReference.child(uid);
        frndDatabaseRef = msgDatabaseReference.child(frndId);
        msg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    chatListView.setSelection((int) auto + 1);
                }
            }
        });
        msg.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (((i == EditorInfo.IME_ACTION_SEND))) {
                    submitMsg();
                }
                return false;
            }
        });
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitMsg();
                //((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(),0);
                chatListView.setSelection((int) auto + 3);

            }
        });


        final FirebaseListAdapter<Massage> firebaseListAdapter = new FirebaseListAdapter<Massage>(this, Massage.class, R.layout.row_chat_list, msgDatabaseReference.child(uid).child(frndId)) {
            @Override
            protected void populateView(View v, Massage model, int position) {
                TextView sender = (TextView) v.findViewById(R.id.sender);
                TextView sender2 = (TextView) v.findViewById(R.id.sender2);
                TextView msg = (TextView) v.findViewById(R.id.msg);
                TextView msg2 = (TextView) v.findViewById(R.id.msg2);
                TextView datetime = (TextView) v.findViewById(R.id.datetime);
                if (model.getSender().equals(frnd_name)) {
                    sender.setVisibility(View.VISIBLE);
                    sender2.setVisibility(View.GONE);
                    sender.setText(model.getSender() + ":");
                    msg.setVisibility(View.VISIBLE);
                    msg2.setVisibility(View.GONE);
                    msg.setText(model.getMsg());
                } else {
                    sender.setVisibility(View.GONE);
                    sender2.setVisibility(View.VISIBLE);
                    sender2.setText(model.getSender() + ":");
                    msg2.setVisibility(View.VISIBLE);
                    msg.setVisibility(View.GONE);
                    msg2.setText(model.getMsg());
                }
                msg.setText(model.getMsg());
                datetime.setText(model.getDate());
            }
        };

        chatListView.setAdapter(firebaseListAdapter);


        uidDatabaseRef.child(frndId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(final MutableData currentData) {
                if (currentData.getValue() == null) {

                } else {
                    Log.d("arnab", currentData.getChildrenCount() + "");
                    auto = currentData.getChildrenCount() + 1;
                    Log.d("arnab auto", auto + "");
                    chatListView.setSelection((int) auto - 1);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    // Log.d("Firebase counter increment failed.");
                } else {
                    // Log.d("Firebase counter increment succeeded.");
                }
            }
        });


        uidDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    chatListView.setAdapter(firebaseListAdapter);
                    chatListView.setSelection((int) auto - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("msg chatList", "onResume");
        chatListView.setSelection((int) auto + 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.MyDatePickerStyle);
        alertDialog.setMessage("Do you want to delete this chat history?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                msgDatabaseReference.child(uid).child(frndId).removeValue();
                //have to avoid
                //msgDatabaseReference.child(frndId).child(uid).removeValue();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
        return super.onOptionsItemSelected(item);
    }

    private void submitMsg() {
        if (!uid.equals(frndId) && !msg.getText().toString().equals("")) {
            //String date = new SimpleDateFormat("hh:mm a dd/MM/yy").format(new Date(System.currentTimeMillis()));
            String date = new SimpleDateFormat("hh:mm a").format(new Date(System.currentTimeMillis()));
            final Massage massage = new Massage(AmessengerApplication.username, msg.getText().toString().trim(), date);


            //region trying auto increment

            uidDatabaseRef.child(frndId).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(final MutableData currentData) {
                    if (currentData.getValue() == null) {
                        currentData.child("1").setValue(massage);
                        //msgDatabaseReference.child(frndId).child(uid).child("1").setValue(massage);
                    } else {
                        Log.d("arnab", currentData.getChildrenCount() + "");
                        auto = currentData.getChildrenCount() + 1;
                        Log.d("arnab auto", auto + "");
                        uidDatabaseRef.child(frndId).child(auto + "").setValue(massage);
                        //msgDatabaseReference.child(frndId).child(uid).child(auto + "").setValue(massage);
                    }
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    if (databaseError != null) {
                        // Log.d("Firebase counter increment failed.");
                    } else {
                        // Log.d("Firebase counter increment succeeded.");
                    }
                }
            });

            frndDatabaseRef.child(uid).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(final MutableData currentData) {
                    if (currentData.getValue() == null) {
                        currentData.child("1").setValue(massage);
                        //msgDatabaseReference.child(uid).child("1").setValue(massage);
                    } else {
                        Log.d("arnab", currentData.getChildrenCount() + "");
                        auto1 = currentData.getChildrenCount() + 1;
                        Log.d("arnab auto1", auto1 + "");
                        //uidDatabaseRef.child(frndId).child(auto + "").setValue(massage);
                        frndDatabaseRef.child(uid).child(auto1 + "").setValue(massage);
                    }

                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                    if (databaseError != null) {
                        // Log.d("Firebase counter increment failed.");
                    } else {
                        // Log.d("Firebase counter increment succeeded.");
                    }
                }
            });

            //endregion

            //uidDatabaseRef.child(frndId).child(UTC_datetime + "" + System.currentTimeMillis() + "").setValue(massage);
            //msgDatabaseReference.child(frndId).child(uid).child(UTC_datetime + "" + System.currentTimeMillis() + "").setValue(massage);
            msg.setText("");
            chatListView.setSelection((int) auto + 1);
        }
    }

    public void incrementCounter() {

        uidDatabaseRef.child(frndId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(final MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    // Log.d("Firebase counter increment failed.");
                } else {
                    // Log.d("Firebase counter increment succeeded.");
                }
            }
        });
    }

    public void callDelay() {
        Thread thread = new Thread();
        try {
            thread.sleep(5000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void oldSubmitMsg() {
        if (!uid.equals(frndId) && !msg.getText().toString().equals("")) {
            //String date = new SimpleDateFormat("hh:mm a dd/MM/yy").format(new Date(System.currentTimeMillis()));
            String date = new SimpleDateFormat("hh:mm a").format(new Date(System.currentTimeMillis()));
            Massage massage = new Massage(AmessengerApplication.username, msg.getText().toString().trim(), date);
            uidDatabaseRef.child(frndId).child(System.currentTimeMillis() + "").setValue(massage);
            msgDatabaseReference.child(frndId).child(uid).child(System.currentTimeMillis() + "").setValue(massage);
            msg.setText("");
        }
    }

    class NetworkTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL("http://www.timeapi.org/utc/now");
                InputStream inputStream = url.openConnection().getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                UTC_datetime = bufferedReader.readLine();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return UTC_datetime;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

    }
}
