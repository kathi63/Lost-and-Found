package com.example.lostandfound;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

import static com.example.lostandfound.LostQuery.TAG;

public class UpdateProfile extends AppCompatActivity{

    private EditText textName, textMobile;
    private TextView textMail;
    private ImageView userImage;
    private Button updateButton;
    private String userMail, userId;
    private String imageAddress;

    private Uri imageUri;
    final int IMAGE_REQUEST=71;
    private Bitmap compressed;
    private Compressor compressor;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore, fStore;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    DocumentReference documentReference;

    private FirebaseStorage storage;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        textName = findViewById(R.id.update_name);
        textMobile = findViewById(R.id.update_mobile);
        textMail = findViewById(R.id.update_mail);
        userImage = findViewById(R.id.update_imageView);
        updateButton = findViewById(R.id.update_submit);
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        // uploading image storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        documentReference = db.collection("users").document(userId);


        //textMail.setText(firebaseUser.getEmail());

        userMail = firebaseAuth.getCurrentUser().getEmail();
        textMail.setText(userMail);
        firebaseFirestore = FirebaseFirestore.getInstance();

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    String nameResult = task.getResult().getString("Name");
                    String contactResult = task.getResult().getString("Contact");
                    String imageResult = task.getResult().getString("Image");
                 //   String mailResult = task.getResult().getString("Mail");

                    textName.setText(nameResult);
                    textMobile.setText(contactResult);
                   // textMail.setText(mailResult);
                    if (!imageResult.isEmpty()) {
                        Glide.with(UpdateProfile.this).load(imageResult).into(userImage);
                    } else
                        Toast.makeText(getApplicationContext(), "No image", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Toast.makeText(getApplicationContext(), "Update your profile", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }



    public void UpdateProfile(View view) {
        progressDialog.setMessage("Storing Data...");
        progressDialog.show();

        String u_name = textName.getText().toString();
        String u_contact = textMobile.getText().toString();
        String u_image;
        if (imageAddress != null) {
            u_image = imageAddress;

            final DocumentReference docs = db.collection("users").document(userId);

            docs.update("Name", u_name, "Contact", u_contact, "Image", u_image).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "Profile updated Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateProfile.this, Profile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            final DocumentReference docs = db.collection("users").document(userId);

            docs.update("Name", u_name, "Contact", u_contact).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "Profile updated Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateProfile.this, Profile.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST  && resultCode ==RESULT_OK && data != null && data.getData() !=null) {
            try {
                imageUri = data.getData();
                imageAddress = imageUri.toString();
                Bitmap objectBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                userImage.setImageBitmap(objectBitmap);
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void uploadImage() {
        try {
            if (imageUri!=null) {

                ProgressDialog pd = new ProgressDialog(UpdateProfile.this);
                pd.setTitle("Uploading image");
                pd.show();

                String randomKey = UUID.randomUUID().toString();
                String nameOfImage = randomKey+"."+getExtension(imageUri);
                StorageReference imageRef = storageReference.child("image/"+nameOfImage);

                UploadTask objectUploadTask = imageRef.putFile(imageUri);
                objectUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            imageAddress = task.getResult().toString();
                            pd.dismiss();
                        } else if (!task.isSuccessful()) {
                            Toast.makeText(UpdateProfile.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        } catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getExtension(Uri uri) {
        try {
            ContentResolver objectContentResolver = getContentResolver();
            MimeTypeMap objectMimeTypeMap = MimeTypeMap.getSingleton();
            return objectMimeTypeMap.getExtensionFromMimeType(objectContentResolver.getType(uri));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        
    }

}