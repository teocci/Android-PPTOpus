package com.github.teocci.android.pptopus.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.github.teocci.android.pptopus.R;
import com.github.teocci.android.pptopus.views.CircleButtonView;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.github.teocci.android.pptopus.utils.Config.REQUEST_ALL;

public class MainActivity extends AppCompatActivity
{
    static final public String TAG = MainActivity.class.getSimpleName();

    static {
        System.loadLibrary("talkwalkopus");
    }

    private CircleButtonView circleButton;

    private boolean gotPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermissions();
        initSettings();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.menu_quit).setShowAsAction(1);
        menu.findItem(R.id.menu_options).setShowAsAction(1);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.menu_quit:
                closeService();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        Map<String, Integer> perm = new HashMap<>();
        perm.put(RECORD_AUDIO, PERMISSION_DENIED);
        perm.put(WRITE_EXTERNAL_STORAGE, PERMISSION_DENIED);
        perm.put(READ_PHONE_STATE, PERMISSION_DENIED);

        for (int i = 0; i < permissions.length; i++) {
            perm.put(permissions[i], grantResults[i]);
        }

        if (perm.get(RECORD_AUDIO) == PERMISSION_GRANTED &&
                perm.get(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED &&
                perm.get(READ_PHONE_STATE) == PERMISSION_GRANTED) {
            gotPermissions = true;
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, RECORD_AUDIO) ||
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE) ||
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, READ_PHONE_STATE)) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.permission_warning)
                        .setPositiveButton(R.string.dismiss, null)
                        .show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initPermissions()
    {
        int microphonePermission = ContextCompat.checkSelfPermission(this, RECORD_AUDIO);
        int storagePermission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        int phonePermission = ContextCompat.checkSelfPermission(this, READ_PHONE_STATE);
        gotPermissions = microphonePermission == PERMISSION_GRANTED &&
                storagePermission == PERMISSION_GRANTED &&
                phonePermission == PERMISSION_GRANTED;
        if (!gotPermissions)
            requirePermissions();
    }

    private void initSettings()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        circleButton = (CircleButtonView) findViewById(R.id.circleButton);
        circleButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), IntercomActivity.class);
            startActivity(intent);
        });
    }

    private void requirePermissions()
    {
        ActivityCompat.requestPermissions(
                this,
                new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE},
                REQUEST_ALL
        );
    }

    private void closeService()
    {
        // Returns to home menu
        finish();
    }
}
