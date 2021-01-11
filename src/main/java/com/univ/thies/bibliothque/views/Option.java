package com.univ.thies.bibliothque.views;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
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

public class Option extends AppCompatActivity implements IOnCreate {

    Dialog dialog, confirmationDialog;

    MaterialToolbar toolbar;
    TextView editPassword, logout;
    MySharedPreferences preferences;

    Call<ApiResponse> call;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        init();
    }

    @Override
    public void findView() {
        toolbar = findViewById(R.id.option_tool_bar);
        editPassword = findViewById(R.id.option_change_password);
        logout = findViewById(R.id.option_logout);
    }

    @Override
    public void initData() {
        dialog = new Dialog(this);
        confirmationDialog = new Dialog(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24);
        preferences = MySharedPreferences.getInstance(this);
        apiService = RetrofitBuilder.createServiceWithAuth(ApiService.class, preferences.getToken(), this);
    }

    @Override
    public void manageView() {

    }

    @Override
    public void setListeners() {
        editPassword.setOnClickListener( v -> startActivity(new Intent(getApplicationContext(), Settings.class)));
        logout.setOnClickListener( v -> displayConfirmationDialog());
    }

    private void logout() {
        displayDialog();
        call = apiService.logout();
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NotNull Call<ApiResponse> call, @NotNull Response<ApiResponse> response) {
                if(response.isSuccessful() || response.code() == 401){
                    preferences.clearToken();
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("com.univ.thies.bibliotheque.LOGOUT");
                    sendBroadcast(broadcastIntent);
                    finish();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NotNull Call<ApiResponse> call, Throwable t) {
                Toast.makeText(Option.this, "Vérifier votre connexion internet", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void displayDialog(){
        dialog.setContentView( R.layout.loader_layout);
        dialog.setCancelable(false);
        TextView messageText = dialog.findViewById(R.id.loader_layout_message);
        messageText.setText(R.string.deconnexion_en_cour);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void displayConfirmationDialog(){
        confirmationDialog.setContentView(R.layout.reservation_conf);

        final Button cancel, confirm;
        final TextView message;
        message = confirmationDialog.findViewById(R.id.confirmation_message);
        message.setText("Voulez-vouz vraiment vous déconnecter?");
        cancel = confirmationDialog.findViewById(R.id.reservation_delete_btn_cancel);
        confirm = confirmationDialog.findViewById(R.id.reservation_delete_btn_conf);

        cancel.setOnClickListener( v -> confirmationDialog.dismiss());
        confirm.setOnClickListener( v -> {
            logout();
            confirmationDialog.dismiss();
        });

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }
}