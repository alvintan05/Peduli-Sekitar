package com.alvin.projekuas.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable {

    private String name, email, photo, phone, address;

    public Profile() {
    }

    public Profile(String name, String email, String photo, String phone, String address) {
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.phone = phone;
        this.address = address;
    }

    public Profile(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }


    protected Profile(Parcel in) {
        name = in.readString();
        email = in.readString();
        photo = in.readString();
        phone = in.readString();
        address = in.readString();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoto() {
        return photo;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(photo);
        dest.writeString(phone);
        dest.writeString(address);
    }
}
