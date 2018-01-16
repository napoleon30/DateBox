package com.accloud.ac_service_android_demo.fragment;

import com.accloud.ac_service_android_demo.activity.PagerActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

/**
 * Created by Jay on 2015/8/31 0031.
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private final int PAGER_COUNT = 3;
    private ThermFragment myFragment1 = null;
    private EightToEightFragment myFragment2 = null;
    private TransferFragment myFragment3 = null;
//    private SettingFragment myFragment4 = null;


    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        myFragment1 = new ThermFragment();
        myFragment2 = new EightToEightFragment();
        myFragment3 = new TransferFragment();
//        myFragment4 = new SettingFragment();
    }


    @Override
    public int getCount() {
        return PAGER_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup vg, int position) {
        return super.instantiateItem(vg, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        System.out.println("position Destory" + position);
        super.destroyItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case PagerActivity.PAGE_ONE:
                fragment = myFragment1;
                break;
            case PagerActivity.PAGE_TWO:
                fragment = myFragment2;
                break;
            case PagerActivity.PAGE_THREE:
                fragment = myFragment3;
                break;
//            case PagerActivity.PAGE_FOUR:
//                fragment = myFragment4;
//                break;
        }
        return fragment;
    }


}

