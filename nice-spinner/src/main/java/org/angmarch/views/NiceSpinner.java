package org.angmarch.views;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

/**
 * @author angelo.marchesin
 */
@SuppressWarnings("unused")
public class NiceSpinner extends AppCompatTextView {

    private static final int MAX_LEVEL = 10000;
    private static final int DEFAULT_ELEVATION = 16;
    private static final String INSTANCE_STATE = "instance_state";
    private static final String SELECTED_INDEX = "selected_index";
    private static final String IS_POPUP_SHOWING = "is_popup_showing";
    int sMenuBackGround;
    int menueTextColor;
    float menueTextSize;
    int menueDividerColor;
    float menueDividerHeight;
    int arrowDirection;
    int textGravity;
    int arrowImage;
    String title_Full_dialog;

    boolean hasLogo;
    private int mSelectedIndex;
    private Drawable mDrawable;
    private PopupWindow mPopup;
    private ListView mListView;
    private NiceSpinnerBaseAdapter mAdapter;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private AdapterView.OnItemSelectedListener mOnItemSelectedListener;
    private boolean mHideArrow;

    private boolean showFullList;
    private boolean isFullDialog;
    private boolean isMaterial;

    private ViewGroup container;
    private FrameLayout header;

    private Context context;

    @SuppressWarnings("ConstantConditions")
    public NiceSpinner(Context context) {
        super(context);
        init(context, null);
    }

    public NiceSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NiceSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(SELECTED_INDEX, mSelectedIndex);

        if (mPopup != null) {
            bundle.putBoolean(IS_POPUP_SHOWING, mPopup.isShowing());
            dismissDropDown();
        }

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable savedState) {
        if (savedState instanceof Bundle) {
            Bundle bundle = (Bundle) savedState;
            mSelectedIndex = bundle.getInt(SELECTED_INDEX);

            if (mAdapter != null) {
                if (hasLogo) {
                    String name = "";
                    try {
                        name = mAdapter.getItemInDataset(mSelectedIndex).toString().split("\\|\\|")[0];
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                    setText(name);
                } else setText(mAdapter.getItemInDataset(mSelectedIndex).toString());
                mAdapter.notifyItemSelected(mSelectedIndex);
            }

            if (bundle.getBoolean(IS_POPUP_SHOWING)) {
                if (mPopup != null) {
                    // Post the show request into the looper to avoid bad token exception
                    post(new Runnable() {
                        @Override
                        public void run() {
                            showDropDown();
                        }
                    });
                }
            }
            savedState = bundle.getParcelable(INSTANCE_STATE);
        }

        super.onRestoreInstanceState(savedState);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;

        // if(isFullDialog) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        container = (ViewGroup) inflater.inflate(R.layout.container, null, false);
        //mListView.addHeaderView(header, null, false);
        // }
        Resources resources = getResources();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NiceSpinner);
        int defaultPadding = resources.getDimensionPixelSize(R.dimen.one_and_a_half_grid_unit);
        sMenuBackGround = typedArray.getResourceId(R.styleable.NiceSpinner_sMenuBackGround, -1);
        menueTextColor = typedArray.getColor(R.styleable.NiceSpinner_sMenuTextColor, Integer.MAX_VALUE);
        menueDividerColor = typedArray.getColor(R.styleable.NiceSpinner_sMenueDividerColor, Integer.MAX_VALUE);
        menueDividerHeight = typedArray.getDimension(R.styleable.NiceSpinner_sMenueDividerHeight, -1);
        menueTextSize = typedArray.getDimensionPixelSize(R.styleable.NiceSpinner_sMenueTextSize, 12);
        arrowDirection = typedArray.getInt(R.styleable.NiceSpinner_sArrowDirection, 0);
        textGravity = typedArray.getInt(R.styleable.NiceSpinner_sTextGravity, 0);
        arrowImage = typedArray.getInt(R.styleable.NiceSpinner_sArrowImage, 0);
        title_Full_dialog = typedArray.getString(R.styleable.NiceSpinner_titleFullDialog);

        hasLogo = typedArray.getBoolean(R.styleable.NiceSpinner_hasLogo, false);
        setClickable(true);
        //mListView = new ListView(context);
        mListView = container.findViewById(R.id.list);
        header = container.findViewById(R.id.header);

        //mListView.setId(getId());
        if (menueDividerColor != Integer.MAX_VALUE) {
            mListView.setDivider(new ColorDrawable(menueDividerColor));
            if (menueDividerHeight > -1) {
                mListView.setDividerHeight((int) menueDividerHeight);
            }
        }
        mListView.setItemsCanFocus(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= mSelectedIndex && position < mAdapter.getCount()) {
                    position++;
                }

                // Need to set selected index before calling listeners or getSelectedIndex() can be
                // reported incorrectly due to race conditions.
                mSelectedIndex = position;

                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(parent, view, position, id);
                }

                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(parent, view, position, id);
                }

                mAdapter.notifyItemSelected(position);
                if (hasLogo) {
                    String name = "";
                    try {
                        name = mAdapter.getItemInDataset(mSelectedIndex).toString().split("\\|\\|")[0];

                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                    setText(name);
                } else setText(mAdapter.getItemInDataset(position).toString());
                dismissDropDown();
            }
        });

        mPopup = new PopupWindow(context);
        mPopup.setContentView(container);
        mPopup.setOutsideTouchable(true);
        mPopup.setFocusable(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPopup.setElevation(DEFAULT_ELEVATION);
            mPopup.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.spinner_drawable));
        } else {
            mPopup.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.drop_down_shadow));
        }

        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!mHideArrow) {
                    animateArrow(false);
                }
            }
        });

        showFullList = typedArray.getBoolean(R.styleable.NiceSpinner_showFullList, false);
        isFullDialog = typedArray.getBoolean(R.styleable.NiceSpinner_isFullDialog, false);
        isMaterial = typedArray.getBoolean(R.styleable.NiceSpinner_isMaterialNewDesign, false);

        mHideArrow = typedArray.getBoolean(R.styleable.NiceSpinner_hideArrow, false);
        if (!mHideArrow) {
            Drawable basicDrawable = ContextCompat.getDrawable(context, R.drawable.arrow);
            if (!isMaterial) {
                if (arrowImage == 1) {
                    basicDrawable = ContextCompat.getDrawable(context, R.drawable.arrow2);
                }
                if (arrowImage == 2) {
                    basicDrawable = ContextCompat.getDrawable(context, R.drawable.ic_sort);
                }
                if (arrowImage == 3) {
                    basicDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_down);
                }
            } else {
                basicDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_left);
            }
//            else if (arrowImage == 2) {
//                basicDrawable = ContextCompat.getDrawable(context, R.drawable.arrow_bk_gray_down);
//            }

            int resId = typedArray.getColor(R.styleable.NiceSpinner_arrowTint, -1);

            if (basicDrawable != null) {
                mDrawable = DrawableCompat.wrap(basicDrawable);

                if (resId != -1) {
                    DrawableCompat.setTint(mDrawable, resId);
                }
            }
            if (!hasLogo)
                setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);

            if (arrowDirection == 0) {
                setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
                //	setPadding(resources.getDimensionPixelSize(R.dimen.three_grid_unit), defaultPadding, defaultPadding, defaultPadding);
                setCompoundDrawablesWithIntrinsicBounds(mDrawable, null, null, null);
            } else {
                setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                setCompoundDrawablesWithIntrinsicBounds(null, null, mDrawable, null);
            }
            if (textGravity == 1) {
                setGravity(Gravity.CENTER);
            } else {
                setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            }
        }

        typedArray.recycle();
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    /**
     * Set the default spinner item using its index
     * @param position the item's position
     */
    public void setSelectedIndex(int position) {
        if (mAdapter != null) {
            if (position >= 0 && position <= mAdapter.getCount()) {
                mAdapter.notifyItemSelected(position);
                mSelectedIndex = position;
                if (hasLogo) {
                    String name = "";
                    try {
                        name = mAdapter.getItemInDataset(mSelectedIndex).toString().split("\\|\\|")[0];
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                    setText(name);
                } else
                    setText(mAdapter.getItemInDataset(position).toString());
            } else {
                throw new IllegalArgumentException("Position must be lower than adapter count!");
            }
        }
    }

    public void addOnItemClickListener(@NonNull AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(@NonNull AdapterView.OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    public <T> void attachDataSource(@NonNull List<T> dataset) {
        mAdapter = new NiceSpinnerAdapter<>(getContext(), dataset, sMenuBackGround, menueTextColor, menueTextSize, hasLogo);
        setAdapterInternal(mAdapter);
    }

    public void setAdapter(@NonNull ListAdapter adapter) {
        mAdapter = new NiceSpinnerAdapterWrapper(getContext(), adapter, sMenuBackGround, menueTextColor, menueTextSize, hasLogo);
        setAdapterInternal(mAdapter);
    }

    private void setAdapterInternal(@NonNull NiceSpinnerBaseAdapter adapter) {
        mListView.setAdapter(adapter);
        if (hasLogo) {
            String name = "";
            try {
                name = mAdapter.getItemInDataset(mSelectedIndex).toString().split("\\|\\|")[0];
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            setText(name);
        } else
            setText(adapter.getItemInDataset(mSelectedIndex).toString());
    }

    int widthMeasureSpec = 0;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.widthMeasureSpec = widthMeasureSpec;
        mPopup.setWidth(View.MeasureSpec.getSize(widthMeasureSpec));
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!mPopup.isShowing()) {
                showDropDown();
            } else {
                dismissDropDown();
            }
        }

        return super.onTouchEvent(event);
    }

    private void animateArrow(boolean shouldRotateUp) {
        int start = shouldRotateUp ? 0 : MAX_LEVEL;
        int end = shouldRotateUp ? MAX_LEVEL : 0;
        ObjectAnimator animator = ObjectAnimator.ofInt(mDrawable, "level", start, end);
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.start();
    }

    public void dismissDropDown() {
        if (!mHideArrow) {
            animateArrow(false);
        }
        mPopup.dismiss();
    }

    public void showDropDown() {
        if (!mHideArrow) {
            animateArrow(true);
        }
        try {

            if (isFullDialog && mAdapter.getCount() > 10) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                //int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;
                mPopup.setWidth(width);

                header.setVisibility(VISIBLE);
                TextView title = container.findViewById(R.id.title);
                title.setText(title_Full_dialog + "");
                ImageView close = container.findViewById(R.id.close);
                if (isMaterial) {
                    close.setImageResource(R.drawable.ic_arrow_right);
                    close.setColorFilter(null);
                }
                close.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismissDropDown();
                    }
                });
            } else {
                mPopup.setWidth(View.MeasureSpec.getSize(widthMeasureSpec));
                header.setVisibility(GONE);
            }

            if (showFullList) {
                mPopup.showAtLocation(this, Gravity.CENTER, 0, 0);
            } else if (isFullDialog && mAdapter.getCount() > 10) {
                mPopup.showAtLocation(this, Gravity.CENTER, 0, 0);
            }

            mPopup.showAsDropDown(this);
            //mPopup.showAsDropDown(this,0,0,Gravity.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTintColor(@ColorRes int resId) {
        if (mDrawable != null && !mHideArrow) {
            DrawableCompat.setTint(mDrawable, getResources().getColor(resId));
        }
    }

    public ListView getList() {
        return mListView;
    }

    public boolean isHasLogo() {
        return hasLogo;
    }

    public void notifiyDataChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }
}
