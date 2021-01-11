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
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;
import com.univ.thies.bibliothque.R;
import com.univ.thies.bibliothque.models.ApiResponse;
import com.univ.thies.bibliothque.models.Book;
import com.univ.thies.bibliothque.models.User;
import com.univ.thies.bibliothque.utils.ApiService;
import com.univ.thies.bibliothque.utils.BorrowRecyclerViewAdapter;
import com.univ.thies.bibliothque.utils.IOnCreate;
import com.univ.thies.bibliothque.utils.MySharedPreferences;
import com.univ.thies.bibliothque.utils.ReservationRecyclerViewAdapter;
import com.univ.thies.bibliothque.utils.RetrofitBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class Profile extends AppCompatActivity implements IOnCreate {

    private static final int OPEN_PHOTOS_CODE = 1000;
    private static final int ACTIVE_COUNT_CODE = 2000;
    private static final int GOTO_SEARCH_CODE = 3000;
    private static final String MESSAGE_ON_BACK = "Cliquer une deuxième fois pour quitter.";
    private static int BACK = 0;

    Dialog confirmationDialog, loaderDialog;
    SwipeRefreshLayout refreshLayout;
    MaterialToolbar toolbar;

    List<Book> borrowList, reservationList;
    LinearLayoutManager borrowLayoutManager, reservationLayoutManager;
    BorrowRecyclerViewAdapter borrowRecyclerViewAdapter;
    RecyclerView borrowRecyclerView, reservationRecyclerView;

    ReservationRecyclerViewAdapter reservationRecyclerViewAdapter;

    LinearLayout networkLayout, networkDataLayout, profileDataLayout, loadProfileProgressBar, activeCountLayout;
    RelativeLayout profileGlobalLayout;

    MaterialCardView profileNoReservationLayout, profileNoBorrowLayout;

    CircleImageView userImage, userImageBlack, userStatus;

    AppCompatImageButton btnEditUserImage,  btnSearch;
    Button btnReloadData, btnProfileReload, btnActiveCount;

    ProgressBar reservationProgressBar, borrowProgressBar, uploadImageProgressBar;

    MySharedPreferences preferences;

    Call<ApiResponse> callUploadImg;
    Call<ApiResponse> callDeleteReservation;
    Call<User> call;
    Call<List<Book>> callBook;
    ApiService apiService;

    User currentUser;
    private final String TAG = "PROFILE";
    private static String mCurrentPhotoPath;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        isLogin();

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
        getUserProfile();
    }

    @Override
    public void findView(){
        refreshLayout = findViewById(R.id.profile_swipe_layout);
        toolbar = findViewById(R.id.profile_app_bar);

        borrowRecyclerView = findViewById(R.id.profile_user_borrow);
        reservationRecyclerView = findViewById(R.id.profile_user_reservation);

        networkLayout = findViewById(R.id.profile_network_error_layout);
        networkDataLayout = findViewById(R.id.profile_data_network_error_layout);

        profileGlobalLayout = findViewById(R.id.profile_global_layout);

        /* USER VIEWS */
        userImage = findViewById(R.id.profile_image);
        userStatus = findViewById(R.id.profile_stats);

        /* LISTENERS VIEWS */
        btnEditUserImage = findViewById(R.id.profile_btn_edit_image);
        btnSearch = findViewById(R.id.profile_btn_search);
        btnReloadData = findViewById(R.id.profile_btn_reload_data);
        btnProfileReload = findViewById(R.id.profile_btn_reload_profile);
        btnActiveCount = findViewById(R.id.profile_btn_active_count);

        /* PROFILE STATE VIEW */
        profileNoBorrowLayout = findViewById(R.id.profile_no_borrow);
        profileNoReservationLayout = findViewById(R.id.profile_no_reservation);
        profileDataLayout = findViewById(R.id.profile_data_layout);
        activeCountLayout = findViewById(R.id.profile_active_count_layout);

        /* PROGRESS BAR */
        reservationProgressBar = findViewById(R.id.profile_reservation_progress_bar);
        borrowProgressBar = findViewById(R.id.profile_borrow_progress_bar);
        loadProfileProgressBar = findViewById(R.id.profile_progress_bar_begin);
        uploadImageProgressBar = findViewById(R.id.profile_upload_image);

        /* OTHERS VIEWS */
        userImageBlack = findViewById(R.id.profile_image_black_bg_on_load);
    }

    @Override
    public void initData(){
        confirmationDialog = new Dialog(this);
        loaderDialog = new Dialog(this);
        setSupportActionBar(toolbar);

        /* RESERVATION RECYCLER VIEW */
        reservationLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        reservationRecyclerView.setLayoutManager(reservationLayoutManager);

        /* BORROW RECYCLER VIEW */
        borrowLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        borrowRecyclerView.setLayoutManager(borrowLayoutManager);

        /* NETWORK LAYOUT */
        networkLayout.setVisibility(GONE);
        networkDataLayout.setVisibility(GONE);

        /* OTHERS */
        preferences = MySharedPreferences.getInstance(this);
        apiService = RetrofitBuilder.createServiceWithAuth(ApiService.class, preferences.getToken(), this);

        /* USER INFORMATION VIEW */
    }

    @Override
    public void manageView(){
        borrowRecyclerView.setVisibility(GONE);
        reservationRecyclerView.setVisibility(GONE);

        networkDataLayout.setVisibility(GONE);
        networkLayout.setVisibility(GONE);

        profileGlobalLayout.setVisibility(GONE);

        profileNoReservationLayout.setVisibility(GONE);
        profileNoBorrowLayout.setVisibility(GONE);

        reservationProgressBar.setVisibility(GONE);
        borrowProgressBar.setVisibility(GONE);
        loadProfileProgressBar.setVisibility(VISIBLE);

        profileDataLayout.setVisibility(GONE);

        activeCountLayout.setVisibility(GONE);

        userImageBlack.setVisibility(GONE);
        uploadImageProgressBar.setVisibility(GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setListeners(){
        btnSearch.setOnClickListener( v -> {
            Intent i = new Intent(this, Bibliotheque.class);
            i.putExtra("school", preferences.getUserProfileOnShared().getDepartement());
            startActivityForResult(i, GOTO_SEARCH_CODE);
        });

        btnActiveCount.setOnClickListener( v -> {
            Intent i = new Intent(this, CountActivation.class);
            startActivityForResult(i, ACTIVE_COUNT_CODE);
        });

        refreshLayout.setOnRefreshListener(this::getUserProfile);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getUserProfile(){
        if(preferences.getUserProfileOnShared().getEmail() == null){
            requestUserProfile();
        }
        else{
            setUserProfile(preferences.getUserProfileOnShared());
        }
    }

    private void requestUserProfile() {
        networkDataLayout.setVisibility(GONE);
        call = apiService.getProfile();
        call.enqueue(new Callback<User>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                if(response.isSuccessful()){
                    assert response.body() != null;
                    preferences.saveUserProfile(response.body());
                    currentUser = response.body();
                    setUserProfile(currentUser);
                }
                else if(response.code() == 401){
                    preferences.clearToken();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                refreshLayout.setRefreshing(false);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                networkLayout.setVisibility(VISIBLE);
                loadProfileProgressBar.setVisibility(GONE);
                profileGlobalLayout.setVisibility(GONE);
                btnProfileReload.setOnClickListener(v -> {
                    networkLayout.setVisibility(GONE);
                    loadProfileProgressBar.setVisibility(VISIBLE);
                    requestUserProfile();
                });

                refreshLayout.setRefreshing(false);
            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUserProfile(@NotNull User currentUser) {
        String firstName = currentUser.getFirstName().substring(0, 1).toUpperCase() + currentUser.getFirstName().substring(1);
        String lastName = currentUser.getLastName().substring(0, 1).toUpperCase() + currentUser.getLastName().substring(1);

        String fullName = firstName.length() > 7 ? firstName.substring(0, 1) + ". " + lastName : firstName + " " + lastName;

        toolbar.setTitle(fullName);
        toolbar.setSubtitle(currentUser.getDepartement());

        if(currentUser.getStatus() == 0){
            userStatus.setImageDrawable(getDrawable(R.color.red));
        }
        else{
            userStatus.setImageDrawable(getDrawable(R.color.green));
        }
        if(currentUser.getProfileImage() != null){
            Picasso.get().load(RetrofitBuilder.HOST + currentUser.getProfileImage())
                    .error(getDrawable(R.drawable.user_default_profile))
                    .into(userImage);
        }
        loadProfileProgressBar.setVisibility(GONE);
        profileGlobalLayout.setVisibility(VISIBLE);
        networkLayout.setVisibility(GONE);
        setUserData();
    }

    private void setUserData() {
        networkDataLayout.setVisibility(GONE);
        profileDataLayout.setVisibility(VISIBLE);
        reservationProgressBar.setVisibility(VISIBLE);
        borrowRecyclerView.setVisibility(GONE);
        reservationRecyclerView.setVisibility(GONE);
        borrowProgressBar.setVisibility(VISIBLE);
        profileNoReservationLayout.setVisibility(GONE);
        activeCountLayout.setVisibility(GONE);
        callBook = apiService.getBooks();
        callBook.enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NotNull Call<List<Book>> call, @NotNull Response<List<Book>> response) {
                if(response.isSuccessful()){
                    assert response.body() != null;
                    final List<Book> l =  new ArrayList<>(response.body());
                    if(l.size() == 0){
                        borrowProgressBar.setVisibility(GONE);
                        borrowRecyclerView.setVisibility(GONE);
                        profileNoBorrowLayout.setVisibility(VISIBLE);

                        reservationProgressBar.setVisibility(GONE);
                        reservationRecyclerView.setVisibility(GONE);
                        profileNoReservationLayout.setVisibility(VISIBLE);

                        profileDataLayout.setVisibility(VISIBLE);

                    }
                    else{
                        /* GERER LES RECYCLERS VIEWS*/
                        borrowList = new ArrayList<>();
                        reservationList = new ArrayList<>();

                        for(Book b : l){
                            if(b.getStatus() == 1)
                                borrowList.add(b);
                            else{
                                reservationList.add(b);
                            }
                        }

                        if(borrowList.size() == 0){
                            borrowRecyclerView.setVisibility(GONE);
                            profileNoBorrowLayout.setVisibility(VISIBLE);
                        }
                        else{
                            borrowRecyclerViewAdapter = new BorrowRecyclerViewAdapter(borrowList);
                            borrowRecyclerView.setAdapter(borrowRecyclerViewAdapter);
                            borrowRecyclerViewAdapter.notifyDataSetChanged();
                            borrowRecyclerView.setVisibility(VISIBLE);
                            profileDataLayout.setVisibility(VISIBLE);
                            profileNoBorrowLayout.setVisibility(GONE);
                        }
                        if(reservationList.size() == 0){
                            reservationRecyclerView.setVisibility(GONE);
                            profileNoReservationLayout.setVisibility(VISIBLE);
                        }
                        else{
                            reservationRecyclerViewAdapter = new ReservationRecyclerViewAdapter(reservationList);
                            reservationRecyclerView.setAdapter(reservationRecyclerViewAdapter);
                            reservationRecyclerView.setVisibility(VISIBLE);
                            reservationRecyclerViewAdapter.notifyDataSetChanged();
                            reservationRecyclerViewAdapter.setOnItemClickListener( position -> deleteReservationIconClicked(position));
                            profileDataLayout.setVisibility(VISIBLE);
                            profileNoReservationLayout.setVisibility(GONE);
                        }
                        reservationProgressBar.setVisibility(GONE);
                        borrowProgressBar.setVisibility(GONE);

                    }
                }
                else if(response.code() == 401){
                    preferences.clearToken();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                else if (response.code() == 402){
                    profileDataLayout.setVisibility(GONE);
                    activeCountLayout.setVisibility(VISIBLE);
                }
                else {
                    borrowProgressBar.setVisibility(GONE);
                    borrowRecyclerView.setVisibility(GONE);
                    profileNoBorrowLayout.setVisibility(VISIBLE);

                    reservationProgressBar.setVisibility(GONE);
                    reservationRecyclerView.setVisibility(GONE);
                    profileNoReservationLayout.setVisibility(VISIBLE);

                    profileDataLayout.setVisibility(VISIBLE);
                }
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NotNull Call<List<Book>> call, @NotNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                profileDataLayout.setVisibility(GONE);
                networkDataLayout.setVisibility(VISIBLE);
                btnReloadData.setOnClickListener(v -> {
                    networkDataLayout.setVisibility(GONE);
                    setUserData();
                });
                refreshLayout.setRefreshing(false);
            }
        });
    }

    public void openOptions(MenuItem item){
        startActivity(new Intent(this, Option.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private void deleteReservationIconClicked(final int position) {
        confirmationDialog.setContentView(R.layout.reservation_conf);

        final Button cancel, confirm;
        cancel = confirmationDialog.findViewById(R.id.reservation_delete_btn_cancel);
        confirm = confirmationDialog.findViewById(R.id.reservation_delete_btn_conf);
        
        cancel.setOnClickListener( v -> confirmationDialog.dismiss());
        confirm.setOnClickListener( v -> {
            deleteReservation(position);
            confirmationDialog.dismiss();
        });
        
        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    private void deleteReservation(int position) {
        displayLoader();
        callDeleteReservation = apiService.deleteReservation(reservationList.get(position).getIsbn());
        callDeleteReservation.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NotNull Call<ApiResponse> call, @NotNull Response<ApiResponse> response) {
                if(response.isSuccessful()){
                    removeReservationItem(position);
                    if(reservationList.size() == 0){
                        profileNoReservationLayout.setVisibility(VISIBLE);
                        reservationRecyclerView.setVisibility(GONE);
                    }
                    assert response.body() != null;
                    Toast.makeText(Profile.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
                else if (response.code() == 422){
                    Toast.makeText(Profile.this, "Une erreur est survenue. Réessayer", Toast.LENGTH_SHORT).show();
                }
                else if (response.code() == 423){
                    setUserData();
                }
                loaderDialog.dismiss();
            }

            @Override
            public void onFailure(@NotNull Call<ApiResponse> call, @NotNull Throwable t) {
                Toast.makeText(Profile.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileImg() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), OPEN_PHOTOS_CODE);
    }

    private void isLogin(){
        if(!MySharedPreferences.getInstance(this).isLogin()){
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    public void changeImageProfile(View view) {
        openFileImg();
    }

    private void uploadImage(){
        File file = new File(mCurrentPhotoPath);
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("profile_image", file.getName(), requestFile);

        userImageBlack.setVisibility(VISIBLE);
        uploadImageProgressBar.setVisibility(VISIBLE);

        callUploadImg = apiService.postImage(body);
        callUploadImg.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NotNull Call<ApiResponse> call, @NotNull Response<ApiResponse> response) {
                if(response.isSuccessful()){
                    ApiResponse c = response.body();
                    assert c != null;
                    preferences.saveUserImagePath(c.getImagePath());
                    Picasso.get()
                            .load(RetrofitBuilder.HOST + c.getImagePath())
                            .error(R.drawable.ic_baseline_person_24)
                            .into(userImage);
                }
                else if(response.code() == 401){
                    preferences.clearToken();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                else if(response.code() == 402){
                    Toast.makeText(Profile.this, "Votre photo n'a pas été modifiée.", Toast.LENGTH_SHORT).show();
                }
                userImageBlack.setVisibility(GONE);
                uploadImageProgressBar.setVisibility(GONE);
            }

            @Override
            public void onFailure(@NotNull Call<ApiResponse> call, @NotNull Throwable t) {
                Toast.makeText(Profile.this, "Veuillez vérifier votre connexion internet.", Toast.LENGTH_SHORT).show();
                userImageBlack.setVisibility(GONE);
                uploadImageProgressBar.setVisibility(GONE);
            }
        });
    }

    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "BIBLIOTEQUE_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private void displayLoader(){
        loaderDialog.setContentView(R.layout.loader_layout);
        loaderDialog.setCancelable(false);
        TextView messageText = loaderDialog.findViewById(R.id.loader_layout_message);
        messageText.setText(R.string.traitement_en_cour);
        loaderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loaderDialog.show();
    }

    private void removeReservationItem(int position){
        reservationList.remove(position);
        reservationRecyclerViewAdapter.notifyItemRemoved(position);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        boolean sta;

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case OPEN_PHOTOS_CODE:
                    if(data != null){
                        try {
                            // Creating file
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                Log.d(TAG, "Error occurred while creating the file");
                            }
                            InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                            FileOutputStream fileOutputStream = new FileOutputStream(photoFile);
                            // Copying
                            copyStream(inputStream, fileOutputStream);
                            uploadImage();
                            fileOutputStream.close();
                            inputStream.close();
                        } catch (Exception e) {
                            Log.d(TAG, "onActivityResult: " + e.toString());
                        }
                    }
                    break;
                case ACTIVE_COUNT_CODE:
                case GOTO_SEARCH_CODE:
                    assert data != null;
                    sta = data.getBooleanExtra("state", false);
                    if(sta){
                        setUserData();
                        currentUser = MySharedPreferences.getInstance(getApplicationContext()).getUserProfileOnShared();
                        Log.e(TAG, "STATUS ++++++++++++ " + currentUser.getStatus() );
                        if(currentUser.getStatus() == 0){
                            userStatus.setImageDrawable(getDrawable(R.color.red));
                        }
                        else{
                            userStatus.setImageDrawable(getDrawable(R.color.green));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(BACK == 1) {
            super.onBackPressed();
            return;
        }
        BACK++;
        Toast.makeText(this, MESSAGE_ON_BACK, Toast.LENGTH_SHORT).show();
    }
}