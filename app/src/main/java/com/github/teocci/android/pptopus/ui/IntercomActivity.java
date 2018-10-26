package com.github.teocci.android.pptopus.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.teocci.android.pptopus.R;
import com.github.teocci.android.pptopus.adapters.DeviceAdapter;
import com.github.teocci.android.pptopus.audio.Connection;
import com.github.teocci.android.pptopus.audio.Input;
import com.github.teocci.android.pptopus.audio.Output;
import com.github.teocci.android.pptopus.interfaces.JmDNSDiscoveryListener;
import com.github.teocci.android.pptopus.interfaces.ServiceRegisteredListener;
import com.github.teocci.android.pptopus.managers.ServiceListManager;
import com.github.teocci.android.pptopus.model.DeviceInfo;
import com.github.teocci.android.pptopus.utils.JmDNSHelper;
import com.github.teocci.android.pptopus.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

import javax.jmdns.ServiceEvent;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static com.github.teocci.android.pptopus.utils.Config.KEY_STATION_NAME;
import static com.github.teocci.android.pptopus.utils.Config.KEY_STREAM_AUDIO;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-10
 */
public class IntercomActivity extends AppCompatActivity
{
    private final static String TAG = LogHelper.makeLogTag(IntercomActivity.class);

    //    private NSDHelper nsdHelper;
    private JmDNSHelper jmDNSHelper;

    private String stationName;

    private Handler handler;

    private SharedPreferences config;

    private ServiceListManager serviceListManager = new ServiceListManager();

    private List<DeviceInfo> deviceList = new ArrayList<>();

    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;

    private TextView textStatus;

    private Button push2Talk;

    private JmDNSDiscoveryListener discoveryListener = new JmDNSDiscoveryListener()
    {
        @Override
        public void onServiceResolved(ServiceEvent event)
        {
            serviceListManager.add(event.getName(), event.getInfo());
            LogHelper.e(TAG, "onServiceResolved");

            runOnUiThread(() -> {
                // UI code goes here
                deviceAdapter.addAll(serviceListManager.getDeviceList());
                push2Talk.setEnabled(true);
            });

            Connection.setTarget(serviceListManager.getIPAddress(event.getName()));
            Output.start(getApplicationContext());
            Input.encodeTerminationSound(getApplicationContext());
        }

        @Override
        public void onServiceRemoved(ServiceEvent event)
        {
            if (serviceListManager.contains(event.getName())) {
                serviceListManager.remove(event.getName());
            }
        }
    };

    private ServiceRegisteredListener registeredListener = () -> {
        registerJmDNSDiscovery();
        textStatus.setText(stationName);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_intercom);

        initSettings();

        initElements();
        initHandlers();

        initJmDNSHelper();
        registerJmDNSService();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterJmDNSService();
        unregisterJmDNSDiscovery();
    }

    private void initSettings()
    {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        config = PreferenceManager.getDefaultSharedPreferences(this);

        final SharedPreferences.Editor editor = config.edit();

        stationName = config.getString(KEY_STATION_NAME, null);
        if ((stationName == null) || stationName.isEmpty()) {
            stationName = Build.MODEL;
            editor.putString(KEY_STATION_NAME, stationName);
            editor.apply();
        }

        handler = new Handler(Looper.getMainLooper());

        editor.putBoolean(KEY_STREAM_AUDIO, true);
        editor.apply();
    }

    private void initElements()
    {
        textStatus = (TextView) findViewById(R.id.text_status);
        recyclerView = (RecyclerView) findViewById(R.id.section_list);

        deviceAdapter = new DeviceAdapter();

        // vertical RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setChangeDuration(500);
        recyclerView.getItemAnimator().setMoveDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);

        recyclerView.setAdapter(deviceAdapter);

        push2Talk = (Button) findViewById(R.id.button_talk);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initHandlers()
    {
        final Handler handler = new Handler(Looper.getMainLooper());
        push2Talk.setOnTouchListener(new View.OnTouchListener()
        {
            private boolean enabled = true;
            private boolean pressed = false;

            private Runnable setEnabled = () -> {

                enabled = true;

                push2Talk.setEnabled(true);
                push2Talk.setText(getString(R.string.push_to_talk));
            };

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (enabled) {
                            pressed = true;
                            push2Talk.setText(getString(R.string.push_to_talk_active));
                            Input.start();
                        }

                        return false;

                    case MotionEvent.ACTION_MOVE:
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (enabled && pressed) {

                            enabled = false;
                            pressed = false;

                            push2Talk.setEnabled(false);
                            push2Talk.setText("Wait");

                            Input.stop();

                            handler.postDelayed(setEnabled, 1000);
                        }

                        return false;
                }

                return false;
            }
        });
    }

    private void initJmDNSHelper()
    {
        jmDNSHelper = new JmDNSHelper(this, stationName, discoveryListener, registeredListener);
    }

    private void registerJmDNSService()
    {
        if (jmDNSHelper != null && !jmDNSHelper.isServiceRegistered()) {
            jmDNSHelper.registerService();
        }
    }

    private void registerJmDNSDiscovery()
    {
        if (jmDNSHelper != null && !jmDNSHelper.isDiscoveryStarted()) {
            jmDNSHelper.addDiscovery();
        }
    }

    private void unregisterJmDNSDiscovery()
    {
        if (jmDNSHelper != null) {
            jmDNSHelper.unregisterService();
        }
    }

    private void unregisterJmDNSService()
    {
        if (jmDNSHelper != null) {
            jmDNSHelper.removeDiscovery();
        }
    }
}
