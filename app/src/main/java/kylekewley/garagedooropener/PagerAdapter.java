package kylekewley.garagedooropener;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import kylekewley.garagedooropener.fragments.GarageOpenerFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int doorCount;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public PagerAdapter(FragmentManager fm, int doorCount) {
        super(fm);

        this.doorCount = doorCount;
    }

    @Override
    public Fragment getItem(int position) {
        return GarageOpenerFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        return doorCount;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    public void setDoorCount(int doorCount) {
        this.doorCount = doorCount;
        notifyDataSetChanged();
    }
}
