package org.apache.cordova.media;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class MediaButtonReceiver extends WakefulBroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		final String intentAction = intent.getAction();
		Log.d("PluginBroadCast", "intent received --- ");
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			
			
		}else{
			
		}
		
	}

}
