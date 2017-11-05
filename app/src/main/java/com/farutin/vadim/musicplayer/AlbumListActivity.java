package com.farutin.vadim.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.konifar.fab_transformation.FabTransformation;

import java.util.ArrayList;

public class AlbumListActivity extends AppCompatActivity {
	ArrayList<Song> albums;
	String artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activities_list);

		albums = new ArrayList<Song>();
		artist = getIntent().getExtras().getString("artist");

		fillAlbumArrayList();
		setListView();
	}

	@Override
	protected void onStart() {
		super.onStart();
		MainActivity.currentActivityType = ActivityType.ALBUM_LIST_ACTIVITY;
		MainActivity.currentActivity = this;

		MainActivity.util.setToolbar();
		MainActivity.util.setToolbarFooter();
		MainActivity.util.setFloatingActionButton();
	}

	private void fillAlbumArrayList() {
		for (Song song : MainActivity.albums)
			if (song.getArtist().equals(artist))
				albums.add(song);
	}

	private void setListView() {
		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		recyclerView.setAdapter(new RecyclerViewAdapter(this, albums));

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
		MainActivity.currentActivityType = ActivityType.ALBUM_LIST_ACTIVITY;
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
			holder.mTextView1.setText(mItems.get(position).getAlbum());
			holder.mTextView2.setText(mItems.get(position).getArtist());

			holder.mView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Song song = albums.get(position);
					Intent intent = new Intent(AlbumListActivity.this, SongListActivity.class);
					intent.putExtra("album", song.getAlbum());
					intent.putExtra("artist", artist);
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
