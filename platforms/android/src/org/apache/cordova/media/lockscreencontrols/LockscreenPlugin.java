package org.apache.cordova.media.lockscreencontrols;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.media.lockscreencontrols.MediaSessionController.MediaAction;
import org.apache.cordova.media.lockscreencontrols.MediaSessionController.MediaState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Picasso.LoadedFrom;

public class LockscreenPlugin extends CordovaPlugin {
	public static String TAG = "LockscreenPlugin";

	private CallbackContext messageChannel;
	private MediaSessionController lockScreenController;

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {

		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";
		if (action.equals("init")) {
			setupLockScreen(this.cordova.getActivity());
		} else if (action.equals("setState")) {
			String stat = args.getString(0);
			setState(stat);
		} else if (action.equals("setMetadata")) {
			JSONObject obj = args.getJSONObject(0);
			setMetadata(obj);
		} else if (action.equals("release")) {
			release();
		} else if (action.equals("listenActions")) {
			messageChannel = callbackContext;
			listenActions();
			return true;
		}

		callbackContext.sendPluginResult(new PluginResult(status, result));

		return true;
	}

	private void setupLockScreen(Context context) {
		this.lockScreenController = new MediaSessionController(context);
	}

	private void setState(String s) {
		MediaState state = Enum.valueOf(MediaState.class, s);
		if (lockScreenController != null)
			lockScreenController.setMediaState(state);
	}

	private void setMetadata(JSONObject args) throws JSONException {
		if (args != null) {
			final String title = args.has("title") ? args.getString("title") : "";
			final String subTitle = args.has("subTitle") ? args.getString("subTitle")
					: "";
			final long duration = args.has("duration") ? args.getLong("duration") : 0;
			String image = args.has("image") ? args.getString("image") : "";
			
			if (!image.isEmpty()) {
				Picasso.with(cordova.getActivity()).load(image)
						.into(new Target() {

							@Override
							public void onBitmapLoaded(Bitmap b,
									LoadedFrom arg1) {
								// TODO Auto-generated method stub
								lockScreenController.setMetaData(title,
										subTitle, duration, b);
							}

							@Override
							public void onBitmapFailed() {
								// TODO Auto-generated method stub
								lockScreenController.setMetaData(title,
										subTitle, duration, null);
							}
						});
			} else {
				lockScreenController.setMetaData(title, subTitle, duration,
						null);
			}

		}

	}

	private void release() {
		if (lockScreenController != null)
			lockScreenController.release();
	}

	private void listenActions() {
		if (lockScreenController != null) {
			lockScreenController
					.setMediaActionListener(new MediaActionListener() {

						@Override
						public void onMediaAction(MediaAction action) {
							sendEventMessage(action.toString(), null);
						}
					});
		}
	}

	void sendEventMessage(String action, JSONObject actionData) {
		JSONObject message = new JSONObject();
		try {
			message.put("action", action);
			if (actionData != null) {
				message.put(action, actionData);
			}
		} catch (JSONException e) {
			Log.e(TAG, "Failed to create event message", e);
		}

		PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
				message);
		pluginResult.setKeepCallback(true);
		if (messageChannel != null) {
			messageChannel.sendPluginResult(pluginResult);
		}
	}

}
