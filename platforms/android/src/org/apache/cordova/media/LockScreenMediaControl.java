package org.apache.cordova.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.preference.PreferenceManager.OnActivityDestroyListener;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat.TransportControls;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

public class LockScreenMediaControl implements OnAudioFocusChangeListener{

	private AudioPlayer mAudioPlayer;
	private Context mContext;
	private AudioManager mAudioManager;
	private MediaSessionCompat mMediaSessionCompat;
	private TransportControls mTransportController;
	
	/**
	 * Initializes the remote control client
	 */
	public void setupMediaSession(Context context, AudioPlayer mAudioPlayer) {
		this.mContext = context;
		this.mAudioPlayer = mAudioPlayer;
		
		/* Activate Audio Manager */
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int focusGain = mAudioManager.requestAudioFocus(this,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		if (focusGain != AudioManager.AUDIOFOCUS_GAIN) {
			Log.d("LockScreen", "Foucs not gained --- ");
		    return; //Failed to gain audio focus
		}
		Log.d("LockScreen", "Foucs gained --- ");
		ComponentName mRemoteControlResponder = new ComponentName(
				context.getPackageName(), MediaButtonReceiver.class.getName());
		final Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonIntent.setComponent(mRemoteControlResponder);
		mMediaSessionCompat = new MediaSessionCompat(context,
				"JairSession", mRemoteControlResponder, null);
		mMediaSessionCompat
				.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
						| MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
		PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
				.setActions(
						PlaybackStateCompat.ACTION_SEEK_TO
								| PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
								| PlaybackStateCompat.ACTION_REWIND
								| PlaybackStateCompat.ACTION_FAST_FORWARD
								| PlaybackStateCompat.ACTION_SKIP_TO_NEXT
								| PlaybackStateCompat.ACTION_PLAY
								| PlaybackStateCompat.ACTION_PAUSE
								| PlaybackStateCompat.ACTION_STOP)
				.setState(
						mAudioPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING
								: PlaybackStateCompat.STATE_PAUSED,
						mAudioPlayer.getCurrentPosition(), 1.0f).build();
		mMediaSessionCompat.setPlaybackState(playbackStateCompat);
		mMediaSessionCompat.setCallback(mMediaSessionCallback);
		//mMediaSessionCompat.setSessionActivity(retrievePlaybackActions(5));
		mMediaSessionCompat.setActive(true);
		updateMediaSessionMetaData();
		mTransportController = mMediaSessionCompat.getController()
				.getTransportControls();
	}

	/**
	 * Updates the lockscreen controls, if enabled.
	 */
	private void updateMediaSessionMetaData() {
		MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
		builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,
				"Artist Name");
		builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
				"Album Name");
		builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,
				"Track Name");
		builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
				mAudioPlayer.getDuration());
//		builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
//				MusicUtils.getArtwork(this, getAlbumID(), true));
		mMediaSessionCompat.setMetadata(builder.build());
	}
	
	
	private final MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

	    @Override
	    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
	        final String intentAction = mediaButtonEvent.getAction();
//	        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
//	            if (PrefUtils.isHeadsetPause(getBaseContext())) {
//	                Log.d(LOG_TAG, "Headset disconnected");
//	                pause();
//	            }
//	        } else 
	        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
	            final KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
	            if (event == null) return super.onMediaButtonEvent(mediaButtonEvent);
	            final int keycode = event.getKeyCode();
	            final int action = event.getAction();
	            final long eventTime = event.getEventTime();
	            if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {
	                switch (keycode) {
	                    case KeyEvent.KEYCODE_HEADSETHOOK:
//	                        if (eventTime - mLastClickTime < DOUBLE_CLICK) {
//	                            playNext(mSongNumber);
//	                            mLastClickTime = 0;
//	                        } else {
//	                            mLastClickTime = eventTime;
//	                        }
	                        if (mAudioPlayer.isPlaying())
                                mAudioPlayer.pausePlaying();
                            else mAudioPlayer.startPlaying(mAudioPlayer.getAudioFile());
	                        break;
	                    case KeyEvent.KEYCODE_MEDIA_STOP:
	                        mTransportController.stop();
	                        break;
	                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
	                           	if (mAudioPlayer.isPlaying()) mTransportController.pause();
	                            else mTransportController.play();
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
	        return super.onMediaButtonEvent(mediaButtonEvent);
	    }

	    @Override
	    public void onPlay() {
	        super.onPlay();
	        mAudioPlayer.startPlaying(mAudioPlayer.getAudioFile());
	    }

	    @Override
	    public void onPause() {
	        super.onPause();
	        mAudioPlayer.pausePlaying();
	    }

	    @Override
	    public void onSkipToNext() {
	        super.onSkipToNext();
	        //playNext(mSongNumber);
	    }

	    @Override
	    public void onSkipToPrevious() {
	        super.onSkipToPrevious();
	        //playPrevious(mSongNumber);
	    }

	    @Override
	    public void onSeekTo(long pos) {
	        super.onSeekTo(pos);
	        mAudioPlayer.seekToPlaying((int)pos);
	    }

	    @Override
	    public void onStop() {
	        super.onStop();
	        mAudioPlayer.stopPlaying();
	        //commitMusicData();
	        //updatePlayingUI(STOP_ACTION);
	        //stopSelf();
	    }
	};
	
	public void onDestroy(){
		if(mMediaSessionCompat != null ){
			if(mMediaSessionCompat.isActive()) mMediaSessionCompat.release();
		}
	}

	@Override
	public void onAudioFocusChange(int arg0) {
		// TODO Auto-generated method stub
		Log.d("LockScreenMediaControl", "Foucs Changed: "+arg0);
		
	}

}