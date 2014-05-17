/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.v17.leanback.R;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Collection;

/**
 * DetailsOverviewRowPresenter renders {@link DetailsOverviewRow} to display an
 * overview of an item. Typically this row will be the first row in a fragment
 * such as {@link android.support.v17.leanback.app.DetailsFragment
 * DetailsFragment}.
 *
 * <p>The detailed description is rendered using a {@link Presenter}.
 */
public class DetailsOverviewRowPresenter extends RowPresenter {

    private static final String TAG = "DetailsOverviewRowPresenter";
    private static final boolean DEBUG = false;

    public static class ViewHolder extends RowPresenter.ViewHolder {
        final ImageView mImageView;
        final FrameLayout mDetailsDescriptionFrame;
        final HorizontalGridView mActionsRow;
        Presenter.ViewHolder mDetailsDescriptionViewHolder;

        public ViewHolder(View rootView) {
            super(rootView);
            mImageView = (ImageView) rootView.findViewById(R.id.details_overview_image);
            mDetailsDescriptionFrame =
                    (FrameLayout) rootView.findViewById(R.id.details_overview_description);
            mActionsRow =
                    (HorizontalGridView) rootView.findViewById(R.id.details_overview_actions);
        }
    }

    private final Presenter mDetailsPresenter;
    private final ActionPresenterSelector mActionPresenterSelector;
    private final ItemBridgeAdapter mActionBridgeAdapter;
    private int mBackgroundColor = Color.TRANSPARENT;
    private boolean mBackgroundColorSet;
    private boolean mIsStyleLarge = true;

    /**
     * Constructor that uses the given {@link Presenter} to render the detailed
     * description for the row.
     */
    public DetailsOverviewRowPresenter(Presenter detailsPresenter) {
        setHeaderPresenter(null);
        setSelectEffectEnabled(false);
        mDetailsPresenter = detailsPresenter;
        mActionPresenterSelector = new ActionPresenterSelector();
        mActionBridgeAdapter = new ItemBridgeAdapter();
        FocusHighlightHelper.setupActionItemFocusHighlight(mActionBridgeAdapter);
    }

    /**
     * Sets the listener for action click events.
     */
    public void setOnActionClickedListener(OnActionClickedListener listener) {
        mActionPresenterSelector.setOnActionClickedListener(listener);
    }

    /**
     * Gets the listener for action click events.
     */
    public OnActionClickedListener getOnActionClickedListener() {
        return mActionPresenterSelector.getOnActionClickedListener();
    }

    /**
     * Sets the background color.  If not set a default from the theme will be used.
     */
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        mBackgroundColorSet = true;
    }

    /**
     * Returns the background color.  If no background color was set, returns transparent.
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * Sets the layout style to be large or small.
     * The default is large.
     */
    public void setStyleLarge(boolean large) {
        mIsStyleLarge = large;
    }

    /**
     * Returns true if the layout style is large.
     */
    public boolean isStyleLarge() {
        return mIsStyleLarge;
    }

    private int getDefaultBackgroundColor(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorBackground, outValue, true);
        return context.getResources().getColor(outValue.resourceId);
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.lb_details_overview, parent, false);
        ViewHolder vh = new ViewHolder(v);

        vh.mDetailsDescriptionViewHolder =
            mDetailsPresenter.onCreateViewHolder(vh.mDetailsDescriptionFrame);
        vh.mDetailsDescriptionFrame.addView(vh.mDetailsDescriptionViewHolder.view);

        initDetailsOverview(v.findViewById(R.id.details_overview));

        return vh;
    }

    private void initDetailsOverview(View view) {
        int resId = mIsStyleLarge ? R.dimen.lb_details_overview_height_large :
            R.dimen.lb_details_overview_height_small;

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = view.getResources().getDimensionPixelSize(resId);
        view.setLayoutParams(lp);

        view.setBackgroundColor(mBackgroundColorSet ?
                mBackgroundColor : getDefaultBackgroundColor(view.getContext()));

    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);

        DetailsOverviewRow row = (DetailsOverviewRow) item;
        ViewHolder vh = (ViewHolder) holder;
        if (row.getImageDrawable() != null) {
            vh.mImageView.setImageDrawable(row.getImageDrawable());
        }
        if (vh.mDetailsDescriptionViewHolder == null) {
        }
        mDetailsPresenter.onBindViewHolder(vh.mDetailsDescriptionViewHolder, row);

        mActionBridgeAdapter.clear();
        ArrayObjectAdapter aoa = new ArrayObjectAdapter(mActionPresenterSelector);
        aoa.addAll(0, (Collection)row.getActions());
        mActionBridgeAdapter.setAdapter(aoa);
        vh.mActionsRow.setAdapter(mActionBridgeAdapter);
    }

    @Override
    protected void onUnbindRowViewHolder(RowPresenter.ViewHolder holder) {
        super.onUnbindRowViewHolder(holder);

        ViewHolder vh = (ViewHolder) holder;
        if (vh.mDetailsDescriptionViewHolder != null) {
            mDetailsPresenter.onUnbindViewHolder(vh.mDetailsDescriptionViewHolder);
        }

        vh.mActionsRow.setAdapter(null);
    }
}
