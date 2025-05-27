package com.example.emsismartpresence;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;

public class NFCManager {
    private Activity activity;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private NFCListener listener;

    public interface NFCListener {
        void onNfcDetected(String nfcId);
    }

    public NFCManager(Activity activity) {
        this.activity = activity;
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        pendingIntent = PendingIntent.getActivity(
                activity, 0,
                new Intent(activity, activity.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
        );
    }

    public void setNfcListener(NFCListener listener) {
        this.listener = listener;
    }

    public void enableForegroundDispatch() {
        if (nfcAdapter != null) {
            try {
                IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
                nfcAdapter.enableForegroundDispatch(activity, pendingIntent, new IntentFilter[]{filter}, null);
            } catch (Exception e) {
                Log.e("NFC", "Error enabling NFC foreground dispatch", e);
            }
        }
    }

    public void disableForegroundDispatch() {
        if (nfcAdapter != null) {
            try {
                nfcAdapter.disableForegroundDispatch(activity);
            } catch (Exception e) {
                Log.e("NFC", "Error disabling NFC foreground dispatch", e);
            }
        }
    }

    public void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                String nfcId = bytesToHex(tag.getId());
                if (listener != null) {
                    listener.onNfcDetected(nfcId);
                }
            }
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}