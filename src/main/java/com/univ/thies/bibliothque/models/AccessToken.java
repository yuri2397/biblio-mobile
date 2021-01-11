package com.univ.thies.bibliothque.models;

import com.google.gson.annotations.SerializedName;

public class AccessToken {
    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private long expiresIn;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    public AccessToken(){}

    public String getTokenType() { return tokenType; }
    public void setTokenType(String value) { this.tokenType = value; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long value) { this.expiresIn = value; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String value) { this.accessToken = value; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String value) { this.refreshToken = value; }
}