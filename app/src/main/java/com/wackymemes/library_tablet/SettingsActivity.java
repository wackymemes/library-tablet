package com.wackymemes.library_tablet;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.MediaRouteButton;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wackymemes.library_tablet.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends Activity {
    PreferenceManager preferences;

    @BindView(R.id.InputAPIKey)
    TextView InputAPIKey;
    // TODO: add "change backend urls"-feature

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        preferences = PreferenceManager.getInstance(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadSettings();
    }

    @Override
    public void onPause() {
        super.onPause();
        save();
    }


    private void save() {
        String APIKey = InputAPIKey.getText().toString();
        preferences.setToken(APIKey);

        Toast.makeText(getApplicationContext(), "Settings saved", Toast.LENGTH_SHORT).show();

        System.out.println("Settings saved");

    }

    private void reloadSettings() {
        refreshAPIKey();
    }

    public void refreshAPIKey() {

        String APIString = preferences.getToken();
        InputAPIKey.setText(APIString);

    }


}
