package com.example.vrinda.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private EditText emailid;
    private EditText pwd;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailid = findViewById(R.id.emailLogin);
        pwd = findViewById(R.id.passwordLogin);
        mAuth = FirebaseAuth.getInstance();
    }

    public void userButton_Click(View v)
    {
        final ProgressDialog progressDialog = ProgressDialog.show(Login.this, "Please Wait", "Processing", true);
        (mAuth.signInWithEmailAndPassword(emailid.getText().toString(), pwd.getText().toString()))
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Login.this, Home.class));
                        }
                        else
                        {
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, Menu.class));
                        }
                    }
                });
    }
}
