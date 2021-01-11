package com.univ.thies.bibliothque.utils;

import com.univ.thies.bibliothque.models.AccessToken;
import com.univ.thies.bibliothque.models.ApiResponse;
import com.univ.thies.bibliothque.models.Book;
import com.univ.thies.bibliothque.models.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @POST("login")
    @FormUrlEncoded
    Call<AccessToken> login(@Field("username") String username, @Field("password") String password);

    @POST("refresh")
    @FormUrlEncoded
    Call<AccessToken> refreshToken(@Field("refresh_token") String refreshToken);


    @POST("profile")
    Call<User> getProfile();


    @GET("reservation")
    Call<List<Book>> getBooks();

    @POST("edit/profile")
    @Multipart
    Call<ApiResponse> postImage(@Part() MultipartBody.Part image);

    @POST("validation")
    @FormUrlEncoded
    Call<ApiResponse> validateCount(@Field("email") String email, @Field("code") String code);


    @POST("search")
    @FormUrlEncoded
    Call<List<Book>> searchDocuments(@Field("text") String text);

    @POST("reservation")
    @FormUrlEncoded
    Call<ApiResponse> doReservation(@Field("isbn") String isbn);

    @POST("edit/reservation")
    @FormUrlEncoded
    Call<ApiResponse> deleteReservation(@Field("isbn") String isbn);


    @POST("edit/password")
    @FormUrlEncoded
    Call<ApiResponse> editPassword(@Field("password") String currentPassword, @Field("new_password") String newPassword);

    @POST("logout")
    Call<ApiResponse> logout();

    @POST("password/email")
    @FormUrlEncoded
    Call<ApiResponse> sendForgotPasswordLink(@Field("email") String email);
}
