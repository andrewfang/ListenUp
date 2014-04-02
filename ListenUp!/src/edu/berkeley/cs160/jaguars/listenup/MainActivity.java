package edu.berkeley.cs160.jaguars.listenup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
<<<<<<< HEAD
import android.widget.Button;
import android.widget.TextView;
=======
>>>>>>> b642c87dbfc90358cd479c532d0629248268b80e
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String TAG = "ListenUpMain";
	private int mNotifyId = 1;
	private NotificationCompat.Builder mBuilder;
	
	Button start;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        start = (Button) findViewById(R.id.button_start);
        start.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//moveTaskToBack(true);
				Toast.makeText(getApplicationContext(), "Listening in background",
						   Toast.LENGTH_LONG).show();
				
			     ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE);
		            List<RunningTaskInfo> runningTaskInfoList =  am.getRunningTasks(10);
		            List<String> backStack = new ArrayList<String>();
		            Iterator<RunningTaskInfo> itr = runningTaskInfoList.iterator();
		            while(itr.hasNext()){
		                RunningTaskInfo runningTaskInfo = (RunningTaskInfo)itr.next();
		                String topActivity = runningTaskInfo.topActivity.getShortClassName();
		                backStack.add(topActivity.trim());
		            }
		            if(backStack!=null){
		        		            		
		                if(backStack.get(0).equals(".MainActivity")){
		                    moveTaskToBack(true); // or finish() if you want to finish it. I don't.
		                } else {
		                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
		                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		                    startActivity(intent);
		                    finish();
		                }
		            }
			}
        });
        
//		if (savedInstanceState == null) {
//			getFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
		
		//Put notification icon in taskbar
		startNotification();
		
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
	    case R.id.start_page:
	    	/* AlertDialog dialog = new AlertDialog.Builder(this)
	         .setMessage(Html.fromHtml("<font color='#359CFC'>" + "In Home"))
	         .setTitle("Menu")
	         .setCancelable(true)
	         .setNeutralButton(android.R.string.ok,
	            new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton){}
	            })
	         .show();
	    	TextView textView = (TextView) dialog.findViewById(android.R.id.message);
	    	
	    	 textView.setTextSize(14);
	    	 
	    	 break;
	    	 */
	    case R.id.action_settings:
	    	  Intent goToCanvas = new Intent(MainActivity.this, Settings.class);
	    	  //Bundle extras = goToCanvas.getExtras();
	    	 // extras.putExtra("");
	    	  startActivity(goToCanvas);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	//	return false;
	}

<<<<<<< HEAD

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
=======
>>>>>>> b642c87dbfc90358cd479c532d0629248268b80e
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

    public void start() {
        Toast toast = Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_SHORT);
        toast.show();
        Log.d("ANDREW", "aldkfalkd");
    }

}
