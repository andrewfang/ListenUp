package edu.berkeley.cs160.jaguars.listenup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import be.hogent.tarsos.dsp.AudioEvent;

public class MainActivity extends Activity {

	public static final String TAG = "ListenUpMain";
	private int mNotifyId = 1;
	private NotificationCompat.Builder mBuilder;
    static final int SAMPLE_RATE = 8000;
    private int bufferSize;
    private short[] buffer;
    private AudioRecord recorder;
    private boolean recording;
    private AudioEvent audioEvent;
	private AudioManager mAudioManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        Button backgroundButton = (Button) findViewById(R.id.button_background);
        backgroundButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                //moveTaskToBack(true);
                Toast.makeText(getApplicationContext(), "Listening in background",
                        Toast.LENGTH_LONG).show();

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

                    if (backStack.get(0).equals(".MainActivity")) {
                        moveTaskToBack(true); // or finish() if you want to finish it. I don't.
                    } else {
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
                //Put notification icon in taskbar
                startNotification();
            }
        });

        final ToggleButton startStopButton = (ToggleButton) findViewById(R.id.startStop);
        this.bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        this.buffer = new short[bufferSize];
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        be.hogent.tarsos.dsp.AudioFormat format = new be.hogent.tarsos.dsp.AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        this.audioEvent = new AudioEvent(format, this.buffer.length);

        startStopButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

//                    MainActivity.this.recorder.startRecording();
//                    MainActivity.this.recording = true;
//                    Thread recordingThread = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            MainActivity.this.getAudioData();
//                        }
//                    });
//
//                    recordingThread.start();
                } else {
//                    MainActivity.this.recorder.stop();
//                    MainActivity.this.recording = false;
                }
            }
        });
		
        //Audio Manager
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    // Pause playback
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // Resume playback 
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                	//mAudioManager.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                	mAudioManager.abandonAudioFocus(this);
                    // Stop playback
                }
            }
        };
        
     // Request audio focus for playback
        int result = mAudioManager.requestAudioFocus(afChangeListener,
                                     // Use the music stream.
                                     AudioManager.STREAM_MUSIC,
                                     // Request permanent focus.
                                     AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
           
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Start playback.
        	Log.d(TAG,"Got audio focus");
        	// Abandon audio focus when playback complete    
        	mAudioManager.abandonAudioFocus(afChangeListener);
        }
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.action_settings:
	    	  Intent goToCanvas = new Intent(MainActivity.this, Settings.class);
	    	  //Bundle extras = goToCanvas.getExtras();
	    	 // extras.putExtra("");
	    	  startActivity(goToCanvas);
	        return true;
	    default:
//	        return super.onOptionsItemSelected(item);
            return false;
	    }
	}
    @Override
    protected void onPause() {
        this.recorder.release();
        this.recorder = null;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.recorder == null) {
            this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        }
    }
	
	private void startNotification() {
		mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("ListenUp! is running")
		        .setContentText("Click to resume")
		        .setOngoing(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		
		
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mNotifyId allows you to update the notification later on.
		mNotificationManager.notify(mNotifyId, mBuilder.build());
	}

    private void getAudioData() {
        while (this.recording) {
            this.recorder.read(this.buffer, 0, this.bufferSize);
            Arrays.sort(this.buffer);
            Log.d("ANDREW", "" + this.buffer[0]);
            this.buffer = new short[bufferSize];
        }
    }
}
