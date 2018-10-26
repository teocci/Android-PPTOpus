package com.github.teocci.android.pptopus.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.teocci.android.pptopus.R;
import com.github.teocci.android.pptopus.views.StateView;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-11
 */
public class DeviceItemHolder extends RecyclerView.ViewHolder
{
    private TextView tvDeviceName;
    private TextView tvAddressAndPing;
    private StateView stateView;

    public DeviceItemHolder(View itemView)
    {
        super(itemView);

        tvDeviceName = (TextView) itemView.findViewById(R.id.text_device_name);
        tvAddressAndPing = (TextView) itemView.findViewById(R.id.text_address_ping);
        stateView = (StateView) itemView.findViewById(R.id.view_state);
    }

    public void setDeviceName(String stationName)
    {
        this.tvDeviceName.setText(stationName);
    }

    public void setAddressAndPing(String addressAndPing)
    {
        this.tvAddressAndPing.setText(addressAndPing);
    }

    public void setState(int state)
    {
        this.stateView.setIndicatorState(state);
    }
}
