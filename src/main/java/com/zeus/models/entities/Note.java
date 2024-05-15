package com.zeus.models.entities;

import com.github.f4b6a3.ulid.UlidCreator;
import com.google.gson.annotations.SerializedName;

public class Note {
    @SerializedName(value = "Id")
    private String id;
    @SerializedName(value = "UserId")
    private String userId;
    @SerializedName(value = "Title")
    private String title;
    @SerializedName(value = "Content")
    private String content;
    @SerializedName(value = "IsDeleted")
    private boolean isDeleted;

    public Note(String title, String content, String userId) {
        this.id = UlidCreator.getUlid().toString();
        this.title = title;
        this.content = content;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}