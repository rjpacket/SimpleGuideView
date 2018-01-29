package com.rjp.guideview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

import static android.os.Build.VERSION_CODES.M;

/**
 *
 * @author RJP on 2017/2/23 19:40
 *
 */

public class PermissionUtils {
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final int CODE_WRITE_EXTERNAL_STORAGE = 9001;

    public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;

    private Activity mActivity;
    private OnPermissionsCallBack permissionsCallBack;

    public PermissionUtils(Activity targetActivity, OnPermissionsCallBack permissionsCallBack) {
        this.mActivity = targetActivity;
        this.permissionsCallBack = permissionsCallBack;
    }

    /**
     * 检查权限
     * @param requestCode 请求码
     * @param permissions 权限
     */
    public void checkPermissions(int requestCode, String... permissions) {
        if(Build.VERSION.SDK_INT >= M) {
            ArrayList lacks = new ArrayList();
            String[] lacksPermissions = permissions;
            int var5 = permissions.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String permission = lacksPermissions[var6];
                int checkSelfPermission = ContextCompat.checkSelfPermission(this.mActivity.getApplicationContext(), permission);
                if(checkSelfPermission == -1) {
                    lacks.add(permission);
                }
            }

            if(!lacks.isEmpty()) {
                lacksPermissions = new String[lacks.size()];
                lacksPermissions = (String[])lacks.toArray(lacksPermissions);
                ActivityCompat.requestPermissions(this.mActivity, lacksPermissions, requestCode);
            } else {
                permissionsCallBack.onGranted(requestCode);
            }
        } else {
            permissionsCallBack.onGranted(requestCode);
        }

    }

    public void recheckPermissions(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int[] var4 = grantResults;
        int var5 = grantResults.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            int grantResult = var4[var6];
            if(grantResult == -1) {
                permissionsCallBack.onDenied(requestCode, permissions);
                return;
            }
        }

        permissionsCallBack.onGranted(requestCode);
    }

    /**
     * 去设置 开启应用权限
     * @param context 上下文
     */
    public static void startAppSettings(Context context) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    public interface OnPermissionsCallBack{
        void onGranted(int requestCode);

        void onDenied(int requestCode, String[] permissions);
    }
}
