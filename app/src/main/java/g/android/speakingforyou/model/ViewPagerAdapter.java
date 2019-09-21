package g.android.speakingforyou.model;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import g.android.speakingforyou.view.HistoryFragment;
import g.android.speakingforyou.view.SavedSentencesFragment;
import g.android.speakingforyou.view.SettingsFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 3;
    private Map<Integer, String> mFragmentTags;
    private FragmentManager mFragmentManager;


    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
        mFragmentTags = new HashMap<Integer, String>();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object object = super.instantiateItem(container, position);
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            String tag = fragment.getTag();
            mFragmentTags.put(position, tag);
        }
        return object;
    }

    public Fragment getFragment(int position) {
        Fragment fragment = null;
        String tag = mFragmentTags.get(position);
        if (tag != null) {
            fragment = mFragmentManager.findFragmentByTag(tag);
        }
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return SavedSentencesFragment.newInstance();
            case 1:
                return HistoryFragment.newInstance();
            case 2:
                return SettingsFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return "";
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}