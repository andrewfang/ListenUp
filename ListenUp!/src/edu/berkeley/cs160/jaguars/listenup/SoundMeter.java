package edu.berkeley.cs160.jaguars.listenup;

import android.media.MediaRecorder;

public class SoundMeter{
    private MediaRecorder recorder;

    /**
     * Starts recording. If there is no recorder yet, make one.
     */
    public void start() {
        if (recorder == null) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile("/dev/null");
           // recorder.prepare();
        }
        recorder.start();
    }

    /**
     * Stops recording
     */
    public void stop() {
        if (recorder != null) {
            recorder.stop();
        }
    }

    /**
     * Destroys the recorder
     */
    public void kill() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    /**
     * Gets the max amplitude heard.
     */
    public double getAmplitude() {
        if (recorder != null) {
            return recorder.getMaxAmplitude();
        } else {
            return 0;
        }
    }
}
