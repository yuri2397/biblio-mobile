package com.univ.thies.bibliothque.views;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import com.univ.thies.bibliothque.utils.MySharedPreferences;
import com.univ.thies.bibliothque.utils.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CountActivation extends AppCompatActivity implements IOnCreate {
    private static final String TAG = "CountActivation";
    public static final String SERVER_MESSAGE = "Oups, nous n'arrivons pas à se connecter au serveur.";
    public static boolean state = false;

    Dialog dialog;

    TextView errorText;
    TextInputEditText codeInput;
    Button btnValidate;
    ProgressBar loadProgressBar;
    TextInputLayout codeInputLayout;

    String code;

    Call<ApiResponse> call;
    ApiService apiService;
    MySharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_activation);

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

    public void findView(){
        errorText = findViewById(R.id.activation_error_message);
        codeInput = findViewById(R.id.activation_input_code);
        btnValidate = findViewById(R.id.activation_btn_validate);
        loadProgressBar = findViewById(R.id.activation_progress_bar);
        codeInputLayout = findViewById(R.id.activation_text_input_layout);
    }

    public void initData(){
        preferences = MySharedPreferences.getInstance(this);
        apiService = RetrofitBuilder.createServiceWithAuth(ApiService.class, preferences.getToken(), this);
        dialog = new Dialog(this);
    }

    public void manageView(){
        errorText.setVisibility(GONE);
        loadProgressBar.setVisibility(GONE);
        codeInputLayout.setHelperTextEnabled(false);
        codeInputLayout.setErrorEnabled(false);
    }

    public void setListeners(){
        btnValidate.setOnClickListener( v -> sendRequest());
    }

    private boolean validator(){
        code = codeInput.getText().toString();
        codeInputLayout.setHelperTextEnabled(false);
        codeInputLayout.setErrorEnabled(false);
        if(code.isEmpty()){
            codeInputLayout.setError("Veuillez saisir votre code.");
            return false;
        }
        if(code.length() != 6){
            codeInputLayout.setError("Code incorrect");
            return false;
        }
        codeInputLayout.setHelperTextEnabled(false);
        return true;
    }

    private void sendRequest() {
       if(validator()){
            loadProgressBar.setVisibility(VISIBLE);
            errorText.setVisibility(GONE);

            call = apiService.validateCount(preferences.getUserProfileOnShared().getEmail(), code);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    Log.e(TAG, "onResponse: " + response.message());
                    if(response.isSuccessful()){
                        state = true;
                        MySharedPreferences pre = MySharedPreferences.getInstance(getApplicationContext());
                        pre.setUserStatus(1);
                        displayDialog();
                    }
                    else if(response.code() == 401){
                        preferences.clearToken();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    }
                    else if(response.code() >= 400){
                        codeInputLayout.setErrorEnabled(true);
                        codeInputLayout.setError("Le code saisi est invalide.");
                    }
                    loadProgressBar.setVisibility(GONE);
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    errorText.setVisibility(VISIBLE);
                    errorText.setText(SERVER_MESSAGE);
                    loadProgressBar.setVisibility(GONE);
                }
            });
        }
    }

    private void displayDialog() {
        dialog.setContentView( R.layout.activation_dialog_layout);
        dialog.setCancelable(false);
        ImageView dialogBtnConfirm = dialog.findViewById(R.id.activation_close_popup_btn);
        dialogBtnConfirm.setOnClickListener(view -> {
            dialog.dismiss();
            getIntent().putExtra("state",state);
            MySharedPreferences pref = MySharedPreferences.getInstance(this);
            pref.setUserStatus(1);
            setResult(RESULT_OK, getIntent());
            finish();
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        getIntent().putExtra("state",state);
        setResult(RESULT_OK, getIntent());
        finish();
    }
}