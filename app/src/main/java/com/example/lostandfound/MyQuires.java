 package com.example.lostandfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class MyQuires extends AppCompatActivity {

    String mailId;
    private FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter mAdapter;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_quires);


        recyclerView = (RecyclerView) findViewById(R.id.myquery_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);


        Toolbar tb = (Toolbar) findViewById(R.id.myquery_toolbar);
        tb.setTitle("My Quires");
        setSupportActionBar(tb);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        mailId = firebaseAuth.getCurrentUser().getEmail();

        final ArrayList<ProductsModel> cmps = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        CollectionReference complaints = db.collection("Item_Details");

        complaints.whereEqualTo("Mail", mailId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                    mAdapter = new MyAdapter(MyQuires.this, cmps);
                    recyclerView.setAdapter(mAdapter);
                } else {
                    Toast.makeText(MyQuires.this, "Failed to fetch Complaints", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int item_id = item.getItemId();

        if (item_id == R.id.menu_home) {
            Intent intent = new Intent(MyQuires.this, ListQueries.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (item_id == R.id.menu_profile) {
            startActivity(new Intent(getApplicationContext(), Profile.class));
        } else if (item_id == R.id.menu_my_quries)
            startActivity(new Intent(getApplicationContext(), MyQuires.class));

        return true;

    }
}