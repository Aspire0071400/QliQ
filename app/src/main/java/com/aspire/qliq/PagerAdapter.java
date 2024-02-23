package com.aspire.qliq;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {

    int tabCount;

    public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);

        tabCount = behavior;

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return new ChatsFragment();
        } else if (position == 1) {
            return new StatusFragment();
        } else if (position == 2) {
            return new CallsFragment();
        }
        return null;

    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
