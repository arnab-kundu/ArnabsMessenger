package com.example.akundu.arnabsmessenger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    EditText etEmail, etPassword;
    ProgressDialog progressDialog;
    Animation animation;
    int count = 0;
    private CheckBox cbSaveUser;
    private SharedPreferences sharedPreferences;
    boolean show = true;
    private ImageButton showHidePasswordButton;
    private RelativeLayout relativeLayoutEdittextPassword;
    private TextView tv_forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d("msg", "onCreate");

        firebaseAuth = FirebaseAuth.getInstance();
        AmessengerApplication.appfirebaseAuth = firebaseAuth;
        if (AmessengerApplication.appfirebaseAuth.getCurrentUser()!= null) {
            Log.d("msggggg",""+AmessengerApplication.appfirebaseAuth);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AllUserInfo").child(Objects.requireNonNull(AmessengerApplication.appfirebaseAuth.getCurrentUser()).getUid());
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("online", "offline");
            if (AmessengerApplication.appfirebaseAuth.getCurrentUser() != null)
                databaseReference.updateChildren(hashMap);
        }
        //AmessengerApplication.appfirebaseUser = null;

        animation = AnimationUtils.loadAnimation(this, R.anim.shake_horizontal);
        etEmail = (EditText) findViewById(R.id.email);
        relativeLayoutEdittextPassword = (RelativeLayout) findViewById(R.id.pwd);
        etPassword = (EditText) findViewById(R.id.password);
        showHidePasswordButton = (ImageButton) findViewById(R.id.show_hide);
        cbSaveUser = (CheckBox) findViewById(R.id.save_user);
        tv_forgot_password = (TextView) findViewById(R.id.tv_forgot_password);
        progressDialog = new ProgressDialog(this, android.app.AlertDialog.THEME_HOLO_DARK);
        progressDialog.setMessage("Please wait.....");
        progressDialog.setCancelable(false);

        sharedPreferences = getSharedPreferences("SP",MODE_PRIVATE);

        if(sharedPreferences.getBoolean("cbSaveUser",false)){
            cbSaveUser.setChecked(true);
            etEmail.setText(sharedPreferences.getString("username",""));
            etPassword.setText(sharedPreferences.getString("password",""));
        }
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                login(findViewById(R.id.login));
                return false;
            }
        });

        tv_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if(email.equalsIgnoreCase("")){
                    Toast.makeText(LoginActivity.this, "Please Enter Your Email Id and then Retry", Toast.LENGTH_SHORT).show();
                }else {
                    forgotPasswordAlertDialog(email);
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("msg", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("msg", "onResume");
        if (AmessengerApplication.appfirebaseUser!=null){
            String u_id = AmessengerApplication.appfirebaseUser.getUid();
            Intent intent = new Intent(LoginActivity.this, FriendListActivity.class);
            intent.putExtra("uid", u_id);
            startActivity(intent);
            finish();
        }
    }

    public void login(View view) {
        final String email, password;
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        if (email.equals("") || password.equals("")) {
            etEmail.startAnimation(animation);
            relativeLayoutEdittextPassword.startAnimation(animation);
            //Toast.makeText(LoginActivity.this, "Fill", Toast.LENGTH_SHORT).show();
        } else if (checkInternet()){
            progressDialog.show();
            if(cbSaveUser.isChecked()){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("cbSaveUser",true);
                editor.putString("username",email);
                editor.putString("password",password);
                editor.apply();
            }
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        AmessengerApplication.appfirebaseUser = firebaseUser;
                        String uid = firebaseUser.getUid();
                        Intent intent = new Intent(LoginActivity.this, FriendListActivity.class);
                        intent.putExtra("uid", uid);
                        progressDialog.dismiss();
                        startActivity(intent);
                        finish();
                    } else {
                        count++;
                        progressDialog.dismiss();
                        etPassword.setText("");
                        Toast.makeText(LoginActivity.this, "Wrong Email or Password!", Toast.LENGTH_SHORT).show();
                        if (count >= 3) {
                            forgotPasswordAlertDialog(email);
                        }
                    }
                }
            });
        }
        else {
            Snackbar.make(findViewById(R.id.pwd),"No Internet",Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).setActionTextColor(Color.RED).show();
        }
    }

    private void forgotPasswordAlertDialog(final String email) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this, R.style.MyDatePickerStyle);
        //alertDialog.setTitle("Forgot password");
        alertDialog.setMessage("Do you want to reset your password?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.sendPasswordResetEmail(email);
                Toast.makeText(LoginActivity.this, "Password reset mail will be sent to your Email in few minutes.", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }

    public void register(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("msg login", "onDestroy");
    }

    public boolean checkInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        } else
            return false ;
    }
    public void showHidePassword(View view) {
        if (show) {
            showHidePasswordButton.setImageResource(R.drawable.ic_visibility_white_24dp);
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD|InputType.TYPE_CLASS_NUMBER);
            show=false;
        }else {
            showHidePasswordButton.setImageResource(R.drawable.ic_visibility_off_white_24dp);
            etPassword.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD| InputType.TYPE_CLASS_NUMBER);
            show=true;
        }
    }
}
