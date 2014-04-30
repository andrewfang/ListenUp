package edu.berkeley.cs160.jaguars.listenup;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver{
	public String contactName, contactId;
	@Override
	public void onReceive(Context context, Intent intent) {

        if (MainActivity.careAboutCall && MainActivity.running) {
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);

            switch (tm.getCallState()) {

                case TelephonyManager.CALL_STATE_RINGING:

                    String phoneNr= intent.getStringExtra("incoming_number");
                    
                    //Set default to "unknown number"
                    contactName = "Unknown Number";

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
                        }
                    } finally {
                        contactLookupCursor.close();
                    }

             		int ringMode = MainActivity.mAudioManager.getRingerMode();
             		
             		//Silence the ringtone to pronounce the name
             		MainActivity.mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                    MainActivity.ttobj.speak("Call from" + contactName, TextToSpeech.QUEUE_FLUSH, null);
                    
                    //Unsilence
                    MainActivity.mAudioManager.setRingerMode(ringMode);
              
                    break;
            }
        }

	}

}

