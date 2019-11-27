package dev.vlamir.trinitymenu;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private Bundle lunch, dinner;
    private int mNumOfTabs;
    private FragmentManager fm;

    PagerAdapter(FragmentManager fm, int NumOfTabs, Bundle l, Bundle d) {
        super(fm);
        this.fm = fm;
        this.mNumOfTabs = NumOfTabs;
        lunch = l;
        dinner = d;
    }

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
                return null;
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