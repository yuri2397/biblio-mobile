package com.univ.thies.bibliothque.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class Book {

    private String title;
    private String author;
    private String description;
    private String pages;
    private String rayon;
    private int status;
    private int copy;
    private String isbn;

    public Book(String title, String author, String pages) {
        this.title = title;
        this.author = author;
        this.pages = pages;
    }

    @SerializedName("reserved_at")
    @Nullable
    private String reservedAt;
    @SerializedName("image_path")
    @Nullable
    private String imagePath;
    @SerializedName("expired_at")
    @Nullable
    private String expiredAt;

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getPages() {
        return pages;
    }

    public int getCopy() {
        return copy;
    }

    public int getStatus() {
        return status;
    }

    @Nullable
    public String getReservedAt() {
        return reservedAt;
    }

    @Nullable
    public String getExpiredAt() {
        return expiredAt;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getRayon() {
        return rayon;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", pages='" + pages + '\'' +
                ", rayon='" + rayon + '\'' +
                ", status=" + status +
                ", copy=" + copy +
                ", isbn='" + isbn + '\'' +
                ", reservedAt='" + reservedAt + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", expiredAt='" + expiredAt + '\'' +
                '}';
    }
}
