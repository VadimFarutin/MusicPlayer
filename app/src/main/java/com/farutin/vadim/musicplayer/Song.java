package com.farutin.vadim.musicplayer;

public class Song {
    private String mPath;
    private String mTitle;
    private String mArtist;
    private String mAlbum;
    private byte[] mAlbumArt;
	private long mDuration;
	private int mTrack;

    public Song(String path, String title, String artist, String album, byte[] albumArt, long duration, int track) {
        mPath = path;
        mTitle = title;
        mArtist = artist;
        mAlbum = album;
		mAlbumArt = albumArt;
		mDuration = duration;
		mTrack = track;
    }
	public String getPath() { return mPath;	}
	public String getTitle() { return mTitle; }
    public String getArtist() { return mArtist; }
    public String getAlbum() { return mAlbum; }
	public byte[] getAlbumArt() { return mAlbumArt; }
	public long getDuration() { return mDuration; }
	public int getTrack() { return mTrack; }
}
