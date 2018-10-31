package com.github.teocci.android.pptopus.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.teocci.android.pptopus.R;
import com.github.teocci.android.pptopus.model.DeviceInfo;
import com.github.teocci.android.pptopus.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-11
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceItemHolder>
{
    private static final String TAG = LogHelper.makeLogTag(DeviceAdapter.class);

    private List<DeviceInfo> items;

    public DeviceAdapter()
    {
        items = new ArrayList<>();
    }

    public DeviceAdapter(List<DeviceInfo> items)
    {
        this.items = items;
    }

    @NonNull
    @Override
    public DeviceItemHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = inflater.inflate(R.layout.station_item, container, false);

        return new DeviceItemHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceItemHolder itemHolder, int position)
    {
        DeviceInfo item = items.get(position);
        itemHolder.setDeviceName(String.valueOf(item.name));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);
        stringBuilder.append(item.address);

        final long ping = item.ping;
        if (ping > 0) {
            stringBuilder.append(", ");
            stringBuilder.append(item.ping);
            stringBuilder.append(" ms");
        }

        itemHolder.setAddressAndPing(stringBuilder.toString());
        itemHolder.setState(item.transmission);
    }

    @Override
    public int getItemCount()
    {
        return items == null ? 0 : items.size();
    }

    /**
     * Inserting a new item at the head of the list. This uses a specialized
     * RecyclerView method, notifyItemInserted(), to trigger any enabled item
     * animations in addition to updating the view.
     */
    public void addItem(DeviceInfo info)
    {
        items.add(info);
        notifyItemInserted(items.indexOf(info));
    }

    /**
     * Inserting a new item at the head of the list. This uses a specialized
     * RecyclerView method, notifyItemInserted(), to trigger any enabled item
     * animations in addition to updating the view.
     */
    public void setAll(List<DeviceInfo> stations)
    {
        items.clear();
        items.addAll(stations);
        notifyDataSetChanged();
    }

    /**
     * Inserting a new item at the head of the list. This uses a specialized
     * RecyclerView method, notifyItemRemoved(), to trigger any enabled item
     * animations in addition to updating the view.
     */
    public void removeItem(int position)
    {
        if (position >= items.size()) return;

        items.remove(position);
        notifyItemRemoved(position);
    }
}
