package org.apache.cordova.media.lockscreencontrols;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Created by Bilal on 24/04/2016.
 */
public class MediaSessionController implements MusicFocusable {

	public enum MediaAction {
		PLAY_PAUSE, REWIND, FORWARD, STOP, PLAY, PAUSE
	}

	public enum MediaState {
		PLAYING, PAUSED, STOPPED
	}

	// do we have audio focus?
	enum AudioFocus {
		NoFocusNoDuck, // we don't have audio focus, and can't duck
		NoFocusCanDuck, // we don't have focus, but can play at a low volume
						// ("ducking")
		Focused // we have full audio focus
	}

	AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;
	// our AudioFocusHelper object, if it's available (it's available on SDK
	// level >= 8)
	// If not available, this will be null. Always check for null before using!
	AudioFocusHelper mAudioFocusHelper = null;

	private Context mContext;
	private MediaSessionCompat mMediaSessionCompat;
	private MediaControllerCompat.TransportControls mTransportController;
	private PlaybackStateCompat.Builder mPlaybackStateCompatBuilder;
	private MediaActionListener mediaActionListener = null;

	public MediaSessionController(Context context) {
		this.mContext = context;

		// create the Audio Focus Helper, if the Audio Focus feature is
		// available (SDK 8 or above)
		if (android.os.Build.VERSION.SDK_INT >= 8)
			mAudioFocusHelper = new AudioFocusHelper(context, this);
		else
			mAudioFocus = AudioFocus.Focused; // no focus feature, so we always
												// "have" audio focus

		ComponentName mRemoteControlResponder = new ComponentName(context,
				MusicIntentReceiver.class);

		mMediaSessionCompat = new MediaSessionCompat(mContext, "ElearnPlayer",
				mRemoteControlResponder, null);

		mMediaSessionCompat
				.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
						| MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		mPlaybackStateCompatBuilder = new PlaybackStateCompat.Builder()
				.setActions(
						PlaybackStateCompat.ACTION_SEEK_TO
								| PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
								| PlaybackStateCompat.ACTION_SKIP_TO_NEXT
								| PlaybackStateCompat.ACTION_PLAY
								| PlaybackStateCompat.ACTION_PAUSE
								| PlaybackStateCompat.ACTION_STOP).setState(
						PlaybackStateCompat.STATE_STOPPED,
						PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
		mMediaSessionCompat.setPlaybackState(mPlaybackStateCompatBuilder
				.build());

		mMediaSessionCompat.setCallback(mMediaSessionCallback);
		// mMediaSessionCompat.setSessionActivity(retrievePlaybackActions(5));
		mMediaSessionCompat.setActive(true);

		mTransportController = mMediaSessionCompat.getController()
				.getTransportControls();

		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicIntentReceiver.ACTION);
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}

	public void setMediaState(MediaState mediaState) {
		switch (mediaState) {
		case PLAYING:
			tryToGetAudioFocus();
			mPlaybackStateCompatBuilder.setState(
					PlaybackStateCompat.STATE_PLAYING,
					PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
			mMediaSessionCompat.setPlaybackState(mPlaybackStateCompatBuilder
					.build());
			break;

		case PAUSED:
			mPlaybackStateCompatBuilder.setState(
					PlaybackStateCompat.STATE_PAUSED,
					PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
			mMediaSessionCompat.setPlaybackState(mPlaybackStateCompatBuilder
					.build());
			break;

		case STOPPED:
			giveUpAudioFocus();

			mPlaybackStateCompatBuilder.setState(
					PlaybackStateCompat.STATE_STOPPED,
					PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
			mMediaSessionCompat.setPlaybackState(mPlaybackStateCompatBuilder
					.build());

			break;
		}
	}

	public void setMetaData(String title, String subTitle, long duration,
			Bitmap backgroundImage) {

		// if (backgroundImage == null) {
		// backgroundImage =
		// BitmapFactory.decodeResource(mContext.getResources(),
		// R.drawable.sample_img);
		// }

		MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
		builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, subTitle);
		builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);
		builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
		builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
				backgroundImage);

		// builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
		// BitmapFactory.decodeResource(mContext.getResources(),
		// R.drawable.sample_img));
		mMediaSessionCompat.setMetadata(builder.build());
	}

	private void tryToGetAudioFocus() {
		if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
				&& mAudioFocusHelper.requestFocus())
			mAudioFocus = AudioFocus.Focused;
	}

	void giveUpAudioFocus() {
		if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
				&& mAudioFocusHelper.abandonFocus())
			mAudioFocus = AudioFocus.NoFocusNoDuck;
	}

	@Override
	public void onGainedAudioFocus() {
		Toast.makeText(mContext, "gained audio focus.", Toast.LENGTH_SHORT)
				.show();
		mAudioFocus = AudioFocus.Focused;
	}

	@Override
	public void onLostAudioFocus(boolean canDuck) {
		Toast.makeText(mContext,
				"lost audio focus." + (canDuck ? "can duck" : "no duck"),
				Toast.LENGTH_SHORT).show();
		mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck
				: AudioFocus.NoFocusNoDuck;
	}

	MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {
		@Override
		public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
			Toast.makeText(mContext, "Media session keycode received -- ",
					Toast.LENGTH_SHORT).show();
			final String intentAction = mediaButtonEvent.getAction();
			if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
				final KeyEvent event = mediaButtonEvent
						.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if (event == null)
					return super.onMediaButtonEvent(mediaButtonEvent);
				final int keycode = event.getKeyCode();
				final int action = event.getAction();
				if (event.getRepeatCount() == 0
						&& action == KeyEvent.ACTION_DOWN) {
					switch (keycode) {
					case KeyEvent.KEYCODE_HEADSETHOOK:
						break;
					case KeyEvent.KEYCODE_MEDIA_STOP:
						mTransportController.stop();
						break;
					case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
						mTransportController.play();
						break;
					case KeyEvent.KEYCODE_MEDIA_NEXT:
						mTransportController.skipToNext();
						break;
					case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
						mTransportController.skipToPrevious();
						break;
					case KeyEvent.KEYCODE_MEDIA_PAUSE:
						mTransportController.pause();
						break;
					case KeyEvent.KEYCODE_MEDIA_PLAY:
						mTransportController.play();
						break;
					}
				}
			}
			// return true;
			return super.onMediaButtonEvent(mediaButtonEvent);
		}

		@Override
		public void onPlay() {
			Toast.makeText(mContext, "OnPlay", Toast.LENGTH_SHORT).show();
			super.onPlay();
		}

		@Override
		public void onPause() {
			Toast.makeText(mContext, "onPause", Toast.LENGTH_SHORT).show();
			super.onPause();
		}

		@Override
		public void onSkipToNext() {
			Toast.makeText(mContext, "onSkipToNext", Toast.LENGTH_SHORT).show();
			super.onSkipToNext();
		}

		@Override
		public void onSkipToPrevious() {
			Toast.makeText(mContext, "onSkipToPrevious", Toast.LENGTH_SHORT)
					.show();
			super.onSkipToPrevious();
		}

		@Override
		public void onSeekTo(long pos) {
			Toast.makeText(mContext, "onSeekTo", Toast.LENGTH_SHORT).show();
			super.onSeekTo(pos);
		}
	};

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getStringExtra(MusicIntentReceiver.ACTION);
			if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {

				KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(
						Intent.EXTRA_KEY_EVENT);
				if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
					return;

				switch (keyEvent.getKeyCode()) {
				case KeyEvent.KEYCODE_HEADSETHOOK:
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					if (mediaActionListener != null) {
						mediaActionListener
								.onMediaAction(MediaAction.PLAY_PAUSE);
					}
					break;
				case KeyEvent.KEYCODE_MEDIA_PLAY:
					if (mediaActionListener != null) {
						mediaActionListener.onMediaAction(MediaAction.PLAY);
					}
					break;
				case KeyEvent.KEYCODE_MEDIA_PAUSE:
					if (mediaActionListener != null) {
						mediaActionListener.onMediaAction(MediaAction.PAUSE);
					}
					break;
				case KeyEvent.KEYCODE_MEDIA_STOP:
					if (mediaActionListener != null) {
						mediaActionListener.onMediaAction(MediaAction.STOP);
					}
					break;
				case KeyEvent.KEYCODE_MEDIA_NEXT:
					if (mediaActionListener != null) {
						mediaActionListener.onMediaAction(MediaAction.FORWARD);
					}
					break;
				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
					if (mediaActionListener != null) {
						mediaActionListener.onMediaAction(MediaAction.REWIND);
					}
					break;
				}
			}
		}
	};

	public void release() {
		if (mMediaSessionCompat != null)
			mMediaSessionCompat.release();

		if (mBroadcastReceiver != null)
			mContext.unregisterReceiver(mBroadcastReceiver);
	}

	public void setMediaActionListener(MediaActionListener mediaActionListener) {
		this.mediaActionListener = mediaActionListener;
	}
}
