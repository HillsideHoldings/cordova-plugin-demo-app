/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.billinar.mediabufferplugin;

import java.io.IOException;

import org.apache.cordova.CordovaActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends CordovaActivity implements
		OnBufferingUpdateListener, OnInfoListener, OnPreparedListener,
		OnErrorListener {
	MediaPlayer player = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//initPlayer();
		// Set by <content src="index.html" /> in config.xml
		loadUrl(launchUrl);
		
	}
	
	public void initPlayer() {
		String url = "http://www.songspk320z.us/songoftheday/[Songs.PK]%20Khaike%20Paan%20Banaraswala%20-%20Don%20(2006).mp3";
//		url = "http://192.168.1.4:81/mediaplayer/tum.mp3";
		try {
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		    player.setDataSource(url);
		    
			
//			File file = new File(path);
//			FileInputStream inputStream = new FileInputStream(file);
//			player.setDataSource(inputStream.getFD());
//			inputStream.close();
			
			
		    player.setOnErrorListener(this);
			player.setOnBufferingUpdateListener(this);
			player.setOnInfoListener(this);
			player.setOnPreparedListener(this);
			startPlayer();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void startPlayer(){
		//player.reset();
		player.prepareAsync();
	}
	
	
	public void stopPlayer() {
		if (player != null) {
			if (player.isPlaying())
				player.stop();
			player.release();
			player = null;
		}
	}

	@Override
	public void onPrepared(MediaPlayer p) {
		player.start();
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		Log.i(TAG, "THERE WAS AN ERROR");
		return false;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.i(TAG, "Within bufferingupdate = " + percent);

	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if(player!=null)
		player.pause();
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		stopPlayer();
		super.onDestroy();
	}

}
