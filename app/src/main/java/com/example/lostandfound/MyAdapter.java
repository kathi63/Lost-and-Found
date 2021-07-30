package com.example.lostandfound;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    ArrayList<ProductsModel> cmps;
    Context context;
    public String Sid;

    public MyAdapter(Context context, ArrayList<ProductsModel> cmps) {
        this.context = context;
        this.cmps = cmps;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.complaint_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.text_subject.setText(cmps.get(position).ItemType + cmps.get(position).ItemName);
        holder.text_date.setText(cmps.get(position).Message);
        holder.textTime.setText(cmps.get(position).Date + "  "+cmps.get(position).Time);
        Sid = cmps.get(position).Document;
    }

    @Override
    public int getItemCount() {
        return cmps.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView text_subject;
        TextView text_date, textTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            text_subject = (TextView) itemView.findViewById(R.id.text_subject_ii);
            text_date = (TextView)itemView.findViewById(R.id.text_date_ii);
            textTime = (TextView)itemView.findViewById(R.id.text_time);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Toast.makeText(v.getContext(), "Complaint clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(v.getContext(), ItemInformation.class);
            intent.putExtra("DocId", cmps.get(getAdapterPosition()).Document);
            v.getContext().startActivity(intent);


        }
    }
}
