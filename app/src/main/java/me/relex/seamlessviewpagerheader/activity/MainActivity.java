package me.relex.seamlessviewpagerheader.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import id.technomotion.stickyheaderviewpager.tools.ScrollableFragmentListener;
import id.technomotion.stickyheaderviewpager.tools.ScrollableListener;
import id.technomotion.stickyheaderviewpager.widget.SlidingTabLayout;
import id.technomotion.stickyheaderviewpager.widget.TouchCallbackLayout;
import id.technomotion.stickyheaderviewpager.wrapper.StickyTabHeader;
import me.relex.seamlessviewpagerheader.R;
import me.relex.seamlessviewpagerheader.fragment.ListViewFragment;
import me.relex.seamlessviewpagerheader.fragment.ScrollViewFragment;

public class MainActivity extends AppCompatActivity implements ScrollableFragmentListener {

    private StickyTabHeader stickyTabHeader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        View mHeaderLayoutView = findViewById(R.id.header);
		TouchCallbackLayout touchCallbackLayout = (TouchCallbackLayout) findViewById(R.id.layout);
		SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);

        //slidingTabLayout.setDistributeEvenly(true);
        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        slidingTabLayout.setViewPager(mViewPager);

        stickyTabHeader=new StickyTabHeader(this,mHeaderLayoutView,mViewPager,touchCallbackLayout);
	}

    @Override
    public void onFragmentAttached(ScrollableListener fragment, int position) {
        stickyTabHeader.onFragmentAttached(fragment,position);
    }

    @Override
    public void onFragmentDetached(ScrollableListener fragment, int position) {
        stickyTabHeader.onFragmentDetached(fragment,position);
    }


    private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 3) {
                return ScrollViewFragment.newInstance(position);
            }
            return ListViewFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_country);
                case 1:
                    return getString(R.string.tab_continent);
                case 2:
                    return getString(R.string.tab_city);
                case 3:
                    return getString(R.string.tab_scroll_view);
            }

            return "";
        }
    }
}
