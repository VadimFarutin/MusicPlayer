package com.farutin.vadim.musicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PlayActivity extends AppCompatActivity {
	Song song;
	Handler handler;
	SeekBar seekBar;
	TextView currentTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);

		getPlaylist();
		setButtons();
		setSong();
	}

	@Override
	protected void onStart() {
		super.onStart();
		MainActivity.currentActivityType = ActivityType.PLAY_ACTIVITY;
		MainActivity.currentActivity = this;
	}

	private void getPlaylist() {
		MainActivity.position = getIntent().getExtras().getInt("position");
		String parentActivity = getIntent().getExtras().getString("activity");
		boolean isFromFooter = false;

		if (parentActivity.equals("main"))
			MainActivity.playlist = MainActivity.songs;
		else if (parentActivity.equals("songList"))
			MainActivity.playlist = SongListActivity.songs;
		else
			isFromFooter = true;

		if (!isFromFooter) {
			MainActivity.isPlaying = true;
			MainActivity.musicService.reset();
		}

		if (MainActivity.isShuffleOn)
			MainActivity.musicService.setRandomOrder();

		song = MainActivity.playlist.get(MainActivity.position);
	}

	private void setInformation() {
		TextView title = (TextView)findViewById(R.id.title);
		title.setText(song.getTitle());
		TextView album = (TextView)findViewById(R.id.album);
		album.setText(song.getAlbum());
		TextView artist = (TextView)findViewById(R.id.artist);
		artist.setText(song.getArtist());

		ImageView albumCover = (ImageView) findViewById(R.id.album_cover);
		if (song.getAlbumArt() != null) {
			Bitmap albumArt = BitmapFactory.decodeByteArray(song.getAlbumArt(), 0, song.getAlbumArt().length);
			albumCover.setImageBitmap(albumArt);
		}
		else {
			albumCover.setImageDrawable(null);
		}

		TextView duration = (TextView)findViewById(R.id.song_duration);
		duration.setText(convertDuration(song.getDuration()));

		currentTime = (TextView)findViewById(R.id.current_time);
		currentTime.setText("00:00");
	}

	private String convertDuration(long duration) {
		long hrs = duration / 3600000;
		long mns = (duration / 60000) % 60000;
		long scs = duration % 60000 / 1000;
		NumberFormat formatter = new DecimalFormat("00");
		String seconds = formatter.format(scs);
		String minutes = formatter.format(mns) + ":";
		String hours = "";
		if (hrs != 0)
			hours = "" + hrs + ":";
		return hours + minutes + seconds;
	}

	void setSong() {
		song = MainActivity.playlist.get(MainActivity.position);
		setInformation();
		setSeekBar();
	}

	private void setButtons() {
		ImageButton playlistButton = (ImageButton)findViewById(R.id.button_playlist);
		playlistButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		ImageButton nextButton = (ImageButton)findViewById(R.id.button_next);
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setNextSong();
			}
		});

		ImageButton previousButton = (ImageButton)findViewById(R.id.button_previous);
		previousButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setPreviousSong();
			}
		});

		final ImageButton shuffleButton = (ImageButton)findViewById(R.id.button_shuffle);
		shuffleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.isShuffleOn = !MainActivity.isShuffleOn;
				if (MainActivity.isShuffleOn)
					MainActivity.musicService.setRandomOrder();
				if (MainActivity.isShuffleOn)
					shuffleButton.setImageResource(R.drawable.ic_shuffle_on_48dp);
				else
					shuffleButton.setImageResource(R.drawable.ic_shuffle_off_48dp);
			}
		});

		if (MainActivity.isShuffleOn)
			shuffleButton.setImageResource(R.drawable.ic_shuffle_on_48dp);
		else
			shuffleButton.setImageResource(R.drawable.ic_shuffle_off_48dp);

		final ImageButton repeatButton = (ImageButton)findViewById(R.id.button_repeat);
		repeatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.isRepeatOn = !MainActivity.isRepeatOn;
				if (MainActivity.isRepeatOn)
					repeatButton.setImageResource(R.drawable.ic_repeat_on_48dp);
				else
					repeatButton.setImageResource(R.drawable.ic_repeat_off_48dp);
				}
		});

		if (MainActivity.isRepeatOn)
			repeatButton.setImageResource(R.drawable.ic_repeat_on_48dp);
		else
			repeatButton.setImageResource(R.drawable.ic_repeat_off_48dp);

		final ImageButton playButton = (ImageButton)findViewById(R.id.button_play);
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.isPlaying = !MainActivity.isPlaying;
				if (MainActivity.isPlaying) {
					MainActivity.musicService.resume();
					playButton.setImageResource(R.drawable.ic_pause_48dp);
				}
				else {
					MainActivity.musicService.pause();
					playButton.setImageResource(R.drawable.ic_play_arrow_48dp);
				}
			}
		});

		if (MainActivity.isPlaying)
			playButton.setImageResource(R.drawable.ic_pause_48dp);
		else
			playButton.setImageResource(R.drawable.ic_play_arrow_48dp);
	}

	private void setSeekBar() {
		seekBar = (SeekBar)findViewById(R.id.seek_bar);
		seekBar.setProgress(0);
		seekBar.setMax((int)song.getDuration() / 1000);

		if (handler != null)
			handler.removeCallbacks(runnable);
		handler = new Handler();
		handler.postDelayed(runnable, 100);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				handler.removeCallbacks(runnable);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				handler.removeCallbacks(runnable);
				MainActivity.musicService.mediaPlayer.seekTo(seekBar.getProgress() * 1000);
				handler.postDelayed(runnable, 100);
			}
		});
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			long currentPosition = MainActivity.musicService.mediaPlayer.getCurrentPosition();
			seekBar.setProgress((int)currentPosition / 1000);
			handler.postDelayed(this, 100);
			currentTime.setText(convertDuration(currentPosition));
		}
	};

	private void setNextSong() {
		if (MainActivity.isShuffleOn)
			MainActivity.position = MainActivity.musicService.getNewRandomPosition();
		else
			MainActivity.position = (MainActivity.position + 1) % MainActivity.playlist.size();

		setSong();
		MainActivity.musicService.reset();
	}

	private void setPreviousSong() {
		if (MainActivity.isShuffleOn)
			MainActivity.position = MainActivity.musicService.getNewRandomPosition();
		else
			MainActivity.position = (MainActivity.position - 1 + MainActivity.playlist.size())
					% MainActivity.playlist.size();

		setSong();
		MainActivity.musicService.reset();
	}

	@Override
	protected void onDestroy() {
		handler.removeCallbacks(runnable);
		super.onDestroy();
	}
}
