package com.univ.thies.bibliothque.utils;

import android.annotation.SuppressLint;
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

public class BorrowRecyclerViewAdapter extends RecyclerView.Adapter<BorrowRecyclerViewAdapter.ViewHolder> {
    private List<Book> list;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public BorrowRecyclerViewAdapter(List<Book> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_borrow_item, parent, false);
        return new ViewHolder(v,onItemClickListener);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book d = list.get(position);
        holder.title.setText(d.getTitle());
        holder.author.setText(d.getAuthor());

        holder.date.setText("A rendre avant : " + d.getExpiredAt().substring(0, 11));
        Picasso.get().load(RetrofitBuilder.HOST + d.getImagePath())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title, author, date;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.document_borrow_item_image);
            title = itemView.findViewById(R.id.document_borrow_item_title);
            author = itemView.findViewById(R.id.document_borrow_item_author);
            date = itemView.findViewById(R.id.document_borrow_item_date);

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
