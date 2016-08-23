package com.amaze.filemanager.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amaze.filemanager.Constant;
import com.amaze.filemanager.R;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.Futils;
import com.amaze.filemanager.utils.PreferenceUtils;
import com.stericson.RootTools.RootTools;

import java.io.IOException;

/**
 * Created by arpitkh996 on 03-03-2016.
 */
public class BaseActivity extends AppCompatActivity {
    public static int baseTheme;
    public SharedPreferences Sp;

    // Accent and Primary hex color string respectively
    public static String accentSkin;
    public static String skin, skinTwo;
    Futils fileUntils;
    boolean  rootMode,checkStorage=true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Sp = PreferenceManager.getDefaultSharedPreferences(this);
        int th = Integer.parseInt(Sp.getString(Constant.THEME, "0"));
        // checking if theme should be set light/dark or automatic
        baseTheme = th == 2 ? PreferenceUtils.hourOfDay() : th;
        fileUntils=new Futils();
        boolean random = Sp.getBoolean(Constant.RANDOM_CHECKBOX, false);
        if ( random )  {
            skin = PreferenceUtils.random(Sp);
            skinTwo = PreferenceUtils.random(Sp);
        } else {
            skin = PreferenceUtils.getPrimaryColorString(Sp);
            skinTwo = PreferenceUtils.getPrimaryTwoColorString(Sp);
        }
        accentSkin = PreferenceUtils.getAccentString(Sp);
        setTheme();
        rootMode = Sp.getBoolean(Constant.ROOT_MODE, false);
        if (rootMode) {
            if (!RootTools.isAccessGiven()) {
                rootMode = false;
                Sp.edit().putBoolean(Constant.ROOT_MODE, false).apply();
            }
        }

        //requesting storage permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkStorage)
            if (!checkStoragePermission())
                requestStoragePermission();

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
    }
    public boolean checkStoragePermission() {

        // Verify that all required contact permissions have been granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            final MaterialDialog materialDialog = fileUntils.showBasicDialog(this,accentSkin,baseTheme, new String[]{getResources().getString(R.string.grant_text), getResources().getString(R.string.grant_per), getResources().getString(R.string.grant), getResources().getString(R.string.cancel), null});
            materialDialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat
                            .requestPermissions(BaseActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 77);
                    materialDialog.dismiss();
                }
            });
            materialDialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            materialDialog.setCancelable(false);
            materialDialog.show();

        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 77);
        }
    }

    void setTheme() {
        if (Build.VERSION.SDK_INT >= 21) {
            int themeStyle = 0;
            switch (accentSkin) {
                case "#F44336":
                    themeStyle = (baseTheme == 0) ? R.style.pref_accent_light_red : R.style.pref_accent_dark_red;
                    break;

                case "#e91e63":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_pink : R.style.pref_accent_dark_pink;

                    break;

                case "#9c27b0":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_purple : R.style.pref_accent_dark_purple;

                    break;

                case "#673ab7":
                    themeStyle = ( baseTheme == 0) ? R.style.pref_accent_light_deep_purple : R.style.pref_accent_dark_deep_purple;
                    break;

                case "#3f51b5":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_indigo : R.style.pref_accent_dark_indigo;
                    break;

                case "#2196F3":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_blue : R.style.pref_accent_dark_blue;

                    break;

                case "#03A9F4":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_light_blue :  R.style.pref_accent_dark_light_blue;
                    break;

                case "#00BCD4":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_cyan : R.style.pref_accent_dark_cyan;
                    break;

                case "#009688":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_teal : R.style.pref_accent_dark_teal;
                    break;

                case "#4CAF50":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_green : R.style.pref_accent_dark_green;
                    break;

                case "#8bc34a":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_light_green : R.style.pref_accent_dark_light_green;
                    break;

                case "#FFC107":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_amber : R.style.pref_accent_dark_amber;
                    break;

                case "#FF9800":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_orange : R.style.pref_accent_dark_orange;
                    break;

                case "#FF5722":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_deep_orange : R.style.pref_accent_dark_deep_orange;
                    break;

                case "#795548":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_brown : R.style.pref_accent_dark_brown;
                    break;

                case "#212121":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_black : R.style.pref_accent_dark_black;
                    break;

                case "#607d8b":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_blue_grey : R.style.pref_accent_dark_blue_grey;
                    break;

                case "#004d40":
                    themeStyle = ( baseTheme == 0 ) ? R.style.pref_accent_light_super_su : R.style.pref_accent_dark_super_su;
                    break;
            }
            if( themeStyle != 0 ){
                setTitle(themeStyle);
            }
        }
        else
        {
            if (baseTheme == 1) {
                setTheme(R.style.appCompatDark);
            } else {
                setTheme(R.style.appCompatLight);
            }
        }
    }
}