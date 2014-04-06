package edu.berkeley.cs160.jaguars.listenup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class TextToSpeechConverter extends Activity{

    private String callName, callNo;
    private TextToSpeech ttobj;
    private TextView callFrom, contactName, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.call_activity);
        contactName = (TextView) findViewById(R.id.tvContactName);
        phoneNumber = (TextView) findViewById(R.id.tvPhoneNumber);


        callName = getIntent().getStringExtra("contactName");
        callNo = getIntent().getStringExtra("phoneNr");

        contactName.setText(callName);
        phoneNumber.setText(callNo);

        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {

                        if (status == TextToSpeech.SUCCESS) {
                            int result = ttobj.setLanguage(Locale.US);
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "This Language is not supported");
                                Toast.makeText(getApplicationContext(), "This Language is not supported",
                                        Toast.LENGTH_SHORT).show();
                                Intent installIntent = new Intent();
                                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                startActivity(installIntent);
                            } else {
                                this.speakText(callName);
                            }
                        } else {
                            Log.e("TTS", "Initilization Failed!");
                            Toast.makeText(getApplicationContext(), "Initilization Failed!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

                    private void speakText(String callInfo) {
                        // TODO Auto-generated method stub

                        ttobj.speak(callInfo, TextToSpeech.QUEUE_FLUSH, null);
                    }
                });

        //  this.speakText(callInfo);

    }


    @Override
    protected void onPause() {

        Log.d("TAG","pausing...");
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onPause();
    }

    public void onDestroy() {
        // Don't forget to shutdown!
        if (ttobj != null) {
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onDestroy();
    }


    public void runInBackground() {
        Toast.makeText(getApplicationContext(), "going to background", Toast.LENGTH_LONG).show();

        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfoList = am.getRunningTasks(10);
        List<String> backStack = new ArrayList<String>();
        Iterator<RunningTaskInfo> itr = runningTaskInfoList.iterator();
        while (itr.hasNext()) {
            RunningTaskInfo runningTaskInfo = (RunningTaskInfo) itr.next();
            String topActivity = runningTaskInfo.topActivity.getShortClassName();
            backStack.add(topActivity.trim());
        }
        if (backStack != null) {

            if (backStack.get(0).equals(".TextToSpeechConverter")) {
                moveTaskToBack(true); // or finish() if you want to finish it. I don't.
            } else {
                Intent intent = new Intent(TextToSpeechConverter.this, TextToSpeechConverter.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }


        }
    }

}