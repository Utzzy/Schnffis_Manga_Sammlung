package com.utzzy.schnffismangasammlung;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.Room;
import androidx.room.Update;

import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities ={Book.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class Bibliothek_Datenbank extends RoomDatabase {

    public void insertBook(Book book) {
        Log.d("Bibliothek_Datenbank", "Buch wird hinzugefügt: " + book.getTitle());
        bookDao().insert(book);
    }
    private static Bibliothek_Datenbank instance;

    public abstract BookDao bookDao();

    public static synchronized Bibliothek_Datenbank getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            Bibliothek_Datenbank.class, "bibliothek_database")
                    .fallbackToDestructiveMigration()
                    .build();
            Log.d("Bibliothek_Datenbank", "Getting database instance");
        }
        return instance;

    }


    @Dao
    public interface BookDao {



        @Insert
        void insert(Book book);



        @Query("SELECT * FROM Book")
        List<Book> getAll();

        @Query("SELECT * FROM book WHERE id = :id")
        Book getBookById(int id);

        @Query("DELETE FROM book")
        void deleteAll();

        @Update
        void update(Book book);

        @Delete
        void delete(Book book);

        @Query("SELECT * FROM book WHERE isbn = :isbn")
        Book getBookByIsbn(String isbn);

    }
}


