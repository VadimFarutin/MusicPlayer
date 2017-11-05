package com.farutin.vadim.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.konifar.fab_transformation.FabTransformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SongListActivity extends AppCompatActivity {
	static ArrayList<Song> songs;
	String album;
	String artist;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activities_list);

		songs = new ArrayList<Song>();
		album = getIntent().getExtras().getString("album");
		artist = getIntent().getExtras().getString("artist");

		fillSongsArrayList();
		setListView();
    }

	@Override
	protected void onStart() {
		super.onStart();
		MainActivity.currentActivityType = ActivityType.SONG_LIST_ACTIVITY;
		MainActivity.currentActivity = this;

		MainActivity.util.setToolbar();
		MainActivity.util.setToolbarFooter();
		MainActivity.util.setFloatingActionButton();
	}

	private void fillSongsArrayList() {
		for (Song song : MainActivity.songs)
			if (song.getAlbum().equals(album) && song.getArtist().equals(artist))
				songs.add(song);

		Collections.sort(songs, new Comparator<Song>(){
			public int compare(Song a, Song b){
				return a.getTrack() - b.getTrack();
			}
		});
	}

	private void setListView() {
		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		recyclerView.setAdapter(new RecyclerViewAdapter(this, songs));

		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				AppCompatActivity activity = MainActivity.currentActivity;
				FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab);
				Toolbar toolbarFooter = (Toolbar) activity.findViewById(R.id.toolbar_footer);

				if (fab.getVisibility() == View.INVISIBLE && !MainActivity.isFabTransforming) {
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
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		MainActivity.currentActivityType = ActivityType.SONG_LIST_ACTIVITY;
		MainActivity.currentActivity = this;
		MainActivity.util.setToolbarFooter();
		MainActivity.util.setFloatingActionButton();
	}

	public class RecyclerViewAdapter
			extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

		public class ViewHolder extends RecyclerView.ViewHolder {

			public final View mView;
			public final TextView mTextView1;
			public final TextView mTextView2;

			public ViewHolder(View view) {
				super(view);
				mView = view;
				mTextView1 = (TextView) view.findViewById(R.id.item_1);
				mTextView2 = (TextView) view.findViewById(R.id.item_2);
			}
		}

		private ArrayList<Song> mItems;

		public RecyclerViewAdapter(Context context, ArrayList<Song> items) {
			mItems = items;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.song_item, parent, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, final int position) {
			holder.mTextView1.setText(mItems.get(position).getTitle());
			holder.mTextView2.setText(mItems.get(position).getArtist());

			holder.mView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(SongListActivity.this, PlayActivity.class);
					intent.putExtra("activity", "songList");
					intent.putExtra("position", position);
					startActivityForResult(intent, 1);
				}
			});
		}

		@Override
		public int getItemCount() {
			return mItems.size();
		}
	}
}
