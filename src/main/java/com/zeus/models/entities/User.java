package com.zeus.models.entities;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName(value = "Id")
    private String id;
    @SerializedName(value = "Email")
    private String email;
    @SerializedName(value = "UserName")
    private String userName;
    @SerializedName(value = "Password")
    private String password;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
