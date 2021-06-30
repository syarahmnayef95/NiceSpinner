package org.angmarch.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import androidx.appcompat.widget.AppCompatTextView;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * @author angelo.marchesin
 */

@SuppressWarnings("unused")
public abstract class NiceSpinnerBaseAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected int mSelectedIndex;
    protected int menueTextColor;
    protected int sMenuBackGround;
    protected float menueTextSize;
    protected boolean mHasLogo;

    public NiceSpinnerBaseAdapter(Context context, int sMenuBackGround, int menueTextColor, float menueTextSize, boolean hasLog) {
        mContext = context;
        this.menueTextColor = menueTextColor;
        this.sMenuBackGround = sMenuBackGround;
        this.menueTextSize = menueTextSize;
        this.mHasLogo = hasLog;

    }


    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        ImageView logo = null;

        if (convertView == null) {
            if (mHasLogo) {
                convertView = View.inflate(mContext, R.layout.spinner_list_with_logo, null);

            } else {
                convertView = View.inflate(mContext, R.layout.spinner_list, null);

            }
            textView = convertView.findViewById(R.id.tv_tinted_spinner);
            if (mHasLogo) {
                logo = convertView.findViewById(R.id.logo);
            }
//            if(mHasLogo){
//                logo.setVisibility(View.VISIBLE);
//            }
//            else{
//                logo.setVisibility(View.GONE);
//            }
            if (menueTextColor != Integer.MAX_VALUE) textView.setTextColor(menueTextColor);
            if (sMenuBackGround > -1)
                textView.setBackgroundResource(sMenuBackGround);

            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) menueTextSize);
            convertView.setTag(new ViewHolder(textView, logo));


        } else {
            textView = ((ViewHolder) convertView.getTag()).textView;
            logo = ((ViewHolder) convertView.getTag()).logo;
        }
        String name = "";
        String logoImg = "";
        String color = "";
        if (getItem(position) != null) {
            name = getItem(position).toString();
            if (mHasLogo) {
                String[] data = getItem(position).toString().split("\\|\\|");
                Log.e("logoImg", data.length + "");
                if (data != null && data.length >= 2) {
                    name = data[0];
                    logoImg = data[1];
                    if (data.length == 3)
                        color = data[2];
                    Log.e("logoImg", logoImg);
                }
            }
        }

//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                (int) convertDpToPx(50));

        if (getItem(position) != null && !TextUtils.isEmpty(getItem(position).toString().trim())
                && !name.equals("من") && !name.equals("إلى")) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(name);
        } else {
            textView.setVisibility(View.GONE);
//            layoutParams = new ViewGroup.LayoutParams(
//                    0,
//                    0);
        }
//        convertView.setLayoutParams(layoutParams);

        if (!TextUtils.isEmpty(logoImg)) {
            if (logo != null) {
                logo.setVisibility(View.VISIBLE);
                if (logoImg.startsWith("http")) {
//                    ImageLoader.getInstance().displayImage(logoImg, logo, sImageObtions);
                    if (isValidContextForGlide(mContext))
                        Glide.with(mContext).load(logoImg).into(logo);
                } else {
                    Log.e("logoImg", logoImg);
                    int imageResource = mContext.getResources().getIdentifier(logoImg, null, mContext.getPackageName());
                    Drawable image = mContext.getResources().getDrawable(imageResource);
                    if (!TextUtils.isEmpty(color)){
                        GradientDrawable shape = new GradientDrawable();
                        shape.setShape(GradientDrawable.OVAL);
                        shape.setStroke(1, Color.parseColor("#cfcfcf"));
                        shape.setColor(Color.parseColor(color));
                        image = shape;
                    }
                    logo.setImageDrawable(image);
                    logo.setPadding(15, 15, 15, 15);
                }
            }
        } else {
            if (logo != null)
                logo.setVisibility(View.GONE);

        }

        return convertView;
    }

//    public float convertDpToPx(float dp) {
//        return dp * mContext.getResources().getDisplayMetrics().density;
//    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isFinishing()) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (activity.isDestroyed()) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void notifyItemSelected(int index) {
        mSelectedIndex = index;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract T getItem(int position);

    @Override
    public abstract int getCount();

    public abstract T getItemInDataset(int position);

    protected static class ViewHolder {

        public TextView textView;
        public ImageView logo = null;

        public ViewHolder(TextView
                                  textView, ImageView logo) {
            this.textView = textView;
            this.logo = logo;
        }
    }
}
