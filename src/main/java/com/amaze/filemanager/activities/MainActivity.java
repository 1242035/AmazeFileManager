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

package com.amaze.filemanager.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amaze.filemanager.Constant;
import com.amaze.filemanager.R;
import com.amaze.filemanager.adapters.DrawerAdapter;
import com.amaze.filemanager.database.Tab;
import com.amaze.filemanager.database.TabHandler;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.filesystem.FileUtil;
import com.amaze.filemanager.filesystem.HFile;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.fragments.frmAppList;
import com.amaze.filemanager.fragments.frmMain;
import com.amaze.filemanager.fragments.frmProcessViewer;
import com.amaze.filemanager.fragments.frmSearchAsyncHelper;
import com.amaze.filemanager.fragments.frmTab;
import com.amaze.filemanager.fragments.frmZipViewer;
import com.amaze.filemanager.services.CopyService;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.services.asynctasks.MoveFiles;
import com.amaze.filemanager.ui.dialogs.RenameBookmark;
import com.amaze.filemanager.ui.dialogs.RenameBookmark.BookmarkCallback;
import com.amaze.filemanager.ui.drawer.EntryItem;
import com.amaze.filemanager.ui.drawer.Item;
import com.amaze.filemanager.ui.drawer.SectionItem;
import com.amaze.filemanager.ui.icons.IconUtils;
import com.amaze.filemanager.ui.views.RoundedImageView;
import com.amaze.filemanager.ui.views.ScrimInsetsRelativeLayout;
import com.amaze.filemanager.utils.BookSorter;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.DataUtils.DataChangeListener;
import com.amaze.filemanager.utils.Futils;
import com.amaze.filemanager.utils.HistoryManager;
import com.amaze.filemanager.utils.MainActivityHelper;
import com.amaze.filemanager.utils.PreferenceUtils;
import com.amaze.filemanager.utils.Resource;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


public class MainActivity extends BaseActivity implements OnRequestPermissionsResultCallback,
        DataChangeListener, BookmarkCallback,
        frmSearchAsyncHelper.HelperCallbacks {

    final Pattern DIR_SEPARATOR = Pattern.compile("/");
    /* Request code used to invoke sign in user interactions. */
    static final int RC_SIGN_IN = 0;
    public Integer selected;
    public DrawerLayout drawerLayout;
    public ListView drawerList;
    public ScrimInsetsRelativeLayout drawerLinear;
    public String  path = "", launchPath;
    public int theme;
    public ArrayList<BaseFile> copyPath = null, movePath = null;
    public FrameLayout frameLayout;
    public boolean returnIntent = false;
    public boolean  aBoolean, openZip = false;
    public boolean ringtonePickerIntent = false, colouredNavigation = false;
    public Toolbar toolbar;
    public int skinStatusBar;
    public int storageCount = 0;

    public FloatingActionMenu floatingActionButton;
    public LinearLayout pathBar;
    public FrameLayout buttonBarFrame;
    public boolean isDrawerLocked = false;
    HistoryManager history, grid;

    MainActivity mainActivity = this;
    public DrawerAdapter adapter;
    IconUtils util;
    Context con = this;
    public MainActivityHelper mainActivityHelper;
    String zipPath;
    FragmentTransaction pendingFragmentTransaction;
    String pendingPath;
    boolean openProcesses = false;
    int hideMode;
    public int operation = -1;
    public ArrayList<BaseFile> openArrayList;
    public String openPath, openPathOne;
    MaterialDialog materialDialog;
    String newPath = null;
    boolean backPressedToExitOnce = false;
    Toast toast = null;
    ActionBarDrawerToggle drawerToggle;
    Intent intent;
    View drawerHeaderLayout;
    View drawerHeaderView, indicatorLayout;
    RoundedImageView drawerProfilePic;
    int sdk, counter = 0;
    LinearLayout buttons;
    HorizontalScrollView scroll, scroll1;
    CountDownTimer timer;
    IconUtils icons;
    TabHandler tabHandler;
    public RelativeLayout drawerHeaderParent;
    static final int image_selected_request_code = 31;
    /* A flag indicating that a PendingIntent is in progress and prevents
    * us from starting further intents.
    */
    boolean intentInProgress, showHidden = false;

    // string builder object variables for pathBar animations
    StringBuffer newPathBuilder, oldPathBuilder;
    AppBarLayout appBarLayout;

    private static final int PATH_ANIM_START_DELAY = 0;
    private static final int PATH_ANIM_END_DELAY = 0;
    public static final String TAG_ASYNC_HELPER = "async_helper";
    public frmMain mainFragment;

    private RelativeLayout searchViewLayout;
    private AppCompatEditText searchViewEditText;
    private int[] searchCoords = new int[2];
    private View fabBackground;
    private CoordinatorLayout screenLayout;

    // the current visible tab, either 0 or 1
    public static int currentTab;

    public static boolean isSearchViewEnabled = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialisePreferences();
        DataUtils.registerOnDataChangedListener(this);
        setContentView(R.layout.main_toolbar);
        initialiseViews();
        tabHandler = new TabHandler(this, null, null, 1);
        mainActivityHelper = new MainActivityHelper(this);
        initialiseFab();

        history = new HistoryManager(this, "Table2");
        history.initializeTable(DataUtils.HISTORY, 0);
        history.initializeTable(DataUtils.HIDDEN, 0);
        grid = new HistoryManager(this, Constant.DB_LIST_GRID_MODE);
        grid.initializeTable(DataUtils.LIST, 0);
        grid.initializeTable(DataUtils.GRID, 0);
        grid.initializeTable(DataUtils.BOOKS, 1);
        if (!Sp.getBoolean(Constant.BOOKMARK_ADDED, false)) {
            grid.make(DataUtils.BOOKS);
            Sp.edit().putBoolean(Constant.BOOKMARK_ADDED, true).apply();
        }
        DataUtils.setHiddenfiles(history.readTable(DataUtils.HIDDEN));
        DataUtils.setGridfiles(grid.readTable(DataUtils.GRID));
        DataUtils.setListfiles(grid.readTable(DataUtils.LIST));

        util = new IconUtils(Sp, this);
        icons = new IconUtils(Sp, this);

        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                fileUntils.crossfadeInverse(buttons,pathBar);
            }
        };
        path = getIntent().getStringExtra(Constant.ARGS_PATH);
        openProcesses = getIntent().getBooleanExtra(Constant.ARGS_OPEN_PROCESS, false);
        try {
            intent = getIntent();
            if (intent.getStringArrayListExtra(Constant.ARGS_FAILED_OPEN) != null) {
                ArrayList<BaseFile> failedOps = intent.getParcelableArrayListExtra(Constant.ARGS_FAILED_OPEN);
                if (failedOps != null) {
                    mainActivityHelper.showFailedOperationDialog(failedOps, intent.getBooleanExtra(Constant.ARGS_MOVE, false), this);
                }
            }
            if (intent.getAction() != null) 
            {
                if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)) 
                {
                    // file picker intent
                    returnIntent = true;
                    Toast.makeText(this, fileUntils.getString(con, R.string.pick_a_file), Toast.LENGTH_LONG).show();
                } 
                else if (intent.getAction().equals(RingtoneManager.ACTION_RINGTONE_PICKER)) {
                    // ringtone picker intent
                    returnIntent = true;
                    ringtonePickerIntent = true;
                    Toast.makeText(this, fileUntils.getString(con, R.string.pick_a_file), Toast.LENGTH_LONG).show();
                } 
                else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                    // zip viewer intent
                    Uri uri = intent.getData();
                    openZip = true;
                    zipPath = uri.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateDrawer();

        // setting window background color instead of each item, in order to reduce pixel overdraw
        if ( baseTheme==0 ) {
            getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
        else {
            getWindow().setBackgroundDrawableResource(R.color.holo_dark_background);
        }

        if (savedInstanceState == null) {
            if (openProcesses) {
                android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, new frmProcessViewer());
                //   transaction.addToBackStack(null);
                selected = 102;
                openProcesses = false;
                //title.setText(utils.getString(con, R.string.process_viewer));
                //Commit the transaction
                transaction.commit();
                supportInvalidateOptionsMenu();
            }
            else {
                if (path != null && path.length() > 0) {
                    HFile file = new HFile(HFile.UNKNOWN,path);
                    file.generateMode(this);
                    if(file.isDirectory()) {
                        goToMain(path);
                    }
                    else{
                        goToMain("");
                        fileUntils.openFile(new File(path), this);
                    }
                }
                else {
                    goToMain("");
                }
            }
        }
        else {
            copyPath = savedInstanceState.getParcelableArrayList(Constant.ARGS_COPY_PATH);
            movePath = savedInstanceState.getParcelableArrayList(Constant.ARGS_MOVE_PATH);
            openPath = savedInstanceState.getString(Constant.ARGS_OPEN_PATH);
            openPathOne = savedInstanceState.getString(Constant.ARGS_OPEN_PATH_ONE);
            openArrayList = savedInstanceState.getParcelableArrayList(Constant.ARGS_OPEN_ARRAY_LIST);
            operation = savedInstanceState.getInt(Constant.ARGS_OPERATION);
            selected = savedInstanceState.getInt(Constant.ARGS_SELECTED_ITEM, 0);
            adapter.toggleChecked(selected);
            //mainFragment = (Main) savedInstanceState.getParcelable("main_fragment");
        }

        if (baseTheme == 1) {
            drawerList.setBackgroundColor(ContextCompat.getColor(this, R.color.holo_dark_background));
        }
        drawerList.setDivider(null);
        if (!isDrawerLocked) {
            drawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    drawerLayout,         /* DrawerLayout object */
                   // R.drawable.ic_drawer_l,  /* nav drawer image to replace 'Up' caret */
                    R.string.drawer_open,  /* "open drawer" description for accessibility */
                    R.string.drawer_close  /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {
                    mainActivity.onDrawerClosed();
                }

                public void onDrawerOpened(View drawerView) {
                    //title.setText("Amaze File Manager");
                    // creates call to onPrepareOptionsMenu()
                }
            };
            drawerLayout.addDrawerListener(drawerToggle);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer_l);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            drawerToggle.syncState();
        }
        if (drawerToggle != null) {
            drawerToggle.setDrawerIndicatorEnabled(true);
            drawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer_l);
        }
        //recents header color implementation
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription("Amaze",
                    ((BitmapDrawable) Resource.getResource(this, R.mipmap.ic_launcher)).getBitmap(),
                    Color.parseColor((currentTab==1 ? skinTwo : skin)));
            ((Activity) this).setTaskDescription(taskDescription);
        }
    }

    /**
     * Returns all available SD-Cards in the system (include emulated)
     * <p>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standard way to get it.
     * TODO: Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */


    public List<String> getStorageDirectories() {
        // Final set of paths
        final ArrayList<String> rv = new ArrayList<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkStoragePermission())
            rv.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            String strings[] = FileUtil.getExtSdCardPathsForActivity(this);
            for (String s : strings) {
                File f = new File(s);
                if (!rv.contains(s) && fileUntils.canListFiles(f))
                    rv.add(s);
            }
        }
        rootMode = Sp.getBoolean(Constant.ROOT_MODE, false);
        if (rootMode) {
            rv.add("/");
        }
        File usb = getUsbDrive();
        if (usb != null && !rv.contains(usb.getPath())) {
            rv.add(usb.getPath());
        }
        return rv;
    }

    @Override
    public void onBackPressed() {
        if (!isDrawerLocked) {
            if (drawerLayout.isDrawerOpen(drawerLinear)) {
                drawerLayout.closeDrawer(drawerLinear);
            } else {
                onbackpressed();
            }
        } else onbackpressed();
    }

    void onbackpressed() {
        try {

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            String name = fragment.getClass().getName();
            if (searchViewLayout.isShown()) {
                // hide search view if visible, with an animation
                hideSearchView();

            }
            else if (name.contains("TabFragment")) {
                if (floatingActionButton.isOpened()) {
                    floatingActionButton.close(true);
                    fileUntils.revealShow(findViewById(R.id.fab_bg), false);
                } else {
                    frmTab tabFragment = ((frmTab) getSupportFragmentManager().findFragmentById(R.id.content_frame));
                    Fragment fragment1 = tabFragment.getTab();
                    frmMain main = (frmMain) fragment1;
                    main.goBack();
                }
            }
            else if (name.contains("ZipViewer"))
            {
                frmZipViewer zipViewer = (frmZipViewer) getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (zipViewer.mActionMode == null) {
                    if (zipViewer.cangoBack()) {
                        zipViewer.goBack();
                    } else if (openZip) {
                        openZip = false;
                        finish();
                    } else {

                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.setCustomAnimations(R.anim.slide_out_bottom, R.anim.slide_out_bottom);
                        fragmentTransaction.remove(zipViewer);
                        fragmentTransaction.commit();
                        supportInvalidateOptionsMenu();
                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.showMenuButton(true);

                    }
                } else {
                    zipViewer.mActionMode.finish();
                }
            } else
                goToMain("");
        } catch (ClassCastException e) {
            goToMain("");
        }
    }

    public void invalidatePasteButton(MenuItem paste) {
        if (movePath != null || copyPath != null) {
            paste.setVisible(true);
        } else {
            paste.setVisible(false);
        }
    }

    public void exit() {
        if (backPressedToExitOnce) {
            finish();
            if (rootMode) {
                try {
                    RootTools.closeAllShells();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.backPressedToExitOnce = true;
            showToast(fileUntils.getString(this, R.string.press_again));
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backPressedToExitOnce = false;
                }
            }, 2000);
        }
    }

    public void updateDrawer() {
        ArrayList<Item> list = new ArrayList<>();
        List<String> val = getStorageDirectories();
        ArrayList<String[]> books = new ArrayList<>();
        ArrayList<String[]> Servers = new ArrayList<>();
        ArrayList<String[]> accounts = new ArrayList<>();
        storageCount = 0;
        for (String file : val) {
            File f = new File(file);
            String name;
            Drawable icon1 = ContextCompat.getDrawable(this, R.drawable.ic_sd_storage_white_56dp);
            if ("/storage/emulated/legacy".equals(file) || "/storage/emulated/0".equals(file)) {
                name = getResources().getString(R.string.storage);

            }
            else if ("/storage/sdcard1".equals(file)) {
                name = getResources().getString(R.string.ext_storage);
            }
            else if ("/".equals(file)) {
                name = getResources().getString(R.string.root_directory);
                icon1 = ContextCompat.getDrawable(this, R.drawable.ic_drawer_root_white);
            }
            else {
                name = f.getName();
            }
            if (!f.isDirectory() || f.canExecute()) {
                storageCount++;
                list.add(new EntryItem(name, file, icon1));
            }
        }
        DataUtils.setStorages(val);
        list.add(new SectionItem());


        try {
            for (String[] file : grid.readTableSecondary(DataUtils.DRIVE)) {
                accounts.add(file);
            }
            DataUtils.setAccounts(accounts);
            if (accounts.size() > 0) {
                Collections.sort(accounts, new BookSorter());
                for (String[] file : accounts)
                    list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable
                            .drive)));
                list.add(new SectionItem());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            for (String[] file : grid.readTableSecondary(DataUtils.BOOKS)) {
                books.add(file);
            }
            DataUtils.setBooks(books);
            if (books.size() > 0) {
                Collections.sort(books, new BookSorter());
                for (String[] file : books)
                    list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable
                            .folder_fab)));
                list.add(new SectionItem());
            }
        } catch (Exception e) {

        }
        list.add(new EntryItem(getResources().getString(R.string.quick), "5", ContextCompat.getDrawable(this, R.drawable.ic_star_white_18dp)));
        list.add(new EntryItem(getResources().getString(R.string.recent), "6", ContextCompat.getDrawable(this, R.drawable.ic_history_white_48dp)));
        list.add(new EntryItem(getResources().getString(R.string.images), "0", ContextCompat.getDrawable(this, R.drawable.ic_doc_image)));
        list.add(new EntryItem(getResources().getString(R.string.videos), "1", ContextCompat.getDrawable(this, R.drawable.ic_doc_video_am)));
        list.add(new EntryItem(getResources().getString(R.string.audio), "2", ContextCompat.getDrawable(this, R.drawable.ic_doc_audio_am)));
        list.add(new EntryItem(getResources().getString(R.string.documents), "3", ContextCompat.getDrawable(this, R.drawable.ic_doc_doc_am)));
        //list.add(new EntryItem(getResources().getString(R.string.apk), "4", ContextCompat.getDrawable(this, R.drawable.ic_doc_apk_grid)));
        DataUtils.setList(list);
        adapter = new DrawerAdapter(this, list, MainActivity.this, Sp);
        drawerList.setAdapter(adapter);
    }

    public void updateDrawer(String path) {
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                String path = strings[0];
                int k = 0, i = 0;
                for (Item item : DataUtils.getList()) {
                    if (!item.isSection()) {
                        if (((EntryItem) item).getPath().equals(path))
                            k = i;
                    }
                    i++;
                }
                return k;
            }

            @Override
            public void onPostExecute(Integer integers) {
                if (adapter != null) {
                    adapter.toggleChecked(integers);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

    }

    public void goToMain(String path) {
        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //title.setText(R.string.app_name);
        frmTab tabFragment = new frmTab();
        if (path != null && path.length() > 0) {
            Bundle b = new Bundle();
            b.putString(Constant.ARGS_PATH, path);
            tabFragment.setArguments(b);
        }
        transaction.replace(R.id.content_frame, tabFragment);
        // Commit the transaction
        selected = 0;
        transaction.addToBackStack("tabt" + 1);
        transaction.commitAllowingStateLoss();
        setActionBarTitle(null);
        floatingActionButton.setVisibility(View.VISIBLE);
        floatingActionButton.showMenuButton(true);
        if (openZip && zipPath != null) {
            if (zipPath.endsWith(".zip") || zipPath.endsWith(".apk")) {
                openZip(zipPath);
            }
            else {
                openRar(zipPath);
            }
            zipPath = null;
        }
    }

    public void selectedItem(final int i) {
        ArrayList<Item> list=DataUtils.getList();
        if (!list.get(i).isSection())
            if ((selected == null || selected >= list.size())) {

                frmTab tabFragment = new frmTab();
                Bundle a = new Bundle();
                a.putString(Constant.ARGS_PATH, ((EntryItem) list.get(i)).getPath());
                tabFragment.setArguments(a);

                android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, tabFragment);

                transaction.addToBackStack("tabt1" + 1);
                pendingFragmentTransaction = transaction;
                selected = i;
                adapter.toggleChecked(selected);
                if (!isDrawerLocked) {
                    drawerLayout.closeDrawer(drawerLinear);
                }
                else {
                    onDrawerClosed();
                }
                floatingActionButton.setVisibility(View.VISIBLE);
                floatingActionButton.showMenuButton(true);
            }
            else {
                pendingPath = ((EntryItem) list.get(i)).getPath();
                if (pendingPath.equals("drive")) {
                    pendingPath = ((EntryItem) list.get(i)).getTitle();
                }
                selected = i;
                adapter.toggleChecked(selected);
                if (!isDrawerLocked) drawerLayout.closeDrawer(drawerLinear);
                else onDrawerClosed();

            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_extra, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void setActionBarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem s = menu.findItem(R.id.view);
        MenuItem search = menu.findItem(R.id.search);
        MenuItem paste = menu.findItem(R.id.paste);
        String f = null;
        Fragment fragment;
        try {
            fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            f = fragment.getClass().getName();
        } catch (Exception e1) {
            return true;
        }
        if ( f.contains("TabFragment") ) {
            try {
                frmTab tabFragment = (frmTab) fragment;
                frmMain ma = ((frmMain) tabFragment.getTab());
                if (ma.isList) {
                    s.setTitle(R.string.grid_view);
                }
                else {
                    s.setTitle(R.string.list_view);
                }
                updatePath(ma.currentPath, ma.results, ma.openMode, ma.folderCount, ma.fileCount);
            } catch (Exception e) {
                e.printStackTrace();
            }

            initiatebbar();
            if (Build.VERSION.SDK_INT >= 21) {
                toolbar.setElevation(0);
            }
            invalidatePasteButton(paste);
            search.setVisible(true);
            if (indicatorLayout != null) {
                indicatorLayout.setVisibility(View.VISIBLE);
            }
            menu.findItem(R.id.search).setVisible(true);
            menu.findItem(R.id.home).setVisible(true);
            menu.findItem(R.id.history).setVisible(true);
            menu.findItem(R.id.set_home).setVisible(true);

            menu.findItem(R.id.item10).setVisible(true);
            if (showHidden) {
                menu.findItem(R.id.hidden_items).setVisible(true);
            }
            menu.findItem(R.id.view).setVisible(true);
            menu.findItem(R.id.extract).setVisible(false);
            invalidatePasteButton(menu.findItem(R.id.paste));
            findViewById(R.id.button_bar_frame).setVisibility(View.VISIBLE);
        }
        else if (f.contains("frgAppsList") || f.contains("frgProcessViewer") ) {
            appBarLayout.setExpanded(true);
            menu.findItem(R.id.set_home).setVisible(false);
            if (indicatorLayout != null) {
                indicatorLayout.setVisibility(View.GONE);
            }
            findViewById(R.id.button_bar_frame).setVisibility(View.GONE);
            menu.findItem(R.id.search).setVisible(false);
            menu.findItem(R.id.home).setVisible(false);
            menu.findItem(R.id.history).setVisible(false);
            menu.findItem(R.id.extract).setVisible(false);
            if (f.contains("ProcessViewer")) {
                menu.findItem(R.id.item10).setVisible(false);
            }
            else {
                menu.findItem(R.id.directory_sort).setVisible(false);
                menu.findItem(R.id.sort_by).setVisible(false);
            }
            menu.findItem(R.id.hidden_items).setVisible(false);
            menu.findItem(R.id.view).setVisible(false);
            menu.findItem(R.id.paste).setVisible(false);
        }
        else if (f.contains("ZipViewer")) {
            menu.findItem(R.id.set_home).setVisible(false);
            if (indicatorLayout != null) {
                indicatorLayout.setVisibility(View.GONE);
            }
            TextView textView = (TextView) mainActivity.pathBar.findViewById(R.id.full_path);
            pathBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            menu.findItem(R.id.search).setVisible(false);
            menu.findItem(R.id.home).setVisible(false);
            menu.findItem(R.id.history).setVisible(false);
            menu.findItem(R.id.item10).setVisible(false);
            menu.findItem(R.id.hidden_items).setVisible(false);
            menu.findItem(R.id.view).setVisible(false);
            menu.findItem(R.id.paste).setVisible(false);
            menu.findItem(R.id.extract).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    void showToast(String message) {
        if (this.toast == null) {
            // Create toast if found null, it would he the case of first call only
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        }
        else if (this.toast.getView() == null) {
            // Toast not showing, so create new one
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            // Updating toast message is showing
            this.toast.setText(message);
        }
        // Showing toast finally
        this.toast.show();
    }

    void killToast() {
        if (this.toast != null) {
            this.toast.cancel();
        }
    }

    public void back() {
        super.onBackPressed();
    }
/*
    @Override
    public boolean onOptionsItemselecteded(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (drawerToggle != null && drawerToggle.onOptionsItemselecteded(item)) {
            return true;
        }
        // Handle action buttons
        Main ma = null;
        try {
            TabFragment tabFragment=getFragment();
            if(tabFragment!=null)
                ma = (Main)tabFragment .getTab();
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (item.getItemId()) {
            case R.id.home:
                if(ma!=null)
                    ma.home();
                break;
            case R.id.history:
                if(ma!=null) {
                    fileUntils.showHistoryDialog(ma);
                }
                break;
            case R.id.set_home:
                if(ma==null) {
                    return super.onOptionsItemselecteded(item);
                }
                final Main main = ma;
                if (main.openMode != 0 && main.openMode != 3) {
                    Toast.makeText(mainActivity, R.string.not_allowed, Toast.LENGTH_SHORT).show();
                    break;
                }
                final MaterialDialog b = fileUntils.showBasicDialog(mainActivity,BaseActivity.accentSkin,baseTheme,
                        new String[]{getResources().getString(R.string.question_set),
                                getResources().getString(R.string.set_as_home), getResources().getString(R.string.yes), getResources().getString(R.string.no), null});
                b.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        main.home = main.CURRENT_PATH;
                        updatepaths(main.no);
                        b.dismiss();
                    }
                });
                b.show();
                break;
            case R.id.item3:
                finish();
                break;
            case R.id.item10:
                Fragment fragment = getDFragment();
                if (fragment.getClass().getName().contains("frgAppsList")) {
                    fileUntils.showSortDialog((frgAppsList) fragment);
                }
                break;
            case R.id.sort_by:
                if(ma!=null) {
                    fileUntils.showSortDialog(ma);
                }
                break;
            case R.id.directory_sort:
                if(ma==null) {
                    return super.onOptionsItemselecteded(item);
                }
                String[] sort = getResources().getStringArray(R.array.directory_sort_mode);
                MaterialDialog.Builder a = new MaterialDialog.Builder(mainActivity);
                if (theme == 1) a.theme(Theme.DARK);
                a.title(R.string.directory_sort);
                int current = Integer.parseInt(Sp.getString("dirontop", "0"));
                a.items(sort).itemsCallbackSingleChoice(current, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onselectedion(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Sp.edit().putString("dirontop", "" + which).commit();
                        dialog.dismiss();
                        return true;
                    }
                });
                a.build().show();
                break;
            case R.id.hidden_items:
                fileUntils.showHiddenDialog(ma);
                break;
            case R.id.view:
                if (ma.IS_LIST) {
                    if (DataUtils.listfiles.contains(ma.CURRENT_PATH)) {
                        DataUtils.listfiles.remove(ma.CURRENT_PATH);
                        grid.removePath(ma.CURRENT_PATH, DataUtils.LIST);
                    }
                    grid.addPath(null, ma.CURRENT_PATH,DataUtils. GRID, 0);
                    DataUtils.gridfiles.add(ma.CURRENT_PATH);
                } else {
                    if (DataUtils.gridfiles.contains(ma.CURRENT_PATH)) {
                        DataUtils.gridfiles.remove(ma.CURRENT_PATH);
                        grid.removePath(ma.CURRENT_PATH,DataUtils. GRID);
                    }
                    grid.addPath(null, ma.CURRENT_PATH, DataUtils.LIST, 0);
                    DataUtils.listfiles.add(ma.CURRENT_PATH);

                }
                ma.switchView();
                break;
            case R.id.paste:
                String path = ma.CURRENT_PATH;
                ArrayList<BaseFile> arrayList = new ArrayList<>();
                if (copyPath != null) {
                    arrayList = copyPath;
                    new CopyFileCheck(ma, path, false,mainActivity,rootMode).executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, arrayList);
                }
                else if (movePath != null) {
                    arrayList = movePath;
                    new CopyFileCheck(ma, path, true,mainActivity,rootMode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            arrayList);
                }
                copyPath = null;
                movePath = null;

                invalidatePasteButton(item);
                break;
            case R.id.extract:
                Fragment fragment1 = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (fragment1.getClass().getName().contains("ZipViewer"))
                    mainActivityHelper.extractFile(((ZipViewer) fragment1).f);
                break;
            case R.id.search:
                View searchItem = toolbar.findViewById(R.id.search);
                searchViewEditText.setText("");
                searchItem.getLocationOnScreen(searchCoords);
                revealSearchView();
                break;
        }
        return super.onOptionsItemselecteded(item);
    }
       */
    /**
     * show search view with a circular reveal animation
     */
    void revealSearchView() {

        final int START_RADIUS = 16;
        int endRadius = Math.max(toolbar.getWidth(), toolbar.getHeight());

        Animator animator;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            animator = ViewAnimationUtils.createCircularReveal(searchViewLayout,
                    searchCoords[0]+32, searchCoords[1]-16, START_RADIUS, endRadius);
        } else {
            // TODO:ViewAnimationUtils.createCircularReveal
            animator = new ObjectAnimator().ofFloat(searchViewLayout,"alpha",0f,1f);
        }
        fileUntils.revealShow(fabBackground, true);

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(600);
        searchViewLayout.setVisibility(View.VISIBLE);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                searchViewEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchViewEditText, InputMethodManager.SHOW_IMPLICIT);
                isSearchViewEnabled = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    /**
     * hide search view with a circular reveal animation
     */
    public void hideSearchView() {

        final int END_RADIUS = 16;
        int startRadius = Math.max(searchViewLayout.getWidth(), searchViewLayout.getHeight());
        Animator animator;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            animator = ViewAnimationUtils.createCircularReveal(searchViewLayout,
                    searchCoords[0]+32, searchCoords[1]-16, startRadius, END_RADIUS);
        }else {
            // TODO: ViewAnimationUtils.createCircularReveal
            animator = new ObjectAnimator().ofFloat(searchViewLayout,"alpha",1f,0f);
        }

        fileUntils.revealShow(fabBackground, false);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(600);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                searchViewLayout.setVisibility(View.GONE);
                isSearchViewEnabled = false;
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(searchViewEditText.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /*@Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        copyPath=savedInstanceState.getStringArrayList("copyPath");
        movePath=savedInstanceState.getStringArrayList("movePath");
        openPath = savedInstanceState.getString("openPath");
        openPathOne = savedInstanceState.getString("openPathOne");
        openArrayList = savedInstanceState.getStringArrayList("openArrayList");
        opnameList=savedInstanceState.getStringArrayList("opnameList");
        operation = savedInstanceState.getInt("operation");
        selected = savedInstanceState.getInt("selecteditem", 0);
    }*/
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (drawerToggle != null) {
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selected != null) {
            outState.putInt(Constant.ARGS_SELECTED_ITEM, selected);
        }
        if (copyPath != null) {
            outState.putParcelableArrayList(Constant.ARGS_COPY_PATH, copyPath);
        }
        if (movePath != null) {
            outState.putParcelableArrayList(Constant.ARGS_MOVE_PATH, movePath);
        }
        if (openPath != null) {
            outState.putString(Constant.ARGS_OPEN_PATH, openPath);
            outState.putString(Constant.ARGS_OPEN_PATH_ONE, openPathOne);
            outState.putParcelableArrayList(Constant.ARGS_OPEN_ARRAY_LIST, (openArrayList));
            outState.putInt(Constant.ARGS_OPERATION, operation);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mainActivityHelper.mNotificationReceiver);
        unregisterReceiver(receiver2);
        killToast();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (materialDialog != null && !materialDialog.isShowing()) {
            materialDialog.show();
            materialDialog = null;
        }
        IntentFilter newFilter = new IntentFilter();
        newFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        newFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        newFilter.addDataScheme(ContentResolver.SCHEME_FILE);
        registerReceiver(mainActivityHelper.mNotificationReceiver, newFilter);
        registerReceiver(receiver2, new IntentFilter("general_communications"));
        if (getSupportFragmentManager().findFragmentById(R.id.content_frame)
                .getClass().getName().contains("TabFragment")) {

            floatingActionButton.setVisibility(View.VISIBLE);
            floatingActionButton.showMenuButton(false);
        } else {

            floatingActionButton.setVisibility(View.INVISIBLE);
            floatingActionButton.hideMenuButton(false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        // let the system handle all other key events
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rootMode) {
            try {
                RootTools.closeAllShells();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        DataUtils.clear();

        //unbindDrive();
        if (grid != null) {
            grid.end();
        }
        if (history != null) {
            history.end();
        }
    }

    public void updatepaths(int pos) {
        frmTab tabFragment=getFragment();
        if(tabFragment!=null) {
            tabFragment.updatepaths(pos);
        }
    }

    public void openZip(String path) {
        findViewById(R.id.lin).animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_in_bottom);
        Fragment zipFragment = new frmZipViewer();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.ARGS_PATH, path);
        zipFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.content_frame, zipFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void openRar(String path) {
        openZip(path);
    }

    public frmTab getFragment() {
        Fragment fragment = getDFragment();
        if (fragment == null) {
            return null;
        }
        if (fragment instanceof frmTab) {
            frmTab tabFragment = (frmTab) fragment;
            return tabFragment;
        }
        return null;
    }

    public Fragment getDFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }

    public void setPagingEnabled(boolean b) {
        getFragment().mViewPager.setPagingEnabled(b);
    }

    public File getUsbDrive() {
        File parent;
        parent = new File("/storage");

        try {
            for (File f : parent.listFiles()) {
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute()) {
                    return f;
                }
            }
        } catch (Exception e) {
        }
        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute()) {
            return (parent);
        }
        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute()) {
            return parent;
        }
        return null;
    }

    public void refreshDrawer() {
        List<String> val=DataUtils.getStorages();
        if (val == null) {
            val = getStorageDirectories();
        }
        ArrayList<Item> list = new ArrayList<>();
        storageCount = 0;
        for (String file : val) {
            File f = new File(file);
            String name;
            Drawable icon1 = ContextCompat.getDrawable(this, R.drawable.ic_sd_storage_white_56dp);
            if ("/storage/emulated/legacy".equals(file) || "/storage/emulated/0".equals(file)) {
                name = getResources().getString(R.string.storage);
            }
            else if ("/storage/sdcard1".equals(file)) {
                name = getResources().getString(R.string.ext_storage);
            }
            else if ("/".equals(file)) {
                name = getResources().getString(R.string.root_directory);
                icon1 = ContextCompat.getDrawable(this, R.drawable.ic_drawer_root_white);
            }
            else {
                name = f.getName();
            }
            if (!f.isDirectory() || f.canExecute()) {
                storageCount++;
                list.add(new EntryItem(name, file, icon1));
            }
        }
        list.add(new SectionItem());
        ArrayList<String[]> Servers=DataUtils.getServers();
        if (Servers!=null && Servers.size() > 0) {
            for (String[] file : Servers) {
                list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable.ic_settings_remote_white_48dp)));
            }

            list.add(new SectionItem());
        }
        ArrayList<String[]> accounts=DataUtils.getAccounts();
        if (accounts != null && accounts.size() > 0) {
            Collections.sort(accounts,new BookSorter());
            for (String[] file : accounts) {
                list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable.drive)));
            }

            list.add(new SectionItem());
        }
        ArrayList<String[]> books=DataUtils.getBooks();
        if (books != null && books.size() > 0) {
            Collections.sort(books,new BookSorter());
            for (String[] file : books) {
                list.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable
                        .folder_fab)));
            }
            list.add(new SectionItem());
        }
        list.add(new EntryItem(getResources().getString(R.string.quick), "5", ContextCompat.getDrawable(this, R.drawable.ic_star_white_18dp)));
        list.add(new EntryItem(getResources().getString(R.string.recent), "6", ContextCompat.getDrawable(this, R.drawable.ic_history_white_48dp)));
        list.add(new EntryItem(getResources().getString(R.string.images), "0", ContextCompat.getDrawable(this, R.drawable.ic_doc_image)));
        list.add(new EntryItem(getResources().getString(R.string.videos), "1", ContextCompat.getDrawable(this, R.drawable.ic_doc_video_am)));
        list.add(new EntryItem(getResources().getString(R.string.audio), "2", ContextCompat.getDrawable(this, R.drawable.ic_doc_audio_am)));
        list.add(new EntryItem(getResources().getString(R.string.documents), "3", ContextCompat.getDrawable(this, R.drawable.ic_doc_doc_am)));
        list.add(new EntryItem(getResources().getString(R.string.apk), "4", ContextCompat.getDrawable(this, R.drawable.ic_doc_apk_grid)));
        DataUtils.setList(list);
        adapter = new DrawerAdapter(con, list, MainActivity.this, Sp);
        drawerList.setAdapter(adapter);

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == image_selected_request_code) {
            if (Sp != null && intent != null && intent.getData() != null) {
                if (Build.VERSION.SDK_INT >= 19)
                    getContentResolver().takePersistableUriPermission(intent.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Sp.edit().putString(Constant.DRAWER_HEADER_PATH, intent.getData().toString()).apply();
            }
        }
        else if (requestCode == 3) {
            String p = Sp.getString(Constant.URI, null);
            Uri oldUri = null;
            if (p != null) {
                oldUri = Uri.parse(p);
            }
            Uri treeUri = null;
            if (responseCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = intent.getData();
                // Persist URI - this is required for verification of writability.
                if (treeUri != null) {
                    Sp.edit().putString(Constant.URI, treeUri.toString()).apply();
                }
            }
            // If not confirmed SAF, or if still not writable, then revert settings.
            if (responseCode != Activity.RESULT_OK) {
                if (treeUri != null) {
                    Sp.edit().putString(Constant.URI, oldUri.toString()).apply();
                }
                return;
            }

            // After confirmation, update stored value of folder.
            // Persist access permissions.
            final int takeFlags = intent.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
            switch (operation) {
                case DataUtils.DELETE://deletion
                    new DeleteTask(null, mainActivity).execute((openArrayList));
                    break;
                case DataUtils.COPY://copying
                    Intent intent1 = new Intent(con, CopyService.class);
                    intent1.putExtra(Constant.ARGS_FILE_PATH, (openArrayList));
                    intent1.putExtra(Constant.ARGS_COPY_DIRECTORY, openPath);
                    startService(intent1);
                    break;
                case DataUtils.MOVE://moving
                    new MoveFiles((openArrayList), ((frmMain) getFragment().getTab()), ((frmMain) getFragment().getTab()).getActivity(),0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
                    break;
                case DataUtils.NEW_FOLDER://mkdir
                    frmMain ma1 = ((frmMain) getFragment().getTab());
                    mainActivityHelper.mkDir(RootHelper.generateBaseFile(new File(openPath),true), ma1);
                    break;
                case DataUtils.RENAME:
                    mainActivityHelper.rename(HFile.LOCAL_MODE,(openPath), (openPathOne),mainActivity,rootMode);
                    frmMain ma2 = ((frmMain) getFragment().getTab());
                    ma2.updateList();
                    break;
                case DataUtils.NEW_FILE:
                    frmMain ma3 = ((frmMain) getFragment().getTab());
                    mainActivityHelper.mkFile(new HFile(HFile.LOCAL_MODE,openPath), ma3);

                    break;
                case DataUtils.EXTRACT:
                    mainActivityHelper.extractFile(new File(openPath));
                    break;
                case DataUtils.COMPRESS:
                    mainActivityHelper.compressFiles(new File(openPath), openArrayList);
            }
            operation = -1;
        }
    }


    public void bbar(final frmMain main) {
        final String text = main.currentPath;
        try {
            buttons.removeAllViews();
            buttons.setMinimumHeight(pathBar.getHeight());
            Drawable arrow = Resource.getResource(this,R.drawable.abc_ic_ab_back_holo_dark);
            Bundle b = fileUntils.getPaths(text, this);
            ArrayList<String> names = b.getStringArrayList("names");
            ArrayList<String> rnames = new ArrayList<String>();

            for (int i = names.size() - 1; i >= 0; i--) {
                rnames.add(names.get(i));
            }

            ArrayList<String> paths = b.getStringArrayList("paths");
            final ArrayList<String> rpaths = new ArrayList<String>();

            for (int i = paths.size() - 1; i >= 0; i--) {
                rpaths.add(paths.get(i));
            }
            View view = new View(this);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    toolbar.getContentInsetLeft(), LinearLayout.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(params1);
            buttons.addView(view);
            for (int i = 0; i < names.size(); i++) {
                final int k = i;
                ImageView v = new ImageView(this);
                v.setImageDrawable(arrow);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                v.setLayoutParams(params);
                final int index = i;
                if (rpaths.get(i).equals("/")) {
                    ImageButton ib = new ImageButton(this);
                    ib.setImageDrawable(icons.getRootDrawable());
                    ib.setBackgroundColor(Color.parseColor("#00ffffff"));
                    ib.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View p1) {
                            main.loadlist(("/"), false, main.openMode);
                            timer.cancel();
                            timer.start();
                        }
                    });
                    ib.setLayoutParams(params);
                    buttons.addView(ib);
                    if (names.size() - i != 1) {
                        buttons.addView(v);
                    }
                }
                else if (isStorage(rpaths.get(i))) {
                    ImageButton ib = new ImageButton(this);
                    ib.setImageDrawable(icons.getSdDrawable());
                    ib.setBackgroundColor(Color.parseColor("#00ffffff"));
                    ib.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View p1) {
                            main.loadlist((rpaths.get(k)), false, main.openMode);
                            timer.cancel();
                            timer.start();
                        }
                    });
                    ib.setLayoutParams(params);
                    buttons.addView(ib);
                    if (names.size() - i != 1) {
                        buttons.addView(v);
                    }
                }
                else {
                    Button button = new Button(this);
                    button.setText(rnames.get(index));
                    button.setTextColor(Resource.getColor(this,android.R.color.white));
                    button.setTextSize(13);
                    button.setLayoutParams(params);
                    button.setBackgroundResource(0);
                    button.setOnClickListener(new Button.OnClickListener() {

                        public void onClick(View p1) {
                            main.loadlist((rpaths.get(k)), false, main.openMode);
                            main.loadlist((rpaths.get(k)), false, main.openMode);
                            timer.cancel();
                            timer.start();
                        }
                    });
                    button.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            File file1 = new File(rpaths.get(index));
                            copyToClipboard(MainActivity.this, file1.getPath());
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.path_copied), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    });

                    buttons.addView(button);
                    if (names.size() - i != 1) {
                        buttons.addView(v);
                    }
                }
            }

            scroll.post(new Runnable() {
                @Override
                public void run() {
                    sendScroll(scroll);
                    sendScroll(scroll1);
                }
            });

            if (buttons.getVisibility() == View.VISIBLE) {
                timer.cancel();
                timer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("button view not available");
        }
    }

    boolean isStorage(String path) {
        List<String> val=DataUtils.getStorages();
        for (String s:val) {
            if (s.equals(path)) {
                return true;
            }
        }
        return false;
    }

    void sendScroll(final HorizontalScrollView scrollView) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_RIGHT);
                    }
                });
            }
        }).start();
    }

    void initialisePreferences() {
        theme = Integer.parseInt(Sp.getString(Constant.THEME, "0"));
        hideMode = Sp.getInt(Constant.HIDDEN_MODE, 0);
        showHidden = Sp.getBoolean(Constant.SHOW_HIDDEN, false);
        aBoolean = Sp.getBoolean(Constant.VIEW, true);
        currentTab = Sp.getInt(Constant.CURRENT_TAB, PreferenceUtils.DEFAULT_CURRENT_TAB);
        skinStatusBar = (PreferenceUtils.getStatusColor((currentTab==1 ? skinTwo : skin)));

        colouredNavigation = Sp.getBoolean(Constant.COLORED_NAVIGATION, false);
    }

    void initialiseViews() {
        appBarLayout = (AppBarLayout) findViewById(R.id.lin);

        screenLayout = (CoordinatorLayout) findViewById(R.id.main_frame);
        buttonBarFrame = (FrameLayout) findViewById(R.id.button_bar_frame);

        //buttonBarFrame.setBackgroundColor(Color.parseColor(currentTab==1 ? skinTwo : skin));
        drawerHeaderLayout = getLayoutInflater().inflate(R.layout.drawer_header, null);
        drawerHeaderParent = (RelativeLayout) drawerHeaderLayout.findViewById(R.id.drawer_header_parent);
        drawerHeaderView = (View) drawerHeaderLayout.findViewById(R.id.drawer_header);
        fabBackground = findViewById(R.id.fab_bg);
        drawerHeaderView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                }
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, image_selected_request_code);
                return false;
            }
        });
        drawerProfilePic = (RoundedImageView) drawerHeaderLayout.findViewById(R.id.profile_pic);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        /* For SearchView, see onCreateOptionsMenu(Menu menu)*/
        //TOOLBAR_START_INSET = toolbar.getContentInsetStart();
        setSupportActionBar(toolbar);
        frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        indicatorLayout = findViewById(R.id.indicator_layout);
        drawerLinear = (ScrimInsetsRelativeLayout) findViewById(R.id.left_drawer);
        if (baseTheme == 1) {
            drawerLinear.setBackgroundColor(Color.parseColor("#303030"));
        }
        else {
            drawerLinear.setBackgroundColor(Color.WHITE);
        }
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawerLayout.setStatusBarBackgroundColor(Color.parseColor((currentTab==1 ? skinTwo : skin)));
        drawerList = (ListView) findViewById(R.id.menu_drawer);
        drawerHeaderView.setBackgroundResource(R.drawable.amaze_header);
        //drawerHeaderParent.setBackgroundColor(Color.parseColor((currentTab==1 ? skinTwo : skin)));
        /*if (findViewById(R.id.tab_frame) != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, drawerLinear);
            drawerLayout.setScrimColor(Color.TRANSPARENT);
            isDrawerLocked = true;
        }
        */
        drawerList.addHeaderView(drawerHeaderLayout);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View v = findViewById(R.id.fab_bg);
        /*if (baseTheme != 1)
            v.setBackgroundColor(Color.parseColor("#a6ffffff"));*/
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionButton.close(true);
                fileUntils.revealShow(view, false);
                if (isSearchViewEnabled) hideSearchView();
            }
        });

        pathBar = (LinearLayout) findViewById(R.id.path_bar);
        buttons = (LinearLayout) findViewById(R.id.buttons);
        scroll = (HorizontalScrollView) findViewById(R.id.scroll);
        scroll1 = (HorizontalScrollView) findViewById(R.id.scroll1);
        scroll.setSmoothScrollingEnabled(true);
        scroll1.setSmoothScrollingEnabled(true);
        View settingsbutton = findViewById(R.id.settings_button);
        if (baseTheme == 1) {
            settingsbutton.setBackgroundResource(R.drawable.safr_ripple_black);
            ((ImageView) settingsbutton.findViewById(R.id.setting_icon)).setImageResource(R.drawable.ic_settings_white_48dp);
            ((TextView) settingsbutton.findViewById(R.id.setting_text)).setTextColor( Resource.getColor(this,android.R.color.white));
        }
        settingsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, Preferences.class);
                finish();
                final int enter_anim = android.R.anim.fade_in;
                final int exit_anim = android.R.anim.fade_out;
                Activity s = MainActivity.this;
                s.overridePendingTransition(exit_anim, enter_anim);
                s.finish();
                s.overridePendingTransition(enter_anim, exit_anim);
                s.startActivity(in);
            }

        });
        View appbutton = findViewById(R.id.app_button);
        if (baseTheme == 1) {
            appbutton.setBackgroundResource(R.drawable.safr_ripple_black);
            ((ImageView) appbutton.findViewById(R.id.app_icon)).setImageResource(R.drawable.ic_doc_apk_white);
            ((TextView) appbutton.findViewById(R.id.app_text)).setTextColor(Resource.getColor(this, android.R.color.white));
        }
        appbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.support.v4.app.FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                transaction2.replace(R.id.content_frame, new frmAppList() );
                findViewById(R.id.lin).animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                pendingFragmentTransaction = transaction2;
                if (!isDrawerLocked) drawerLayout.closeDrawer(drawerLinear);
                else onDrawerClosed();
                selected = -2;
                adapter.toggleChecked(false);
            }
        });

        // status bar0
        sdk = Build.VERSION.SDK_INT;

        if (sdk == 20 || sdk == 19) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            //tintManager.setStatusBarTintColor(Color.parseColor((currentTab==1 ? skinTwo : skin)));
            FrameLayout.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) findViewById(R.id.drawer_layout).getLayoutParams();
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            if (!isDrawerLocked) p.setMargins(0, config.getStatusBarHeight(), 0, 0);
        }
        else if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (isDrawerLocked) {
                window.setStatusBarColor((skinStatusBar));
            }
            else {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            if (colouredNavigation) {
                window.setNavigationBarColor(skinStatusBar);
            }
        }

        searchViewLayout = (RelativeLayout) findViewById(R.id.search_view);
        searchViewEditText = (AppCompatEditText) findViewById(R.id.search_edit_text);
        searchViewEditText.setOnKeyListener(new TextView.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    mainActivityHelper.search(searchViewEditText.getText().toString());
                    hideSearchView();
                    return true;
                }
                return false;
            }
        });

        searchViewEditText.setTextColor(getResources().getColor(android.R.color.black));
        searchViewEditText.setHintTextColor(Color.parseColor(BaseActivity.accentSkin));
    }

    /**
     * Call this method when you need to update the MainActivity view components' colors based on
     * update in the {@link MainActivity#currentTab}
     * Warning - All the variables should be initialised before calling this method!
     */
    public void updateViews(ColorDrawable colorDrawable) {

        // appbar view color
        mainActivity.buttonBarFrame.setBackgroundColor(colorDrawable.getColor());
        // action bar color
        mainActivity.getSupportActionBar().setBackgroundDrawable(colorDrawable);
        // drawer status bar I guess
        mainActivity.drawerLayout.setStatusBarBackgroundColor(colorDrawable.getColor());
        // drawer header background
        mainActivity.drawerHeaderParent.setBackgroundColor(colorDrawable.getColor());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // for lollipop devices, the status bar color
            mainActivity.getWindow().setStatusBarColor(colorDrawable.getColor());
            if (mainActivity.colouredNavigation) {
                mainActivity.getWindow().setNavigationBarColor(PreferenceUtils
                        .getStatusColor(colorDrawable.getColor()));
            }
        } 
        else if (Build.VERSION.SDK_INT == 20 || Build.VERSION.SDK_INT == 19) {
            // for kitkat devices, the status bar color
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(colorDrawable.getColor());
        }
    }

    void initialiseFab() {
        String folder_skin = PreferenceUtils.getFolderColorString(Sp);
        int fabSkinPressed = PreferenceUtils.getStatusColor(BaseActivity.accentSkin);
        int folderskin = Color.parseColor(folder_skin);
        int fabskinpressed = (PreferenceUtils.getStatusColor(folder_skin));
        floatingActionButton = (FloatingActionMenu) findViewById(R.id.menu);
        floatingActionButton.setMenuButtonColorNormal(Color.parseColor(BaseActivity.accentSkin));
        floatingActionButton.setMenuButtonColorPressed(fabSkinPressed);

        //if (baseTheme == 1) floatingActionButton.setMen
        floatingActionButton.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean b) {
                View v = findViewById(R.id.fab_bg);
                if (b) {
                    fileUntils.revealShow(v, true);
                }
                else {
                    fileUntils.revealShow(v, false);
                }
            }
        });

        FloatingActionButton floatingActionButtonFolder = (FloatingActionButton) findViewById(R.id.menu_item);
        floatingActionButtonFolder.setColorNormal(folderskin);
        floatingActionButtonFolder.setColorPressed(fabskinpressed);
        floatingActionButtonFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivityHelper.add(0);
                fileUntils.revealShow(findViewById(R.id.fab_bg), false);
                floatingActionButton.close(true);
            }
        });
        FloatingActionButton floatingActionButtonFile = (FloatingActionButton) findViewById(R.id.menu_item_one);
        floatingActionButtonFile.setColorNormal(folderskin);
        floatingActionButtonFile.setColorPressed(fabskinpressed);
        floatingActionButtonFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivityHelper.add(1);
                fileUntils.revealShow(findViewById(R.id.fab_bg), false);
                floatingActionButton.close(true);
            }
        });
    }

    public void updatePath(@NonNull final String news, boolean results, int
            openmode, int folder_count, int file_count) {

        if (news == null || news.length() == 0) {
            return;
        }

        if (openmode == Constant.OPEN_CUSTOM) {
            newPath = mainActivityHelper.getIntegralNames(news);
        }
        else {
            newPath = news;
        }
        final TextView bapath = (TextView) pathBar.findViewById(R.id.full_path);
        final TextView animPath = (TextView) pathBar.findViewById(R.id.full_path_anim);
        TextView textView = (TextView) pathBar.findViewById(R.id.path_name);
        if (!results) {
            textView.setText(folder_count + " " + getResources().getString(R.string.folders) + "" +
                    " " + file_count + " " + getResources().getString(R.string.files));
        } else {
            bapath.setText(R.string.search_results);
            textView.setText(R.string.empty);
            return;
        }
        final String oldPath = bapath.getText().toString();
        if (oldPath != null && oldPath.equals(newPath)) {
            return;
        }

        // implement animation while setting text
        newPathBuilder = new StringBuffer().append(newPath);
        oldPathBuilder = new StringBuffer().append(oldPath);

        final Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);

        if (newPath.length() > oldPath.length() &&
                newPathBuilder.delete(oldPath.length(), newPath.length()).toString().equals(oldPath) &&
                oldPath.length() != 0) {

            // navigate forward
            newPathBuilder.delete(0, newPathBuilder.length());
            newPathBuilder.append(newPath);
            newPathBuilder.delete(0, oldPath.length());
            animPath.setAnimation(slideIn);
            animPath.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            animPath.setVisibility(View.GONE);
                            bapath.setText(newPath);
                        }
                    }, PATH_ANIM_END_DELAY);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    animPath.setVisibility(View.VISIBLE);
                    animPath.setText(newPathBuilder.toString());
                    //bapath.setText(oldPath);

                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll1.fullScroll(View.FOCUS_RIGHT);
                        }
                    });
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    //onAnimationEnd(animation);
                }
            }).setStartDelay(PATH_ANIM_START_DELAY).start();
        }
        else if (newPath.length() < oldPath.length() &&
                oldPathBuilder.delete(newPath.length(), oldPath.length()).toString().equals(newPath))
        {

            // navigate backwards
            oldPathBuilder.delete(0, oldPathBuilder.length());
            oldPathBuilder.append(oldPath);
            oldPathBuilder.delete(0, newPath.length());
            animPath.setAnimation(slideOut);
            animPath.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animPath.setVisibility(View.GONE);
                    bapath.setText(newPath);

                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll1.fullScroll(View.FOCUS_RIGHT);
                        }
                    });
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    animPath.setVisibility(View.VISIBLE);
                    animPath.setText(oldPathBuilder.toString());
                    bapath.setText(newPath);

                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll1.fullScroll(View.FOCUS_LEFT);
                        }
                    });
                }
            }).setStartDelay(PATH_ANIM_START_DELAY).start();
        }
        else if (oldPath.isEmpty())
        {
            // case when app starts
            // FIXME: counter is incremented twice on app startup
            counter++;
            if (counter == 2) {

                animPath.setAnimation(slideIn);
                animPath.setText(newPath);
                animPath.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        animPath.setVisibility(View.VISIBLE);
                        bapath.setText("");
                        scroll.post(new Runnable() {
                            @Override
                            public void run() {
                                scroll1.fullScroll(View.FOCUS_RIGHT);
                            }
                        });
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animPath.setVisibility(View.GONE);
                                bapath.setText(newPath);
                            }
                        }, PATH_ANIM_END_DELAY);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        //onAnimationEnd(animation);
                    }
                }).setStartDelay(PATH_ANIM_START_DELAY).start();
            }

        }
        else {

            // completely different path
            // first slide out of old path followed by slide in of new path
            animPath.setAnimation(slideOut);
            animPath.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    animPath.setVisibility(View.VISIBLE);
                    animPath.setText(oldPath);
                    bapath.setText("");

                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll1.fullScroll(View.FOCUS_LEFT);
                        }
                    });
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);

                    //animPath.setVisibility(View.GONE);
                    animPath.setText(newPath);
                    bapath.setText("");
                    animPath.setAnimation(slideIn);

                    animPath.animate().setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    animPath.setVisibility(View.GONE);
                                    bapath.setText(newPath);
                                }
                            }, PATH_ANIM_END_DELAY);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            // we should not be having anything here in path bar
                            animPath.setVisibility(View.VISIBLE);
                            bapath.setText("");
                            scroll.post(new Runnable() {
                                @Override
                                public void run() {
                                    scroll1.fullScroll(View.FOCUS_RIGHT);
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    //onAnimationEnd(animation);
                }
            }).setStartDelay(PATH_ANIM_START_DELAY).start();
        }
    }

    public int dpToPx(double dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)));
        return px;
    }

    public void initiatebbar() {
        final View pathBar = findViewById(R.id.path_bar);
        TextView textView = (TextView) findViewById(R.id.full_path);

        pathBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frmMain m = ((frmMain) getFragment().getTab());
                if (m.openMode == 0) {
                    bbar(m);
                    fileUntils.crossfade(buttons,pathBar);
                    timer.cancel();
                    timer.start();
                }
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frmMain m = ((frmMain) getFragment().getTab());
                if (m.openMode == 0) {
                    bbar(m);
                    fileUntils.crossfade(buttons,pathBar);
                    timer.cancel();
                    timer.start();
                }
            }
        });

    }


    public boolean copyToClipboard(Context context, String text) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("Path copied to clipboard", text);
            clipboard.setPrimaryClip(clip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public void invalidateFab(int openmode) {
        if (openmode == Constant.OPEN_CUSTOM) {
            floatingActionButton.setVisibility(View.INVISIBLE);
            floatingActionButton.hideMenuButton(true);
        } else {
            floatingActionButton.setVisibility(View.VISIBLE);
            floatingActionButton.showMenuButton(true);
        }
    }

    public void renameBookmark(final String title, final String path) {
        if (DataUtils.containsBooks(new String[]{title,path}) != -1 || DataUtils.containsAccounts(new String[]{title,path}) != -1) {
            RenameBookmark renameBookmark=RenameBookmark.getInstance(title,path,BaseActivity.accentSkin,baseTheme);
            if(renameBookmark!=null){
                renameBookmark.show(getFragmentManager(),"renamedialog");
            }
        }
    }

    void onDrawerClosed() {
        if (pendingFragmentTransaction != null) {
            pendingFragmentTransaction.commit();
            pendingFragmentTransaction = null;
        }
        if (pendingPath != null) {
            try {

                HFile hFile = new HFile(HFile.UNKNOWN,pendingPath);
                hFile.generateMode(this);
                if (hFile.isSimpleFile()) {
                    fileUntils.openFile(new File(pendingPath), mainActivity);
                    pendingPath = null;
                    return;
                }
                frmTab m = getFragment();
                if(m==null){
                    goToMain(pendingPath);
                    return;
                }
                frmMain main = ((frmMain) m.getTab());
                if (main != null) {
                    main.loadlist(pendingPath, false, -1);
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
                selected = null;
                goToMain("");
            }
            pendingPath = null;
        }
        supportInvalidateOptionsMenu();
    }


    @Override
    public void onNewIntent(Intent i) {
        intent = i;
        path = i.getStringExtra("path");
        if (path != null) {
            if (new File(path).isDirectory()) {
                Fragment f = getDFragment();
                if ((f.getClass().getName().contains("TabFragment"))) {
                    frmMain m = ((frmMain) getFragment().getTab());
                    m.loadlist(path, false, 0);
                }
                else {
                    goToMain(path);
                }
            }
            else {
                fileUntils.openFile(new File(path), mainActivity);
            }
        }
        else if (i.getStringArrayListExtra(Constant.ARGS_FAILED_OPEN) != null) {
            ArrayList<BaseFile> failedOps = i.getParcelableArrayListExtra(Constant.ARGS_FAILED_OPEN);
            if (failedOps != null) {
                mainActivityHelper.showFailedOperationDialog(failedOps, i.getBooleanExtra(Constant.ARGS_MOVE, false), this);
            }
        }
        else if ((openProcesses = i.getBooleanExtra(Constant.ARGS_OPEN_PROCESS, false))) {

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, new frmProcessViewer());
            //   transaction.addToBackStack(null);
            selected = 102;
            openProcesses = false;
            //title.setText(utils.getString(con, R.string.process_viewer));
            //Commit the transaction
            transaction.commitAllowingStateLoss();
            supportInvalidateOptionsMenu();
        }
        else if (intent.getAction() != null)
            if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {

                // file picker intent
                returnIntent = true;
                Toast.makeText(this, fileUntils.getString(con, R.string.pick_a_file), Toast.LENGTH_LONG).show();
            }
            else if (intent.getAction().equals(RingtoneManager.ACTION_RINGTONE_PICKER)) {
                // ringtone picker intent
                returnIntent = true;
                ringtonePickerIntent = true;
                Toast.makeText(this, fileUntils.getString(con, R.string.pick_a_file), Toast.LENGTH_LONG).show();
            }
            else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                // zip viewer intent
                Uri uri = intent.getData();
                zipPath = uri.toString();
                openZip(zipPath);
            }
    }

    private BroadcastReceiver receiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent i) {
            if (i.getStringArrayListExtra(Constant.ARGS_FAILED_OPEN) != null) {
                ArrayList<BaseFile> failedOps = i.getParcelableArrayListExtra(Constant.ARGS_FAILED_OPEN);
                if (failedOps != null) {
                    mainActivityHelper.showFailedOperationDialog(failedOps, i.getBooleanExtra(Constant.ARGS_MOVE, false), mainActivity);
                }
            }
        }
    };


    public void translateDrawerList(boolean down) {
        if (down) {
            drawerList.animate().translationY(toolbar.getHeight());
        }
        else {
            drawerList.setTranslationY(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == 77) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateDrawer();
                frmTab tabFragment = getFragment();
                boolean b = Sp.getBoolean(Constant.NEED_TO_SET_HOME, true);
                //reset home and current paths according to new storages
                if (b) {
                    tabHandler.clear();
                    if (storageCount > 1) {
                        tabHandler.addTab(new Tab(1, "", ((EntryItem) DataUtils.list.get(1)).getPath(), "/"));
                    }
                    else {
                        tabHandler.addTab(new Tab(1, "", "/", "/"));
                    }
                    if (!DataUtils.list.get(0).isSection()) {
                        String pa = ((EntryItem) DataUtils.list.get(0)).getPath();
                        tabHandler.addTab(new Tab(2, "", pa, pa));
                    }
                    else {
                        tabHandler.addTab(new Tab(2, "", ((EntryItem) DataUtils.list.get(1)).getPath(), "/"));
                    }
                    if (tabFragment != null) {
                        Fragment main = tabFragment.getTab(0);
                        if (main != null) {
                            ((frmMain) main).updateTabWithDb(tabHandler.findTab(1));
                        }
                        Fragment main1 = tabFragment.getTab(1);
                        if (main1 != null) {
                            ((frmMain) main1).updateTabWithDb(tabHandler.findTab(2));
                        }
                    }
                    Sp.edit().putBoolean(Constant.NEED_TO_SET_HOME, false).apply();
                }
                else {
                    //just refresh list
                    if (tabFragment != null) {
                        Fragment main = tabFragment.getTab(0);
                        if (main != null) {
                            ((frmMain) main).updateList();
                        }
                        Fragment main1 = tabFragment.getTab(1);
                        if (main1 != null) {
                            ((frmMain) main1).updateList();
                        }
                    }
                }
            }
            else {
                Toast.makeText(this, R.string.grant_failed, Toast.LENGTH_SHORT).show();
                requestStoragePermission();
            }

        }
    }
    @Override
    public void onHiddenFileAdded(String path) {
        history.addPath(null,path,DataUtils.HIDDEN,0);
    }

    @Override
    public void onHiddenFileRemoved(String path) {
        history.removePath(path, DataUtils.HIDDEN);
    }

    @Override
    public void onHistoryAdded(String path) {
        history.addPath(null, path, DataUtils.HISTORY, 0);
    }

    @Override
    public void onBookAdded(String[] path, boolean refreshdrawer) {
        grid.addPath(path[0], path[1], DataUtils.BOOKS, 1);
        if(refreshdrawer) {
            refreshDrawer();
        }
    }

    @Override
    public void onHistoryCleared() {
        history.clear(DataUtils.HISTORY);
    }

    @Override
    public void delete(String title, String path) {
        grid.removePath(title, path, DataUtils.BOOKS);
        refreshDrawer();

    }

    @Override
    public void modify(String oldpath, String oldname, String newPath, String newname) {
        grid.rename( oldname,oldpath, newPath, newname, DataUtils.BOOKS);
        refreshDrawer();
    }

    @Override
    public void onPreExecute() {
        mainFragment.swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onPostExecute() {

        mainFragment.onSearchCompleted();
        mainFragment.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onProgressUpdate(BaseFile val) {

        mainFragment.addSearchResult(val);
    }

    @Override
    public void onCancelled() {

        mainFragment.createViews(mainFragment.listElement, false, mainFragment.currentPath,
                mainFragment.openMode, false, !mainFragment.isList);
        mainFragment.swipeRefreshLayout.setRefreshing(false);
    }
}