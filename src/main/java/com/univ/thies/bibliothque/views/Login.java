package com.univ.thies.bibliothque.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.univ.thies.bibliothque.R;
import com.univ.thies.bibliothque.models.AccessToken;
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

public class Login extends AppCompatActivity implements IOnCreate {
    private static final String TAG = "LOGIN";
    private TextInputEditText usernameInput, passwordInput;
    private TextInputLayout usernameInputLayout, passwordInputLayout;
    private Button loginButton;
    private TextView errorText;
    private ProgressBar progressBar;
    private String password, username;

    private Call<AccessToken> call;
    private ApiService apiService;
    private MySharedPreferences pref;

    private static final String DATA_ERROR = "N° dossier ou mot de passe incorrect.";
    private static final String NETWORK_ERROR = "Oups ! Veuillez vérifier votre connexion internet.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isLogin();
        init();
    }

    private void isLogin(){
        if(MySharedPreferences.getInstance(this).isLogin()){
            startActivity(new Intent(this, Profile.class));
            finish();
        }
    }

    public void findView(){
        usernameInput = findViewById(R.id.user_number_input);
        passwordInput = findViewById(R.id.user_password_input);
        loginButton = findViewById(R.id.login_btn_validate);
        errorText = findViewById(R.id.login_error_message);
        progressBar = findViewById(R.id.login_progress_bar);
        usernameInputLayout = findViewById(R.id.login_username_text_field);
        passwordInputLayout = findViewById(R.id.login_password_text_field);
    }

    public void manageView(){
        progressBar.setVisibility(GONE);
        errorText.setVisibility(GONE);
        usernameInputLayout.setHelperTextEnabled(false);
        passwordInputLayout.setHelperTextEnabled(false);
    }

    public void initData(){
        apiService = RetrofitBuilder.createService(ApiService.class);
        pref = MySharedPreferences.getInstance(this);
    }

    public void setListeners(){
        loginButton.setOnClickListener(v -> login());
    }

    private boolean validator(){
        errorText.setVisibility(GONE);
        usernameInputLayout.setHelperTextEnabled(false);
        passwordInputLayout.setHelperTextEnabled(false);

        username = usernameInput.getText().toString().trim();
        password = passwordInput.getText().toString().trim();

        if(username.isEmpty()){
            usernameInputLayout.setHelperTextEnabled(true);
            usernameInputLayout.setHelperText("Le numéro de dossier est requi");
            return false;
        }
        if(password.isEmpty()){
            passwordInputLayout.setHelperTextEnabled(true);
            passwordInputLayout.setHelperText("Le mot de passe est requi");
            return false;
        }
        return true;
    }

    private void login(){
        if(validator()){
            progressBar.setVisibility(VISIBLE);
            call = apiService.login(username, password);
            call.enqueue(new Callback<AccessToken>() {
                @Override
                public void onResponse(@NotNull Call<AccessToken> call, @NotNull Response<AccessToken> response) {
                    if(response.isSuccessful()){
                        Log.e(TAG, "onResponse: " + response.body().getAccessToken());
                        AccessToken accessToken = response.body();
                        pref.saveToken(accessToken);
                        startActivity(new Intent(getApplicationContext(), Profile.class));
                        finish();
                    }
                    else if(response.code() == 400){
                        Log.e(TAG, "NOT SUCCESS: " + response.errorBody());
                        errorText.setVisibility(VISIBLE);
                        errorText.setText(DATA_ERROR);
                    }
                    progressBar.setVisibility(GONE);
                }

                @Override
                public void onFailure(@NotNull Call<AccessToken> call, @NotNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage() );
                    errorText.setVisibility(VISIBLE);
                    errorText.setText(t.getMessage());
                    progressBar.setVisibility(GONE);
                }
            });
        }
    }


    public void forgotPassword(View view) {
        startActivity(new Intent(this, ForgotPassword.class));
    }
}