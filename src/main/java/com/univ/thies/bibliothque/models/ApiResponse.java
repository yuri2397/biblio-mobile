package com.univ.thies.bibliothque.models;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

public class ApiResponse {
    private boolean error;

    @Nullable
    @SerializedName("error_code")
    private Integer errorCode;

    @SerializedName("message")
    private String message;

    public boolean isError() {
        return error;
    }

    @Nullable
    public String getImagePath() {
        return imagePath;
    }

    @Nullable
    @SerializedName("new_image_path")
    private String imagePath;

    public boolean getError() {
        return error;
    }

    public Integer getErrorCode(){ return errorCode; }

    public String getMessage(){ return message; }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "error=" + error +
                ", errorCode=" + errorCode +
                ", message='" + message + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
