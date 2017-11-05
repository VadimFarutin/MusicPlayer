package com.farutin.vadim.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.media.AudioManager;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class MusicService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener {

	MediaPlayer mediaPlayer;
	private final IBinder musicBind = new MusicBinder();

	ArrayList<Integer> shuffledOrder;
	int shuffledOrderPosition;

	@Override
	public void onCreate() {
		super.onCreate();
		initMusicPlayer();
	}

	public void initMusicPlayer() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mediaPlayer.stop();
		mediaPlayer.release();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		if (!MainActivity.isPlaying)
			mp.pause();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (!MainActivity.isRepeatOn) {
			if (MainActivity.isShuffleOn)
				MainActivity.position = getNewRandomPosition();
			else {
				MainActivity.position = (MainActivity.position + 1) % MainActivity.playlist.size();
				if (MainActivity.position == 0)
					MainActivity.isPlaying = false;
			}
		}

		if (MainActivity.currentActivityType == ActivityType.PLAY_ACTIVITY)
			((PlayActivity)MainActivity.currentActivity).setSong();
		else
			MainActivity.util.setToolbarFooter();

		reset();
	}

	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

	public void reset() {
		mediaPlayer.reset();

		Song song = MainActivity.playlist.get(MainActivity.position);
		try {
			mediaPlayer.setDataSource(song.getPath());
		}
		catch (Exception e) {
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}
		mediaPlayer.prepareAsync();
	}

	public void pause() {
		mediaPlayer.pause();
	}

	public void resume() {
		mediaPlayer.start();
	}

	public void setRandomOrder() {
		shuffledOrder = new ArrayList<Integer>();

		for (int i = 0; i < MainActivity.playlist.size(); i++)
			shuffledOrder.add(i);
		Collections.shuffle(shuffledOrder);

		for (int i = 0; i < shuffledOrder.size(); i++)
			if (shuffledOrder.get(i) == MainActivity.position) {
				shuffledOrder.set(i, shuffledOrder.get(0));
				break;
			}
		shuffledOrder.set(0, MainActivity.position);
		shuffledOrderPosition = 0;
	}

	public int getNewRandomPosition() {
		shuffledOrderPosition = (shuffledOrderPosition + 1) % shuffledOrder.size();
		return shuffledOrder.get(shuffledOrderPosition);
	}
}
