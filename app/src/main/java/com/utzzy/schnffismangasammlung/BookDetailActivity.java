package com.utzzy.schnffismangasammlung;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.DatePicker;
import android.content.Intent;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BookDetailActivity extends AppCompatActivity {

    private Bibliothek_Datenbank db;
    private Book book;

    private ImageView coverUrlImageView;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView isbnTextView;
    private TextView publisherTextView;
    private TextView editionTextView;
    private TextView countTextView;

    private TextView synopsisTextView;

    private EditText purchaseDateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        Log.d("BookDetailActivity", "onCreate: Activity created");

        // initialisiere das purchaseDateEditText
        purchaseDateEditText = findViewById(R.id.textPurchaseDate);
        purchaseDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Initialisieren der Datenbank
        db = Bibliothek_Datenbank.getInstance(getApplicationContext());

        Book book = (Book) getIntent().getSerializableExtra("book");

        if (book != null) {
            // Hier sicher auf book zugreifen
            String title = book.getTitle();
            // Weitere Buchinformationen abrufen und anzeigen
        } else {
            // Handle den Fall, dass book null ist
            Log.e("BookDetailActivity", "Book ist null");
        }
    



        // Verweise auf Texviews von Layout erhalten
        titleTextView = findViewById(R.id.textTitle);
        authorTextView = findViewById(R.id.textAutor);
        isbnTextView = findViewById(R.id.textIsbn);
        publisherTextView = findViewById(R.id.textPublisher);
        editionTextView = findViewById(R.id.textAuflage);
        synopsisTextView = findViewById(R.id.textSynopsis);
        coverUrlImageView = findViewById(R.id.book_image);
        countTextView = findViewById(R.id.textCount);


        // Text mithilfe von Getter Methode einfügen
        Log.d("BookDetailActivity", "titleTextView is null: " + (titleTextView == null));

        titleTextView.setText(book.getTitle());
        authorTextView.setText(book.getAuthor());
        isbnTextView.setText(book.getIsbn());
        publisherTextView.setText(book.getPublisher());
        editionTextView.setText(String.valueOf(book.getEdition()));
        countTextView.setText(String.valueOf(book.getCount()));
        purchaseDateEditText.setText(String.valueOf(book.getPurchaseDate()));
        synopsisTextView.setText(book.getSynopsis());

        // Buchcover laden mit Picasso
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Picasso.get().load(book.getCoverUrl())
                    .error(R.drawable.placeholder_image) // Bei Fehler das Platzhalterbild verwenden
                    .into(coverUrlImageView);
        } else {
            // Wenn keine Cover-URL vorhanden ist, lade das Platzhalterbild
            Picasso.get().load(R.drawable.placeholder_image).into(coverUrlImageView);
        }




        // Initialisieren des Speicher-Buttons
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Speichern der Daten in der Datenbank
                saveBookData();
            }
        });
    }

    private void saveBookData() {
        // Speichern der Daten des Buchobjekts in der Datenbank
        book.setTitle(titleTextView.getText().toString());
        book.setAuthor(authorTextView.getText().toString());
        book.setIsbn(isbnTextView.getText().toString());
        book.setPublisher(publisherTextView.getText().toString());
        book.setEdition(Integer.parseInt(editionTextView.getText().toString()));

        // Konvertieren des ausgewählten Datums in ein Date-Objekt
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date purchaseDate = null;
        try {
            purchaseDate = dateFormat.parse(purchaseDateEditText.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        book.setPurchaseDate(purchaseDate);

        book.setSynopsis(synopsisTextView.getText().toString());
        db.bookDao().update(book);
    }


    private void showDatePickerDialog() {
        // das aktuelle Datum als Standard im DatePickerDialog setzen.
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        // Datepickerdialog erstellen und result setzen
        DatePickerDialog datePickerDialog = new DatePickerDialog(BookDetailActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Ausgewähltes Datum einsetzten
                        String selectedDate = String.format("%02d.%02d.%d", dayOfMonth, monthOfYear + 1, year);
                        purchaseDateEditText.setText(selectedDate);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        Date selectedDateAsDate = calendar.getTime();

                    }
                },
                year, month, day);
        datePickerDialog.show();

    }
                }




