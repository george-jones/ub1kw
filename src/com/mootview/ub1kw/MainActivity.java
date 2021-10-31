package com.mootview.ub1kw;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {
	private File extdir;
	private String currLang;
	private int currPage;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currLang = "arabic";
		currPage = 0;
		setContentView(R.layout.activity_main);
		extdir = Environment.getExternalStorageDirectory();	
		
		//setVolumeControlStream()
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		EditText pageNumText = (EditText) findViewById(R.id.pagenum);
		pageNumText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView tv, int ev, KeyEvent kev) {
				if (ev == EditorInfo.IME_ACTION_SEARCH) {
					String pnumstr = tv.getText().toString();
					if (pnumstr.length() > 0) {
						try {
							int pnum = Integer.parseInt(pnumstr);
							if (pnum != currPage) {
								gotoPage(pnum);
								findViewById(R.id.content).requestFocus();
							}
						} catch (NumberFormatException nfe) {
							tv.setText("");
						}
					}
				}
				return false;
			}
		});
		
	}
	
	private String extReadWholeFile(String fn) {
		String s = null;
		String state = Environment.getExternalStorageState();
		File file = new File(extdir, fn);
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			try {				
				InputStream os = new FileInputStream(file);				
				byte buffer[] = new byte[(int) file.length()];
				os.read(buffer);
				s = new String(buffer).trim();
			} catch (IOException ioe) {
				Toast.makeText(getApplicationContext(), ioe.toString(), Toast.LENGTH_LONG).show();
			}
		}
		return s;				
	}	
	
	private void gotoLang(String lang) {
		currLang = lang;
		if (currPage > 0) {
			gotoPage(currPage);
		}
	}
	
	private void gotoPage(int pnum) {
		if (pnum % 2 == 1) {
			// even pages only
			pnum++;
		}
		currPage = pnum;
		
		String pageData = extReadWholeFile("ub1kw/" + currLang + "/data" + pnum + ".txt");
		if (pageData != null) {
			String lines[] = pageData.split("\n");
			LinearLayout ll = (LinearLayout) findViewById(R.id.content);
			//Toast t = Toast.makeText(getApplicationContext(), "" + lines.length, Toast.LENGTH_SHORT);
			//t.show();
			// kill all the children of the content view
			ll.removeAllViews();
			for (int i=0; i < lines.length; i += 2) {
				String desc = lines[i];
				final String fname = lines[i+1];
				if (desc != null && fname != null) {
					Button b = new Button(this);
					b.setText(desc);
					LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					ll.addView(b, lp);
					b.setOnClickListener(new View.OnClickListener() {						
						@Override
						public void onClick(View v) {
							//Toast t = Toast.makeText(v.getContext(), fname, Toast.LENGTH_SHORT);
							//t.show();
							MediaPlayer mp = new MediaPlayer();
							try {
								FileDescriptor fd = null;
						        String audioPath = extdir + "/ub1kw/" + currLang + "/" + fname;
						        FileInputStream fis = new FileInputStream(audioPath);
						        fd = fis.getFD();																
						        if (fd != null) {
						            MediaPlayer mediaPlayer = new MediaPlayer();
						            mediaPlayer.setDataSource(fd);
						            mediaPlayer.prepare();
						            mediaPlayer.start();
						        }
							} catch (Exception e) {
								Toast t = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
								t.show();								
							}
							
						}
					});
				}				
			}
		}
		
	}

	OnMenuItemClickListener langClickListener = new OnMenuItemClickListener() {
	    @Override
	    public boolean onMenuItemClick(MenuItem item) {
			String langs[] = new String[4];	
			langs[0] = "arabic";
			langs[1] = "chinese";
			langs[2] = "russian";
			langs[3] = "spanish";
			
			gotoLang(langs[item.getItemId()]);
			
			return false;
	    }
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		int ids[] = new int[4];		
		String langdesc[] = new String[4];
		
		langdesc[0] = "Arabic";
		langdesc[1] = "Chinese";
		langdesc[2] = "Russian";
		langdesc[3] = "Spanish";		
		
		for (int i=0; i<4; i++) {
			MenuItem item = menu.add(0, i, i, langdesc[i]);
			item.setOnMenuItemClickListener(langClickListener);		
		}
		
		//return super.onCreateOptionsMenu(menu);
		return true;
	}

}
