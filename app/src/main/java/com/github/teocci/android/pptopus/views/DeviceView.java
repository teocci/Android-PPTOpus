package com.github.teocci.android.pptopus.views;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.teocci.android.pptopus.R;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-11
 */
public class DeviceView extends RelativeLayout
{
    private TextView tvDeviceName, tvAddressAndPing;

    public DeviceView(Context context)
    {
        super(context);
    }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        tvDeviceName = (TextView) findViewById(R.id.text_device_name);
        tvAddressAndPing = (TextView) findViewById(R.id.text_address_ping);
    }

    @Override
    public String toString()
    {
        return tvDeviceName.getText() + " | " + tvAddressAndPing.getText();
    }
}
