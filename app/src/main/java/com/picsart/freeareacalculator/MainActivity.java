package com.picsart.freeareacalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final FreeSpaceView freeSpaceView = findViewById(R.id.freeSpaceView);

		final Button button = findViewById(R.id.previewBtn);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				freeSpaceView.changePreviewMode();
				button.setText(freeSpaceView.isShowPreview() ? "Hide Preview" : "Show Preview");
			}
		});
		findViewById(R.id.addRect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				freeSpaceView.addRect();
			}
		});
	}


}
