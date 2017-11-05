package com.farutin.vadim.musicplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class CategoryAdapter extends FragmentPagerAdapter {

	private final ArrayList<Fragment> fragments = new ArrayList<>();
	private final ArrayList<CharSequence> pageTitles = new ArrayList<>();

    public CategoryAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
	}

	public void addFragment(Fragment fragment, CharSequence title) {
		fragments.add(fragment);
		pageTitles.add(title);
	}

    @Override
    public Fragment getItem(int position) {	return fragments.get(position); }

    @Override
    public int getCount() { return fragments.size(); }

    @Override
    public CharSequence getPageTitle(int position) { return pageTitles.get(position); }
}