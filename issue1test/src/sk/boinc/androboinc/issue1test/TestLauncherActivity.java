/* 
 * Copyright (C) 2010, Pavol Michalec
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package sk.boinc.androboinc.issue1test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TestLauncherActivity extends Activity {
	private static final String TAG = "TestLauncherActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		setContentView(R.layout.main);

		Button theButton;

		theButton = (Button)findViewById(R.id.button1);
		theButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Launching ManageClientActivity...");
				startActivity(new Intent(TestLauncherActivity.this, ManageClientActivity.class));
			}
		});

		theButton = (Button)findViewById(R.id.button2);
		theButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Launching ManageClientActivity2...");
				startActivity(new Intent(TestLauncherActivity.this, ManageClientActivity2.class));
			}
		});

		theButton = (Button)findViewById(R.id.button3);
		theButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Launching ManageClientActivity3...");
				startActivity(new Intent(TestLauncherActivity.this, ManageClientActivity3.class));
			}
		});

		theButton = (Button)findViewById(R.id.button4);
		theButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Launching ManageClientActivity4...");
				startActivity(new Intent(TestLauncherActivity.this, ManageClientActivity4.class));
			}
		});

		theButton = (Button)findViewById(R.id.button5);
		theButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Launching ManageClientActivity5...");
				startActivity(new Intent(TestLauncherActivity.this, ManageClientActivity5.class));
			}
		});
	}
}