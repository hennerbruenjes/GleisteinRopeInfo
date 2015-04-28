package net.bits_bremen.bruenjes.GleisteinRopeInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class GelesenActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// Die benötigte XML-Layout Datei:
        setContentView(R.layout.activity_tag_gelesen);

        TextView Uid = (TextView)findViewById(R.id.textViewUID);
        TextView Daten = (TextView)findViewById(R.id.textViewDaten);

        Intent i = getIntent();
        // Receiving the Data
        String uid = i.getStringExtra("UID");
        String daten = i.getStringExtra("Daten");
        // Displaying Received data
        Uid.setText(uid);
        Daten.setText(daten);



        Button btnLesen = (Button) findViewById(R.id.buttonDatenLesen);
        btnLesen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Button btnSchreiben = (Button) findViewById(R.id.buttonDatenSchreiben);
        btnSchreiben.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Button btnZurueck = (Button) findViewById(R.id.buttonZurueck);
        btnZurueck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
