package org.angmarch.views;

import android.content.Context;

import java.util.List;

/**
 * @author angelo.marchesin
 */

public class NiceSpinnerAdapter<T> extends NiceSpinnerBaseAdapter {

    private final List<T> mItems;

    public NiceSpinnerAdapter(Context context, List<T> items, int sMenuBackGround, int menueTextColor, float menueTextSize, boolean hasLogo) {
        super(context, sMenuBackGround, menueTextColor, menueTextSize, hasLogo);
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size() - 1;
    }

    @Override
    public T getItem(int position) {
        if (position >= mSelectedIndex) {
            return mItems.get(position + 1);
        } else {
            return mItems.get(position);
        }
    }

    @Override
    public T getItemInDataset(int position) {
        if (position < 0) return mItems.get(0);
        if (position >= mItems.size()) return mItems.get(getCount());

        return mItems.get(position);
    }
}