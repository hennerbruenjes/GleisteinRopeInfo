package net.bits_bremen.bruenjes.GleisteinRopeInfo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EinstellungenActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Die benötigte XML-Layout Datei:
        setContentView(R.layout.activity_einstellungen);

        Button btnAbbrechen = (Button) findViewById(R.id.buttonAbbrechen);
        Button btnSpeichern = (Button) findViewById(R.id.buttonSpeichern);

        SharedPreferences settings =  getSharedPreferences("RopeInfoEinstellungen", MODE_PRIVATE);

        final TextView tvUser = (TextView) findViewById((R.id.textviewUser));
        tvUser.setText(settings.getString("Username", "Default" ));

        final TextView tvPw = (TextView) findViewById((R.id.textviewPw));
        tvPw.setText(settings.getString("Password", "Default" ));

        btnAbbrechen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSpeichern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MODE_PRIVATE defines that no other app can access the data
                SharedPreferences settings =  getSharedPreferences("RopeInfoEinstellungen", MODE_PRIVATE);
                // save data in the shared preferences
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Username", tvUser.getText().toString());
                editor.putString("Password", tvPw.getText().toString());
                editor.commit();
                // remove the setting again
                // editor.remove("Username");
                // editor.commit();
                finish();
            }
        });

    }
}
