package edu.berkeley.cs160.jaguars.listenup;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver{
	public String contactName, contactId;
	@Override
	public void onReceive(Context context, Intent intent) {

        if (MainActivity.careAboutCall) {
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);

            switch (tm.getCallState()) {

                case TelephonyManager.CALL_STATE_RINGING:

                    String phoneNr= intent.getStringExtra("incoming_number");

                    ContentResolver localContentResolver = context.getContentResolver();
                    Cursor contactLookupCursor =
                            localContentResolver.query(
                                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                                            Uri.encode(phoneNr)),
                                    new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup._ID},
                                    null,
                                    null,
                                    null);
                    try {
                        while(contactLookupCursor.moveToNext()){
                            contactName = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
                            contactId = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup._ID));
                            Log.d("nameTag", "contactMatch name: " + contactName);
                            Log.d("numTag", "contactMatch id: " + contactId);
                        }
                    } finally {
                        contactLookupCursor.close();
                    }

                    Intent intentMain  = new Intent(context,
                            TextToSpeechConverter.class);
                    Log.d("Phone number", phoneNr);
                    intentMain.putExtra("contactName", contactName);  //<<< put sms text
                    intentMain.putExtra("phoneNr", phoneNr);  //<<< put sms text

                    Toast.makeText(context, "Contact Name : " + contactName + " Phone Number: " + phoneNr,Toast.LENGTH_LONG).show();
                    intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(intentMain);

                    break;
            }
        }

	}

}

