package com.example.lostandfound;

import com.google.firebase.firestore.Exclude;

public class ProductsModel implements Comparable<ProductsModel>{
    public String ItemName;
    public String Date;
    public String Time;
    public String Document;
    public String ItemType;
    public String Message;

    @Exclude
    public String getDocumentId() {
        return Document;
    }

    public void setDocumentId(String documentId) {
        this.Document = Document;
    }

    public ProductsModel(String subject, String date, String time, String Document, String itemType, String message) {
        this.ItemName = subject;
        this.Date = date;
        this.Time = time;
        this.Document = Document;
        this.ItemType = itemType;
        this.Message = message;
    }

    @Override
    public int compareTo(ProductsModel o) {
        return Date.compareTo(o.Date);
    }

}
