package com.example.vrinda.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Registeration extends AppCompatActivity {
    private EditText fullname;
    private EditText email;
    private EditText phoneno;
    private EditText emg;
    private EditText altemg;
    private EditText password;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        fullname = (EditText) findViewById(R.id.name);
        email= (EditText) findViewById(R.id.emailRegister);
        phoneno = (EditText) findViewById(R.id.userphone);
        emg = (EditText) findViewById(R.id.emg1);
        altemg= (EditText) findViewById(R.id.emg2);
        password=(EditText) findViewById(R.id.passwordRegister);
        mAuth = FirebaseAuth.getInstance();
    }


    public void btnRegistration_Click(View v)
    {
        final ProgressDialog progressDialog = ProgressDialog.show(Registeration.this, "Please wait", "Processing", true);

        final String username = fullname.getText().toString().trim();
        final String useremail= email.getText().toString().trim();
        final String userphone = phoneno.getText().toString().trim();
        final String emgphone = emg.getText().toString().trim();
        final String altemgphone = altemg.getText().toString().trim();
        final String pwd = password.getText().toString().trim();

        if(username.isEmpty())
        {
            fullname.setError("Full Name required");
            fullname.requestFocus();
            return;
        }

        if(useremail.isEmpty())
        {
            email.setError("Email required");
            email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(useremail).matches())
        {
            email.setError("Enter valid email address");
            email.requestFocus();
            return;
        }

        if(pwd.isEmpty())
        {
            password.setError("Enter password");
            password.requestFocus();
            return;
        }

        if(pwd.length()<6)
        {
            password.setError("Password should be atleast 6 characters long.");
            password.requestFocus();
            return;
        }

        if(userphone.isEmpty())
        {
            phoneno.setError("Phone number required");
            phoneno.requestFocus();
            return;
        }

        if(userphone.length()!=10)
        {
            phoneno.setError("Enter a valid phone number");
            phoneno.requestFocus();
            return;
        }

        if(emgphone.isEmpty())
        {
            emg.setError("Phone number required");
            emg.requestFocus();
            return;
        }

        if(emgphone.length()!=10)
        {
            emg.setError("Enter a valid phone number");
            emg.requestFocus();
            return;
        }

        if(altemgphone.isEmpty())
        {
            altemg.setError("Phone number required");
            altemg.requestFocus();
            return;
        }

        if(altemgphone.length()!=10)
        {
            altemg.setError("Enter a valid phone number");
            altemg.requestFocus();
            return;
        }

        (mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()))
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            User user = new User(username, useremail, userphone, emgphone, altemgphone);

                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if(firebaseUser != null)
                            {
                                FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(Registeration.this,"Registration successful", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(Registeration.this, Login.class));
                                        }
                                        else
                                        {
                                            Toast.makeText(Registeration.this, "Unsuccessful Registration", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(Registeration.this, Menu.class));
                                        }
                                    }
                                });

                            }
                            else
                            {
                                Toast.makeText(Registeration.this, "Unsuccessful", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(Registeration.this, Menu.class));
                            }
                        }
                        else
                        {
                            Toast.makeText(Registeration.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Registeration.this, Menu.class));
                        }
                    }
                });
    }


}
