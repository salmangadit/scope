package com.example.scope;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ResultActivity extends Activity {
	List<SegmentationResult> ocrResults;
	Button home;
	final Context a=this;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getActionBar();
		ab.setTitle("Title");
		ab.setDisplayShowTitleEnabled(false);
		ab.setSubtitle("Subtitle");
		ab.setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_result);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //ocrText = getIntent().getStringExtra("ocrText");
        
        Globals appState = ((Globals) getApplicationContext());
		ocrResults = appState.getSegmentationResult();
		
		
        //result = (TextView) findViewById(R.id.resultLabel);
		final ListView lv = (ListView) findViewById(R.id.srListView);
		lv.setAdapter(new ResultsAdapter(this, ocrResults));
		
        home = (Button) findViewById(R.id.button1);
        //result.setText(ocrText);
        
        home.setOnClickListener(new ButtonClickHandler());
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_result, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
 // Upon button click
 	public class ButtonClickHandler implements View.OnClickListener {
 		@Override
 		public void onClick(View view) {
 			// Activity to open Main
 			Intent intent = new Intent(a, MainActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 		}
 	}

}
