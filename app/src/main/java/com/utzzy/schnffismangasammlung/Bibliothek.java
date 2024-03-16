package com.utzzy.schnffismangasammlung;

import static android.app.PendingIntent.getActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.Iterator;

import com.utzzy.schnffismangasammlung.databinding.ActivityBibliothekBinding;
import com.utzzy.schnffismangasammlung.databinding.ActivityMainBinding;
import com.utzzy.schnffismangasammlung.Bibliothek_Datenbank.BookDao;



import com.utzzy.schnffismangasammlung.ScanActivity;

public class Bibliothek extends AppCompatActivity implements OnBookClickListener, BookAdapter.OnLongClickListener{

    private AppBarConfiguration appBarConfiguration;

    private BookDao mBookDao;
    private Executor mExecutor;
    private List<Book> mBookList;
    private BookAdapter mAdapter;





    private ActivityBibliothekBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBibliothekBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        Button deleteSelectedButton = findViewById(R.id.delete_selected_button);
        mAdapter = new BookAdapter(this, mBookList, this, this, deleteSelectedButton);
        RecyclerView recyclerView = findViewById(R.id.book_list);
        recyclerView.setAdapter(mAdapter);
        mBookList = new ArrayList<>(); // Initialisierung der Buchliste



        Button deleteAllButton = findViewById(R.id.delete_all_button);
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllBooks();
                Log.d("Bibliothek", "deleteAllBooks() aufgerufen");
            }
        });

        //Initialisiere die Datenbank und das DAO
        Bibliothek_Datenbank database = Bibliothek_Datenbank.getInstance(this);
        mBookDao = database.bookDao();

        // Erstellen einen Executor mit einem FixedThreadPool mit 4 Threads
        mExecutor = Executors.newFixedThreadPool(4);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());



        // Setzen des Layout-Manager für den RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        BookAdapter adapter = new BookAdapter(Bibliothek.this, mBookList, Bibliothek.this, Bibliothek.this, deleteSelectedButton);

        recyclerView.setAdapter(adapter);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Hier den Code für den FAB-Klick in der Bibliothek-Aktivität behandeln
                Log.d("Bibliothek", "FAB-Klick in der Bibliothek-Aktivität behandelt.");

                // Senden Sie eine Broadcast-Nachricht, um den Scanner in der MainActivity zu starten
                Intent intent = new Intent("START_SCANNER");
                sendBroadcast(intent);

            }
        });

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Book> books = Bibliothek_Datenbank.getInstance(getApplicationContext()).bookDao().getAll();
                Log.d("BookDao", "Anzahl der Bücher: " + books.size());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBookList.clear();
                        mBookList.addAll(books);
                        adapter.setBooks(mBookList);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        deleteSelectedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (Book book : mBookList) {
                            if (book.isSelected()) {
                                mBookDao.delete(book);
                            }
                        }

                        // Update the RecyclerView on the main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mBookList.clear();
                                mBookList.addAll(mBookDao.getAll());
                                mAdapter.setBooks(mBookList);
                                mAdapter.notifyDataSetChanged();
                                Log.d("Bibliothek", "deleteSelectedBooks() aufgerufen");
                                // Hide the delete button
                                deleteSelectedButton.setVisibility(View.GONE);
                                Log.d("Bibliothek", "deleteSelectedButton ausgeblendet");
                            }
                        });
                    }
                });
            }
        });
    }


    @Override
    public void onBookClick(int position) {
        // Behandele den Klick auf ein Buch in der RecyclerView
        // Hier BookDetailActivity starten und die entsprechenden Buchdaten übergeben.
        Book clickedBook = mBookList.get(position);

        Log.d("RecyclerViewClick", "Clicked on book: " + clickedBook.getTitle());

        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("book", clickedBook);
        Log.d("RecyclerViewClick", "id " + clickedBook.getId());


        Log.d("RecyclerViewClick", "Starting BookDetailActivity");
        // Starte die BookDetailActivity
        startActivity(intent);

    }

    public boolean onLongClick(int position) {
        // Behandle das lange Drücken auf ein Buch in der RecyclerView
        Book longClickedBook = mBookList.get(position);
        longClickedBook.toggleSelected(); // Markiere oder entferne Markierung des Buches

        // Aktualisiere die Ansicht, um die Markierung anzuzeigen
        mAdapter.notifyDataSetChanged();

        return true;
    }

    private void deleteSelectedBooks() {
        // Lösche die markierten Bücher aus der Liste
        Iterator<Book> iterator = mBookList.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.isSelected()) {
                iterator.remove(); // Entferne das Buch aus der Liste
            }
        }

        // Aktualisiere die RecyclerView-Ansicht
        mAdapter.notifyDataSetChanged();
    }


    private void deleteAllBooks() {

        Bibliothek bibliothekInstance = this;
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d("BookDao", "Anzahl der Bücher vor dem Löschen: " + mBookList.size());
                mBookDao.deleteAll();
                mBookList.clear();
                Log.d("BookDao", "Anzahl der Bücher nach dem Löschen: " + mBookList.size());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Hier wird die RecyclerView aktualisiert, nachdem die Bücher gelöscht wurden
                        BookAdapter adapter = new BookAdapter(bibliothekInstance, mBookList, bibliothekInstance, bibliothekInstance, null);
                        RecyclerView recyclerView = findViewById(R.id.book_list);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }



}