package com.mrbimc.selinux;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(View.generateViewId());
        setContentView(frame, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        getFragmentManager()
                .beginTransaction()
                .add(frame.getId(), new CreditsFragment())
                .commit();

    }

    public static class CreditsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_credits);

            for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                PreferenceCategory cat = (PreferenceCategory) getPreferenceScreen().getPreference(i);
                for(int j = 0; j < cat.getPreferenceCount(); j++){
                    Preference pref = cat.getPreference(j);
                    pref.setOnPreferenceClickListener(this);
                }
            }
        }


        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch(preference.getKey()) {
                case "author":
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/MrBIMC")));
                    return true;
                case "github":
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mrbimc/SELinuxModeChanger")));
                    return true;
                case "gpl3":
                    showDialog(getString(R.string.gpl3), "file:///android_asset/gpl3.txt");
                    return true;
                case "apache2":
                    showDialog(getString(R.string.apache2), "file:///android_asset/apache2.txt");
                    return true;
                case "rootshell":
                    showDialog(getString(R.string.gpl2), "file:///android_asset/gpl2.txt");
                    return true;
                default:
                    return false;
            }
        }

        private void showDialog(String title, String url) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(title);
            WebView wv = new WebView(getActivity());
            wv.loadUrl(url);
            wv.setHorizontalScrollBarEnabled(false);
            alert.setView(wv);
            alert.setNegativeButton(getActivity().getString(R.string.close), null);
            alert.show();
        }
    }
}
