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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
//	    switch (item.getItemId()) {
//	    case R.id.start_page:
//	    	Intent goToCanvas = new Intent(Settings.this, MainActivity.class);
//	    	  //Bundle extras = goToCanvas.getExtras();
//	    	 // extras.putExtra("");
//	    	 startActivity(goToCanvas);
//	    default:
	        return super.onOptionsItemSelected(item);
//	    }
	}
}
