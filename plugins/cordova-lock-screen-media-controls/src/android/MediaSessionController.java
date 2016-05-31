package org.apache.cordova.media.lockscreencontrols;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.billinar.mediabufferplugin.R;

/**
 * Created by Bilal on 24/04/2016.
 */
public class MediaSessionController implements MusicFocusable {

    private final int NOTIFICATION_REQUEST_PLAY_PAUSE = 222;
    private final int NOTIFICATION_REQUEST_REWIND = 223;
    private final int NOTIFICATION_REQUEST_FORWARD = 224;
    private final int NOTIFICATION_REQUEST_STOP = 225;

    public enum MediaAction {
        REWIND, PLAY_PAUSE, FORWARD, STOP, PLAY, PAUSE, HEADSET_SINGLE_CLICK, HEADSET_DOUBLE_CLICK, HEADSET_TRIPPLE_CLICK
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

    private static final long DOUBLE_PRESS_INTERVAL = 250; // in millis
    private long lastTapTimeMs = 0;
    private int numberOfTaps = 0;

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
//		try {
//			MediaControllerCompat mediaControllerCompat = new MediaControllerCompat(mContext, mMediaSessionCompat.getSessionToken());
//			mTransportController = mediaControllerCompat.getTransportControls();
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicIntentReceiver.ACTION);
        //filter.addAction(MusicIntentReceiver.ACTION_NOTIFICATION);
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
                showNotification();
                break;

            case PAUSED:
                mPlaybackStateCompatBuilder.setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
                mMediaSessionCompat.setPlaybackState(mPlaybackStateCompatBuilder
                        .build());
                showNotification();

                break;

            case STOPPED:
                giveUpAudioFocus();

                mPlaybackStateCompatBuilder.setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
                mMediaSessionCompat.setPlaybackState(mPlaybackStateCompatBuilder
                        .build());
                cancelNotification();

                break;
        }
    }


    public void setMetaData(String title, String subTitle, long duration,
                            Bitmap backgroundImage) {

        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, subTitle);
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                backgroundImage);

        Log.d("DemoApp", "Setting metadata");
        mMediaSessionCompat.setMetadata(builder.build());
        showNotification();
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

        mAudioFocus = AudioFocus.Focused;
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {

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
            if (action
                    .equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                // headset is now disconnected
            } else if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {

                KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(
                        Intent.EXTRA_KEY_EVENT);
                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                    return;

                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_HEADSETHOOK:
                        detectAndSendTapAction();

                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        sendMediaAction(MediaAction.PLAY_PAUSE);

                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        sendMediaAction(MediaAction.PLAY);

                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        sendMediaAction(MediaAction.PAUSE);

                        break;
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        sendMediaAction(MediaAction.STOP);

                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        sendMediaAction(MediaAction.FORWARD);

                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        sendMediaAction(MediaAction.REWIND);

                        break;
                }
            } else if (action.equals(MusicIntentReceiver.ACTION_NOTIFICATION)) {
                MediaAction eventAction = (MediaAction) intent.getSerializableExtra(MusicIntentReceiver.ACTION_NOTIFICATION);
                switch (eventAction) {
                    case FORWARD:
                        sendMediaAction(MediaAction.FORWARD);
                        break;

                    case REWIND:
                        sendMediaAction(MediaAction.REWIND);
                        break;

                    case PLAY_PAUSE:
                        sendMediaAction(MediaAction.PLAY_PAUSE);
                        break;

                    case STOP:
                        cancelNotification();
                        sendMediaAction(MediaAction.STOP);
                        break;

                }
            }
        }
    };


    private void detectAndSendTapAction() {
        // Get current time in nano seconds.
        long pressTime = System.currentTimeMillis();

        if (numberOfTaps > 0
                && (pressTime - lastTapTimeMs) < DOUBLE_PRESS_INTERVAL) {
            numberOfTaps += 1;
        } else {
            numberOfTaps = 1;
        }

        lastTapTimeMs = pressTime;

        if (numberOfTaps == 1) {
            // handle single tap
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (numberOfTaps >= 3) {
                        sendMediaAction(MediaAction.HEADSET_TRIPPLE_CLICK);
                    } else if (numberOfTaps == 2) {
                        sendMediaAction(MediaAction.HEADSET_DOUBLE_CLICK);
                    } else if (numberOfTaps == 1) {
                        sendMediaAction(MediaAction.HEADSET_SINGLE_CLICK);
                    }

                    numberOfTaps = 0;

                }
            }, DOUBLE_PRESS_INTERVAL * 2);
        }
    }

    private void sendMediaAction(MediaAction action) {
        if (mediaActionListener != null) {
            mediaActionListener.onMediaAction(action);
        }
    }

    public void release() {
        if (mMediaSessionCompat != null)
            mMediaSessionCompat.release();

        if (mBroadcastReceiver != null)
            mContext.unregisterReceiver(mBroadcastReceiver);
    }

    public void setMediaActionListener(MediaActionListener mediaActionListener) {
        this.mediaActionListener = mediaActionListener;
    }



    private NotificationCompat.Action generateAction(int icon, String title, MediaAction intentAction, int requestCode) {
        Intent intent = new Intent(mContext, MusicIntentReceiver.class);
        intent.setAction(MusicIntentReceiver.ACTION_NOTIFICATION);
        intent.putExtra(MusicIntentReceiver.ACTION_NOTIFICATION, intentAction);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void buildNotification(NotificationCompat.Action action, String title, String text, Bitmap bm) {
        Intent intent = new Intent(mContext, MusicIntentReceiver.class);
        intent.setAction(MusicIntentReceiver.ACTION_NOTIFICATION);
        intent.putExtra(MusicIntentReceiver.ACTION_NOTIFICATION, MediaAction.STOP);
        PendingIntent closeIntent = PendingIntent.getBroadcast(mContext, NOTIFICATION_REQUEST_STOP, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
        style.setMediaSession(mMediaSessionCompat.getSessionToken());
        style.setShowCancelButton(true);
        style.setCancelButtonIntent(closeIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setDeleteIntent(closeIntent);
        builder.setStyle(style);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setLargeIcon(bm);

        builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", MediaAction.REWIND, NOTIFICATION_REQUEST_REWIND));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", MediaAction.FORWARD, NOTIFICATION_REQUEST_FORWARD));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(1, builder.build());
    }


    private void showNotification() {
        if(mMediaSessionCompat != null) {
            int playState = mMediaSessionCompat.getController().getPlaybackState().getState();
            if (playState == PlaybackStateCompat.STATE_PLAYING) {
                applyNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", MediaAction.PLAY_PAUSE, NOTIFICATION_REQUEST_PLAY_PAUSE));

            } else if (playState == PlaybackStateCompat.STATE_PAUSED) {
                applyNotification(generateAction(android.R.drawable.ic_media_play, "Play", MediaAction.PLAY_PAUSE, NOTIFICATION_REQUEST_PLAY_PAUSE));
            }
        }

    }

    private void applyNotification(NotificationCompat.Action action) {
        MediaMetadataCompat metadata = mMediaSessionCompat.getController().getMetadata();
        if (metadata != null) {
            String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            String album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
            Bitmap bm = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
            buildNotification(action, title, album, bm);
        }
    }

    private void cancelNotification(){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.cancelAll();
    }

}
