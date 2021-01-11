package com.univ.thies.bibliothque.utils;

import androidx.annotation.Nullable;

import com.univ.thies.bibliothque.models.AccessToken;

import java.io.IOException;

import okhttp3.Authenticator;
import retrofit2.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class CustumAuth implements Authenticator {

    private MySharedPreferences preferences;
    private static CustumAuth instance;

    private CustumAuth(MySharedPreferences preferences){
        this.preferences = preferences;
    }

    public synchronized static CustumAuth getInstance(MySharedPreferences preferences){
        if(instance == null)
            instance = new CustumAuth(preferences);
        return instance;
    }

    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        if(RequestCount(response) > 3){
            return null;
        }

        ApiService apiService = RetrofitBuilder.createService(ApiService.class);
        Call<AccessToken> call = apiService.refreshToken(preferences.getToken().getRefreshToken());

        retrofit2.Response<AccessToken> res = call.execute();

        if(res.isSuccessful()){
            AccessToken newToken = res.body();
            preferences.saveToken(newToken);

            return response.request().newBuilder().header("Authorization", "Bearer " + newToken.getAccessToken()).build();
        }
        else
            return null;
    }

    private int RequestCount(Response response){
        int result = 1;
        while((response = response.priorResponse()) != null)
            result++;
        return result;
    }
}
