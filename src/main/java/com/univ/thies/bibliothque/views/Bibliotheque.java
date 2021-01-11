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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Picasso;
import com.univ.thies.bibliothque.R;
import com.univ.thies.bibliothque.models.ApiResponse;
import com.univ.thies.bibliothque.models.Book;
import com.univ.thies.bibliothque.utils.ApiService;
import com.univ.thies.bibliothque.utils.BookRecyclerViewAdapter;
import com.univ.thies.bibliothque.utils.DomainRecyclerViewAdapter;
import com.univ.thies.bibliothque.utils.IOnCreate;
import com.univ.thies.bibliothque.utils.MySharedPreferences;
import com.univ.thies.bibliothque.utils.RetrofitBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 *
 */
public class Bibliotheque extends AppCompatActivity implements IOnCreate {
    private String data;
    private static boolean state = false;
    private static final int ACTIVE_COUNT_CODE = 2000;

    private static final String TAG = "BIBLIOTHEQUE";
    private final static String ALERT_MESSAGE = "Veuillez taper quelque chose.";
    private static boolean requestIsSending = false;
    private static int currentItemPos = -1;

    Dialog dialog;

    List<String> domainList;
    List<Book> bookList;

    LinearLayoutManager bookLayoutManager;
    BookRecyclerViewAdapter bookRecyclerViewAdapter;
    DomainRecyclerViewAdapter domainRecyclerViewAdapter;

    RelativeLayout bottomSheetBehaviorLayout;

    BottomSheetBehavior bottomSheetBehaviorItem;
    View sheetBlackView;
    AppCompatImageButton btnSearch;

    EditText searchInput;

    AppCompatImageButton sheetBtnAdd;

    Button btnNetworkReload, btnActiveCount;

    LinearLayout  appBarLayout, networkLayout, activationLayout, bookNotFoundLayout;

    ProgressBar loadProgressBar, sheetProgressBar;

    RecyclerView domainRecyclerView, bookRecyclerView;

    ImageView sheetImage;

    TextView messageText, sheetTitle, sheetDescription, sheetAuthor;

    ApiService apiService;
    Call<List<Book>> call;
    Call<ApiResponse> callReservation;
    MySharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bibliotheque);

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
        domainRecyclerView = findViewById(R.id.bibliotheque_domaines_recycler_view);
        bookRecyclerView = findViewById(R.id.bibliotheque_documents_recycler_view);

        searchInput = findViewById(R.id.bibliotheque_search_input);
        btnSearch = findViewById(R.id.bibliotheque_search_btn);
        btnNetworkReload = findViewById(R.id.bibliotheque_btn_reload);

        appBarLayout = findViewById(R.id.bibliotheque_search_bar);
        networkLayout = findViewById(R.id.bibliotheque_network_error_layout);
        activationLayout = findViewById(R.id.bibliotheque_active_count_layout);
        bookNotFoundLayout = findViewById(R.id.bibliotheque_message_layout);

        messageText = findViewById(R.id.bibliotheque_message_text);

        loadProgressBar = findViewById(R.id.bibliotheque_progress_bar);

        bottomSheetBehaviorLayout = findViewById(R.id.bottom_sheet_collection_item);
        sheetBlackView = findViewById(R.id.bibliotheque_transparent_view);

        sheetTitle = findViewById(R.id.document_item_sheet_title);
        sheetDescription = findViewById(R.id.document_item_sheet_description);
        sheetAuthor = findViewById(R.id.document_item_sheet_author);
        sheetImage = findViewById(R.id.document_item_sheet_image);
        sheetBtnAdd = findViewById(R.id.document_sheet_btn_add);
        sheetProgressBar = findViewById(R.id.document_sheet_add_progress_bar);
    }

    @Override
    public void initData() {
        dialog = new Dialog(this);
        preferences = MySharedPreferences.getInstance(this);
        apiService = RetrofitBuilder.createServiceWithAuth(ApiService.class, preferences.getToken(), this);

        LinearLayoutManager domainLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        domainRecyclerView.setLayoutManager(domainLayoutManager);
        createSchoolList();
        domainRecyclerViewAdapter = new DomainRecyclerViewAdapter(domainList);
        domainRecyclerView.setAdapter(domainRecyclerViewAdapter);

        bookLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        bookRecyclerView.setLayoutManager(bookLayoutManager);

        bottomSheetBehaviorItem = BottomSheetBehavior.from(bottomSheetBehaviorLayout);
        bottomSheetBehaviorItem.setHideable(false);
        sheetBlackView.setVisibility(GONE);
        sheetProgressBar.setVisibility(GONE);

        btnActiveCount = findViewById(R.id.bibliotheque_btn_active_count);
    }

    @Override
    public void manageView() {
        appBarLayout.setVisibility(VISIBLE);
        activationLayout.setVisibility(GONE);
        networkLayout.setVisibility(GONE);
        bookRecyclerView.setVisibility(VISIBLE);
        loadProgressBar.setVisibility(VISIBLE);
        bookNotFoundLayout.setVisibility(GONE);
    }

    @Override
    public void setListeners() {
        btnSearch.setOnClickListener( v -> search());

        btnNetworkReload.setOnClickListener(v -> {
            if(data != null)
                search();
        });

        sheetBtnAdd.setOnClickListener(v -> addToReservation());

        domainRecyclerViewAdapter.setOnItemClickListener(position -> searchInput.setText(domainList.get(position)));

        bottomSheetBehaviorItem.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED){
                    sheetBlackView.setVisibility(VISIBLE);
                }
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED && requestIsSending){
                    callReservation.cancel();
                    requestIsSending = false;
                    sheetProgressBar.setVisibility(GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                sheetBlackView.setVisibility(VISIBLE);
                sheetBlackView.setAlpha(slideOffset);
            }
        });
        btnActiveCount.setOnClickListener( v -> {
            Intent i = new Intent(this, CountActivation.class);
            startActivityForResult(i, ACTIVE_COUNT_CODE);
        });

        Intent intent = getIntent();
        if(intent != null ){
            data = intent.getStringExtra("school");
            if(data != null) {
                searchInput.setText(data);
                searchInput.selectAll();
                state = false;
                search();
            }
        }
    }

    private void addToReservation() {
        sheetProgressBar.setVisibility(VISIBLE);
        callReservation = apiService.doReservation(bookList.get(currentItemPos).getIsbn());
        requestIsSending = true;
        callReservation.enqueue(new Callback<ApiResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onResponse(@NotNull Call<ApiResponse> call, @NotNull Response<ApiResponse> response) {
                Log.e(TAG, "onResponse: " + response.message());
                bottomSheetBehaviorItem.setState(BottomSheetBehavior.STATE_COLLAPSED);
                sheetProgressBar.setVisibility(GONE);

                if(response.isSuccessful()){
                    displayDialog(response.body().getMessage(), R.drawable.ic_baseline_check_circle_24);
                    state = true;
                }
                else if(response.code() == 401){
                    preferences.clearToken();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                }
                else if(response.code() == 404){
                    // livre indisponible
                    displayDialog("Ce livre n'est pas disponible pour le moment, veuillez revenir plus tard.", R.drawable.ic_baseline_warning_24);
                }
                else if(response.code() == 400){
                    // deja deux livres
                    displayDialog("Vous ne pouvez pas réserver plus de deux livres en même temps.", R.drawable.ic_baseline_error_24);
                }
                else if(response.code() == 406){
                    // deja deux livres
                    displayDialog("Vous ne pouvez pas réserver deux livres dans le mếme domaine.", R.drawable.ic_baseline_error_24);
                }

            }

            @Override
            public void onFailure(@NotNull Call<ApiResponse> call, @NotNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage() );
            }
        });
    }

    /**
     * Vérifier si le champ inputSearch contient quelque chose ou pas
     * @return boolean
     */
    private boolean validator(){
        data = searchInput.getText().toString().trim().toLowerCase();
        return !data.isEmpty();
    }

    /**
     * Ajouter les differents département dans la liste
     */
    private void createSchoolList(){
        domainList = new ArrayList<>();
        domainList.add("Mathèmatique");
        domainList.add("Tourisme Et Loisirs");
        domainList.add("Informatique");
        domainList.add("Physique");
        domainList.add("Chimie");
        domainList.add("Géographie");
        domainList.add("Science Économique");
        domainList.add("Art");
        domainList.add("Science Des Eaux");
        domainList.add("Santé");
        domainList.add("Biologie");
        domainList.add("Géologie");
    }

    /**
     * Methode de recherche de livres
     * @return void
     * */
    public void search() {
        Log.i(TAG, "SEARCH BUTTON CLICKED");

        if(validator()){
            activationLayout.setVisibility(GONE);
            networkLayout.setVisibility(GONE);
            bookNotFoundLayout.setVisibility(GONE);
            loadProgressBar.setVisibility(VISIBLE);
            bookRecyclerView.setVisibility(GONE);
            activationLayout.setVisibility(GONE);

            call = apiService.searchDocuments(data);
            call.enqueue(new Callback<List<Book>>() {
                @Override
                public void onResponse(@NotNull Call<List<Book>> call, @NotNull Response<List<Book>> response) {
                    if(response.isSuccessful()){
                        bookList = response.body();
                        assert bookList != null;
                        if(bookList.isEmpty()){
                            bookNotFoundLayout.setVisibility(VISIBLE);
                        }
                        else{
                            // SET VIEWS
                            bookRecyclerView.setVisibility(VISIBLE);

                            /* SET RECYCLER VIEWS */
                            bookRecyclerViewAdapter = new BookRecyclerViewAdapter(bookList);
                            bookRecyclerView.setAdapter(bookRecyclerViewAdapter);
                            bookRecyclerViewAdapter.notifyDataSetChanged();
                            bookRecyclerViewAdapter.setOnItemClickListener( position -> bookItemClicked(position));
                        }
                    }
                    else if(response.code() == 401){
                        preferences.clearToken();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    }
                    else if (response.code() == 402){
                        activationLayout.setVisibility(VISIBLE);
                    }
                    loadProgressBar.setVisibility(GONE);
                }

                @Override
                public void onFailure(@NotNull Call<List<Book>> call, @NotNull Throwable t) {
                    loadProgressBar.setVisibility(GONE);
                    networkLayout.setVisibility(VISIBLE);
                }
            });
        }
        else
            Toast.makeText(this, ALERT_MESSAGE, Toast.LENGTH_SHORT).show();

    }

    /**
     * Gere les cliques sur les items
     *
     * @param position de l'élément qui a été cliqué
     */
    private void bookItemClicked(int position) {
        currentItemPos = position;
        Book b = bookList.get(position);
        Picasso.get().load(RetrofitBuilder.HOST + b.getImagePath()).into(sheetImage);
        sheetAuthor.setText(b.getAuthor());
        sheetDescription.setText(b.getDescription());
        sheetTitle.setText(b.getTitle());

        sheetBlackView.setVisibility(VISIBLE);
        bottomSheetBehaviorItem.setState(BottomSheetBehavior.STATE_EXPANDED);
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
        dialogBtnConfirm.setOnClickListener(view -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        getIntent().putExtra("state", state);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean sta;
        if(resultCode == RESULT_OK && requestCode == ACTIVE_COUNT_CODE){
            assert data != null;
            sta = data.getBooleanExtra("state", false);
            if(sta){
                state = true;
                Toast.makeText(this, "RETOUR", Toast.LENGTH_SHORT).show();
                search();
            }
        }
    }
}