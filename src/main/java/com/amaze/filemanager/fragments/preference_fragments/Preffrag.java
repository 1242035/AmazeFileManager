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

package com.amaze.filemanager.fragments.preference_fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amaze.filemanager.Constant;
import com.amaze.filemanager.R;
import com.amaze.filemanager.ui.views.CheckBx;
import com.amaze.filemanager.utils.PreferenceUtils;
import com.stericson.RootTools.RootTools;

public class Preffrag extends PreferenceFragment{

    private static final CharSequence PREFERENCE_KEY_ABOUT = Constant.ABOUT;

    int theme;
    SharedPreferences sharedPref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtils.reset();
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final int th1 = Integer.parseInt(sharedPref.getString(Constant.THEME, "0"));
        theme = th1==2 ? PreferenceUtils.hourOfDay() : th1;

        findPreference(Constant.COLUMNS).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String[] sort = getResources().getStringArray(R.array.columns);
                MaterialDialog.Builder a = new MaterialDialog.Builder(getActivity());
                if(theme==1)a.theme(Theme.DARK);
                a.title(R.string.gridcolumnno);
                int current = Integer.parseInt(sharedPref.getString(Constant.COLUMNS, "-1"));
                current=current==-1?0:current;
                if(current!=0)current=current-1;
                a.items(sort).itemsCallbackSingleChoice(current, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        sharedPref.edit().putString(Constant.COLUMNS, "" + (which!=0?sort[which]:""+-1)).commit();
                        dialog.dismiss();
                        return true;
                    }
                });
                a.build().show();
                return true;
            }
        });

        findPreference(Constant.THEME).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String[] sort = getResources().getStringArray(R.array.theme);
                int current = Integer.parseInt(sharedPref.getString(Constant.THEME, "0"));
                MaterialDialog.Builder a = new MaterialDialog.Builder(getActivity());
                if(theme==1)a.theme(Theme.DARK);
                a.items(sort).itemsCallbackSingleChoice(current, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        sharedPref.edit().putString(Constant.THEME, "" + which).commit();
                        dialog.dismiss();
                        restartPC(getActivity());
                        return true;
                    }
                });
                a.title(R.string.theme);
                a.build().show();
                return true;
            }
        });
        findPreference(Constant.COLORS).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((com.amaze.filemanager.activities.Preferences) getActivity()).selectItem(1);
                return true;
            }
        });



        final CheckBx rootmode = (CheckBx) findPreference(Constant.ROOT_MODE);
        rootmode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean b = sharedPref.getBoolean(Constant.ROOT_MODE, false);
                if (b) {
                    if (RootTools.isAccessGiven()) {
                        rootmode.setChecked(true);
                    
                    } else {  rootmode.setChecked(false);
				
                        Toast.makeText(getActivity(), getResources().getString(R.string.rootfailure), Toast.LENGTH_LONG).show();
                    }
                } else {
                    rootmode.setChecked(false);
                    
                }


                return false;
            }
        });
        // About
        Preference aboutPreference = findPreference(PREFERENCE_KEY_ABOUT);
        aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //startActivity(new Intent(getActivity(), AboutActivity.class));
                return false;
            }
        });

    }

    public static void restartPC(final Activity activity) {
        if (activity == null)
            return;
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());
    }
}