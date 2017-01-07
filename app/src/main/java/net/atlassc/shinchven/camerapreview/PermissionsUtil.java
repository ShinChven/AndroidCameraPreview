package net.atlassc.shinchven.camerapreview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * 权限工具类
 * 用来申请系统权限
 * <p/>
 * Created by Roy on 16/3/21.
 */
public class PermissionsUtil {
    private static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案

    private static Activity mActivity;

    /**
     * 判断权限集合
     *
     * @param permissions
     * @return
     */
    public static boolean lacksPermissions(Activity activity, String... permissions) {
        mActivity = activity;
        for (String permission : permissions) {
            if (lacksPermission(mActivity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否缺少权限
     *
     * @param permission
     * @return true:缺少;false:已申请权限
     */
    public static boolean lacksPermission(Activity activity, String permission) {
        mActivity = activity;
        return ContextCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_DENIED;
    }

    /**
     * 请求权限兼容低版本
     */
    public static void requestPermissions(Activity activity, String... permissions) {
        mActivity = activity;
        ActivityCompat.requestPermissions(mActivity, permissions, PERMISSION_REQUEST_CODE);
    }

    /**
     * 显示缺失权限提示
     *
     * @param activity
     */
    public static void showMissingPermissionDialog(final Activity activity) {
        mActivity = activity;
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.permission_checker_title);
        builder.setMessage(R.string.string_help_text);

        builder.setNegativeButton(R.string.deny, null);

        builder.setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings(activity);
            }
        });

        builder.show();
    }

    /**
     * 启动应用的设置
     *
     * @param activity
     */
    public static void startAppSettings(Activity activity) {
        mActivity = activity;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + mActivity.getPackageName()));
        activity.startActivity(intent);
    }

    /**
     * 用户关闭并不再提醒所有权限提
     *
     * @param activity
     * @param permissions
     * @return
     */
    public static boolean hasDelayAllPermissions(Activity activity, String... permissions) {
        mActivity = activity;
        int count = 0;
        for (String permission : permissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)
                    && ContextCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_DENIED) {
                count++;
            }
        }
        if (count == permissions.length) {
            return true;
        }
        return false;
    }

    /**
     * 检查权限
     *
     * @param activity
     * @param permissions
     */
    public static boolean checkPermissions(Activity activity, String... permissions) {
        mActivity = activity;
        if (lacksPermissions(mActivity, permissions)) {
            if (!hasDelayAllPermissions(activity, permissions)) {
                showMissingPermissionDialog(activity);
            } else {
                requestPermissions(activity, permissions);
            }
            return false;
        }else{
            return true;
        }

    }

}
