/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.filemanager.fragments;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amaze.filemanager.Constant;
import com.amaze.filemanager.IMyAidlInterface;
import com.amaze.filemanager.Loadlistener;
import com.amaze.filemanager.R;
import com.amaze.filemanager.activities.BaseActivity;
import com.amaze.filemanager.activities.MainActivity;
import com.amaze.filemanager.adapters.Recycleradapter;
import com.amaze.filemanager.database.Tab;
import com.amaze.filemanager.database.TabHandler;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.filesystem.HFile;
import com.amaze.filemanager.filesystem.MediaStoreHack;
import com.amaze.filemanager.services.asynctasks.LoadList;
import com.amaze.filemanager.ui.Layoutelements;
import com.amaze.filemanager.ui.icons.IconHolder;
import com.amaze.filemanager.ui.icons.IconUtils;
import com.amaze.filemanager.ui.icons.Icons;
import com.amaze.filemanager.ui.icons.MimeTypes;
import com.amaze.filemanager.ui.views.DividerItemDecoration;
import com.amaze.filemanager.ui.views.FastScroller;
import com.amaze.filemanager.ui.views.RoundedImageView;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.FileListSorter;
import com.amaze.filemanager.utils.Futils;
import com.amaze.filemanager.utils.MainActivityHelper;
import com.amaze.filemanager.utils.PreferenceUtils;
import com.amaze.filemanager.utils.Resource;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class frmMain extends Fragment implements Ifragment {
    protected long code = 1L;
    public ArrayList<Layoutelements> listElement;
    public Recycleradapter adapter;
    public Futils utils;
    public ActionMode actionMode;
    public SharedPreferences sharePreference;
    public Drawable folder, apk, darkImage, darkVideo;
    public LinearLayout buttons;
    public int sortBy, directorySort, asc;
    public String home, currentPath = "", year, goBack;
    public boolean selection, results = false, rootMode, showHidden, circularImage, showPermission, showSize, showLastModified;
    public LinearLayout pathbar;
    public int openMode = 0;
    public RecyclerView listView;

    public boolean goBackItem, showThumb, coloriseIcon, showDivider;

    /**
     * {@link frmMain#isList} boolean to identify if the view is a list or grid
     */
    public boolean isList = true;
    public IconHolder ic;
    public MainActivity mainActivity;
    public String fabSkin, iconSkin;
    public float[] color;
    public ColorMatrixColorFilter colorMatrixColorFilter;
    public SwipyRefreshLayout swipeRefreshLayout;
    public int skinColor, skinTwoColor, iconSkinColor, baseTheme, theme, fileCount, folderCount, columns;
    public ArrayList<BaseFile> searchHelper = new ArrayList<>();
    public int skinSelection;
    HashMap<String, Bundle> scrolls = new HashMap<String, Bundle>();
    frmMain ma = this;
    IconUtils icons;
    View footerView;
    String itemsString;
    public int no;
    TabHandler tabHandler;
    LinearLayoutManager layoutManager;
    GridLayoutManager layoutManagerGrid;
    boolean addheader = false;
    StickyRecyclerHeadersDecoration headersDecor;
    DividerItemDecoration dividerItemDecoration;
    int hideMode;
    AppBarLayout toolbarContainer;
    TextView pathName, fullPath;
    boolean stopAnims = true;
    View nofilesview;
    DisplayMetrics displayMetrics;
    HFile f;
    private View rootView;
    private View actionModeView;
    private FastScroller fastScroller;

    // defines the current visible tab, default either 0 or 1
    //private int mCurrentTab;

    /*
     * boolean identifying if the search task should be re-run on back press after pressing on
     * any of the search result
     */
    private boolean retainSearchTask = false;

    public frmMain() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        setRetainInstance(true);
        no = getArguments().getInt("no", 1);
        home = getArguments().getString("home");
        currentPath = getArguments().getString("lastpath");
        tabHandler = new TabHandler(getActivity(), null, null, 1);
        sharePreference = PreferenceManager.getDefaultSharedPreferences(getActivity());

        fabSkin = PreferenceUtils.getAccentString(sharePreference);
        int icon = sharePreference.getInt(Constant.ICON_SKIN, PreferenceUtils.DEFAULT_ICON);
        iconSkin = PreferenceUtils.getFolderColorString(sharePreference);
        skinColor = Color.parseColor(BaseActivity.skin);
        skinTwoColor = Color.parseColor(BaseActivity.skinTwo);
        iconSkinColor = Color.parseColor(iconSkin);
        Calendar calendar = Calendar.getInstance();
        year = ("" + calendar.get(Calendar.YEAR)).substring(2, 4);
        theme = Integer.parseInt(sharePreference.getString(Constant.THEME, "0"));
        baseTheme = theme == 2 ? PreferenceUtils.hourOfDay() : theme;
        hideMode = sharePreference.getInt(Constant.HIDDEN_MODE, 0);

        showPermission = sharePreference.getBoolean(Constant.SHOW_PERMISSIONS, false);
        showSize = sharePreference.getBoolean(Constant.SHOW_FILE_SIZE, false);
        showDivider = sharePreference.getBoolean(Constant.SHOW_DIVIDERS, true);
        goBackItem = sharePreference.getBoolean(Constant.GO_BACK_CHECK_BOX, false);
        circularImage = sharePreference.getBoolean(Constant.CIRCULAR_IMAGES, true);
        showLastModified = sharePreference.getBoolean(Constant.SHOW_LAST_MODIFIED, true);
        icons = new IconUtils(sharePreference, getActivity());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public void stopAnimation() {
        if ((!adapter.stoppedAnimation)) {
            for (int j = 0; j < listView.getChildCount(); j++) {
                View v = listView.getChildAt(j);
                if (v != null) v.clearAnimation();
            }
        }
        adapter.stoppedAnimation = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.main_frag, container, false);
        setRetainInstance(true);
        listView = (android.support.v7.widget.RecyclerView) rootView.findViewById(R.id.list_view);
        toolbarContainer = (AppBarLayout) getActivity().findViewById(R.id.lin);
        fastScroller = (FastScroller) rootView.findViewById(R.id.fast_scroll);
        fastScroller.setPressedHandleColor(Color.parseColor(fabSkin));
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (adapter != null && stopAnims) {
                    stopAnimation();
                    stopAnims = false;
                }
                return false;
            }
        });
        toolbarContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (adapter != null && stopAnims) {
                    stopAnimation();
                    stopAnims = false;
                }
                return false;
            }
        });

        swipeRefreshLayout = (SwipyRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                loadlist((currentPath), false, openMode);
            }
        });
        buttons = (LinearLayout) getActivity().findViewById(R.id.buttons);
        pathbar = (LinearLayout) getActivity().findViewById(R.id.path_bar);
        showThumb = sharePreference.getBoolean(Constant.SHOW_THUMB, true);

        pathName = (TextView) getActivity().findViewById(R.id.path_name);
        fullPath = (TextView) getActivity().findViewById(R.id.full_path);
        goBack = getResources().getString(R.string.go_back);
        itemsString = getResources().getString(R.string.items);
        apk = Resource.getResource(getContext(),R.drawable.ic_doc_apk_grid);
        toolbarContainer.setBackgroundColor(MainActivity.currentTab==1 ? skinTwoColor : skinColor);
        //   listView.setPadding(listView.getPaddingLeft(), paddingTop, listView.getPaddingRight(), listView.getPaddingBottom());
        return rootView;
    }

    public int dpToPx(int dp) {
        if (displayMetrics == null) {
            displayMetrics = getResources().getDisplayMetrics();
        }
        int px = Math.round(dp * (displayMetrics.xdpi / displayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(false);
        //mainActivity = (MainActivity) getActivity();
        initNoFileLayout();
        utils = new Futils();
        String x = PreferenceUtils.getSelectionColor(MainActivity.currentTab==1 ?
                BaseActivity.skinTwo : BaseActivity.skin);
        skinSelection = Color.parseColor(x);
        color = PreferenceUtils.calculatevalues(x);
        ColorMatrix colorMatrix = new ColorMatrix(PreferenceUtils.calculatefilter(color));
        colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        rootMode = sharePreference.getBoolean(Constant.ROOT_MODE, false);
        showHidden = sharePreference.getBoolean(Constant.SHOW_HIDDEN, false);
        coloriseIcon = sharePreference.getBoolean(Constant.COLOR_ICONS, true);
        folder = Resource.getResource(getContext(), R.drawable.ic_grid_folder_new);
        getSortModes();
        darkImage = Resource.getResource(getContext(), R.drawable.ic_doc_image_dark);
        darkVideo = Resource.getResource(getContext(), R.drawable.ic_doc_video_dark);
        this.setRetainInstance(false);
        f = new HFile(HFile.UNKNOWN, currentPath);
        f.generateMode(getActivity());
        mainActivity.initiatebbar();
        ic = new IconHolder(getActivity(), showThumb, !isList);

        if ( baseTheme==0 && !isList ) {
            listView.setBackgroundColor(Resource
                    .getColor(getContext(),R.color.grid_background_light));
        }
        else  {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                listView.setBackgroundDrawable(null);
            } else {
                listView.setBackground(null);
            }
        }

        listView.setHasFixedSize(true);
        columns = Integer.parseInt(sharePreference.getString(Constant.COLUMNS, "-1"));
        if (isList) {
            layoutManager = new LinearLayoutManager(getActivity());
            listView.setLayoutManager(layoutManager);
        } else {
            if (columns == -1 || columns == 0) {
                layoutManagerGrid = new GridLayoutManager(getActivity(), 3);
            }
            else {
                layoutManagerGrid = new GridLayoutManager(getActivity(), columns);
            }
            listView.setLayoutManager(layoutManagerGrid);
        }
        // use a linear layout manager
        footerView = getActivity().getLayoutInflater().inflate(R.layout.divider, null);
        dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, false, showDivider);
        listView.addItemDecoration(dividerItemDecoration);
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor(fabSkin));
        DefaultItemAnimator animator = new DefaultItemAnimator();
        listView.setItemAnimator(animator);
        toolbarContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if ((columns == 0 || columns == -1)) {
                    int screen_width = listView.getWidth();
                    int dptopx = dpToPx(115);
                    columns = screen_width / dptopx;
                    if (columns == 0 || columns == -1) {
                        columns = 3;
                    }
                }
                if (savedInstanceState != null && !isList) {
                    retrieveFromSavedInstance(savedInstanceState);
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    toolbarContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    toolbarContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }

        });
        if (savedInstanceState == null) {
            loadlist(currentPath, false, openMode);
        }
        else {
            if (isList) {
                retrieveFromSavedInstance(savedInstanceState);
            }
        }
    }

    void switchToGrid() {
        isList = false;

        ic = new IconHolder(getActivity(), showThumb, !isList);
        folder = Resource.getResource(getContext(),R.drawable.ic_grid_folder_new );
        fixIcons();
        if (baseTheme==0) {
            // will always be grid, set alternate white background
            listView.setBackgroundColor(getResources().getColor(R.color.grid_background_light));
        }
        if (layoutManagerGrid == null) {
            if (columns == -1 || columns == 0) {
                layoutManagerGrid = new GridLayoutManager(getActivity(), 3);
            }
            else {
                layoutManagerGrid = new GridLayoutManager(getActivity(), columns);
            }
        }
        listView.setLayoutManager(layoutManagerGrid);
        adapter = null;
    }

    void switchToList() {
        isList = true;
        if (baseTheme==0) {
            listView.setBackgroundDrawable(null);
        }
        ic = new IconHolder(getActivity(), showThumb, !isList);
        folder = Resource.getResource(getContext(), R.drawable.ic_grid_folder_new);
        fixIcons();
        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(getActivity());
        }
        listView.setLayoutManager(layoutManager);
        adapter = null;
    }

    public void switchView() {
        createViews(listElement, false, currentPath, openMode, results, checkforpath(currentPath));
    }

    void retrieveFromSavedInstance(final Bundle savedInstanceState) {

        Bundle b = new Bundle();
        String cur = savedInstanceState.getString("currentPath");
        if (cur != null) {
            b.putInt("index", savedInstanceState.getInt("index"));
            b.putInt("top", savedInstanceState.getInt("top"));
            scrolls.put(cur, b);

            openMode = savedInstanceState.getInt("openMode", 0);
            listElement = savedInstanceState.getParcelableArrayList("list");
            currentPath = cur;
            folderCount = savedInstanceState.getInt("folderCount", 0);
            fileCount = savedInstanceState.getInt("fileCount", 0);
            results = savedInstanceState.getBoolean("results");
            mainActivity.updatePath(currentPath, results, openMode, folderCount, fileCount);
            createViews(listElement, true, (currentPath), openMode, results, !isList);
            if (savedInstanceState.getBoolean("selection")) {
                for (int i : savedInstanceState.getIntegerArrayList("position")) {
                    adapter.toggleChecked(i, null);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int index;
        View vi;
        if (listView != null) {
            if (isList) {
                index = (layoutManager).findFirstVisibleItemPosition();
                vi = listView.getChildAt(0);
            } else {
                index = (layoutManagerGrid).findFirstVisibleItemPosition();
                vi = listView.getChildAt(0);
            }
            int top = (vi == null) ? 0 : vi.getTop();
            outState.putInt("index", index);
            outState.putInt("top", top);
            //outState.putBoolean("isList", isList);
            outState.putParcelableArrayList("list", listElement);
            outState.putString("currentPath", currentPath);
            outState.putBoolean("selection", selection);
            outState.putInt("openMode", openMode);
            outState.putInt("folderCount", folderCount);
            outState.putInt("fileCount", fileCount);
            if (selection) {
                outState.putIntegerArrayList("position", adapter.getCheckedItemPositions());
            }
            outState.putBoolean("results", results);

        }
    }

    public ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        private void hideOption(int id, Menu menu) {
            MenuItem item = menu.findItem(id);
            item.setVisible(false);
        }

        private void showOption(int id, Menu menu) {
            MenuItem item = menu.findItem(id);
            item.setVisible(true);
        }

        public void initMenu(Menu menu) {
            /*menu.findItem(R.id.cpy).setIcon(icons.getCopyDrawable());
            menu.findItem(R.id.cut).setIcon(icons.getCutDrawable());
            menu.findItem(R.id.delete).setIcon(icons.getDeleteDrawable());
            menu.findItem(R.id.all).setIcon(icons.getAllDrawable());*/
        }

        // called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            actionModeView = getActivity().getLayoutInflater().inflate(R.layout.actionmode, null);
            mode.setCustomView(actionModeView);

            mainActivity.setPagingEnabled(false);
            mainActivity.floatingActionButton.hideMenuButton(true);
            if (mainActivity.isDrawerLocked) {
                mainActivity.translateDrawerList(true);
            }
            // assumes that you have "contexual.xml" menu resources
            inflater.inflate(R.menu.contextual, menu);
            initMenu(menu);
            hideOption(R.id.add_shortcut, menu);
            hideOption(R.id.share, menu);
            hideOption(R.id.open_with, menu);
            if (mainActivity.returnIntent) {
                showOption(R.id.open_multi, menu);
            }

            mode.setTitle(utils.getString(getActivity(), R.string.select));

            mainActivity.updateViews(new ColorDrawable(Resource.getColor( getContext(), R.color.holo_dark_action_mode)));

            if (!mainActivity.isDrawerLocked) {
                mainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        mainActivity.drawerLinear);
            }
            return true;
        }

        // the following method is called each time
        // the action mode is shown. Always called after
        // onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            ArrayList<Integer> positions = adapter.getCheckedItemPositions();
            TextView textView1 = (TextView) actionModeView.findViewById(R.id.item_count);
            textView1.setText(positions.size() + "");
            textView1.setOnClickListener(null);
            mode.setTitle(positions.size() + "");
            hideOption(R.id.open_multi, menu);
            if (openMode == 1) {
                hideOption(R.id.add_shortcut, menu);
                hideOption(R.id.open_with, menu);
                hideOption(R.id.share, menu);
                hideOption(R.id.compress, menu);
                return true;
            }
            if (mainActivity.returnIntent) {
                if (Build.VERSION.SDK_INT >= 16) {
                    showOption(R.id.open_multi, menu);
                }
            }
            //tv.setText(positions.size());
            if (!results) {
                hideOption(R.id.open_parent, menu);
                if (positions.size() == 1) {
                    showOption(R.id.add_shortcut, menu);
                    showOption(R.id.open_with, menu);
                    showOption(R.id.share, menu);

                    File x = new File(listElement.get(adapter.getCheckedItemPositions().get(0))
                            .getDesc());

                    if (x.isDirectory()) {
                        hideOption(R.id.open_with, menu);
                        hideOption(R.id.share, menu);
                        hideOption(R.id.open_multi, menu);
                    }

                    if (mainActivity.returnIntent) {
                        if (Build.VERSION.SDK_INT >= 16) {
                            showOption(R.id.open_multi, menu);
                        }
                    }

                } else {
                    try {
                        showOption(R.id.share, menu);
                        if (mainActivity.returnIntent) {
                            if (Build.VERSION.SDK_INT >= 16) {
                                showOption(R.id.open_multi, menu);
                            }
                        }
                        for (int c : adapter.getCheckedItemPositions()) {
                            File x = new File(listElement.get(c).getDesc());
                            if (x.isDirectory()) {
                                hideOption(R.id.share, menu);
                                hideOption(R.id.open_multi, menu);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    hideOption(R.id.open_with, menu);

                }
            } else {
                if (positions.size() == 1) {
                    showOption(R.id.add_shortcut, menu);
                    showOption(R.id.open_parent, menu);
                    showOption(R.id.open_with, menu);
                    showOption(R.id.share, menu);

                    File x = new File(listElement.get(adapter.getCheckedItemPositions().get(0))

                            .getDesc());

                    if (x.isDirectory()) {
                        hideOption(R.id.open_with, menu);
                        hideOption(R.id.share, menu);
                        hideOption(R.id.open_multi, menu);
                    }
                    if (mainActivity.returnIntent) {
                        if (Build.VERSION.SDK_INT >= 16) {
                            showOption(R.id.open_multi, menu);
                        }
                    }

                } else {
                    hideOption(R.id.open_parent, menu);

                    if (mainActivity.returnIntent) {
                        if (Build.VERSION.SDK_INT >= 16) {
                            showOption(R.id.open_multi, menu);
                        }
                    }
                    try {
                        for (int c : adapter.getCheckedItemPositions()) {
                            File x = new File(listElement.get(c).getDesc());
                            if (x.isDirectory()) {
                                hideOption(R.id.share, menu);
                                hideOption(R.id.open_multi, menu);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    hideOption(R.id.open_with, menu);

                }
            }

            return true; // Return false if nothing is done
        }

        // called when the user selects a contextual menu item
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            computeScroll();
            ArrayList<Integer> plist = adapter.getCheckedItemPositions();
            switch (item.getItemId()) {
                case R.id.open_multi:
                    if (Build.VERSION.SDK_INT >= 16) {
                        Intent intentresult = new Intent();
                        ArrayList<Uri> resulturis = new ArrayList<Uri>();
                        for (int k : plist) {
                            try {
                                resulturis.add(Uri.fromFile(new File(listElement.get(k).getDesc())));
                            } catch (Exception e) {

                            }
                        }
                        final ClipData clipData = new ClipData(
                                null, new String[]{"*/*"}, new ClipData.Item(resulturis.get(0)));
                        for (int i = 1; i < resulturis.size(); i++) {
                            clipData.addItem(new ClipData.Item(resulturis.get(i)));
                        }
                        intentresult.setClipData(clipData);
                        mode.finish();
                        getActivity().setResult(getActivity().RESULT_OK, intentresult);
                        getActivity().finish();
                    }
                    return true;
                case R.id.about:
                    Layoutelements x;
                    x = listElement.get((plist.get(0)));
                    utils.showProps((x).generateBaseFile(), x.getPermissions(), ma, rootMode);
                    mode.finish();
                    return true;
                /*case R.id.setringtone:
                    File fx;
                    if(results)
                        fx=new File(slist.get((plist.get(0))).getDesc());
                        else
                        fx=new File(list.get((plist.get(0))).getDesc());

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, fx.getAbsolutePath());
                    values.put(MediaStore.MediaColumns.TITLE, "Amaze");
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                    //values.put(MediaStore.MediaColumns.SIZE, fx.);
                    values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                    values.put(MediaStore.Audio.Media.IS_ALARM, false);
                    values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                    Uri uri = MediaStore.Audio.Media.getContentUriForPath(fx.getAbsolutePath());
                    Uri newUri = getActivity().getContentResolver().insert(uri, values);
                    try {
                        RingtoneManager.setActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_RINGTONE, newUri);
                        //Settings.System.putString(getActivity().getContentResolver(), Settings.System.RINGTONE, newUri.toString());
                        Toast.makeText(getActivity(), "Successful" + fx.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (Throwable t) {

                        Log.d("ringtone", "failed");
                    }
                    return true;*/
                case R.id.delete:
                    utils.deleteFiles(listElement, ma, plist);
                    return true;
                case R.id.share:
                    ArrayList<File> arrayList = new ArrayList<File>();
                    for (int i : plist) {
                        arrayList.add(new File(listElement.get(i).getDesc()));
                    }
                    if (arrayList.size() > 100) {
                        Toast.makeText(getActivity(), "Can't share more than 100 files", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        utils.shareFiles(arrayList, getActivity(), baseTheme, Color.parseColor
                                (fabSkin));
                    }
                    return true;
                case R.id.open_parent:
                    loadlist(new File(listElement.get(plist.get(0)).getDesc()).getParent(), false, 0);
                    return true;
                case R.id.all:
                    if (adapter.areAllChecked(currentPath)) {
                        adapter.toggleChecked(false, currentPath);
                    } else {
                        adapter.toggleChecked(true, currentPath);
                    }
                    mode.invalidate();

                    return true;
                case R.id.rename:

                    final ActionMode m = mode;
                    final BaseFile f;
                    f = (listElement.get(
                            (plist.get(0)))).generateBaseFile();
                    rename(f);
                    mode.finish();
                    return true;
                case R.id.hide:
                    for (int i1 = 0; i1 < plist.size(); i1++) {
                        hide(listElement.get(plist.get(i1)).getDesc());
                    }
                    updateList();
                    mode.finish();
                    return true;
                case R.id.ex:
                    mainActivity.mainActivityHelper.extractFile(new File(listElement.get(plist.get(0)).getDesc()));
                    mode.finish();
                    return true;
                case R.id.cpy:
                    mainActivity.movePath = null;
                    ArrayList<BaseFile> copies = new ArrayList<>();
                    for (int i2 = 0; i2 < plist.size(); i2++) {
                        copies.add(listElement.get(plist.get(i2)).generateBaseFile());
                    }
                    mainActivity.copyPath = copies;
                    mainActivity.supportInvalidateOptionsMenu();
                    mode.finish();
                    return true;
                case R.id.cut:
                    mainActivity.copyPath = null;
                    ArrayList<BaseFile> copie = new ArrayList<>();
                    for (int i3 = 0; i3 < plist.size(); i3++) {
                        copie.add(listElement.get(plist.get(i3)).generateBaseFile());
                    }
                    mainActivity.movePath = copie;
                    mainActivity.supportInvalidateOptionsMenu();
                    mode.finish();
                    return true;
                case R.id.compress:
                    ArrayList<BaseFile> copies1 = new ArrayList<>();
                    for (int i4 = 0; i4 < plist.size(); i4++) {
                        copies1.add(listElement.get(plist.get(i4)).generateBaseFile());
                    }
                    utils.showCompressDialog((MainActivity) getActivity(), copies1, currentPath);
                    mode.finish();
                    return true;
                case R.id.open_with:
                    utils.openunknown(new File(listElement.get((plist.get(0))).getDesc()), getActivity(), true);
                    return true;
                case R.id.add_shortcut:
                    addShortcut(listElement.get(plist.get(0)));
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // called when the user exits the action mode
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selection = false;
            if (mainActivity.isDrawerLocked) {
                mainActivity.translateDrawerList(false);
            }

            mainActivity.floatingActionButton.showMenuButton(true);
            if (!results) {
                adapter.toggleChecked(false, currentPath);
            }
            else {
                adapter.toggleChecked(false);
            }
            mainActivity.setPagingEnabled(true);

            mainActivity.updateViews(new ColorDrawable(MainActivity.currentTab==1 ?
                    skinTwoColor : skinColor));

            if (!mainActivity.isDrawerLocked) {
                mainActivity.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
                        mainActivity.drawerLinear);
            }
        }
    };

    private BroadcastReceiver receiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateList();
        }
    };

    public void home() {
        ma.loadlist((ma.home), false, 0);
    }

    /**
     * method called when list item is clicked in the adapter
     * @param position the {@link int} position of the list item
     * @param imageView the check {@link RoundedImageView} that is to be animated
     */
    public void onListItemClicked(int position, ImageView imageView) {
        if (position >= listElement.size()) return;

        if (results) {

            // check to initialize search results
            // if search task is been running, cancel it
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            frmSearchAsyncHelper fragment = (frmSearchAsyncHelper) fragmentManager
                    .findFragmentByTag(MainActivity.TAG_ASYNC_HELPER);
            if (fragment != null) {
                if (fragment.mSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
                    fragment.mSearchTask.cancel(true);
                }
                getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }

            retainSearchTask = true;
            results = false;
        } else {
            retainSearchTask = false;
            MainActivityHelper.SEARCH_TEXT = null;
        }
        if (selection == true) {
            if (!listElement.get(position).getSize().equals(goBack)) {
                // the first {goBack} item if back navigation is enabled
                adapter.toggleChecked(position, imageView);
            } else {
                selection = false;
                if (actionMode != null) {
                    actionMode.finish();
                }
                actionMode = null;
            }

        } else {
            if (!listElement.get(position).getSize().equals(goBack)) {

                // hiding search view if visible
                if (MainActivity.isSearchViewEnabled)   mainActivity.hideSearchView();

                String path;
                Layoutelements l = listElement.get(position);
                if (!l.hasSymlink()) {

                    path = l.getDesc();
                } else {

                    path = l.getSymlink();
                }
                if (listElement.get(position).isDirectory()) {
                    computeScroll();
                    loadlist(path, false, openMode);
                } else {
                    if (mainActivity.returnIntent) {
                        returnIntentResults(new File(l.getDesc()));
                    } else {

                        utils.openFile(new File(l.getDesc()), (MainActivity) getActivity());
                    }
                    DataUtils.addHistoryFile(l.getDesc());
                }
            } else {

                goBackItemClick();

            }
        }
    }

    public void updateTabWithDb(Tab tab) {
        currentPath = tab.getPath();
        home = tab.getHome();
        loadlist(currentPath, false, -1);
    }

    private void returnIntentResults(File file) {
        mainActivity.returnIntent = false;

        Intent intent = new Intent();
        if (mainActivity.ringtonePickerIntent) {

            Uri mediaStoreUri = MediaStoreHack.getUriFromFile(file.getPath(), getActivity());
            System.out.println(mediaStoreUri.toString() + "\t" + MimeTypes.getMimeType(file));
            intent.setDataAndType(mediaStoreUri, MimeTypes.getMimeType(file));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, mediaStoreUri);
            getActivity().setResult(getActivity().RESULT_OK, intent);
            getActivity().finish();
        } else {

            Log.d("pickup", "file");
            intent.setData(Uri.fromFile(file));
            getActivity().setResult(getActivity().RESULT_OK, intent);
            getActivity().finish();
        }
    }

    LoadList loadList;

    public void loadlist(String path, boolean back, int openMode) {
        if (actionMode != null) {
            actionMode.finish();
        }
        /*if(openMode==-1 && android.util.Patterns.EMAIL_ADDRESS.matcher(path).matches())
            bindDrive(path);
        else */
        if (loadList != null) {
            loadList.cancel(true);
        }
        loadList = new LoadList(back, ma.getActivity(), ma, openMode);
        loadList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (path));

    }

    void initNoFileLayout() {
        nofilesview = rootView.findViewById(R.id.no_file_layout);
        if (baseTheme == 0) {
            ((ImageView) nofilesview.findViewById(R.id.image)).setColorFilter(Color.parseColor("#666666"));
        }
        else {
            nofilesview.setBackgroundColor(Resource.getColor(getContext(), R.color.holo_dark_background));
            ((TextView) nofilesview.findViewById(R.id.no_file_text)).setTextColor(Color.WHITE);
        }
    }

    public boolean checkforpath(String path) {
        boolean grid = false, both_contain = false;
        int index1 = -1, index2 = -1;
        for (String s : DataUtils.gridfiles) {
            index1++;
            if ((path).contains(s)) {
                grid = true;
                break;
            }
        }
        for (String s : DataUtils.listfiles) {
            index2++;
            if ((path).contains(s)) {
                if (grid == true) both_contain = true;
                grid = false;
                break;
            }
        }
        if (!both_contain) return grid;
        String path1 = DataUtils.gridfiles.get(index1), path2 = DataUtils.listfiles.get(index2);
        if (path1.contains(path2)) {
            return true;
        }
        else if (path2.contains(path1)) {
            return false;
        }
        else {
            return grid;
        }
    }

    public void createViews(ArrayList<Layoutelements> bitmap, boolean back, String f, int
            openMode, boolean results, boolean grid) {
        try {
            if (bitmap != null) {
                if (goBackItem) {
                    if (!f.equals("/") && (openMode == 0 || openMode == 3)) {
                        if (bitmap.size() == 0 || !bitmap.get(0).getSize().equals(goBack)) {
                            bitmap.add(0, utils.newElement(Resource.getResource(getContext(), R.drawable.abc_ic_ab_back_material), "..", "", "", goBack, 0, false, true, ""));
                        }
                    }
                }
                if (bitmap.size() == 0 && !results) {
                    nofilesview.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    swipeRefreshLayout.setEnabled(false);
                } else {
                    swipeRefreshLayout.setEnabled(true);
                    nofilesview.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);

                }
                listElement = bitmap;
                if (grid && isList) {
                    switchToGrid();
                }
                else if (!grid && !isList) {
                    switchToList();
                }
                if (adapter == null) {
                    adapter = new Recycleradapter(ma,
                            bitmap, ma.getActivity());
                }
                else {
                    adapter.generate(listElement);
                }
                stopAnims = true;
                this.openMode = openMode;
                if (openMode != 2)
                    DataUtils.addHistoryFile(f);
                //swipeRefreshLayout.setRefreshing(false);
                try {
                    listView.setAdapter(adapter);
                    if (!addheader) {
                        listView.removeItemDecoration(headersDecor);
                        listView.removeItemDecoration(dividerItemDecoration);
                        addheader = true;
                    }
                    if (addheader && isList) {
                        dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, true, showDivider);
                        listView.addItemDecoration(dividerItemDecoration);
                        headersDecor = new StickyRecyclerHeadersDecoration(adapter);
                        listView.addItemDecoration(headersDecor);
                        addheader = false;
                    }
                    if (!results) this.results = false;
                    currentPath = f;
                    if (back) {
                        if (scrolls.containsKey(currentPath)) {
                            Bundle b = scrolls.get(currentPath);
                            if (isList) {
                                layoutManager.scrollToPositionWithOffset(b.getInt("index"), b.getInt("top"));
                            }
                            else {
                                layoutManagerGrid.scrollToPositionWithOffset(b.getInt("index"), b.getInt("top"));
                            }
                        }
                    }
                    //floatingActionButton.show();
                    mainActivity.updatepaths(no);
                    listView.stopScroll();
                    fastScroller.setRecyclerView(listView, isList ? 1 : columns);
                    toolbarContainer.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                        @Override
                        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                            fastScroller.updateHandlePosition(verticalOffset, 112);
                            //    fastScroller.setPadding(fastScroller.getPaddingLeft(),fastScroller.getTop(),fastScroller.getPaddingRight(),112+verticalOffset);
                            //      fastScroller.updateHandlePosition();
                        }
                    });
                    fastScroller.registerOnTouchListener(new FastScroller.onTouchListener() {
                        @Override
                        public void onTouch() {
                            if (stopAnims && adapter != null) {
                                stopAnimation();
                                stopAnims = false;
                            }
                        }
                    });
                    if (buttons.getVisibility() == View.VISIBLE) mainActivity.bbar(this);
                    //mainActivity.invalidateFab(openMode);
                } catch (Exception e) {
                }
            } else {//Toast.makeText(getActivity(),res.getString(R.string.error),Toast.LENGTH_LONG).show();
                loadlist(home, true, 0);
            }
        } catch (Exception e) {
        }

    }

    public void rename(final BaseFile f) {
        MaterialDialog.Builder a = new MaterialDialog.Builder(getActivity());
        String name = f.getName();
        a.input("", name, false, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

            }
        });
        if (baseTheme == 1) {
            a.theme(Theme.DARK);
        }
        a.title(utils.getString(getActivity(), R.string.rename));
        a.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog materialDialog) {
                String name = materialDialog.getInputEditText().getText().toString();
                if (MainActivityHelper.validateFileName(new HFile(openMode, currentPath + "/" + name), false)) {

                    if (openMode == 1)
                        mainActivity.mainActivityHelper.rename(openMode, f.getPath(), currentPath + name, getActivity(), rootMode);
                    else
                        mainActivity.mainActivityHelper.rename(openMode, (f).getPath(), (currentPath + "/" + name), getActivity(), rootMode);

                } else {
                    Toast.makeText(mainActivity, R.string.invalid_name, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNegative(MaterialDialog materialDialog) {

                materialDialog.cancel();
            }
        });
        a.positiveText(R.string.save);
        a.negativeText(R.string.cancel);
        int color = Color.parseColor(fabSkin);
        a.positiveColor(color).negativeColor(color).widgetColor(color);
        a.build().show();
    }

    public void computeScroll() {
        View vi = listView.getChildAt(0);
        int top = (vi == null) ? 0 : vi.getTop();
        int index;
        if (isList) {
            index = layoutManager.findFirstVisibleItemPosition();
        }
        else{
            index = layoutManagerGrid.findFirstVisibleItemPosition();
        }
        Bundle b = new Bundle();
        b.putInt("index", index);
        b.putInt("top", top);
        scrolls.put(currentPath, b);
    }

    public void goBack() {
        if (openMode == 2) {
            loadlist(home, false, 0);
            return;
        }

        File f = new File(currentPath);
        if (!results && !retainSearchTask) {

            // normal case
            if (selection) {
                adapter.toggleChecked(false);
            } else {
                if (currentPath.equals("/") || currentPath.equals(home))
                    mainActivity.exit();
                else if (utils.canGoBack(f)) {
                    loadlist(f.getParent(), true, openMode);
                } else mainActivity.exit();
            }
        }
        else if (!results && retainSearchTask) {
             if (MainActivityHelper.SEARCH_TEXT!=null) {

                mainActivity.mainFragment = (frmMain) mainActivity.getFragment().getTab();
                FragmentManager fm = mainActivity.getSupportFragmentManager();

                // getting parent path to resume search from there
                String parentPath = new File(currentPath).getParent();
                // don't fuckin' remove this line, we need to change
                // the path back to parent on back press
                currentPath = parentPath;

                MainActivityHelper.addSearchFragment(fm, new frmSearchAsyncHelper(),
                        parentPath, MainActivityHelper.SEARCH_TEXT, openMode, rootMode,
                        sharePreference.getBoolean(frmSearchAsyncHelper.KEY_REGEX, false),
                        sharePreference.getBoolean(frmSearchAsyncHelper.KEY_REGEX_MATCHES, false));
            }
            else {
                loadlist(currentPath, true, -1);
            }

            retainSearchTask = false;
        } else {
            // to go back after search list have been popped
            FragmentManager fm = getActivity().getSupportFragmentManager();
            frmSearchAsyncHelper fragment = (frmSearchAsyncHelper) fm.findFragmentByTag(MainActivity.TAG_ASYNC_HELPER);
            if (fragment != null) {
                if (fragment.mSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
                    fragment.mSearchTask.cancel(true);
                }
            }
            loadlist(new File(currentPath).getPath(), true, -1);
            results = false;
        }
    }


    public void goBackItemClick() {
        if (openMode == 2) {
            loadlist(home, false, 0);
            return;
        }
        File f = new File(currentPath);
        if (!results) {
            if (selection) {
                adapter.toggleChecked(false);
            } else {
                if (currentPath.equals("/")) {
                    mainActivity.exit();
                }
                else if (utils.canGoBack(f)) {
                    loadlist(f.getParent(), true, openMode);
                }
                else {
                    mainActivity.exit();
                }
            }
        } else {
            loadlist(f.getPath(), true, openMode);
        }
    }

    public void updateList() {
        computeScroll();
        ic.cleanup();
        loadlist((currentPath), true, openMode);
    }

    public void getSortModes() {
        int t = Integer.parseInt(sharePreference.getString("sortBy", "0"));
        if (t <= 3) {
            sortBy = t;
            asc = 1;
        } else if (t > 3) {
            asc = -1;
            sortBy = t - 4;
        }
        directorySort = Integer.parseInt(sharePreference.getString("dirontop", "0"));

    }

    @Override
    public void onResume() {
        super.onResume();
        (getActivity()).registerReceiver(receiver2, new IntentFilter("loadlist"));
    }

    @Override
    public void onPause() {
        super.onPause();
        (getActivity()).unregisterReceiver(receiver2);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tabHandler.close();
    }

    void fixIcons() {
        Resources res = getResources();
        for (Layoutelements layoutelements : listElement) {
            Drawable ic = layoutelements.isDirectory() ? folder : Icons.loadMimeIcon(getActivity(), layoutelements.getDesc(), !isList, res);
            layoutelements.setImageId(ic);
        }
    }
    // method to add search result entry to the LIST_ELEMENT arrayList
    private void addTo(BaseFile mFile) {
        File f = new File(mFile.getPath());
        String size = "";
        if (!DataUtils.hiddenfiles.contains(mFile.getPath())) {
            if (mFile.isDirectory()) {
                size = "";
                Layoutelements layoutelements = utils.newElement(folder, f.getPath(), mFile.getPermisson(), mFile.getLink(), size, 0, true, false, mFile.getDate() + "");
                layoutelements.setMode(mFile.getMode());
                listElement.add(layoutelements);
                folderCount++;
            } else {
                long longSize = 0;
                try {
                    if (mFile.getSize() != -1) {
                        longSize = Long.valueOf(mFile.getSize());
                        size = utils.readableFileSize(longSize);
                    } else {
                        size = "";
                        longSize = 0;
                    }
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                }
                try {
                    Layoutelements layoutelements = utils.newElement(Icons.loadMimeIcon(getActivity(), f.getPath(), !isList, getResources() ), f.getPath(), mFile.getPermisson(), mFile.getLink(), size, longSize, false, false, mFile.getDate() + "");
                    layoutelements.setMode(mFile.getMode());
                    listElement.add(layoutelements);
                    fileCount++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void hide(String path) {

        DataUtils.addHiddenFile(path);
        if (new File(path).isDirectory()) {
            File f1 = new File(path + "/" + ".nomedia");
            if (!f1.exists()) {
                try {
                    mainActivity.mainActivityHelper.mkFile(new HFile(HFile.LOCAL_MODE, f1.getPath()), this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            utils.scanFile(path, getActivity());
        }

    }

    private void addShortcut(Layoutelements path) {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent = new Intent(getActivity().getApplicationContext(),
                MainActivity.class);
        shortcutIntent.putExtra("path", path.getDesc());
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Intent addIntent = new Intent();
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, new File(path.getDesc()).getName());

        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getActivity(),
                        R.mipmap.ic_launcher));

        addIntent
                .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getActivity().sendBroadcast(addIntent);
    }

    // adds search results based on result boolean. If false, the adapter is initialised with initial
    // values, if true, new values are added to the adapter.
    public void addSearchResult(BaseFile a) {
        if (listView != null) {

            // initially clearing the array for new result set
            if (!results) {
                listElement.clear();
                fileCount = 0;
                folderCount = 0;
            }

            // adding new value to listElement
            addTo(a);
            if (!results) {
                createViews(listElement, false, (currentPath), openMode, false, !isList);
                pathName.setText(mainActivity.getString(R.string.empty));
                fullPath.setText(mainActivity.getString(R.string.searching));
                results = true;
            } else {
                adapter.addItem();
            }
            stopAnimation();
        }
    }

    public void onSearchCompleted() {
        if (!results) {
            // no results were found
            listElement.clear();
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Collections.sort(listElement, new FileListSorter(directorySort, sortBy, asc, rootMode));
                return null;
            }

            @Override
            public void onPostExecute(Void c) {
                createViews(listElement, true, (currentPath), openMode, true, !isList);
                pathName.setText(mainActivity.getString(R.string.empty));
                fullPath.setText(mainActivity.getString(R.string.search_results));
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    Loadlistener loadlistener = new Loadlistener.Stub() {
        @Override
        public void load(final List<Layoutelements> layoutelements, String driveId) throws RemoteException {
            System.out.println(layoutelements.size() + "\t" + driveId);
        }

        @Override
        public void error(final String message, final int mode) throws RemoteException {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity, "Error " + message + mode, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    IMyAidlInterface aidlInterface;
    boolean mbound = false;

    public void bindDrive(String account) {
        Intent i = new Intent();
        i.setClassName("com.amaze.filemanager.driveplugin", "com.amaze.filemanager.driveplugin.MainService");
        i.putExtra("account", account);
        try {
            getActivity().bindService((i), mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void unbindDrive() {
        if (mbound != false)
            getActivity().unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            aidlInterface = (IMyAidlInterface.Stub.asInterface(service));
            mbound = true;
            try {
                aidlInterface.registerCallback(loadlistener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                aidlInterface.loadRoot();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mbound = false;
            Log.d("DriveConnection", "DisConnected");
            aidlInterface = null;
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public long getCode() {
        return code;
    }
}