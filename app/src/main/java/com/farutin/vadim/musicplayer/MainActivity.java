package com.farutin.vadim.musicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.farutin.vadim.musicplayer.MusicService.MusicBinder;
import com.konifar.fab_transformation.FabTransformation;

public class MainActivity extends AppCompatActivity {
    static ArrayList<Song> songs;
    static ArrayList<Song> artists;
    static ArrayList<Song> albums;
	static ArrayList<Song> playlist;
	static int position;
	static boolean isShuffleOn;
	static boolean isRepeatOn;
	static boolean isPlaying;

	static MusicService musicService;
	private Intent playIntent;
	private boolean musicBound;

	static ActivityType currentActivityType;
	static AppCompatActivity currentActivity;
	static Util util;

	static boolean isFabTransforming;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songs = new ArrayList<Song>();
        artists = new ArrayList<Song>();
        albums = new ArrayList<Song>();

		getSongsArrayList();
		fillArtistsArrayList();
		fillAlbumsArrayList();

		playlist = songs;
		position = 0;
		isShuffleOn = false;
		isRepeatOn = false;
		isPlaying = false;

		util = new Util();

		isFabTransforming = false;

		createViewPager();
	}

    private void createViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		CategoryAdapter adapter = new CategoryAdapter(getSupportFragmentManager());

		Bundle bundle1 = new Bundle();
		SongListFragment fragment1 = new SongListFragment();
		bundle1.putInt("fragment_type", FragmentType.getInt(FragmentType.SONG_FRAGMENT));
		fragment1.setArguments(bundle1);
		adapter.addFragment(fragment1, "Songs");

		Bundle bundle2 = new Bundle();
		SongListFragment fragment2 = new SongListFragment();
		bundle2.putInt("fragment_type", FragmentType.getInt(FragmentType.ALBUM_FRAGMENT));
		fragment2.setArguments(bundle2);
		adapter.addFragment(fragment2, "Albums");

		Bundle bundle3 = new Bundle();
		SongListFragment fragment3 = new SongListFragment();
		bundle3.putInt("fragment_type", FragmentType.getInt(FragmentType.ARTIST_FRAGMENT));
		fragment3.setArguments(bundle3);
		adapter.addFragment(fragment3, "Artists");

		viewPager.setAdapter(adapter);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

	private void getSongsArrayList() {
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

		if (musicCursor != null && musicCursor.moveToFirst()) {
			int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
			int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
			int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
			int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
			int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
			int trackColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);

			do {
				String thisPath = musicCursor.getString(pathColumn);
				metaRetriever.setDataSource(thisPath);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				String thisAlbum = musicCursor.getString(albumColumn);
				byte[] thisAlbumArt = metaRetriever.getEmbeddedPicture();
				long thisDuration = musicCursor.getLong(durationColumn);
				int thisTrack = musicCursor.getInt(trackColumn);
				songs.add(new Song(thisPath, thisTitle, thisArtist, thisAlbum, thisAlbumArt, thisDuration, thisTrack));
			}
			while (musicCursor.moveToNext());
		}

		musicCursor.close();
	}

	private void fillArtistsArrayList() {
		Collections.sort(songs, new Comparator<Song>(){
			public int compare(Song a, Song b){
				return a.getArtist().compareTo(b.getArtist());
			}
		});

		artists.add(songs.get(0));
		for (int i = 1; i < songs.size(); i++) {
			if (!songs.get(i).getArtist().equals(songs.get(i - 1).getArtist()))
				artists.add(songs.get(i));
		}
	}

	private void fillAlbumsArrayList() {
        Collections.sort(songs, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getAlbum().compareTo(b.getAlbum());
            }
        });

        albums.add(songs.get(0));
		for (int i = 1; i < songs.size(); i++) {
			if (!songs.get(i).getAlbum().equals(songs.get(i - 1).getAlbum())
				|| !songs.get(i).getArtist().equals(songs.get(i - 1).getArtist()))
				albums.add(songs.get(i));
        }

		Collections.sort(songs, new Comparator<Song>(){
			public int compare(Song a, Song b){
				return a.getTitle().compareTo(b.getTitle());
			}
		});
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		currentActivityType = ActivityType.MAIN_ACTIVITY;
		currentActivity = this;
		util.setToolbarFooter();
		util.setFloatingActionButton();
	}

	// service
	private ServiceConnection musicConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder)service;
			musicService = binder.getService();
			musicBound = true;
			musicService.reset();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		currentActivityType = ActivityType.MAIN_ACTIVITY;
		currentActivity = this;

		util.setToolbar();
		util.setToolbarFooter();
		util.setFloatingActionButton();

		if (playIntent == null) {
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}

	@Override
	protected void onDestroy() {
		stopService(playIntent);
		unbindService(musicConnection);
		musicService = null;
		super.onDestroy();
	}

	//menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		switch (AppCompatDelegate.getDefaultNightMode()) {
			case AppCompatDelegate.MODE_NIGHT_YES:
				menu.findItem(R.id.menu_theme_dark).setChecked(true);
				break;
			default:
				menu.findItem(R.id.menu_theme_light).setChecked(true);
				break;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_theme_light:
				setNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				break;
			case R.id.menu_theme_dark:
				setNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void setNightMode(@AppCompatDelegate.NightMode int nightMode) {
		AppCompatDelegate.setDefaultNightMode(nightMode);

		if (Build.VERSION.SDK_INT >= 11) {
			recreate();
		}
	}
}

enum ActivityType {
	MAIN_ACTIVITY,
	ALBUM_LIST_ACTIVITY,
	SONG_LIST_ACTIVITY,
	PLAY_ACTIVITY
}

enum FragmentType {
	SONG_FRAGMENT,
	ALBUM_FRAGMENT,
	ARTIST_FRAGMENT;

	static public FragmentType getType(int value) {
		if (value == 0)
			return SONG_FRAGMENT;

		if (value == 1)
			return ALBUM_FRAGMENT;

		return ARTIST_FRAGMENT;
	}

	static public int getInt(FragmentType fragmentType) {
		if (fragmentType == SONG_FRAGMENT)
			return 0;

		if (fragmentType == ALBUM_FRAGMENT)
			return 1;

		return 2;
	}
}

class Util {
	void setFloatingActionButton() {
		final AppCompatActivity activity = MainActivity.currentActivity;

		final Toolbar toolbarFooter = (Toolbar) activity.findViewById(R.id.toolbar_footer);
		final FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (fab.getVisibility() == View.VISIBLE)
					FabTransformation.with(fab)
							.duration(500)
							.transformTo(toolbarFooter);
			}
		});

		if (!MainActivity.isFabTransforming) {
			if (toolbarFooter.getVisibility() == View.VISIBLE && fab.getVisibility() == View.VISIBLE)
				toolbarFooter.setVisibility(View.INVISIBLE);
			else if (toolbarFooter.getVisibility() == View.VISIBLE) {
				FabTransformation.with(fab)
						.duration(500)
						.setListener(new FabTransformation.OnTransformListener() {
							@Override
							public void onStartTransform() {
								MainActivity.isFabTransforming = true;
							}

							@Override
							public void onEndTransform() {
								MainActivity.isFabTransforming = false;
							}
						})
						.transformFrom(toolbarFooter);
			}
		}
	}

	void setToolbarFooter() {
		final AppCompatActivity activity = MainActivity.currentActivity;

		TextView titleTextView = (TextView)activity.findViewById(R.id.footer_title);
		titleTextView.setText(MainActivity.playlist.get(MainActivity.position).getTitle());
		TextView artistTextView = (TextView)activity.findViewById(R.id.footer_artist);
		artistTextView.setText(MainActivity.playlist.get(MainActivity.position).getArtist());

		LinearLayout footerLinearLayout = (LinearLayout)activity.findViewById(R.id.footer);
		footerLinearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, PlayActivity.class);
				intent.putExtra("activity", "footer");
				intent.putExtra("position", MainActivity.position);
				activity.startActivityForResult(intent, 1);
			}
		});

		final ImageButton playImageButton = (ImageButton)activity.findViewById(R.id.footer_button);
		playImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.isPlaying = !MainActivity.isPlaying;
				if (MainActivity.isPlaying) {
					MainActivity.musicService.resume();
					playImageButton.setImageResource(R.drawable.ic_pause_48dp);
				}
				else {
					MainActivity.musicService.pause();
					playImageButton.setImageResource(R.drawable.ic_play_arrow_48dp);
				}
			}
		});

		if (MainActivity.isPlaying)
			playImageButton.setImageResource(R.drawable.ic_pause_48dp);
		else
			playImageButton.setImageResource(R.drawable.ic_play_arrow_48dp);
	}

	void setToolbar() {
		final AppCompatActivity activity = MainActivity.currentActivity;

		Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
		activity.setSupportActionBar(toolbar);
		ActionBar actionBar = activity.getSupportActionBar();

		if (MainActivity.currentActivityType == ActivityType.MAIN_ACTIVITY) {
			actionBar.setTitle(R.string.app_name);
		}
		else if (MainActivity.currentActivityType == ActivityType.ALBUM_LIST_ACTIVITY) {
			actionBar.setTitle(((AlbumListActivity) activity).artist);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.finish();
				}
			});
		}
		else {
			actionBar.setTitle(((SongListActivity) activity).album);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					activity.finish();
				}
			});
		}
	}
}