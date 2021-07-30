package com.example.lostandfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Profile extends AppCompatActivity {

    private TextView textName, textMobile, textMail;
    private ImageView userImage;
    private Button updateButton;
    private String userMail;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textName = findViewById(R.id.profile_name);
        textMobile = findViewById(R.id.profile_mobile);
        textMail = findViewById(R.id.profile_mail);
        userImage = findViewById(R.id.profile_imageView);
        updateButton = findViewById(R.id.profile_updateButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        textMail.setText(firebaseUser.getEmail());
        userMail = textMail.getText().toString();

        firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("users");
        collectionReference.whereEqualTo("Mail", userMail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    String t_name = (String) doc.get("Name");
                    String t_mail = (String) doc.get("Mail");
                    String t_contact = (String) doc.get("Contact");
                    String t_image = (String) doc.get("Image");

                    String displayName = "Name : "+t_name;
                    String displayMail = "Mail : "+t_mail;
                    String displayContact = "Mobile : "+t_contact;
                    textName.setText(displayName);
                    textMobile.setText(displayContact);
                    textMail.setText(displayMail);

                    if (!t_image.isEmpty()) {
                        Glide.with(Profile.this).load(t_image).into(userImage);
                    }

                } else {
                    Toast.makeText(Profile.this, "Failed to fetch", Toast.LENGTH_SHORT).show();
                }
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), UpdateProfile.class));
            }
        });


    }


}