package com.boozefinder.main;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.WazaBe.HoloEverywhere.HoloAlertDialogBuilder;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("rawtypes")
public class ConcreteItemizedOverlay extends ItemizedOverlay {

    private List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private Context mContext;

    public ConcreteItemizedOverlay(Drawable arg0) {
        super(boundCenterBottom(arg0));
        populate();
    }

    public ConcreteItemizedOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
        populate();
    }

    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }

    @Override
    public int size() {
        return mOverlays.size();
    }

    @Override
    protected boolean onTap(int index) {
        OverlayItem item = mOverlays.get(index);
        HoloAlertDialogBuilder dialog = new HoloAlertDialogBuilder(mContext);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());
        dialog.create().show();
        return true;
    }
    
    public void clearOverlays() {
        mOverlays.clear();
    }

}
