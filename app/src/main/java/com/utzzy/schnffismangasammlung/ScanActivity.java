package com.utzzy.schnffismangasammlung;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


public class ScanActivity extends AppCompatActivity {




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

        public static void scanCode(){
            // Starte den Barcode-Scanner
            ScanOptions integrator = new ScanOptions();
            integrator.setOrientationLocked(false); // Ermöglicht die Drehung der Kamera
            integrator.setPrompt("Scanne einen Barcode"); // Text, der über dem Scanner angezeigt wird
            integrator.setBeepEnabled(false);
            integrator.setCaptureActivity(CaptureAct.class);

        }

        ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {

            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                builder.setTitle("Result");
                builder.setMessage(result.getContents());
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                    }
                }).show();

            }
        });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Verarbeite das Scan-Ergebnis
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String scannedCode = result.getContents();

            Log.d("ScanTest", "Barcode gescannt: " + scannedCode);

        } else {
            // Wenn das Scannen abgebrochen wurde oder kein Barcode erkannt wurde

            Log.d("ScanTest", "Scannen abgebrochen oder kein Barcode erkannt");
        }

        // Beende die Aktivität nach dem Scannen, du kannst dies anpassen, wie du es benötigst
        //finish();
    }

}