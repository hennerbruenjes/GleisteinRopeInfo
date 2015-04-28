/**
 * @version $Id: LesenActivity.java 168 2012-08-09 09:14:39Z mroland $
 *
 * @author Michael Roland <mi.roland@gmail.com>
 *
 * Copyright (c) 2012 Michael Roland
 *
 * ALL RIGHTS RESERVED.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name Michael Roland nor the names of any contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL MICHAEL ROLAND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.bits_bremen.bruenjes.GleisteinRopeInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;

import java.nio.charset.Charset;
import java.util.Arrays;



/**
 * Utilities for URI record encoding.
 */
public class LesenActivity extends Activity implements View.OnClickListener {

    private static final int PENDING_INTENT_TECH_DISCOVERED = 1;
    private static final int DIALOG_WRITE_URL = 1;
    private static final int DIALOG_WRITE_ERROR = 2;
    private static final int DIALOG_NEW_TAG = 3;
    private static final String ARG_MESSAGE = "message";
    private NfcAdapter mNfcAdapter;
    //private EditText mMyUrl;

    private Button btnZurueck;
    private boolean mWriteUrl = false;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesen);
        btnZurueck = (Button)findViewById(R.id.buttonZurueck);
        btnZurueck.setOnClickListener(this);
        // Resolve the intent that started us:
        resolveIntent(this.getIntent(), false);
    }

    public void onClick(View v)
    {
        if(v== btnZurueck) {
            finish();
        }
    }

    /**
     * Called when the activity gets focus.
     */
    @Override
    public void onResume() {
        super.onResume();

        // Retrieve an instance of the NfcAdapter ("connection" to the NFC system service):
        NfcManager nfcManager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
        if (nfcManager != null) {
            mNfcAdapter = nfcManager.getDefaultAdapter();
        }

        if (mNfcAdapter != null) {
            // The isEnabled() method, if invoked directly after the NFC service
            // crashed, returns false regardless of the real setting (Android 2.3.3+).
            // As a nice side-effect it re-establishes the link to the correct instance
            // of NfcAdapter. Therefore, just execute this method twice whenever we
            // re-request the NfcAdapter, so we can be sure to have a valid handle.
            try {
                mNfcAdapter.isEnabled();
            } catch (NullPointerException e) {
                // Drop NullPointerException that is sometimes thrown
                // when NFC service crashed
            }
            try {
                mNfcAdapter.isEnabled();
            } catch (NullPointerException e) {
                // Drop NullPointerException that is sometimes thrown
                // when NFC service crashed
            }

            // Create a PendingIntent to handle discovery of Ndef and NdefFormatable tags:
            PendingIntent pi = createPendingResult(
                    PENDING_INTENT_TECH_DISCOVERED,
                    new Intent(),
                    0);
            if (pi != null) {
                try {
                    // Enable foreground dispatch for Ndef and NdefFormatable tags:
                    mNfcAdapter.enableForegroundDispatch(
                            this,
                            pi,
                            new IntentFilter[]{
                                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
                            },
                            new String[][]{
                                new String[]{"android.nfc.tech.NdefFormatable"},
                                new String[]{"android.nfc.tech.Ndef"}
                            });
                } catch (NullPointerException e) {
                    // Drop NullPointerException that is sometimes thrown
                    // when NFC service crashed
                }
            }
        }
    }

    /**
     * Called when the activity loses focus.
     */
    @Override
    public void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            try {
                // Disable foreground dispatch:
                mNfcAdapter.disableForegroundDispatch(this);
            } catch (NullPointerException e) {
                // Drop NullPointerException that is sometimes thrown
                // when NFC service crashed
            }
        }
    }

    /**
     * Called when activity receives a new intent.
     */
    @Override
    public void onNewIntent(Intent data) {
        // Resolve the intent that re-invoked us:
        resolveIntent(data, false);
    }

    /**
     * Called when a pending intent returns (e.g. our foreground dispatch).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PENDING_INTENT_TECH_DISCOVERED:
                // Resolve the foreground dispatch intent:
                resolveIntent(data, true);
                break;
        }
    }

    /**
     * Method to handle any intents that triggered our activity.
     * @param data                The intent we received and want to process.
     * @param foregroundDispatch  True if this intent was received through foreground dispatch.
     */
    private void resolveIntent(Intent data, boolean foregroundDispatch) {
        this.setIntent(data);

        String action = data.getAction();

        // We were started from the recent applications history: just show our main activity
        // (otherwise, the last intent that invoked us will be re-processed)
        if ((data.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) { return; }

        // Intent is a tag technology (we are sensitive to Ndef, NdefFormatable) or
        // an NDEF message (we are sensitive to URI records with the URI http://www.mroland.at/)
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // The reference to the tag that invoked us is passed as a parameter (intent extra EXTRA_TAG)
            Tag tag = data.getParcelableExtra(NfcAdapter.EXTRA_TAG);


                // Let's read the tag whenever we are not in write mode
              
                // Retrieve information from tag and display it
                StringBuilder tagInfo = new StringBuilder();
                String uidS;
                String datenS = "";

                // Get tag's UID:
                byte[] uid = tag.getId();
                tagInfo.append("UID: ").append(bytesToHex(uid)).append("\n\n");
                uidS = bytesToHex(uid);

                // Get tag's NDEF messages: The NDEF messages are passed as parameters (intent
                // extra EXTRA_NDEF_MESSAGES) and have to be casted into an NdefMessage array.
                Parcelable[] ndefRaw = data.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                NdefMessage[] ndefMsgs = null;
                if (ndefRaw != null) {
                    ndefMsgs = new NdefMessage[ndefRaw.length];
                    for (int i = 0; i < ndefMsgs.length; ++i) {
                        // Cast each NDEF message from Parcelable to NdefMessage:
                        ndefMsgs[i] = (NdefMessage) ndefRaw[i];
                    }
                }

                // Find records:

                if (ndefMsgs != null) {
                    // Iterate through all NDEF messages on the tag:
                    for (int i = 0; i < ndefMsgs.length; ++i) {
                        // Get NDEF message's records:
                        NdefRecord[] records = ndefMsgs[i].getRecords();

                        if (records != null) {
                            // Iterate through all NDEF records:
                            for (int j = 0; j < records.length; ++j) {
                                // Test if this record is a URI record:
                                if ((records[j].getTnf() == NdefRecord.TNF_WELL_KNOWN)
                                        && Arrays.equals(records[j].getType(), NdefRecord.RTD_URI)) {
                                    // Get record payload:
                                    byte[] payload = records[j].getPayload();

                                    // Drop prefix identifier byte and convert remaining URL to string (UTF-8):
                                    String uri = new String(Arrays.copyOfRange(payload, 1, payload.length), Charset.forName("UTF-8"));

                                    // Use UriRecordHelper to decode URI record payload:
                                    //String uri = UriRecordHelper.decodeUriRecordPayload(payload);

                                    tagInfo.append("URI: ");
                                    tagInfo.append(uri);
                                    tagInfo.append("\n");
                                }
                                // Neu! Extrahiert Text
                                if ((records[j].getTnf() == NdefRecord.TNF_WELL_KNOWN)
                                        && Arrays.equals(records[j].getType(), NdefRecord.RTD_TEXT)) {
                                    // Get record payload:
                                    byte[] payload = records[j].getPayload();

                                    // Drop prefix identifier byte and convert remaining URL to string (UTF-8):
                                    String text = new String(Arrays.copyOfRange(payload, 3, payload.length), Charset.forName("UTF-8"));
                                    text = seperateText(text);
                                    tagInfo.append("Daten:\n");
                                    tagInfo.append(text);
                                    tagInfo.append("\n");
                                    datenS = text;
                                }
                            }
                        }
                    }
                }

                // tagInfo wird dem Dialog übergeben
                //Bundle args = new Bundle();
                //args.putString(ARG_MESSAGE, tagInfo.toString());
                //showDialog(DIALOG_NEW_TAG, args);

                // Infos werden an GelesenActivity übergeben
                Intent nextScreen = new Intent(getApplicationContext(), GelesenActivity.class);

                //Intent mit den Daten füllen
                nextScreen.putExtra("UID", uidS);
                nextScreen.putExtra("Daten", datenS);

                // Intent starten und zur zweiten Activity wechseln
                startActivity(nextScreen);

        }
    }

    private String seperateText(String s)
    {
        String newS =" ";
        String[] parts = s.split(";");
        for (String sp : parts)
        {
           newS += (sp + "\n");
        }
        return newS;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Called when a dialog is created.
     */
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case DIALOG_WRITE_URL:
                // A dialog that we show when we are ready to write to a tag:
                return new AlertDialog.Builder(this)
                        .setTitle("Write URL to tag...")
                        .setMessage("Touch tag to start writing.")
                        .setCancelable(true)
                        .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int arg) {
                                d.cancel();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface d) {
                                mWriteUrl = false;
                            }
                        }).create();

            case DIALOG_WRITE_ERROR:
                // A dialog that we show when the write operation fails:
                return new AlertDialog.Builder(this)
                        .setTitle("Write    failed")
                        .setMessage("")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(true)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int arg) {
                                d.dismiss();
                            }
                        }).create();

            case DIALOG_NEW_TAG:
                // A dialog that we show when we detected and read a tag:
                return new AlertDialog.Builder(this)
                        .setTitle("Tag gelesen")
                        .setMessage("")
                        .setCancelable(true)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int arg) {
                                d.dismiss();
                            }
                        })
                        .create();
        }

        return null;
    }

    /**
     * Called before a dialog is shown to the user.
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch (id) {
            case DIALOG_WRITE_ERROR:
            case DIALOG_NEW_TAG:
                // Pass parameters to the tag detected and the write error dialog:
                if (args != null) {
                    String message = args.getString(ARG_MESSAGE);
                    if (message != null) { ((AlertDialog) dialog).setMessage(message); }
                }
                break;
        }
    }
}
