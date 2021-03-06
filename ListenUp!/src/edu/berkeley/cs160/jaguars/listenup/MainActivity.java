package edu.berkeley.cs160.jaguars.listenup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
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

public class MainActivity extends Activity {

	public static final String TAG = "ListenUpMain";
	
	/* Audio record stuff */
    static final int SAMPLE_RATE = 8000;
    private int bufferSize;
    private short[] buffer;
    private AudioRecord recorder;
    private AudioTrack audioTrack;
    public static boolean running;
    private boolean mIsRecording;
    public int timer = 20;
    private int musicVolume;
    //private AudioEvent audioEvent;
	
    /* Settings stuff */
    private boolean timeToUpdateMaxAmpBar = false;
    public static boolean careAboutMusic = true;
    public static boolean careAboutLoud = true;
    public static boolean careAboutCall = true;
    public static int sensitivity = 60;
    public boolean defaultLoudSetting = true;
    public boolean defaultCallSetting = true;
    public boolean defaultMusicSetting = true;
    public int defaultSensitivity = 60;
   
    private boolean shouldDestroyOnBack;
    private long shouldDestroyOnBackTime;
    private Toast toast;
    private SharedPreferences sharedPref;
    public static String sharedFilename = "ListenUpSharedFile";
    
    /* Notify, audio manager, text to speech stuff */
    public static AudioManager mAudioManager;
	public static OnAudioFocusChangeListener afChangeListener;
    private NotificationManager mNotificationManager;
    public static TextToSpeech ttobj;
    
	/* Timer stuff */
	private Timer timeoutTimer = new Timer();
	private TimerTask timerTask;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Initialize everything
        this.initalizeAudioListener();
        this.initializeAudioManager();
        this.initializeMaxAmpBar();
        this.initializeAudioTrack();
        this.initializeTTS();
        mIsRecording = false;
        
        //Set up saved settings
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
        updateSensitivityMarker(sensitivity);
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

                    settingsDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    settingsDialog.show();
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
		// Creates an explicit intent 
		Intent resultIntent = new Intent(this, MainActivity.class);

		// mNotifyId allows you to update the notification later on.
        int mNotifyId = 1;

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, mNotifyId);

		mBuilder.setContentIntent(resultPendingIntent);
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        this.mNotificationManager.notify(mNotifyId, mBuilder.build());
	}

    /**
     * This is the method called by @+id/button_background in activity_main.xml
     */
    public void runInBackground(View view) {
        if (this.running) {
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
                        }
                    }
                });
                recordingThread.start();
            }

        } else {
            this.running = false;
        }
    }
    
 // STEP 2: setup AudioTrack - for audio output
    private void initializeAudioTrack() {
    		
    	audioTrack = new AudioTrack(
    		AudioManager.STREAM_MUSIC,
    		SAMPLE_RATE,
    		AudioFormat.CHANNEL_OUT_MONO,
    		AudioFormat.ENCODING_PCM_16BIT,
    		bufferSize,
    		AudioTrack.MODE_STREAM);
    }
    
    // STEP 3: while the audio is recording, play back the audio
 	public void loopbackAudio(int maxAmp) {
 		
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
        	int maxVolume;
        	int currentAmp;
        	int CUTOFF = (int) (this.sensitivity / 100.0 * 50000.0);
        	
        	//set the stream volume to higher than current level
        	musicVolume = this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        	maxVolume = this.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        	
        	if(this.careAboutMusic && maxVolume > musicVolume) {
        		int loudVolume = musicVolume + 3;
        		if (loudVolume > maxVolume) {
        			loudVolume = maxVolume;
        		}
        		this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, loudVolume, 0);
        	}
        	
        	//loop until loud sound is gone
        	while(maxAmp > CUTOFF) {
	 			audioTrack.play();
 				
	 			maxAmp = 0;
	 			//play 20 ticks at a time
 				while (timer > 0) {
 					int bufferReadResult = recorder.read(buffer, 0, buffer.length);
 					audioTrack.write(buffer, 0, bufferReadResult);
 					timer = timer - 1;
 					currentAmp = Math.abs(buffer[0]);
 					maxAmp = Math.max(currentAmp, maxAmp);
 				}
 				timer = 20;
 				
        	}
        	
            //Pause for a bit after playback
        	timeoutTimer = new Timer();
    		timerTask = new TimerTask() {

    			@Override
    			public void run() {
    				audioTrack.stop();
    				audioTrack.flush();
    		        // Abandon audio focus when playback complete
    				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0);
    		        mAudioManager.abandonAudioFocus(afChangeListener);
    			}	
    		};		
    		timeoutTimer.schedule(timerTask, 1000);
 			
        }
        
 	}
 	
    /**
     * This sets up a buffer and instantiates a recorder that we will use to detect sound
     */
    private void initalizeAudioListener() {
        this.bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        this.buffer = new short[bufferSize];
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        be.hogent.tarsos.dsp.AudioFormat format = new be.hogent.tarsos.dsp.AudioFormat(SAMPLE_RATE, 16, 1, true, true);
        //this.audioEvent = new AudioEvent(format, this.buffer.length);
    }

    
    /**
     * This sets up an AudioManager object to manage audio focus
     */
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

    private void processAudioData() {
        while (this.running) {
            //need to reinitialize recorder because it might be null
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
//                    Log.d(TAG, "Loud sound detected. sensitivity value= " + this.sensitivity);
//                    Log.d(TAG, "Loud sound detected. CUTOFF value= " + CUTOFF);
                    loopbackAudio(maxAmp);

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
//                                Log.e("TTS", "This Language is not supported");
                                Toast.makeText(getApplicationContext(), "This Language is not supported",
                                        Toast.LENGTH_SHORT).show();
                                Intent installIntent = new Intent();
                                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                startActivity(installIntent);
                            }
                        } else {
//                            Log.e("TTS", "Initilization Failed!");
                            Toast.makeText(getApplicationContext(), "Initilization Failed!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

}
