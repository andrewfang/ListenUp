package edu.berkeley.cs160.jaguars.listenup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import be.hogent.tarsos.dsp.AudioEvent;

public class MainActivity extends Activity {

	public static final String TAG = "ListenUpMain";
    static final int SAMPLE_RATE = 8000;
    private int bufferSize;
    private short[] buffer;
    private AudioRecord recorder;
    public static boolean running;
    private boolean mIsRecording;
    private AudioEvent audioEvent;
	private AudioManager mAudioManager;
	private AudioTrack audioTrack;
	private OnAudioFocusChangeListener afChangeListener;
    private NotificationManager mNotificationManager;
    public int timer = 5;
    private boolean timeToUpdateMaxAmpBar = false;
    public static boolean careAboutMusic = true;
    public static boolean careAboutLoud = true;
    public static boolean careAboutCall = true;
    public static int sensitivity = 60;
    public boolean defaultLoudSetting = true;
    public boolean defaultCallSetting = true;
    public boolean defaultMusicSetting = true;
    public int defaultSensitivity = 60;
    public static TextToSpeech ttobj;
    private boolean shouldDestroyOnBack;
    private long shouldDestroyOnBackTime;
    private Toast toast;
    private SharedPreferences sharedPref;
    public static String sharedFilename = "ListenUpSharedFile";

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        this.initalizeAudioListener();
        this.initializeAudioManager();
        this.initializeMaxAmpBar();
        this.initializeAudioTrack();
        this.initializeTTS();
        mIsRecording = false;
        this.sharedPref = getSharedPreferences(sharedFilename,0);
        careAboutLoud = this.sharedPref.getBoolean("loudBoolean", defaultLoudSetting);
        careAboutCall = this.sharedPref.getBoolean("callBoolean", defaultCallSetting);
        careAboutMusic = this.sharedPref.getBoolean("musicBoolean", defaultMusicSetting);
        sensitivity = this.sharedPref.getInt("sensitivityInt", defaultSensitivity);

        View settingsView = getLayoutInflater().inflate(R.layout.settings, null);
        CheckBox cBoxLoud = (CheckBox) settingsView.findViewById(R.id.checkBoxLoud);
        CheckBox cBoxMusic = (CheckBox) settingsView.findViewById(R.id.checkBoxMusic);
        CheckBox cBoxCall = (CheckBox) settingsView.findViewById(R.id.checkBoxCall);
        SeekBar pBar = (SeekBar) settingsView.findViewById(R.id.sensitivityBar);
        cBoxLoud.setChecked(careAboutLoud);
        cBoxMusic.setChecked(careAboutCall);
        cBoxCall.setChecked(careAboutMusic);
        pBar.setProgress(pBar.getMax() - sensitivity);
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
        	case R.id.action_help:
        		 AlertDialog dialog = new AlertDialog.Builder(this)
    	         .setMessage(Html.fromHtml("<font color='#359CFC'>" +
        		 "Listen Up notifies you of loud noises so you can safely listen to music while biking! <br>" +
        		 "<br><b> Settings:" +"</b><br><br>"+
        		 "<b><i>Loud Noise/Voice</i></b><br> loud noises will be replayed" +"<br><br>"+
        		 "<b><i>Caller Id</i></b><br> announces incoming caller information" +"<br><br>"+
        		 "<b><i>Pause Music</i></b><br> pauses music when loud sound detected " +"<br></font>"
        		 ))
    	         .setTitle("Help")
    	         .setCancelable(true)
    	         .setNeutralButton(R.string.confirm,
    	            new DialogInterface.OnClickListener() {
    	            public void onClick(DialogInterface dialog, int whichButton){}
    	            })
    	         .show();
    	    	 TextView textView = (TextView) dialog.findViewById(android.R.id.message);
    	    	 textView.setTextSize(15);

    	    	 return true;
            case R.id.action_settings:
                if (this.running) {
                    assert(getApplicationContext() != null);
                    this.toast = Toast.makeText(getApplicationContext(), "Please press stop before changing settings", Toast.LENGTH_SHORT);
                    this.toast.show();
                } else {
                    View settingsView = getLayoutInflater().inflate(R.layout.settings, null);
                    boolean loudness = careAboutLoud;
                    boolean phone = careAboutCall;
                    boolean music = careAboutMusic;
                    int setSensitivity = sensitivity;
                    final CheckBox cBoxLoud = (CheckBox) settingsView.findViewById(R.id.checkBoxLoud);
                    final CheckBox cBoxMusic = (CheckBox) settingsView.findViewById(R.id.checkBoxMusic);
                    final CheckBox cBoxCall = (CheckBox) settingsView.findViewById(R.id.checkBoxCall);
                    final SeekBar pBar = (SeekBar) settingsView.findViewById(R.id.sensitivityBar);
                    View mainView = getLayoutInflater().inflate(R.layout.activity_main, null);
                    cBoxLoud.setChecked(loudness);
                    cBoxMusic.setChecked(music);
                    cBoxCall.setChecked(phone);
                    pBar.setProgress(pBar.getMax() - setSensitivity);

                    final AlertDialog settingsDialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.action_settings)
                            .setView(settingsView)
                            .setPositiveButton(R.string.confirm,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            MainActivity.careAboutLoud = cBoxLoud.isChecked();
                                            MainActivity.careAboutCall = cBoxCall.isChecked();
                                            MainActivity.careAboutMusic = cBoxMusic.isChecked();
                                            MainActivity.sensitivity = pBar.getMax() - pBar.getProgress();
                                            MainActivity.this.updateSensitivityMarker(MainActivity.sensitivity);

                                            MainActivity.this.defaultLoudSetting = careAboutLoud;
                                            MainActivity.this.defaultCallSetting = careAboutCall;
                                            MainActivity.this.defaultMusicSetting = careAboutMusic;
                                            MainActivity.this.defaultSensitivity = sensitivity;

                                            SharedPreferences.Editor editor = MainActivity.this.sharedPref.edit();
                                            editor.putBoolean("loudBoolean", MainActivity.this.defaultLoudSetting);
                                            editor.putBoolean("callBoolean", MainActivity.this.defaultCallSetting);
                                            editor.putBoolean("musicBoolean", MainActivity.this.defaultMusicSetting);
                                            editor.putInt("sensitivityInt", MainActivity.this.defaultSensitivity);
                                            editor.commit();
                                        }
                                    }
                            )
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    }
                            ).create();
                    return true;
                }
            default:
                return false;
        }
    }

    @Override
    protected void onPause() {
        if (this.running) {
            this.startNotification();
        } else {
            if (this.recorder != null) {
                this.recorder.release();
                this.recorder = null;
            }

            if (ttobj != null) {
                ttobj.stop();
                ttobj.shutdown();
            }
        }
        Log.d(TAG,"pausing...");
       
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (this.running) {
            this.startNotification();
        } else {
        	if (this.recorder != null) {
	            this.recorder.release();
	            this.recorder = null;
        	}
            if (ttobj != null) {
                ttobj.stop();
                ttobj.shutdown();
            }
        }
        Log.d(TAG,"stopping...");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed...");
        if (this.shouldDestroyOnBack) {
            if (System.currentTimeMillis() - this.shouldDestroyOnBackTime < 3000) {
                this.finish();
                this.onStop();
                this.onDestroy();
            } else {
                this.shouldDestroyOnBack = false;
            }
        } else {
            this.shouldDestroyOnBack = true;
            this.shouldDestroyOnBackTime = System.currentTimeMillis();
            assert(getApplicationContext() != null);
            this.toast = Toast.makeText(getApplicationContext(), "Press back again to quit", Toast.LENGTH_SHORT);
            this.toast.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.recorder == null) {
            this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        }
        if(ttobj == null) {
        	this.initializeTTS();
        }
        if (this.mNotificationManager != null) {
            this.mNotificationManager.cancelAll();
        }
        
    }

    @Override
    protected void onDestroy(){
        Log.d(TAG, "destroying...");
    	if(this.recorder != null) {
            if (this.mIsRecording) {
                this.recorder.stop();
            }
    		this.recorder.release();
    		this.recorder = null;
    	}
        if (this.mNotificationManager != null) {
            this.mNotificationManager.cancelAll();
        }
        //turn off recording and call monitoring
        running = false;
        this.mIsRecording = false;
        if (ttobj != null) {
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Constructs the task in the notification bar
     */
	private void startNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle("ListenUp! is running")
                .setContentText("Click to resume")
                .setOngoing(true);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		//resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// mNotifyId allows you to update the notification later on.
        int mNotifyId = 1;

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, mNotifyId);
//		// The stack builder object will contain an artificial back stack for the
//		// started Activity.
//		// This ensures that navigating backward from the Activity leads out of
//		// your application to the Home screen.
//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//		// Adds the back stack for the Intent (but not the Intent itself)
//		stackBuilder.addParentStack(MainActivity.class);
//		// Adds the Intent that starts the Activity to the top of the stack
//		stackBuilder.addNextIntent(resultIntent);
//		PendingIntent resultPendingIntent =
//		        stackBuilder.getPendingIntent(
//		            0,
//		            PendingIntent.FLAG_UPDATE_CURRENT
//		        );
//
//
		mBuilder.setContentIntent(resultPendingIntent);
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        this.mNotificationManager.notify(mNotifyId, mBuilder.build());
	}

    /**
     * This is the method called by @+id/button_background in activity_main.xml
     */
    public void runInBackground(View view) {
        if (this.running) {
//            Toast.makeText(getApplicationContext(), "Listening in background", Toast.LENGTH_LONG).show();

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
        } else {
            this.toast = Toast.makeText(getApplicationContext(), "Please press start first", Toast.LENGTH_SHORT);
            this.toast.show();
        }
    }

    /**
     * This is the method called by @+id/startStop in activity_main.xml
     */
    public void startStop(View view) {
        ToggleButton startStopButton = (ToggleButton) findViewById(R.id.startStop);
        boolean isChecked = startStopButton.isChecked();
        if (isChecked) {
        	
            this.running = true;
            Toast.makeText(getApplicationContext(), "Listening in background", Toast.LENGTH_SHORT).show();
            //I'm going to wrap the entire recording/listening thing in this careAboutLoud boolean. May need to move it
            if (careAboutLoud) {
                if (this.recorder == null) {
                    this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                }
                this.recorder.startRecording();
                Thread recordingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.processAudioData();
                        if (mIsRecording) {
                            mIsRecording = false;
                        } else {
                            mIsRecording = true;
                            // STEP 4: start recording
                            //loopbackAudio();
                            // END STEP 4
                        }
                    }
                });
                recordingThread.start();
            }

        } else {
            this.running = false;
//                    MainActivity.this.recorder.stop();
        }
    }
    
    
    private void initializeAudioTrack() {
    		// STEP 2: setup AudioTrack - for audio output
    		audioTrack = new AudioTrack(
    		AudioManager.STREAM_MUSIC,
    		SAMPLE_RATE,
    		AudioFormat.CHANNEL_OUT_MONO,
    		AudioFormat.ENCODING_PCM_16BIT,
    		bufferSize,
    		AudioTrack.MODE_STREAM);
    }
    
 // STEP 3: while the audio is recording, play back the audio
 	public void loopbackAudio() {
 		
 		int result = 0;
 		
 		// Check if we are supposed to pause music
 		if(this.careAboutMusic) {
	        // Request audio focus for playback
	        result = this.mAudioManager.requestAudioFocus(afChangeListener,
	                // Use the music stream.
	                AudioManager.STREAM_MUSIC,
	                // Request transient focus.
	                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
 		}

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED || !this.careAboutMusic) {
        	
        	//set the stream volume - should we always do this?
        	int musicVolume = this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        	int maxVolume = this.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        	Log.d(TAG, String.valueOf(musicVolume));
        	Log.d(TAG, String.valueOf(maxVolume));
        	
        	if(maxVolume > musicVolume) {
        		int loudVolume = musicVolume + 3;
        		if (loudVolume > maxVolume) {
        			loudVolume = maxVolume;
        		}
        		this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, loudVolume, 0);
        	}
        	
	 		//recorder.startRecording();
	 		audioTrack.play();

 		//Thread loopbackThread = new Thread(new Runnable() {

 			//@Override
 			//public void run() {
 				
 				while (timer > 0) {
 					int bufferReadResult = recorder.read(buffer, 0, buffer.length);
 					audioTrack.write(buffer, 0, bufferReadResult);
 					timer = timer - 1;
 				}
 				timer = 5;
 				//recorder.stop();
 				audioTrack.stop();
 				audioTrack.flush();
 				this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0);
 		//	}
 	//	});
 		//loopbackThread.start();
        }
        
        // Abandon audio focus when playback complete
        this.mAudioManager.abandonAudioFocus(afChangeListener);
 	}
    /**
     * This sets up a buffer and instantiates a recorder that we will use to detect sound
     */
    private void initalizeAudioListener() {
        this.bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        this.buffer = new short[bufferSize];
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        be.hogent.tarsos.dsp.AudioFormat format = new be.hogent.tarsos.dsp.AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        this.audioEvent = new AudioEvent(format, this.buffer.length);
    }

    private void initializeAudioManager() {
        //Audio Manager
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        afChangeListener = new OnAudioFocusChangeListener() {
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

    }

//    //Play the loud sound
//    //Should we do all of this in a new thread?
//    private void playSound() {
//        // Request audio focus for playback
//        int result = this.mAudioManager.requestAudioFocus(afChangeListener,
//                // Use the music stream.
//                AudioManager.STREAM_MUSIC,
//                // Request transient focus.
//                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
//
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//
//            //Mute music stream
//            this.mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
//
//            // Start playback
//            //Replace this sound with the microphone audio
//           MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.carhonk1);
//           mediaPlayer.start();
//            //Log.d(TAG,"Got audio focus");
//
//            //Pause for some seconds.
//
//            // Abandon audio focus when playback complete
//            this.mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
//            this.mAudioManager.abandonAudioFocus(afChangeListener);
//        }
//    }

    private void processAudioData() {
        while (this.running) {
            //need to reinitialize recorder because it might be null??
            if (this.recorder == null) {
                this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            }
            if (this.timeToUpdateMaxAmpBar) {
                this.timeToUpdateMaxAmpBar = false;
                this.recorder.read(this.buffer, 0, this.bufferSize);
                Arrays.sort(this.buffer);
                final int maxAmp = Math.max(this.buffer[0], this.buffer[this.bufferSize - 1]);
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        ProgressBar maxAmpBar = (ProgressBar) findViewById(R.id.maxAmpBar);
                        maxAmpBar.setProgress(maxAmp);
                    }
                });
                int CUTOFF = (int) (this.sensitivity / 100.0 * 50000.0);
                if (maxAmp > CUTOFF) {
                    Log.d(TAG, "Loud sound detected. sensitivity value= " + this.sensitivity);
                    Log.d(TAG, "Loud sound detected. CUTOFF value= " + CUTOFF);
                    loopbackAudio();
                    //playSound();

                }
                this.buffer = new short[bufferSize];
            } else {
                this.timeToUpdateMaxAmpBar = true;
            }
        }
    }

    private void updateSensitivityMarker(int value) {
        final int val = value;
        this.runOnUiThread(new Runnable() {
            public void run() {
                ProgressBar thresholdBar = (ProgressBar) findViewById(R.id.maxAmpBarBack);
                thresholdBar.setProgress(val);
            }
        });
    }

    private void initializeMaxAmpBar(){
        ProgressBar maxAmpBar = (ProgressBar) findViewById(R.id.maxAmpBar);
        maxAmpBar.setMax(39999);
    }
    
    private void initializeTTS(){
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
                            }
                        } else {
                            Log.e("TTS", "Initilization Failed!");
                            Toast.makeText(getApplicationContext(), "Initilization Failed!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

//                    public void speakText(String callInfo, String callNo) {
//                        if (callInfo == "Unknown Caller"){
//                            callInfo = callNo;
//                        }
//                        ttobj.speak(callInfo, TextToSpeech.QUEUE_FLUSH, null);
//                    }
                });
    }

}
