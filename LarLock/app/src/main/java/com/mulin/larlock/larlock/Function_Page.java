package com.mulin.larlock.larlock;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Function_Page extends Fragment {
    private TabLayout mTablayout;
    private ViewPager pager;
    TabsPagerAdapter myAdapter;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fuction_page, container, false);
        pager=(ViewPager)v.findViewById(R.id.pager);
        mTablayout = (TabLayout) v.findViewById(R.id.tabs);
        mTablayout.addTab(mTablayout.newTab().setIcon(R.drawable.ic_power));
        //mTablayout.addTab(mTablayout.newTab().setIcon(R.drawable.ic_security));
        //mTablayout.addTab(mTablayout.newTab().setIcon(R.drawable.ic_lock));
        mTablayout.addTab(mTablayout.newTab().setIcon(R.drawable.ic_more));
        myAdapter = new TabsPagerAdapter(getFragmentManager(),mTablayout.getTabCount());
        pager.setAdapter(myAdapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTablayout));
        mTablayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(pager));
        return v;

    }


    public class TabsPagerAdapter extends FragmentPagerAdapter {
        private int NUM_ITEMS = Main_Page.tabNum;
        public TabsPagerAdapter(FragmentManager fm,int tabCount) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Power_frament();
                case 1:
                    return new more_Frame();
                case 2:
                    return new ChangePassword_Fragment();
                case 3:
                    return  new Anti_frament();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }

}



