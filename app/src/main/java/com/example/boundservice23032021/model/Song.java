package com.example.boundservice23032021.model;

import java.io.Serializable;

public class Song implements Serializable {
    String title;
    int data;
    long duration;

    public Song(String title, int data, long duration) {
        this.title = title;
        this.data = data;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
