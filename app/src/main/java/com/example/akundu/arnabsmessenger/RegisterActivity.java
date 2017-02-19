package com.example.akundu.arnabsmessenger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    ProgressDialog progressDialog;
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private String email;
    private String password;
    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = (EditText) findViewById(R.id.name);
        etEmail = (EditText) findViewById(R.id.email);
        etPassword = (EditText) findViewById(R.id.password);
        etConfirmPassword = (EditText) findViewById(R.id.address);
        progressDialog = new ProgressDialog(this, AlertDialog.THEME_HOLO_DARK);
        progressDialog.setMessage("Please wait.....");
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        animation = AnimationUtils.loadAnimation(this,R.anim.shake_horizontal);

        etConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                register(findViewById(R.id.reg));
                return false;
            }
        });
    }

    public void register(View view) {

        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        if (etName.getText().toString().equals("")) {
            etName.startAnimation(animation);
            etName.requestFocus();
        }else if (email.equals("")) {
            etEmail.startAnimation(animation);
            etEmail.requestFocus();
        }else if (password.equals("")||password.length()<6) {
            etPassword.startAnimation(animation);
            etPassword.requestFocus();
        }else if (etConfirmPassword.getText().toString().equals("")||password.length()<6) {
            etConfirmPassword.startAnimation(animation);
            etConfirmPassword.requestFocus();
        } else if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            Toast.makeText(this, "Password mismatch", Toast.LENGTH_SHORT).show();
            etPassword.setText("");
            etConfirmPassword.setText("");
        } else {
            progressDialog.show();
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    firebaseUser = firebaseAuth.getCurrentUser();
                                    UserInformation userInformation = new UserInformation(etName.getText().toString(), etEmail.getText().toString(),firebaseUser.getUid(),"offline");
                                    databaseReference.child("AllUserInfo").child(firebaseUser.getUid()).setValue(userInformation);
                                } else {
                                    Log.d("msg", "some error");
                                    Toast.makeText(RegisterActivity.this, "Wrong Email or Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Not done", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
    }
}
