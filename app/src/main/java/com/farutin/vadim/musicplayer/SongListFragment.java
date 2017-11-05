package com.farutin.vadim.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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

public class SongListFragment extends Fragment {

	FragmentType fragmentType;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentType = FragmentType.getType(getArguments().getInt("fragment_type"));

		RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.categories_list, container, false);
		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

		if (fragmentType == FragmentType.SONG_FRAGMENT)
			recyclerView.setAdapter(new RecyclerViewAdapter(getActivity(), MainActivity.songs));
		else if (fragmentType == FragmentType.ALBUM_FRAGMENT)
			recyclerView.setAdapter(new RecyclerViewAdapter(getActivity(), MainActivity.albums));
		else
			recyclerView.setAdapter(new RecyclerViewAdapter(getActivity(), MainActivity.artists));

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

		return recyclerView;
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
			if (fragmentType == FragmentType.SONG_FRAGMENT) {
				holder.mTextView1.setText(mItems.get(position).getTitle());
				holder.mTextView2.setText(mItems.get(position).getArtist());

				holder.mView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Context context = v.getContext();
						Intent intent = new Intent(context, PlayActivity.class);
						intent.putExtra("activity", "main");
						intent.putExtra("position", position);
						startActivityForResult(intent, 1);
					}
				});
			}
			else if (fragmentType == FragmentType.ALBUM_FRAGMENT) {
				holder.mTextView1.setText(mItems.get(position).getAlbum());
				holder.mTextView2.setText(mItems.get(position).getArtist());

				holder.mView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Context context = v.getContext();
						Intent intent = new Intent(context, SongListActivity.class);
						intent.putExtra("album", mItems.get(position).getAlbum());
						intent.putExtra("artist", mItems.get(position).getArtist());
						startActivityForResult(intent, 1);
					}
				});
			}
			else {
				holder.mTextView1.setText(mItems.get(position).getArtist());

				holder.mView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Context context = v.getContext();
						Intent intent = new Intent(context, AlbumListActivity.class);
						intent.putExtra("artist", mItems.get(position).getArtist());
						startActivityForResult(intent, 1);
					}
				});
			}
		}

		@Override
		public int getItemCount() {
			return mItems.size();
		}
	}
}
