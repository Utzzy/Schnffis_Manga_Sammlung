package com.utzzy.schnffismangasammlung;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> mBookList;
    private Context mContext;
    private Bibliothek bibliothek;
    private Button deleteSelectedButton;

    private OnBookClickListener onBookClickListener;
    private OnLongClickListener onLongBookClickListener;
    public interface OnLongClickListener {
        boolean onLongClick(int position);
    }
    public boolean isAnyBookSelected() {
        for (Book book : mBookList) {
            if (book.isSelected()) {
                return true;
            }
        }
        return false;
    }


    public BookAdapter(Bibliothek bibliothek, List<Book> bookList, OnBookClickListener listener, OnLongClickListener longListener, Button deleteSelectedButton) {
        this.mContext = bibliothek;
        mBookList = bookList;
        this.bibliothek = bibliothek;
        this.onBookClickListener = listener;
        this.onLongBookClickListener = longListener;
        this.deleteSelectedButton = deleteSelectedButton;
    }


    public void setBooks(List<Book> books) {
        mBookList = books;
        notifyDataSetChanged();
    }
    public class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Andere View-Elemente in ViewHolder
        private OnBookClickListener onBookClickListener;
        private OnLongClickListener onLongBookClickListener;
        public TextView titleTextView;
        public TextView isbnTextView;
        public TextView countTextView;
        public ImageView imageBookView;

        public void bind(Book book) {
            titleTextView.setText(book.getTitle());
            isbnTextView.setText(book.getIsbn());
            countTextView.setText(String.valueOf(getBookCountByIsbn(book.getIsbn())));
            Picasso.get().load(book.getCoverUrl()).into(imageBookView);
        }

        public BookViewHolder(View itemView, OnBookClickListener listener, OnLongClickListener longListener) {
            super(itemView);
            // Initialisierung der View-Elemente
            titleTextView = itemView.findViewById(R.id.book_title);
            isbnTextView = itemView.findViewById(R.id.book_isbn);
            countTextView = itemView.findViewById(R.id.book_count);
            imageBookView = itemView.findViewById(R.id.book_image);

            this.onBookClickListener = listener;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (onBookClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onBookClickListener.onBookClick(position);
                }
            }
        }


    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
        return new BookViewHolder(view, onBookClickListener, onLongBookClickListener);

    }


    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = mBookList.get(position);
        holder.bind(book);
        holder.titleTextView.setText(book.getTitle());
        holder.isbnTextView.setText(book.getIsbn());
        holder.countTextView.setText(String.valueOf(getBookCountByIsbn(book.getIsbn())));
        Picasso.get().load(book.getCoverUrl()).into(holder.imageBookView);



        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Behandle das lange Drücken auf das Buch
                book.toggleSelected(); // Markiere oder entferne Markierung des Buches
                Log.e("BookAdapter", "onLongClick: " + book.getTitle() + " " + book.isSelected());
                notifyItemChanged(holder.getAdapterPosition());
                return true;
            }
        });
        if (book.isSelected()) {
            holder.itemView.setBackgroundResource(R.drawable.selected_book_border);

        } else {
            holder.itemView.setBackgroundResource(0);

        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                book.toggleSelected(); // Toggle the isSelected state of the book
                notifyItemChanged(holder.getAdapterPosition()); // Notify the adapter that the item has changed
                // Update the visibility of the delete button
                if (isAnyBookSelected()) {
                    deleteSelectedButton.setVisibility(View.VISIBLE);
                } else {
                    deleteSelectedButton.setVisibility(View.GONE);
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mBookList.size();
    }

    public void addBook(Book book) {
        mBookList.add(book);
        notifyItemInserted(mBookList.size() - 1);
    }

    public int getBookCountByIsbn(String isbn) {
        int count = 1; // Starte die Zählung bei 1
        for (Book book : mBookList) {
            if (isbn != null && isbn.equals(book.getIsbn())) {
                count += book.getCount(); // Addiere den aktuellen Count-Wert hinzu
            }
        }
        return count;
    }



}
