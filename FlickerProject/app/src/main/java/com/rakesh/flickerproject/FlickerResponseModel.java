package com.rakesh.flickerproject;

/**
 * Created by girish on 7/5/2016.
 */
public class FlickerResponseModel {
    private String id;

    public String getPhotoId() {
        return id;
    }

    public void setPhotoId(String photoId) {
        this.id = id;
    }

    public int getFarm() {
        return farm;
    }

    public void setFarm(int farm) {
        this.farm = farm;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }


    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    private String secret;
    private int farm;
    private String title;
    private String server;
}
