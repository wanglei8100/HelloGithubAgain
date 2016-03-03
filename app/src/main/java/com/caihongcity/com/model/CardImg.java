package com.caihongcity.com.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/10/29 0029.
 */
public class CardImg implements Serializable{
    private String type;
    private String imageUrl;

    private static final long serialVersionUID = 1L;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
