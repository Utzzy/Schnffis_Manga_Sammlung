package com.utzzy.schnffismangasammlung;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;


@Entity(tableName = "book")
public class Book implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "author")
    private String author;
    @ColumnInfo(name = "isbn")
    private String isbn;
    @ColumnInfo(name = "coverUrl")
    private String coverUrl;
    @ColumnInfo(name = "synopsis")
    private String synopsis;
    @ColumnInfo(name = "edition")
    private int edition;
    @ColumnInfo(name = "publisher")
    private String publisher;
    @ColumnInfo(name = "purchaseDate")
    private Date purchaseDate;
    @ColumnInfo(name = "count")
    private int count;


    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    public Book(String title, String author, String isbn, String coverUrl, String synopsis, int edition, String publisher, Date purchaseDate) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.coverUrl = coverUrl;
        this.synopsis = synopsis;
        this.edition = edition;
        this.publisher = publisher;
        this.purchaseDate = purchaseDate;
    }



    // Getter and setter methods

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public int getEdition() {
        return edition;
    }

    public void setEdition(int edition) {
        this.edition = edition;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }


    private boolean isSelected = false;

    // Weitere Eigenschaften und Methoden der Buchklasse...

    public boolean isSelected() {
        Log.e("isSelected", "isSelected: " + isSelected);
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void toggleSelected() {
        isSelected = !isSelected;
    }
}
