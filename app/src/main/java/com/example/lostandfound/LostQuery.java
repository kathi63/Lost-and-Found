package com.example.lostandfound;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class LostQuery extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final int READ_PERMISSION_CODE = 1;
    private static final int PICK_IMAGE_REQUEST_CODE = 2;
    public static final String TAG = "TAG";
    EditText lItemName, lMessage, lContact, lLocation;
    TextView editDate, editTime;
    String spinnerSelection, mailId, imageAddress;
    DatePickerDialog.OnDateSetListener setListener;
    TimePickerDialog timePickerDialog;
    TextView l_textChooseImage;
    private ImageView userImage;
    private Button lSubmitButton;

    private ProgressDialog progressDialog;
    private Uri imageUri = null, imageLocationpath;
    final int IMAGE_REQUEST=71;
    private Bitmap compressed;
    private Compressor compressor;
    int hour, min;

    private FirebaseFirestore fStore, objectfirebaseFirestore;
    FirebaseAuth fAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference, objectStorageReference;
    FirebaseUser firebaseUser;
    String userID;

    ////Notification



    // private FirebaseDatabase db = FirebaseDatabase.getInstance();
    //  private DatabaseReference dbroot = db.getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_query);

        progressDialog = new ProgressDialog(this);
        userImage = findViewById(R.id.l_chooseImage);
        lItemName = findViewById(R.id.l_Item_name);
        lMessage = findViewById(R.id.l_message);
        lContact = findViewById(R.id.l_contact);
        lLocation = findViewById(R.id.l_location);
        lSubmitButton = findViewById(R.id.l_SubmitButton);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        firebaseUser = fAuth.getCurrentUser();

        // notifications for all
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        objectStorageReference = FirebaseStorage.getInstance().getReference("image");
        objectfirebaseFirestore = FirebaseFirestore.getInstance();

        mailId = fAuth.getCurrentUser().getEmail();
        userID = fAuth.getCurrentUser().getUid();

        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(LostQuery.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        editDate = findViewById(R.id.l_date);
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(LostQuery.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month+1;
                        String date = day+"/"+month+"/"+year;
                        editDate.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });

        editTime = findViewById(R.id.l_time);
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(LostQuery.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        hour = hourOfDay;
                        min = minute;
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.set(0,0,0, hour, min);
                        editTime.setText(DateFormat.format("hh:mm aa", calendar1));
                    }
                },12,0,false);
                timePickerDialog.updateTime(hour,min);
                timePickerDialog.show();
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        lSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setMessage("Storing Data...");
                progressDialog.show();

                //Getting current date and time

                Calendar calendar1 = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String Date = simpleDateFormat.format(calendar1.getTime());
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
                String Time = simpleTimeFormat.format(calendar1.getTime());

                String Type = "Lost Item: ";
                String Item_Name = lItemName.getText().toString();
                String Message = lMessage.getText().toString();
                String itemDate = editDate.getText().toString();
                String itemTime = editTime.getText().toString();
                String Contact = lContact.getText().toString();
                String Location = lLocation.getText().toString();
                String Category = spinnerSelection;
                String Mail = mailId;
                String emptyAddress = "";

                String notificationTitle = Type+Item_Name;
                String notificationMessage = Message;


                if(TextUtils.isEmpty(Item_Name)){
                    lItemName.setError("Item name is Required.");
                    lItemName.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(Message)){
                    lMessage.setError("Message is Required.");
                    lMessage.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(Date)){
                    editDate.setError("Date is Required.");
                    editDate.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(Time)){
                    editTime.setError("Time is Required.");
                    editTime.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(Contact)){
                    lContact.setError("Mobile number is Required.");
                    lContact.requestFocus();
                    return;
                }

                if(Contact.length() != 10){
                    lContact.setError("Enter 10 digits number.");
                    lContact.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(Location)){
                    lLocation.setError("Location is Required.");
                    lLocation.requestFocus();
                    return;
                }

                if (imageUri != null) {

                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("ItemType", Type);
                    userMap.put("Category", Category);
                    userMap.put("ItemName", Item_Name);
                    userMap.put("Message", Message);
                    userMap.put("itemDate", itemDate);
                    userMap.put("itemTime", itemTime);
                    userMap.put("Contact", Contact);
                    userMap.put("Location", Location);
                    userMap.put("Mail", Mail);
                    userMap.put("Image", imageAddress);
                    userMap.put("Time", Time);
                    userMap.put("Date", Date);
                    userMap.put("Comment", "");

                    fStore.collection("Item_Details").add(userMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            editDate.setText("");
                            editTime.setText("");
                            lItemName.setText("");
                            lMessage.setText("");
                            lContact.setText("");
                            lLocation.setText("");

                            Toast.makeText(getApplicationContext(), "Lost Query is created.", Toast.LENGTH_SHORT).show();

                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all", notificationTitle, notificationMessage, getApplicationContext(), LostQuery.this);
                            notificationsSender.SendNotifications();



                        }
                    });
                    Intent intent = new Intent(LostQuery.this, ListQueries.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("ItemType", Type);
                    userMap.put("Category", Category);
                    userMap.put("ItemName", Item_Name);
                    userMap.put("Message", Message);
                    userMap.put("itemDate", itemDate);
                    userMap.put("itemTime", itemTime);
                    userMap.put("Contact", Contact);
                    userMap.put("Location", Location);
                    userMap.put("Mail", Mail);
                    userMap.put("Image", emptyAddress);
                    userMap.put("Time", Time);
                    userMap.put("Date", Date);
                    userMap.put("Comment", "");

                    fStore.collection("Item_Details").add(userMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            editDate.setText("");
                            editTime.setText("");
                            lItemName.setText("");
                            lMessage.setText("");
                            lContact.setText("");
                            lLocation.setText("");


                            Toast.makeText(getApplicationContext(), "Lost Query is created.", Toast.LENGTH_SHORT).show();
                            FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all", notificationTitle, notificationMessage, getApplicationContext(), LostQuery.this);
                            notificationsSender.SendNotifications();
                        }
                    });
                    Intent intent = new Intent(LostQuery.this, ListQueries.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        });



    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
        spinnerSelection = text;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void selectImage() {
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

                ProgressDialog pd = new ProgressDialog(LostQuery.this);
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
                            Toast.makeText(LostQuery.this, "Failed to upload", Toast.LENGTH_SHORT).show();
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


}
