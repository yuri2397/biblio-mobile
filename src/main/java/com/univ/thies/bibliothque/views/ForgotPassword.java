package com.univ.thies.bibliothque.views;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.univ.thies.bibliothque.R;
import com.univ.thies.bibliothque.models.ApiResponse;
import com.univ.thies.bibliothque.utils.ApiService;
import com.univ.thies.bibliothque.utils.IOnCreate;
import com.univ.thies.bibliothque.utils.RetrofitBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ForgotPassword extends AppCompatActivity implements IOnCreate {
    private static final String TAG = "ForgotPassword";

    Dialog dialog;
    TextView errorText;
    TextInputLayout inputLayout;
    TextInputEditText input;
    Button btnValidate;
    ProgressBar progressBar;

    String email;

    Call<ApiResponse> call;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.univ.thies.bibliotheque.LOGOUT");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        }, intentFilter);

        init();
    }

    @Override
    public void findView() {
        errorText = findViewById(R.id.forgot_password_error_message);
        inputLayout = findViewById(R.id.forgot_password_input_layout);
        input = findViewById(R.id.forgot_password_edit_text);
        btnValidate = findViewById(R.id.forgot_password_btn_validate);
        progressBar = findViewById(R.id.forgot_password_progress_bar);
    }

    @Override
    public void initData() {
        apiService = RetrofitBuilder.createService(ApiService.class);
        dialog = new Dialog(this);
    }

    @Override
    public void manageView() {
        errorText.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        inputLayout.setErrorEnabled(false);
    }

    @Override
    public void setListeners() {
        btnValidate.setOnClickListener((view) -> sendRequest());
    }

    void sendRequest(){
        if (validator()){
            progressBar.setVisibility(VISIBLE);
            errorText.setVisibility(GONE);
            call = apiService.sendForgotPasswordLink(email);
            call.enqueue(new Callback<ApiResponse>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(@NotNull Call<ApiResponse> call, @NotNull Response<ApiResponse> response) {
                    if(response.isSuccessful()){
                        Log.e(TAG, "onResponse: " + response.body().getMessage() );
                        displayDialog(response.body().getMessage(), R.drawable.ic_baseline_check_circle_24);
                    }
                    else if(response.code() == 422){
                        errorText.setVisibility(VISIBLE);
                        errorText.setText("Veuillez saisir un bon adresse email.");
                    }
                    progressBar.setVisibility(GONE);
                }
                @Override
                public void onFailure(@NotNull Call<ApiResponse> call, @NotNull Throwable t) {
                    Log.e(TAG, "OnFailure: " + t.getCause() );
                    progressBar.setVisibility(GONE);
                    errorText.setVisibility(VISIBLE);
                    errorText.setText("Vérifier votre connexion et réessayer.");
                }
            });
        }
    }

    boolean validator(){
        this.manageView();
        email = input.getText().toString().trim();
        if(email.isEmpty()){
            inputLayout.setError("Veuillez donner votre email.");
            return false;
        }
        if(!emailValidator(email)){
            inputLayout.setError("Adresse email incorrect.");
            return false;
        }
        return true;
    }

    private static boolean emailValidator(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void displayDialog(String message, int drawable){
        dialog.setContentView( R.layout.activation_dialog_layout);
        dialog.setCancelable(false);
        TextView messageText = dialog.findViewById(R.id.activation_popup_message);
        messageText.setText(message);

        ImageView dialogStateImg = dialog.findViewById(R.id.activation_popup_layout_check);
        dialogStateImg.setImageDrawable(getDrawable(drawable));

        ImageView dialogBtnConfirm = dialog.findViewById(R.id.activation_close_popup_btn);
        dialogBtnConfirm.setOnClickListener(view -> {
            dialog.dismiss();
            input.setText("");
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}