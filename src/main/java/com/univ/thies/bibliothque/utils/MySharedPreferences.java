package com.univ.thies.bibliothque.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.univ.thies.bibliothque.models.AccessToken;
import com.univ.thies.bibliothque.models.User;

public class MySharedPreferences {
    private final static String SHARED_NAME = "prefs";
    private final static String ACCESS_TOKEN_KEY = "ACCESS_TOKEN";
    private final static String REFRESH_TOKEN_KEY = "REFRESH_TOKEN";
    private final static String USER_EMAIL_KEY = "email";
    private final static String USER_FIRST_NAME_KEY = "first_name";
    private final static String USER_LAST_NAME_KEY = "last_name";
    private final static String USER_NUMBER_KEY = "number";
    private static final String USER_PROFILE_IMG_KEY = "image_profile";
    private static final String USER_PROFILE_STATUS_KEY = "status";
    private static final String USER_PROFILE_UFR_KEY = "ufr";
    private static final String USER_PROFILE_DEPARTEMENT_KEY = "departement";

    private static MySharedPreferences instance;
    private SharedPreferences preferences;
    private Context context;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    private MySharedPreferences(Context context){
        this.context = context;
        this.preferences = context.getSharedPreferences(SHARED_NAME,Context.MODE_PRIVATE);
        this.editor = preferences.edit();
    }

    public synchronized static MySharedPreferences getInstance(Context context) {
        if(instance == null)
            instance = new MySharedPreferences(context);
        return instance;
    }

    public void saveToken(AccessToken accessToken){
        editor.putString(ACCESS_TOKEN_KEY, accessToken.getAccessToken());
        editor.putString(REFRESH_TOKEN_KEY, accessToken.getRefreshToken());
        editor.commit();
    }

    public void clearToken(){
        editor.remove(ACCESS_TOKEN_KEY);
        editor.remove(REFRESH_TOKEN_KEY);
        editor.remove(USER_EMAIL_KEY);
        editor.remove(USER_FIRST_NAME_KEY);
        editor.remove(USER_LAST_NAME_KEY);
        editor.remove(USER_NUMBER_KEY);
        editor.remove(USER_PROFILE_UFR_KEY);
        editor.remove(USER_PROFILE_DEPARTEMENT_KEY);
        editor.remove(USER_PROFILE_STATUS_KEY);
        editor.commit();
    }

    public AccessToken getToken(){
        AccessToken token = new AccessToken();
        token.setAccessToken(preferences.getString(ACCESS_TOKEN_KEY, null));
        token.setRefreshToken(preferences.getString(REFRESH_TOKEN_KEY, null));
        return token;
    }

    public boolean isLogin() {
        if(preferences.getString(ACCESS_TOKEN_KEY,null) == null)
            return false;
        return true;
    }

    public void saveUserProfile(User user){
        editor.putString(USER_NUMBER_KEY,user.getNumber());
        editor.putString(USER_EMAIL_KEY,user.getEmail());
        editor.putString(USER_FIRST_NAME_KEY,user.getFirstName());
        editor.putString(USER_LAST_NAME_KEY,user.getLastName());
        editor.putString(USER_PROFILE_IMG_KEY, user.getProfileImage());
        editor.putString(USER_PROFILE_UFR_KEY, user.getUfr());
        editor.putString(USER_PROFILE_DEPARTEMENT_KEY, user.getDepartement());
        editor.putInt(USER_PROFILE_STATUS_KEY, user.getStatus());
        editor.commit();
    }

    public void setUserStatus(int status){
        editor.putInt(USER_PROFILE_STATUS_KEY, status);
        editor.commit();
    }

    public void saveUserImagePath(String path){
        editor.putString(USER_PROFILE_IMG_KEY, path);
        editor.commit();
    }

    public User getUserProfileOnShared(){
        User user = new User();
        user.setFirstName(preferences.getString(USER_FIRST_NAME_KEY,null));
        user.setLastName(preferences.getString(USER_LAST_NAME_KEY,null));
        user.setEmail(preferences.getString(USER_EMAIL_KEY,null));
        user.setNumber(preferences.getString(USER_NUMBER_KEY,null));
        user.setProfileImage(preferences.getString(USER_PROFILE_IMG_KEY, null));
        user.setUfr(preferences.getString(USER_PROFILE_UFR_KEY, null));
        user.setDepartement(preferences.getString(USER_PROFILE_DEPARTEMENT_KEY, null));
        user.setStatus(preferences.getInt(USER_PROFILE_STATUS_KEY, -1));
        return user;
    }
}
