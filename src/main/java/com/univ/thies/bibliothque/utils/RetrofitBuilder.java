package com.univ.thies.bibliothque.utils;


import android.content.Context;

import com.univ.thies.bibliothque.models.AccessToken;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {
    public static final String HOST = "http://192.168.43.238/utdocs/public/";
    public static final String BASE_URL = HOST + "utdocs/";

    private static final OkHttpClient client = buildClient();
    private static final Retrofit retrofit = buildRetrofit(client);

    private static OkHttpClient buildClient(){
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Request.Builder builder1 = request.newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Connection", "close");
                    request = builder1.build();
                    return chain.proceed(request);
                });
        return builder.build();
    }

    private static Retrofit buildRetrofit(OkHttpClient client){
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static <T> T createService(Class<T> service){
        return retrofit.create(service);
    }

    public static <T> T createServiceWithAuth(Class<T> service, final AccessToken token, Context context){
        OkHttpClient newClient = client.newBuilder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Request.Builder builder = request.newBuilder();
                    if(token.getAccessToken() != null){
                        builder.addHeader("Authorization", "Bearer " + token.getAccessToken())
                                .addHeader("Accept", "application/json")
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Connection", "close");
                    }
                    request = builder.build();
                    return chain.proceed(request);
                })
                .authenticator(CustumAuth.getInstance(MySharedPreferences.getInstance(context)))
                .build();
        Retrofit newRetrofit = retrofit.newBuilder().client(newClient).build();
        return newRetrofit.create(service);
    }

    public static Retrofit getRetrofit(){ return retrofit; }
}
