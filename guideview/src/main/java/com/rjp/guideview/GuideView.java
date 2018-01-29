package com.rjp.guideview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static com.rjp.guideview.PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE;
import static com.rjp.guideview.PermissionUtils.WRITE_EXTERNAL_STORAGE;

/**
 * @author Gimpo create on 2018/1/29 10:13
 *         email : jimbo922@163.com
 */

public class GuideView extends RelativeLayout implements PermissionUtils.OnPermissionsCallBack {
    private Context mContext;
    private int guideLogoRes;
    private ImageView ivGuideImage;
    private TextView tvSkip;
    private LinearLayout llLogoLabel;
    private ViewPager viewPager;
    //存储view
    private List<View> mViewList;
    private LayoutInflater layoutInflater;
    //图片显示的时长
    private long duration;
    private CountDownTimer countDownTimer;
    private OnGuideClickListener listener;
    private PermissionUtils permissionUtils;
    private String imagePath;

    public GuideView(@NonNull Context context) {
        this(context, null);
    }

    public GuideView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        layoutInflater = LayoutInflater.from(mContext);
        layoutInflater.inflate(R.layout.layout_guide_view, this);
        if (attrs != null) {
            TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.GuideView);
            guideLogoRes = array.getResourceId(R.styleable.GuideView_guide_logo, R.mipmap.ic_launcher);
        }
        ImageView ivLogo = (ImageView) findViewById(R.id.app_logo);
        ivLogo.setImageResource(guideLogoRes);

        ivGuideImage = (ImageView) findViewById(R.id.app_guide_image);
        tvSkip = (TextView) findViewById(R.id.app_skip);
        llLogoLabel = (LinearLayout) findViewById(R.id.app_logo_label);
        viewPager = (ViewPager) findViewById(R.id.app_view_pager);
        tvSkip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                if (listener != null) {
                    listener.onSkipClick();
                }
            }
        });
        ivGuideImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                if (listener != null) {
                    listener.onImageClick();
                }
            }
        });
        llLogoLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * 第一次进入的时候走这个逻辑
     */
    public void isFirstIn() {
        ivGuideImage.setVisibility(GONE);
        tvSkip.setVisibility(GONE);
        llLogoLabel.setVisibility(GONE);
        viewPager.setVisibility(VISIBLE);

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                if (mViewList != null) {
                    return mViewList.size();
                }
                return 0;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mViewList.get(position));
                return mViewList.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViewList.get(position));
            }
        });
    }

    /**
     * 导航view开始导航
     * 本地路径
     */
    public void guideFile(String imagePath, long duration) {
        this.imagePath = imagePath;
        this.duration = duration;
        permissionUtils = new PermissionUtils((Activity) mContext, this);
        permissionUtils.checkPermissions(CODE_WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionUtils.recheckPermissions(requestCode, permissions, grantResults);
    }

    @Override
    public void onGranted(int requestCode) {
        FileTask task = new FileTask();
        task.execute(imagePath);
    }

    @Override
    public void onDenied(int requestCode, String[] permissions) {
        Toast.makeText(mContext, "文件权限被关闭，请去设置里面打开之后再操作", Toast.LENGTH_SHORT).show();
        PermissionUtils.startAppSettings(mContext);
    }

    /**
     * 导航view开始导航
     * 远程路径
     */
    public void guideUrl(String imageUrl, long duration) {
        this.duration = duration;
        UrlTask task = new UrlTask();
        task.execute(imageUrl);
    }

    public void setListener(OnGuideClickListener listener) {
        this.listener = listener;
    }

    public void setmViewList(List<View> mViewList) {
        this.mViewList = mViewList;
    }

    /**
     * 从文件里面读取图片  这里的图片最好做成预加载的形式
     */
    class FileTask extends AsyncTask<String, Void, Bitmap> {
        // 在后台加载图片
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                return decodeSampledBitmapFromFile(params[0], 720, 1280);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            ivGuideImage.setImageBitmap(bitmap);
            countDownTimer = new CountDownTimer(duration, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvSkip.setText(millisUntilFinished / 1000 + "s 跳过");
                }

                @Override
                public void onFinish() {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    if (listener != null) {
                        listener.onSkipClick();
                    }
                }
            };
            countDownTimer.start();
        }
    }

    /**
     * 从网络加载图片
     */
    class UrlTask extends AsyncTask<String, Void, Bitmap> {
        // 在后台加载图片
        @Override
        protected Bitmap doInBackground(String... params) {
            String urlPath = params[0];
            URL url;
            HttpURLConnection urlConnection;
            try {
                url = new URL(urlPath);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3 * 1000);
                urlConnection.setRequestMethod("GET");
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    //这里的图片最好压缩处理一次
                    return BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            ivGuideImage.setImageBitmap(bitmap);
            countDownTimer = new CountDownTimer(duration, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvSkip.setText(millisUntilFinished / 1000 + "s 跳过");
                }

                @Override
                public void onFinish() {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    if (listener != null) {
                        listener.onSkipClick();
                    }
                }
            };
            countDownTimer.start();
        }
    }

    /**
     * 从文件获取压缩大小的图片
     *
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 计算图片的Ratio
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public interface OnGuideClickListener {
        void onSkipClick();

        void onImageClick();
    }
}
