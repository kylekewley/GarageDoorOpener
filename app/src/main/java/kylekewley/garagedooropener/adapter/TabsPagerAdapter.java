package kylekewley.garagedooropener.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import kylekewley.garagedooropener.fragments.GarageHistoryFragment;
import kylekewley.garagedooropener.fragments.GarageOpenerFragment;

/**
 * Created by kylekewley on 6/26/14.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return GarageOpenerFragment.newInstance();
            case 1:
                return GarageHistoryFragment.newInstance();
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }
}
