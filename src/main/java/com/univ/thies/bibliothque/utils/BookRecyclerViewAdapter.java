package com.univ.thies.bibliothque.utils;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.univ.thies.bibliothque.R;
import com.univ.thies.bibliothque.models.Book;

import java.util.List;

import static android.content.ContentValues.TAG;

public class BookRecyclerViewAdapter extends RecyclerView.Adapter<BookRecyclerViewAdapter.ViewHolder> {
    private List<Book> list;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public BookRecyclerViewAdapter(List<Book> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_item, parent, false);
        return new ViewHolder(v,onItemClickListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book d = list.get(position);
        holder.title.setText(d.getTitle());
        holder.author.setText(d.getAuthor());
        if(d.getDescription().length() > 100)
            holder.description.setText(d.getDescription().substring(0, 100) + "...");
        else
            holder.description.setText(d.getDescription());

        if(d.getCopy() == 0){
            holder.imageState.setImageResource(R.drawable.ic_baseline_state_red_1_24);
        }
        else if(d.getCopy() > 0)
            holder.imageState.setImageResource(R.drawable.ic_baseline_state_green_1_24);
        holder.pages.setText(d.getPages() + " pages");
        Picasso.get().load(RetrofitBuilder.HOST + d.getImagePath())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, imageState;
        TextView title, author, pages, description;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.document_item_image);
            title = itemView.findViewById(R.id.document_item_title);
            author = itemView.findViewById(R.id.document_item_author);
            pages = itemView.findViewById(R.id.document_item_pages);
            description = itemView.findViewById(R.id.document_item_description);
            imageState = itemView.findViewById(R.id.document_item_state);

            itemView.setOnClickListener(view -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                        listener.onItemClick(position);
                }
            });
        }
    }
}
