package com.dian.sixggroup.common;

/**
 * @Author: tanjun
 * @CreateTime: 2023-03-07 15:30
 */
public class Packet {
    public static String KEY_CODE = "code";

    public static String KEY_POINT = "points";
    public static String KEY_IMAGE = "imageUri";
    private int code;
    private String points;
    private String imageUri;

    public int getCode() {
        return code;
    }

    public String getPoints() {
        return points;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Packet(int code, String points, String imageUri) {
        this.code = code;
        this.points = points;
        this.imageUri = imageUri;
    }

    public Packet() {
        this.code = -1;
        this.points = "";
        this.imageUri = "";
    }
    @Override
    public String toString() {
        return String.format("code:%d, data:%s", code, points);
    }
}
