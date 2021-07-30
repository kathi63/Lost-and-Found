package com.example.lostandfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ItemInformation extends AppCompatActivity {

    private FirebaseFirestore db, firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    FirebaseAuth fAuth;

    private StorageReference storageReference;
    private DocumentReference documentReference;

    private ImageView imageView;
    private TextView textItemName, textMessage, textItemType;
    private FloatingActionButton commentButton, deleteButton;
    String mailId;
    String querySentMailId;
    String previousComment = "";
    String latestComment;
    EditText queryMail;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_information);



        textItemType = findViewById(R.id.info_itemtype);
        textItemName = findViewById(R.id.info_namecat);
        textMessage = findViewById(R.id.info_message);
        imageView = findViewById(R.id.info_image);
        commentButton = findViewById(R.id.info_pushComment);
        deleteButton = findViewById(R.id.info_delete_button);

        Toolbar tb1 = (Toolbar) findViewById(R.id.info_toolbar1);
        tb1.setTitle("Item information");
        setSupportActionBar(tb1);

        fAuth = FirebaseAuth.getInstance();
        mailId = fAuth.getCurrentUser().getEmail();

        Intent intent = getIntent();
        String S_name = intent.getStringExtra("DocId");

        firebaseFirestore = FirebaseFirestore.getInstance();


        db = FirebaseFirestore.getInstance();
        db.collection("Item_Details").document(S_name).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String t_ItemType = (String) documentSnapshot.get("ItemType");
                String t_ItemName = (String) documentSnapshot.get("ItemName");
                String t_Category = (String) documentSnapshot.get("Category");
                String t_Message = (String) documentSnapshot.get("Message");
                String t_itemDate = (String) documentSnapshot.get("itemDate");
                String t_itemTime = (String) documentSnapshot.get("itemTime");
                String t_Location = (String) documentSnapshot.get("Location");
                String t_Contact = (String) documentSnapshot.get("Contact");
                String t_Mail = (String) documentSnapshot.get("Mail");
                String t_image = (String) documentSnapshot.get("Image");
                String t_comment = (String) documentSnapshot.get("Comment");
                querySentMailId = t_Mail;

                textItemType.setText(t_ItemType);
                textItemName.setText(t_ItemName + "\n( " + t_Category + " )");
                if (!t_comment.isEmpty()) {
                    previousComment = t_comment;
                    textMessage.setText(t_Message + " at " + t_itemTime + " " + t_itemDate + "\nLocation : " + t_Location + "\nContact : " + t_Contact + "\nEmail : " + t_Mail + "\nComments : \n" + t_comment);
                } else {
                    textMessage.setText(t_Message + " at " + t_itemTime + " " + t_itemDate + "\nLocation : " + t_Location + "\nContact : " + t_Contact + "\nEmail : " + t_Mail + "\nComments : No Comments");
                }
                //storageReference = firebaseStorage.getReferenceFromUrl(t_image);

                if (!t_image.isEmpty()) {
                    Glide.with(ItemInformation.this).load(t_image).into(imageView);
                }

                if (mailId.equals(querySentMailId)) {
                    deleteButton.setVisibility(View.VISIBLE);
                } else {
                    deleteButton.setVisibility(View.INVISIBLE);
                }


            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ItemInformation.this, "Failed to fetch", Toast.LENGTH_SHORT).show();
            }
        });




        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText enterComment = new EditText(v.getContext());
                final AlertDialog.Builder commentDialog = new AlertDialog.Builder(v.getContext());
                commentDialog.setTitle("Reply to this Item ?");
                commentDialog.setMessage("Enter Your Comment below given place.");
                commentDialog.setView(enterComment);

                commentDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        latestComment = enterComment.getText().toString();
                        Toast.makeText(getApplicationContext(), "Comment posted successfully", Toast.LENGTH_SHORT).show();
                        updateComment(previousComment, mailId, latestComment, S_name);
                    }
                });

                commentDialog.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getApplicationContext(), "Failed to post", Toast.LENGTH_SHORT).show();
                    }
                });
                commentDialog.create().show();

            }
        });


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseFirestore.getInstance().collection("Item_Details").document(S_name).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getApplicationContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ListQueries.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void updateComment(String pComment, String currMail, String currComment, String docId) {

        String updatedComment = pComment +"\n\t\t------------------------------------\n"+currComment +"\tsend by "+currMail;

        final DocumentReference docs = db.collection("Item_Details").document(docId);
        docs.update("Comment", updatedComment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(getApplicationContext(), "Comment posted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ItemInformation.this, ListQueries.class);
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