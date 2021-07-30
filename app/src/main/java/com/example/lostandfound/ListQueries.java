package com.example.lostandfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static android.net.ConnectivityManager.*;

public class ListQueries extends AppCompatActivity {

    String email;
    private FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter mAdapter;
    TextView testing;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();

    //Backpress keys
    private long backPressedTime;
    private Toast backToast;

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            finish();
            return;
        } else {
            backToast = Toast.makeText(getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_queries);

        if(fAuth.getCurrentUser() == null){
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        tb.setTitle("List Quires");
        setSupportActionBar(tb);

        FloatingActionButton fbutton1;
        FloatingActionButton fbutton2;
        fbutton1 = findViewById(R.id.floating_button1);
        fbutton2 = findViewById(R.id.floating_button2);

        fbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isConnected(ListQueries.this)){
                    showCustomDialog();
                    Toast.makeText(getApplicationContext(), "Please check your network connection and try again", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(ListQueries.this, LostQuery.class);
                    startActivity(intent);
                }
            }
        });

        fbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isConnected(ListQueries.this)){
                    showCustomDialog();
                    Toast.makeText(getApplicationContext(), "Please check your network connection and try again", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(ListQueries.this, FoundQuery.class);
                    startActivity(intent);
                }
            }
        });

        final ArrayList<ProductsModel> cmps = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        CollectionReference complaints = db.collection("Item_Details");

        complaints.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String type = (String) document.get("ItemType");
                        String message = (String) document.get("Message");
                        String subject = (String) document.get("ItemName");
                        String date = (String) document.get("Date");
                        String time = (String) document.get("Time");
                        String documentId = (String) document.getId();
                        ProductsModel cmp = new ProductsModel(subject, date, time, documentId, type, message);
                        cmps.add(cmp);
                    }
                    Collections.sort(cmps, new cmpComparator());
                    mAdapter = new MyAdapter(ListQueries.this, cmps);
                    recyclerView.setAdapter(mAdapter);
                } else {
                    Toast.makeText(ListQueries.this, "Failed to fetch Complaints", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();

        if (item_id == R.id.menu1_home) {
            if (!isConnected(ListQueries.this)){
                showCustomDialog();
                Toast.makeText(getApplicationContext(), "Please check your network connection and try again", Toast.LENGTH_LONG).show();
            }else {
                Intent intent = new Intent(ListQueries.this, ListQueries.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
    } else if (item_id == R.id.menu1_profile) {
            if (!isConnected(ListQueries.this)){
                showCustomDialog();
                Toast.makeText(getApplicationContext(), "Please check your network connection and try again", Toast.LENGTH_LONG).show();
            } else {
                 startActivity(new Intent(getApplicationContext(), Profile.class));
            }
        } else if (item_id == R.id.menu1_my_quries)
            if (!isConnected(ListQueries.this)){
                showCustomDialog();
                Toast.makeText(getApplicationContext(), "Please check your network connection and try again", Toast.LENGTH_LONG).show();
            } else {
                 startActivity(new Intent(getApplicationContext(), MyQuires.class));
            }
        else if (item_id == R.id.menu1_logout) {
            if (!isConnected(ListQueries.this)){
                showCustomDialog();
                Toast.makeText(getApplicationContext(), "Please check your network connection and try again", Toast.LENGTH_LONG).show();
            } else {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ListQueries.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } else if (item_id == R.id.menu1_share) {
            if (!isConnected(ListQueries.this)){
                showCustomDialog();
                Toast.makeText(getApplicationContext(), "Please check your network connection and try again", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "Lost and Found in NITC Android Application Try Now "+ "https://drive.google.com/drive/folders/1WWAbUEWAloNEdgHKeDJsbXmGpz08RvP1?usp=sharing");
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, "ShareVia"));
            }
        }
        return true;
    }

    // internet connection checking

    private boolean isConnected(ListQueries listQueries) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ListQueries.this);
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