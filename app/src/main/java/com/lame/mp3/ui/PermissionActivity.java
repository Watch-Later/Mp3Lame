package com.lame.mp3.ui;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import com.lame.mp3.R;
import java.util.ArrayList;
import java.util.List;

public class PermissionActivity extends AppCompatActivity {
    private final static String[] Permissions =
            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.RECORD_AUDIO};
    private final static String[] UnForcedPermissions =
            new String[] {Manifest.permission.ACCESS_FINE_LOCATION};
    private final static int PERMISSION_CALL = 12321;

    public static void start(Context context) {
        Intent intent = new Intent(context, PermissionActivity.class);
        if (context instanceof ContextWrapper) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
    }

    private void checkPermission() {
        List<String> unGrantedPermission = new ArrayList<>();
        for (String Permission : Permissions) {
            if (ContextCompat.checkSelfPermission(this, Permission)
                    != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermission.add(Permission);
            }
        }
        for (String UnForcedPermission : UnForcedPermissions) {
            if (ContextCompat.checkSelfPermission(this, UnForcedPermission)
                    != PackageManager.PERMISSION_GRANTED) {
                unGrantedPermission.add(UnForcedPermission);
            }
        }
        if (unGrantedPermission.size() > 0) {
            String[] out = new String[unGrantedPermission.size()];
            for (int i = 0; i < unGrantedPermission.size(); i++) {
                out[i] = unGrantedPermission.get(i);
            }
            ActivityCompat.requestPermissions(this, out, PERMISSION_CALL);
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALL) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                } else {
                    if (isForcedPermission(permissions[i])) {
                        buildDialog(permissions[i]);
                        return;
                    }
                }
            }
            init();
        }
    }

    private void buildDialog(final String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.PermissionDialog);
        String msg = "";
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                msg = getString(R.string.str_storage_permission);
                break;
        }
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.str_ikown), (dialog, which) -> {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this, permission)) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.dismiss();
                    checkPermission();
                }
            } else {
                dialog.dismiss();
                checkPermission();
            }
        });
        builder.show();
    }

    private void init() {
        MainActivity.start(PermissionActivity.this);
        finish();
    }

    private boolean isForcedPermission(String permission) {
        for (int i = 0; i < Permissions.length; i++) {
            return Permissions[i].equals(permission);
        }
        return true;
    }
}
