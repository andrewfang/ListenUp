package edu.berkeley.cs160.jaguars.listenup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Settings extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		/*
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		*/
		
		
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
	    	Intent goToCanvas = new Intent(Settings.this, MainActivity.class);
	    	  //Bundle extras = goToCanvas.getExtras();
	    	 // extras.putExtra("");
	    	 startActivity(goToCanvas);
	    	 
	    case R.id.action_settings:
	    	/*
	    	 *  AlertDialog dialog = new AlertDialog.Builder(this)
	    	 
	         .setMessage(Html.fromHtml("<font color='#359CFC'>" + "In Settings"))
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
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
