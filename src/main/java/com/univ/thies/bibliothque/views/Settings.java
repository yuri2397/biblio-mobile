package com.univ.thies.bibliothque.views;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.univ.thies.bibliothque.R;
import com.univ.thies.bibliothque.models.ApiResponse;
import com.univ.thies.bibliothque.utils.ApiService;
import com.univ.thies.bibliothque.utils.IOnCreate;
import com.univ.thies.bibliothque.utils.MySharedPreferences;
import com.univ.thies.bibliothque.utils.RetrofitBuilder;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class Settings extends AppCompatActivity implements IOnCreate {
    Dialog dialog;

    TextView errorText;
    ProgressBar progressBar;
    Button btnValidate;
    MaterialToolbar toolbar;
    String currentPasswordText, newPasswordText, newPasswordConfText;

    TextInputEditText currentPasswordInput, newPasswordInput, newPasswordConfInput;
    TextInputLayout currentPasswordLayout, newPasswordLayout, newPasswordConfLayout;

    Call<ApiResponse> call;
    ApiService apiService;
    MySharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.univ.thies.bibliotheque.LOGOUT");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive","Logout in progress");
                finish();
            }
        }, intentFilter);

        init();
    }

    @Override
    public void findView() {
        currentPasswordLayout = findViewById(R.id.current_password_input_layout);
        currentPasswordInput = findViewById(R.id.current_password_input);

        toolbar = findViewById(R.id.setting_tool_bar);

        newPasswordConfLayout = findViewById(R.id.conf_password_input_layout);
        newPasswordConfInput = findViewById(R.id.conf_password_input);

        newPasswordLayout = findViewById(R.id.new_password_input_layout);
        newPasswordInput = findViewById(R.id.new_password_input);

        errorText = findViewById(R.id.edit_password_error_message);
        progressBar = findViewById(R.id.edit_password_progress_bar);
        btnValidate = findViewById(R.id.edit_password_btn_validate);
    }

    @Override
    public void initData() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24);

        preferences = MySharedPreferences.getInstance(this);
        apiService = RetrofitBuilder.createServiceWithAuth(ApiService.class, preferences.getToken(), this);
        dialog = new Dialog(this);
    }

    @Override
    public void manageView() {
        errorText.setVisibility(GONE);
        progressBar.setVisibility(GONE);
    }

    @Override
    public void setListeners() {
        btnValidate.setOnClickListener( v -> editPassword());
    }

    private boolean validate(){
        currentPasswordLayout.setHelperTextEnabled(false);
        newPasswordLayout.setHelperTextEnabled(false);
        newPasswordConfLayout.setHelperTextEnabled(false);

        currentPasswordText = currentPasswordInput.getText().toString().trim();
        newPasswordText = newPasswordInput.getText().toString().trim();
        newPasswordConfText = newPasswordConfInput.getText().toString().trim();

        if(currentPasswordText.isEmpty()){
            currentPasswordLayout.setErrorEnabled(true);
            currentPasswordLayout.setError("Mot de passe requi.");
            return false;
        }
        if(newPasswordText.isEmpty()){
            newPasswordLayout.setErrorEnabled(true);
            newPasswordLayout.setError("Donner un nouveau mot de passe.");
            return false;
        }
        if(newPasswordText.length() < 8){
            newPasswordLayout.setErrorEnabled(true);
            newPasswordLayout.setError("Le nouveau mot de passe est trop faible.");
            return false;
        }
        if(newPasswordConfText.equalsIgnoreCase(newPasswordText)) {
            currentPasswordLayout.setErrorEnabled(false);
            newPasswordLayout.setErrorEnabled(false);
            newPasswordConfLayout.setErrorEnabled(false);
            return true;
        }
        newPasswordConfLayout.setErrorEnabled(true);
        newPasswordConfLayout.setError("Les mots de passe ne correspondent pas.");
        return false;
    }

    private void editPassword() {
        if(validate()){
            progressBar.setVisibility(VISIBLE);
            errorText.setVisibility(GONE);

            call = apiService.editPassword(currentPasswordText, newPasswordText);
            call.enqueue(new Callback<ApiResponse>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(@NotNull Call<ApiResponse> call, @NotNull Response<ApiResponse> response) {
                    if(response.isSuccessful()){
                        assert response.body() != null;
                        displayDialog(response.body().getMessage());
                    }
                    else if(response.code() == 401){
                        preferences.clearToken();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    }
                    else if(response.code() == 422){
                        currentPasswordLayout.setError("Mot de passe incorrect");
                        currentPasswordLayout.setErrorEnabled(true);
                    }
                    progressBar.setVisibility(GONE);
                }

                @Override
                public void onFailure(@NotNull Call<ApiResponse> call, @NotNull Throwable t) {
                    errorText.setVisibility(VISIBLE);
                    errorText.setText("Veuillez vérifier votre connexion internet.");
                    progressBar.setVisibility(GONE);
                }
            });
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void displayDialog(String message){
        dialog.setContentView( R.layout.activation_dialog_layout);
        dialog.setCancelable(false);
        TextView messageText = dialog.findViewById(R.id.activation_popup_message);
        messageText.setText(message);

        ImageView dialogStateImg = dialog.findViewById(R.id.activation_popup_layout_check);
        dialogStateImg.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_circle_24));

        ImageView dialogBtnConfirm = dialog.findViewById(R.id.activation_close_popup_btn);
        dialogBtnConfirm.setOnClickListener(view -> {
            dialog.dismiss();
            finish();
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}