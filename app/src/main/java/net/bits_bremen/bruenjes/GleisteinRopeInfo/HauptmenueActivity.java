package net.bits_bremen.bruenjes.GleisteinRopeInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HauptmenueActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Die benötigte XML-Layout Datei:
        setContentView(R.layout.activity_hauptmenue);

        Button btnLesen = (Button) findViewById(R.id.buttonLesen);
        btnLesen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Neues Intent anlegen
                Intent nextScreen = new Intent(getApplicationContext(), LesenActivity.class);
                startActivity(nextScreen);
            }
        });

        Button btnEinstellungen = (Button) findViewById(R.id.buttonEinstellungen);
        btnEinstellungen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(), EinstellungenActivity.class);
                startActivity(nextScreen);
            }
        });

    }
}
