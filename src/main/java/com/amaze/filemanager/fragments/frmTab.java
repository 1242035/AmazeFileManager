package com.amaze.filemanager.fragments;

import android.animation.ArgbEvaluator;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.amaze.filemanager.Constant;
import com.amaze.filemanager.R;
import com.amaze.filemanager.activities.BaseActivity;
import com.amaze.filemanager.activities.MainActivity;
import com.amaze.filemanager.database.Tab;
import com.amaze.filemanager.database.TabHandler;
import com.amaze.filemanager.ui.ColorCircleDrawable;
import com.amaze.filemanager.ui.drawer.EntryItem;
import com.amaze.filemanager.ui.views.CustomViewPager;
import com.amaze.filemanager.ui.views.Indicator;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.Futils;
import com.amaze.filemanager.utils.MainActivityHelper;
import com.amaze.filemanager.utils.PreferenceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arpit on 15-12-2014.
 */
public class frmTab extends android.support.v4.app.Fragment
        implements ViewPager.OnPageChangeListener ,Ifragment {
    protected long code = 2L;
    public  List<Fragment> fragments = new ArrayList<Fragment>();
    public ScreenSlidePagerAdapter mSectionsPagerAdapter;
    Futils utils = new Futils();
    public CustomViewPager mViewPager;
    SharedPreferences Sp;
    String path;

    // current visible tab, either 0 or 1
    //public int currenttab;
    MainActivity mainActivity;
    public int theme1;
    View buttons;
    View mToolBarContainer;
    boolean savepaths;
    FragmentManager fragmentManager;

    // ink indicators for viewpager only for Lollipop+
    private Indicator indicator;

    // views for circlular drawables below android lollipop
    private ImageView circleDrawable1, circleDrawable2;
    private boolean coloredNavigation;

    // color drawable for action bar background
    private ColorDrawable colorDrawable = new ColorDrawable();

    // colors relative to current visible tab
    private String startColor, endColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab_fragment,
                container, false);
        fragmentManager=getActivity().getSupportFragmentManager();
        mToolBarContainer=getActivity().findViewById(R.id.lin);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            //indicator = (Indicator) getActivity().findViewById(R.id.indicator);
        } else {
            circleDrawable1 = (ImageView) getActivity().findViewById(R.id.tab_indicator_one);
            circleDrawable2 = (ImageView) getActivity().findViewById(R.id.tab_indicator_one);
        }

        Sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        savepaths=Sp.getBoolean(Constant.SAVE_PATH, true);
        coloredNavigation = Sp.getBoolean(Constant.COLORED_NAVIGATION, true);

        int theme=Integer.parseInt(Sp.getString(Constant.THEME,"0"));
        theme1 = theme==2 ? PreferenceUtils.hourOfDay() : theme;
        mViewPager = (CustomViewPager) rootView.findViewById(R.id.pager);

        if (getArguments() != null){
            path = getArguments().getString("path");
        }
        buttons=getActivity().findViewById(R.id.buttons);
        mainActivity = ((MainActivity)getActivity());
        mainActivity.supportInvalidateOptionsMenu();
        mViewPager.addOnPageChangeListener(this);

        mSectionsPagerAdapter = new ScreenSlidePagerAdapter(
                getActivity().getSupportFragmentManager());
        if (savedInstanceState == null) {
            int l = Sp.getInt(Constant.CURRENT_TAB, PreferenceUtils.DEFAULT_CURRENT_TAB);
            MainActivity.currentTab = l;
            TabHandler tabHandler=new TabHandler(getActivity(),null,null,1);
            List<Tab> tabs1=tabHandler.getAllTabs();
            int i=tabs1.size();
            if(i==0) {
                if (mainActivity.storageCount>1) {
                    addTab(new Tab(1, "", ((EntryItem) DataUtils.list.get(1)).getPath(), "/"), 1, "");
                }
                else {
                    addTab(new Tab(1, "", "/", "/"), 1, "");
                }
                if(!DataUtils.list.get(0).isSection()){
                    String pa=((EntryItem) DataUtils.list.get(0)).getPath();
                    addTab(new Tab(2,"",pa,pa),2,"");
                }
                else {
                    addTab(new Tab(2,"",((EntryItem)DataUtils.list.get(1)).getPath(),"/"),2,"");
                }
            }
            else {
                if(path!=null && path.length()!=0) {
                    if(l==1) {
                        addTab(tabHandler.findTab(1), 1, "");
                    }
                    addTab(tabHandler.findTab(l+1),l+1,path);
                    if(l==0) {
                        addTab(tabHandler.findTab(2), 2, "");
                    }
                }
                else
                {
                    addTab(tabHandler.findTab(1),1,"");
                    addTab(tabHandler.findTab(2),2,"");
                }
            }
            mViewPager.setAdapter(mSectionsPagerAdapter);
            try {
                mViewPager.setCurrentItem(l,true);
                if (circleDrawable1!=null && circleDrawable2 !=null) {
                    updateIndicator(mViewPager.getCurrentItem());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
        else {
            fragments.clear();
            try {
                if (fragmentManager == null) {
                    fragmentManager = getActivity().getSupportFragmentManager();
                }
                fragments.add(0, fragmentManager.getFragment(savedInstanceState, "tab" + 0));
                fragments.add(1, fragmentManager.getFragment(savedInstanceState, "tab" + 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSectionsPagerAdapter = new ScreenSlidePagerAdapter(
                    getActivity().getSupportFragmentManager());

            mViewPager.setAdapter(mSectionsPagerAdapter);
            int pos1 = savedInstanceState.getInt("pos", 0);
            MainActivity.currentTab = pos1;
            mViewPager.setCurrentItem(pos1);
            mSectionsPagerAdapter.notifyDataSetChanged();
        }


        if (indicator!=null) {
            indicator.setViewPager(mViewPager);
        }

        // color of viewpager when current tab is 0
        startColor = BaseActivity.skin;
        // color of viewpager when current tab is 1
        endColor = BaseActivity.skinTwo;
        mainActivity.mainFragment = (frmMain) getTab();

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public void onDestroyView(){
        Sp.edit().putInt(Constant.CURRENT_TAB, MainActivity.currentTab).apply();
        super.onDestroyView();
        try {
            if(tabHandler!=null)
            tabHandler.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    TabHandler tabHandler;

    public void updatepaths(int pos) {
        if(tabHandler==null)
        tabHandler = new TabHandler(getActivity(), null, null, 1);
        int i=1;
        ArrayList<String> items=new ArrayList<String>();

        // Getting old path from database before clearing

        tabHandler.clear();
        for(Fragment fragment:fragments) {
            if(fragment.getClass().getName().contains("frmMain")) {
                frmMain m=(frmMain)fragment;
                items.add(parsePathForName(m.currentPath,m.openMode));
                if(i-1==MainActivity.currentTab && i==pos){
                    mainActivity.updatePath(m.currentPath,m.results,m.openMode,m
                            .folderCount,m.fileCount);
                    mainActivity.updateDrawer(m.currentPath);
                }
                if(m.openMode==0) {
                    tabHandler.addTab(new Tab(i, m.currentPath, m.currentPath, m.home));
                }
                else {
                    tabHandler.addTab(new Tab(i, m.home, m.home, m.home));
                }

                i++;
            }
        }
    }

    String parsePathForName(String path,int openmode){
        Resources resources=getActivity().getResources();
        if("/".equals(path)) {
            return resources.getString(R.string.root_directory);
        }
        else if("/storage/emulated/0".equals(path)) {
            return resources.getString(R.string.storage);
        }
        else if(openmode==2) {
            return new MainActivityHelper(mainActivity).getIntegralNames(path);
        }
        else {
            return new File(path).getName();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            int i = 0;
            if(Sp!=null) {
                Sp.edit().putInt(Constant.CURRENT_TAB, MainActivity.currentTab).apply();
            }
            if (fragments != null && fragments.size() !=0) {
                if(fragmentManager==null) {
                    return;
                }
                for (Fragment fragment : fragments) {
                    fragmentManager.putFragment(outState, "tab" + i, fragment);
                    i++;
                }
                outState.putInt("pos", mViewPager.getCurrentItem());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        ArgbEvaluator evaluator = new ArgbEvaluator();

        int color = (int) evaluator.evaluate(position+positionOffset, Color.parseColor(startColor),
                Color.parseColor(endColor));

        colorDrawable.setColor(color);

        mainActivity.updateViews(colorDrawable);
    }

    @Override
    public void onPageSelected(int p1) {

        mToolBarContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();

        MainActivity.currentTab=p1;
        if (Sp!=null) {
            Sp.edit().putInt(Constant.CURRENT_TAB, MainActivity.currentTab).apply();
        }

        Fragment fragment=fragments.get(p1);
        if(fragment!=null) {
            String name = fragments.get(p1).getClass().getName();
            if (name!=null && name.contains("Main")) {
                frmMain ma = ((frmMain) fragments.get(p1));
                if (ma.currentPath != null) {
                    try {
                        mainActivity.updateDrawer(ma.currentPath);
                        mainActivity.updatePath(ma.currentPath,  ma.results,ma.openMode,
                                ma.folderCount, ma.fileCount);
                        if (buttons.getVisibility() == View.VISIBLE) {
                            mainActivity.bbar(ma);
                        }
                    } catch (Exception e) {
                        //       e.printStackTrace();5
                    }
                }
            }
        }

        if (circleDrawable1!=null && circleDrawable2!=null) {
            updateIndicator(p1);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // nothing to do
    }

    @Override
    public long getCode() {
        return code;
    }

    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        @Override
        public int getItemPosition (Object object)
        {
            int index = fragments.indexOf ((Fragment)object);
            if (index == -1) {
                return POSITION_NONE;
            }
            else {
                return index;
            }
        }

        public int getCount() {
            // TODO: Implement this method
            return fragments.size();
        }

        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            android.support.v4.app.Fragment f;
            f = fragments.get(position);
            return f;
        }
    }

    public void addTab(Tab text,int pos,String path) {
        if(text==null)return;
        android.support.v4.app.Fragment main = new frmMain();
        Bundle b = new Bundle();
        if(path!=null && path.length()!=0) {
            b.putString("lastpath", path);
        }
        else {
            b.putString("lastpath", text.getOriginalPath(savepaths));
        }
        b.putString("home", text.getHome());
        b.putInt("no", pos);
        main.setArguments(b);
        fragments.add(main);
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setOffscreenPageLimit(4);
    }
    public Fragment getTab() {
        if(fragments.size()==2) {
            return fragments.get(mViewPager.getCurrentItem());
        }
        else {
            return null;
        }
    }

    public Fragment getTab(int pos) {
        if(fragments.size()==2 && pos<2) {
            return fragments.get(pos);
        }
        else {
            return null;
        }
    }

    // updating indicator color as per the current viewpager tab
    void updateIndicator(int index) {
        if (index != 0 && index != 1) return;
        if (index == 0) {
            circleDrawable1.setImageDrawable(new ColorCircleDrawable(Color.parseColor(BaseActivity.accentSkin)));
            circleDrawable2.setImageDrawable(new ColorCircleDrawable(Color.GRAY));
            return;
        } else {
            circleDrawable1.setImageDrawable(new ColorCircleDrawable(Color.parseColor(BaseActivity.accentSkin)));
            circleDrawable2.setImageDrawable(new ColorCircleDrawable(Color.GRAY));
            return;
        }
    }
}
