package com.rjp.guideviewtest;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.rjp.guideview.GuideView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private GuideView guideView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        guideView = (GuideView) findViewById(R.id.guide_view);

        ArrayList<View> mViewList = new ArrayList<>();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for (int i = 0; i < 4; i++) {
            View view = layoutInflater.inflate(R.layout.layout_guide_view_pager, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.iv_view_pager);
            switch (i){
                case 0:imageView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.mipmap.wellcome_1, 720, 1280));break;
                case 1:imageView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.mipmap.wellcome_2, 720, 1280));break;
                case 2:imageView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.mipmap.wellcome_3, 720, 1280));break;
                case 3:imageView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.mipmap.wellcome_4, 720, 1280));break;
//                case 0:imageView.setImageResource(R.mipmap.wellcome_1);break;
//                case 1:imageView.setImageResource(R.mipmap.wellcome_2);break;
//                case 2:imageView.setImageResource(R.mipmap.wellcome_3);break;
//                case 3:imageView.setImageResource(R.mipmap.wellcome_4);break;
            }
            mViewList.add(view);
        }
//        guideView.setmViewList(mViewList);
//        guideView.isFirstIn();

        guideView.guideFile(getExternalDir(this) + File.separator + "images" + File.separator + "guide.jpg", 10 * 1000);

//        guideView.guideUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1517209131908&di=e81679446632060337800c7c79e8b0ed&imgtype=0&src=http%3A%2F%2Fimg.taopic.com%2Fuploads%2Fallimg%2F140214%2F234985-14021411055522.jpg", 10 *1000);

        guideView.setListener(new GuideView.OnGuideClickListener() {
            @Override
            public void onSkipClick() {
                finish();
            }

            @Override
            public void onImageClick() {
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        guideView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 从资源获取压缩大小的图片
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     *
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 计算图片的Ratio
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     *
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

    /**
     * 本应用所有的文件存储的根目录
     *
     * @param context
     * @return
     */
    public static String getExternalDir(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File cacheDir = Environment.getExternalStorageDirectory();
            if (cacheDir != null && (cacheDir.exists() || cacheDir.mkdirs())) {
                return cacheDir.getAbsolutePath() + File.separator + "zycai";
            }
        }
        return context.getCacheDir().getAbsolutePath();
    }
}
