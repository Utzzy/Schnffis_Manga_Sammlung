package com.utzzy.schnffismangasammlung;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.utzzy.schnffismangasammlung.databinding.ActivityMainBinding;
import com.utzzy.schnffismangasammlung.Bibliothek_Datenbank.BookDao;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    private BookDao mBookDao;
    private Executor mExecutor;
    private boolean isPopupShown = false;
    private String scannedISBN = "";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bibliothek_Datenbank db = Bibliothek_Datenbank.getInstance(this);
        mBookDao = db.bookDao();

        // Registriere Broadcast-Empfänger, um auf die Nachricht "START_SCANNER" zu hören
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("START_SCANNER".equals(intent.getAction())) {
                    // Hier die startScanner() Methode aufrufen
                    startScanner();
                }
            }
        };

        IntentFilter filter = new IntentFilter("START_SCANNER");
        registerReceiver(receiver, filter);


        com.utzzy.schnffismangasammlung.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(v-> {

                Log.d("BarcodeScanner", "Barcode-Scanner wird gestartet.");
                startScanner();



        });

        if (savedInstanceState != null) {
            isPopupShown = savedInstanceState.getBoolean("isPopupShown", false);
            scannedISBN = savedInstanceState.getString("scannedISBN", "");
            if (isPopupShown) {
                // If the popup was shown before rotation, show it again
                showScanResultPopup(this, scannedISBN);
            }
        }
    }
    private void startScanner() {
        Log.d("startScanner", "Barcode-Scanner wird gestartet.");
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(false); // Hochformat
        integrator.setPrompt("Scanne ISBN"); // Anzeigetext
        integrator.initiateScan();
    }

    // Dies wird aufgerufen, wenn der Scanner ein Ergebnis zurückgibt
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("BarcodeScanner", "Resultat wird an Popup übergeben.");

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String scannedISBN = result.getContents();


            // Popup Fenster öffnet sich
            showScanResultPopup(this, scannedISBN);
        }
    }

    private void showScanResultPopup(Activity activity, String scannedISBN) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Scanergebnis");

        // Layout für das Popup definieren (Titel, Text, Eingabefeld)
        View popupView = getLayoutInflater().inflate(R.layout.scan_result_popup, null);
        builder.setView(popupView);

        // Das gescannte ISBN-Ergebnis in das Eingabefeld einfügen
        EditText isbnEditText = popupView.findViewById(R.id.isbnEditText);
        isbnEditText.setText(scannedISBN);

        // Ok-Button: Führt die Aktion auf dem zweiten Fragment aus
        builder.setPositiveButton("Ok", (dialogInterface, i) -> {
            String updatedISBN = isbnEditText.getText().toString();
            // NavController des aktuellen Fragments aufrufen
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

            // Bestimmen der ID des Aktuellen Fragments
            int currentFragmentId = navController.getCurrentDestination().getId();

            // Navigation zum SecondFragment mit dem gescannten ISBN-Code
            Bundle bundle = new Bundle();
            bundle.putString("scannedISBN", updatedISBN);
           if (currentFragmentId == R.id.FirstFragment) {
            NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main))
                    .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
           }

           else if (currentFragmentId == R.id.SecondFragment) {
               NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main))
                       .navigate(R.id.action_SecondFragment_to_SecondFragment, bundle);
               EventBus.getDefault().post(new ISBNUpdateEvent(updatedISBN));
           }
        });

        // Zurück-Button: Schließt das Popup
        builder.setNegativeButton("Zurück", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            startScanner();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isPopupShown", isPopupShown);
        outState.putString("scannedISBN", scannedISBN);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menu ausklappen
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

}