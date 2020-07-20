package com.alvin.projekuas.entity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Report {
    private String title;
    private String desc;
    private String address;
    private String photo;
    private String user_id;
    private Timestamp created_at;
    private GeoPoint latlong;

    public Report() {
    }

    public Report(String title, String desc, String address, String photo, String user_id, Timestamp created_at, GeoPoint latlong) {
        this.title = title;
        this.desc = desc;
        this.address = address;
        this.photo = photo;
        this.user_id = user_id;
        this.created_at = created_at;
        this.latlong = latlong;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoto() {
        return photo;
    }

    public String getUser_id() {
        return user_id;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public GeoPoint getLatlong() {
        return latlong;
    }
}
