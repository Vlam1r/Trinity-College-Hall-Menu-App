package dev.vlamir.trinitymenu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

class PagerAdapter extends FragmentStatePagerAdapter {

    private final Bundle lunch;
    private final Bundle dinner;
    private final int mNumOfTabs;
    private final FragmentManager fm;

    PagerAdapter(FragmentManager fm, int NumOfTabs, Bundle l, Bundle d) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fm = fm;
        this.mNumOfTabs = NumOfTabs;
        lunch = l;
        dinner = d;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        FoodFragment tab = new FoodFragment();
        switch (position) {
            case 0:
                tab.setArguments(lunch);
                break;
            case 1:
                tab.setArguments(dinner);
                break;
            default:
                throw new RuntimeException("PagerAdapter illegal access");
        }
        return tab;
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    void updateFragments() {
        for (Fragment f : fm.getFragments()) {
            if (f.getClass() == FoodFragment.class)
                ((FoodFragment) f).updateText();
        }
    }
}