package ru.code22.mtrade;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;

import android.view.Gravity;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class PermissionChecker {

// https://ru.stackoverflow.com/questions/513650/Ошибка-при-использовании-locationmanager

    public enum RuntimePermissions {
        PERMISSION_REQUEST_FINE_LOCATION {
            @Override
            public String toStringValue() {
                return android.Manifest.permission.ACCESS_FINE_LOCATION;
            }

            @Override
            public String showInformationMessage(Context context) {
                return context.getString(R.string.info_message_about_request_permission_fine_location);
            }
        };
        public final int VALUE;

        RuntimePermissions() {
            VALUE = this.ordinal();
        }

        public abstract String toStringValue();

        public abstract String showInformationMessage(Context context);
    }

    public boolean isPermissionGranted(Context context, final RuntimePermissions permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ActivityCompat.checkSelfPermission(context, permission.toStringValue()) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkForPermissions(Activity activity, final RuntimePermissions permission, final IPermissionCallback callback) {
        final Context context=activity.getApplicationContext();
        if (!isPermissionGranted(context, permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.toStringValue())) {
                // Здесь показываем обоснование, почему необходимо разрешение, а делаю через Toast
                //Toast toast = AppUtils.showInfoMessage(activity, permission.showInformationMessage(context));
                //...
                //snackbar.setGravity(Gravity.CENTER, 0, 0);
                Snackbar.make(activity.findViewById(android.R.id.content), permission.showInformationMessage(context), Snackbar.LENGTH_LONG).show();
                callback.permissionDenied(permission);
            } else {
                // Запрос диалога пермишна
                ActivityCompat.requestPermissions(activity, new String[]{permission.toStringValue()}, permission.ordinal());
            }
        } else {
            // Права даны или АПИ < 23
            //callback.permissionGranted(permission);
            return true;
        }
        return false;
    }

    public interface IPermissionCallback {
        void permissionGranted(PermissionChecker.RuntimePermissions permission);
        void permissionDenied(PermissionChecker.RuntimePermissions permission);
    }

}
