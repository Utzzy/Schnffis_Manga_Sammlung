package com.utzzy.schnffismangasammlung;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.utzzy.schnffismangasammlung.databinding.FragmentSecondBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executor;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SecondFragment extends Fragment {
    private TextInputEditText isbnEditText;
    private TextInputEditText titleEditText;
    private TextInputEditText authorEditText;
    private TextInputEditText synopsisEditText;
    private TextInputEditText publisherEditText;
    private TextInputEditText editionEditText;
    private TextInputEditText purchaseDateEditText;
    private String coverUrl;

    private FragmentSecondBinding binding;

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private Date parseDateString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.w("Book", "Ungültiges Datum: " + dateString);
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        isbnEditText = rootView.findViewById(R.id.isbnEdittext);
        titleEditText = rootView.findViewById(R.id.titleEdittext);
        authorEditText = rootView.findViewById(R.id.authorEdittext);
        synopsisEditText = rootView.findViewById(R.id.synopsisEdittext);
        publisherEditText = rootView.findViewById(R.id.publisherEdittext);
        editionEditText = rootView.findViewById(R.id.editionEdittext);
        purchaseDateEditText = rootView.findViewById(R.id.purchaseDateEdittext);

        TextInputLayout purchaseDateEdittextLayout = rootView.findViewById(R.id.purchaseDateEdittextLayout);

        purchaseDateEdittextLayout.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Aktuelles Datum abrufen
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // DatePickerDialog anzeigen
                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                                // Das ausgewählte Datum im TextInputLayout anzeigen
                                String selectedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
                                purchaseDateEdittextLayout.getEditText().setText(selectedDate);
                            }
                        }, year, month, dayOfMonth);

                datePickerDialog.show();
            }
        });



        Button saveButton = rootView.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBook();
                clearFields();
            }
        });



        // Überprüfen, ob ein gescanntes ISBN-Code-Argument übergeben wurde
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("scannedISBN")) {
            String scannedISBN = arguments.getString("scannedISBN");

            // Setze gescanntes ISBN-Ergebnis ins isbnEditText-Feld
            isbnEditText.setText(scannedISBN);
            String searchedISBN = isbnEditText.getText().toString().trim();
            fetchBookInfo(searchedISBN);
        }
        isbnEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    String searchedISBN = isbnEditText.getText().toString().trim();
                    fetchBookInfo(searchedISBN);
                    return true; // Rückgabe von 'true', um zu signalisieren, dass das Event verarbeitet wurde
                }
                return false; // Rückgabe von 'false', wenn das Event nicht verarbeitet wurde
            }
        });

        return rootView;
    }


    public class ISBNUpdateEvent {
        private String updatedISBN;


        public String getUpdatedISBN() {
            return updatedISBN;
        }
    }

    private void saveBook() {
        String isbn = isbnEditText.getText().toString();
        String title = titleEditText.getText().toString();
        String author = authorEditText.getText().toString();
        String synopsis = synopsisEditText.getText().toString();
        String publisher = publisherEditText.getText().toString();
        int edition = Integer.parseInt(editionEditText.getText().toString());
        String purchaseDateStr = purchaseDateEditText.getText().toString();
        Date purchaseDate = parseDateString(purchaseDateStr);





        // Erstelle ein Buchobjekt mit den eingegebenen Daten
        Book book = new Book(title,author,isbn,coverUrl,synopsis,edition,publisher,purchaseDate);

        Log.d("SecondFragment", "Saving book: " + book.getTitle());
        Log.d("Second Fragment" , "Cover: " + book.getCoverUrl());


        //Speichern des Buchs in der Datenbank mit Room
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Bibliothek_Datenbank bookDatabase = Bibliothek_Datenbank.getInstance(requireContext());

                // Überprüfen, ob die ISBN bereits vorhanden ist
                Book existingBook = bookDatabase.bookDao().getBookByIsbn(isbn);

                if (existingBook != null) {
                    // Die ISBN ist bereits vorhanden. Erhöhe den Count-Wert.
                    existingBook.setCount(existingBook.getCount() + 1);
                    bookDatabase.bookDao().update(existingBook);
                } else {
                    // Die ISBN ist neu. Füge das Buch zur Datenbank hinzu.
                    bookDatabase.bookDao().insert(book);
                }

                Log.d("Bibliothek_Datenbank", "Buch wird hinzugefügt: " + book.getTitle());
            }
        });
    }

    private void fetchBookInfo(String isbn) {
        // Erstelle einen OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // Erstelle eine Anfrage an die Open Library API
        Request request = new Request.Builder()
                .url("https://openlibrary.org/search.json?q=" + isbn)
                .build();

        // Führe die Anfrage aus
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Behandele den Fehler, z.B. in einem Log-Eintrag
                Log.e("SecondFragment", "Fehler beim Abrufen der Buchinformationen: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    try {
                        JSONObject searchResult = new JSONObject(responseBody);
                        JSONArray docs = searchResult.getJSONArray("docs");

                        if (docs.length() > 0) {
                            JSONObject bookInfo = docs.getJSONObject(0);
                            updateUIWithBookInfo(bookInfo);
                        } else {
                            Log.e("SecondFragment", "Keine Informationen für ISBN: " + isbn);
                            fetchBookInfoFromGoogleBooks(isbn);
                        }
                    } catch (JSONException e) {
                        Log.e("SecondFragment", "Fehler beim Verarbeiten der JSON-Antwort: " + e.getMessage());
                    }
                }
            }

        });

    }
    //Aufrufen von Google Books API
    private void fetchBookInfoFromGoogleBooks(String isbn) {
        Log.e("SecondFragment", "Google books startet");
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn)
                .build();
                Log.e("SecondFragment", "API für ISBN" + " " + isbn + " gefunden");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SecondFragment", "Fehler beim Abrufen der Buchinformationen von Google Books: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    try {
                        JSONObject googleBooksResult = new JSONObject(responseBody);
                        JSONArray items = googleBooksResult.getJSONArray("items");
                        Log.d("SecondFragment", responseBody);

                        if (items.length() > 0) {
                            JSONObject firstItem = items.getJSONObject(0);
                            String selfLink = firstItem.optString("selfLink");
                            Log.e("SecondFragment", selfLink);

                            // Jetzt, da wir den selfLink haben, können wir weitere Informationen abrufen
                            fetchDetailedBookInfo(selfLink);
                        } else {
                            Log.e("SecondFragment", "Keine Informationen für ISBN " + isbn + " in Google Books gefunden.");
                        }
                    } catch (JSONException e) {
                        Log.e("SecondFragment", "Fehler beim Verarbeiten der JSON-Antwort von Google Books: " + e.getMessage());
                    }
                }
            }
        });
    }

    // Erhalte Infos von Google Books API
    private void fetchDetailedBookInfo(String selfLink) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(selfLink)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SecondFragment", "Fehler beim Abrufen der detaillierten Buchinformationen: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.e("SecondFragment", "response von selflink");

                    try {
                        JSONObject rootObject = new JSONObject(responseBody);
                        Log.e("SecondFragment", "rootObjekt: " + rootObject.toString());
                        JSONObject volumeInfo = rootObject.optJSONObject("volumeInfo");
                        Log.e("SecondFragment", "volumeInfo: " + volumeInfo.optString("title", "N/A") +
                                volumeInfo.optJSONArray("authors") + volumeInfo.optString("publisher", "N/A")

                        + volumeInfo.optString("subtitle", ""));

                        if (volumeInfo != null) {
                            updateUIWithGoogleBooksInfo(volumeInfo);
                            Log.e("SecondFragment", "get volumeInfo");
                        } else {
                            Log.e("SecondFragment", "volumeInfo Objekt nicht gefunden");
                        }
                    } catch (JSONException e) {
                        Log.e("SecondFragment", "Fehler beim Verarbeiten der JSON-Antwort von Google Books: " + e.getMessage());
                    }
                }
            }
        });
    }
    // Update Google Books API
    private void updateUIWithGoogleBooksInfo(JSONObject volumeInfo) throws JSONException {
        String title = volumeInfo.optString("title", "N/A");
        JSONArray authorsArray = volumeInfo.optJSONArray("authors");
        String author = (authorsArray != null && authorsArray.length() > 0) ? authorsArray.getString(0) : "N/A";
        String publisher = volumeInfo.optString("publisher", "N/A");
        String synopsis = volumeInfo.optString("description", "N/A");
        String subtitle = volumeInfo.optString("subtitle", "");

        JSONObject imageLinksObject = volumeInfo.optJSONObject("imageLinks");
        String imageLinks = imageLinksObject != null ? imageLinksObject.optString("thumbnail", "N/A") : "N/A";
        Log.e("SecondFragment", imageLinks);



        coverUrl = imageLinks.replace("http://", "https://");


        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String subtitleAdd;


                if (subtitle == "") {
                   subtitleAdd = "";
                }
                else {
                    subtitleAdd = " - " + subtitle;
                }



                titleEditText.setText(title + subtitleAdd);
                authorEditText.setText(author);
                publisherEditText.setText(publisher);
                editionEditText.setText(String.valueOf(volumeInfo.optInt("edition", 0)));
                synopsisEditText.setText(synopsis);

            }
        });
    }

    private void updateUIWithBookInfo(JSONObject bookInfo) throws JSONException {
        // Extrahiere Buchinformationen
        String title = bookInfo.optString("title", "N/A");
        String author = bookInfo.optJSONArray("author_name").optString(0, "N/A");
        String publisher = bookInfo.optJSONArray("publisher").optString(0, "N/A");
        String coverI = bookInfo.optString("cover_i", "N/A");
        coverUrl = "https://covers.openlibrary.org/b/id/" + coverI + "-S.jpg";

        // Aktualisiere die Benutzeroberfläche im UI-Thread
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Aktualisiere die Ansichten mit den Buchinformationen
                titleEditText.setText(title);
                authorEditText.setText(author);
                publisherEditText.setText(publisher);
                editionEditText.setText(String.valueOf(bookInfo.optInt("edition_count", 0)));

            }
        });
    }
    private void clearFields() {
        isbnEditText.setText("");
        titleEditText.setText("");
        authorEditText.setText("");
        synopsisEditText.setText("");
        publisherEditText.setText("");
        editionEditText.setText("");
        purchaseDateEditText.setText("");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onISBNUpdateEvent(ISBNUpdateEvent event) {
        // Implementierung hier
    }

    public void updateISBN(String updatedISBN) {
        isbnEditText.setText(updatedISBN);
        // Rufe die Methode zum Abrufen der Buchinformationen auf
        fetchBookInfo(updatedISBN);
    }
}