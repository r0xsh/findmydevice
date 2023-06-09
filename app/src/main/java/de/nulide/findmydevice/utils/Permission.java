package de.nulide.findmydevice.utils;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import de.nulide.findmydevice.receiver.DeviceAdminReceiver;
import de.nulide.findmydevice.services.ThirdPartyAccessService;

public class Permission {

    private static final int PERM_SMS_ID = 61341;
    private static final int PERM_GPS_ID = 61342;
    private static final int PERM_CONTACT_ID = 61343;
    private static final int PERM_CAMERA_ID = 61344;

    public static boolean GPS = false;
    public static boolean DEVICE_ADMIN = false;
    public static boolean DND = false;
    public static boolean OVERLAY = false;
    public static boolean WRITE_SECURE_SETTINGS = false;
    public static boolean NOTIFICATION = false;
    public static boolean CAMERA = false;
    public static boolean CORE = false;
    public static boolean BATTERY_OPTIMIZATION = false;

    public static int ENABLED_PERMISSIONS = 0;
    public static final int AVAILABLE_PERMISSIONS = 9;

    public static void initValues(Context context) {
        ENABLED_PERMISSIONS = 0;
        GPS = checkGPSForegroundPermission(context) && checkGPSBackgroundPermission(context);
        DEVICE_ADMIN = checkDeviceAdminPermission(context);
        WRITE_SECURE_SETTINGS = checkWriteSecurePermission(context);
        OVERLAY = checkOverlayPermission(context);
        NOTIFICATION = checkNotificationPermission(context);
        CAMERA = checkCameraPermissions(context);
        BATTERY_OPTIMIZATION = checkBatteryOptimizationPermission(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DND = checkDNDPermission(context);
        }
        if (checkContactsPermission(context) && checkSMSPermission(context)) {
            CORE = true;
        }
        if(GPS){
            ENABLED_PERMISSIONS++;
        }
        if(DEVICE_ADMIN){
            ENABLED_PERMISSIONS++;
        }
        if(DND){
            ENABLED_PERMISSIONS++;
        }
        if(OVERLAY){
            ENABLED_PERMISSIONS++;
        }
        if(WRITE_SECURE_SETTINGS){
            ENABLED_PERMISSIONS++;
        }
        if(CORE){
            ENABLED_PERMISSIONS++;
        }
        if(NOTIFICATION){
            ENABLED_PERMISSIONS++;
        }
        if(CAMERA){
            ENABLED_PERMISSIONS++;
        }
        if(BATTERY_OPTIMIZATION){
            ENABLED_PERMISSIONS++;
        }
    }

    public static void requestSMSPermission(Activity activity) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_PHONE_STATE}, PERM_SMS_ID);
        }else{
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, PERM_SMS_ID);
        }
    }

    public static void requestGPSForegroundPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_GPS_ID);
    }

    public static void requestGPSBackgroundPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERM_GPS_ID);
    }

    public static void requestContactPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, PERM_CONTACT_ID);
    }

    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, PERM_CAMERA_ID);
    }

    public static void requestOverlayPermission(Activity activity) {
        if (!checkOverlayPermission(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        }
    }

    public static void requestDNDPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkDNDPermission(activity)) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                activity.startActivity(intent);
            }
        }
    }

    public static void requestNotificationPermission(Activity activity){
        activity.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    public static void requestDeviceAdminPermission(Activity activity) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(activity, DeviceAdminReceiver.class));
        activity.startActivity(intent);

    }

    public static void requestBatteryOptimizationPermission(Activity activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Intent intent = new Intent();
            String packageName = activity.getPackageName();
            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            if(!pm.isIgnoringBatteryOptimizations(packageName)){
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                activity.startActivity(intent);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkDNDPermission(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return mNotificationManager.isNotificationPolicyAccessGranted();
    }

    public static boolean checkOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    public static boolean checkWriteSecurePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkSMSPermission(Context context) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        }else{
            return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean checkGPSForegroundPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkGPSBackgroundPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkCameraPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkDeviceAdminPermission(Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return devicePolicyManager.isAdminActive(new ComponentName(context, DeviceAdminReceiver.class));
    }

    public static boolean checkNotificationPermission(Context context) {
        ComponentName cn = new ComponentName(context, ThirdPartyAccessService.class);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        final boolean enabled = flat != null && flat.contains(cn.flattenToString());
        return enabled;
    }

    public static boolean checkBatteryOptimizationPermission(Context context){
        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pm.isIgnoringBatteryOptimizations(packageName);
        }
        return true;
    }

}
