package com.vogella.ble_fragmentsV5;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.vogella.ble_fragmentsV5.fragments.ConnectionFragmentClasses.Fragment_Connection;
import com.vogella.ble_fragmentsV5.fragments.Fragment_GeneralDisplay;
import com.vogella.ble_fragmentsV5.fragments.PlotFragmentClasses.Fragment_Plots;

/**
 * Created by Matthias on 15.12.2015.
 */
/*When TabPageAdapter adds a fragemtn to the FragmentManager (that's why it needs the FragmentManager as a constructor value)
    it uses internal tags to assign the fragments to the corresponding positions. getItem(int) is only called if the fragment for this
    position does not exist. If there is already a fragment for this position, Android simply tries to reload this fragment as it is more
    performant to just reload it then to instantiate a new Fragment. Android uses the previously assigned tag to find the fragment.
    IMPORTANT: FragmentStatePagerAdapter preloads the fragment behind and before the current position*/
public class TabPageAdapter extends FragmentStatePagerAdapter {
    private final int NO_FRAGS = 3;
    private final String TAG = TabPageAdapter.class.getSimpleName();
    public TabPageAdapter(FragmentManager fm){
       super(fm);
   }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "Entering: getItem");
        Fragment fragment;
        switch (position){
            case 0:
                //Fragment for Bluetooth Connection
                Log.d(TAG, "Fragment_Connection");
                fragment = new Fragment_Connection();
                break;
            case 1:
                //Fragment for general overview
                Log.d(TAG, "Fragment_GeneralDisplay");
                fragment = new Fragment_GeneralDisplay();
                break;
            case 2:
                //Fragment for plots
                Log.d(TAG, "Fragment_Plots");
                fragment = new Fragment_Plots();
                break;
            default:
                fragment = null;

        }
        Log.d(TAG, "Leaving: getItem");
        return fragment;
    }

    @Override
    public int getCount() {
        return NO_FRAGS;
    }
}
