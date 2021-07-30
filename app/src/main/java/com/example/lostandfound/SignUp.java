package com.example.lostandfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    public static final String TAG = "TAG";
  //  TextInputLayout
    TextInputEditText signupName, signupMail, signupPassword, signupContact;
    private Button signupButton;
    TextView sCreateLogin;
    ProgressBar progressBar;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    String userID;

  //  private FirebaseDatabase db = FirebaseDatabase.getInstance();
  //  private DatabaseReference dbroot = db.getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupName = (TextInputEditText) findViewById(R.id.edit_sname);
        signupMail = (TextInputEditText) findViewById(R.id.edit_smail);
        signupPassword = (TextInputEditText) findViewById(R.id.edit_spassword);
        signupContact = (TextInputEditText) findViewById(R.id.edit_scontact);
        signupButton = findViewById(R.id.signup_button);
        sCreateLogin = findViewById(R.id.screatelogin);
        progressBar = findViewById(R.id.progressBar);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), ListQueries.class));
            finish();
        }

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isConnected(SignUp.this)){
                    showCustomDialog();
                    Toast.makeText(getApplicationContext(), "Please check your network connection and try again", Toast.LENGTH_LONG).show();
                } else {

                    final String email = signupMail.getText().toString().trim();
                    String password = signupPassword.getText().toString().trim();
                    final String fullName = signupName.getText().toString();
                    final String phone = signupContact.getText().toString();
                    final String image = "";
                    String nitcMail = email;

                    if (TextUtils.isEmpty(fullName)) {
                        signupName.setError("Full name is Required.");
                        signupName.requestFocus();
                        return;
                    }


                    if (TextUtils.isEmpty(email)) {
                        signupMail.setError("Email is Required.");
                        signupMail.requestFocus();
                        return;
                    }

                    if (!nitcMail.endsWith("@nitc.ac.in")){
                    signupMail.setError("Valid nitc mail is Required.");
                    signupMail.requestFocus();
                    return;
                    }

                    if (TextUtils.isEmpty(password)) {
                        signupPassword.setError("Password is Required.");
                        signupPassword.requestFocus();
                        return;
                    }

                    if (password.length() < 8) {
                        signupPassword.setError("Password Must be >= 8 Characters.");
                        signupPassword.requestFocus();
                        return;
                    }

                    if (TextUtils.isEmpty(phone)) {
                        signupContact.setError("Enter 10 digits mobile number.");
                        signupContact.requestFocus();
                        return;
                    }

                    if (phone.length() != 10) {
                        signupContact.setError("Enter 10 digits mobile number");
                        signupContact.requestFocus();
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    // register the user in firebase

                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // send verification link

                                FirebaseUser fuser = fAuth.getCurrentUser();
                                fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SignUp.this, "Verification Email has been sent.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUp.this, "Invalid mail id", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Toast.makeText(SignUp.this, "User Created.", Toast.LENGTH_SHORT).show();
                                userID = fAuth.getCurrentUser().getUid();

                                DocumentReference documentReference = fStore.collection("users").document(userID);

                                Map<String, Object> user = new HashMap<>();
                                user.put("Name", fullName);
                                user.put("Mail", email);
                                user.put("Contact", phone);
                                user.put("Image", image);

                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: user Profile is created for " + userID);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: " + e.toString());
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });

        sCreateLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isConnected(SignUp.this)){
                    showCustomDialog();
                    Toast.makeText(getApplicationContext(), "Please check your network connection and try again", Toast.LENGTH_LONG).show();
                } else
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }

    // internet connection checking

    private boolean isConnected(SignUp listQueries) {
        ConnectivityManager connectivityManager = (ConnectivityManager) listQueries.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wificonn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileconn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wificonn != null && wificonn.isConnected()) || (mobileconn != null && mobileconn.isConnected())) {
            return true;
        } else {
            return false;
        }
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
        builder.setMessage("Please connect to the internet to proceed further").setCancelable(false).setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

    }
}