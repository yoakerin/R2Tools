package com.yoake.r2base.permissions;


import android.content.Context;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.yoake.r2base.R;

import java.util.ArrayList;
import java.util.List;

public class PermissionRequestFragment extends Fragment {

    R2PermissionLauncher.PermissionSuccess permissionSuccessCallBack;
    R2PermissionLauncher.PermissionFail permissionFailCallBack;

    private String[] permissions = null;
    private String[] permissionsName, permissionsRationale;

    private boolean hasCalled = false;

    private WindowManager windowManager;
    private View rationalView;
    FragmentManager fragmentManager;

    public void setPermissions(@NonNull String[] permissions, @Nullable String[] permissionsName, @Nullable String[] permissionsRationale) {
        if (permissionsName != null
                && permissionsRationale != null
                && permissionsName.length > 0
                && permissionsRationale.length > 0
                && permissionsName.length == permissionsRationale.length) {
            this.permissions = permissions;
            this.permissionsName = permissionsName;
            this.permissionsRationale = permissionsRationale;
            hasCalled = false;
        } else if (permissionsName == null && permissionsRationale == null) {
            this.permissions = permissions;
            hasCalled = false;
        } else {
            throw new IllegalArgumentException("permissionsName and permissionsRational must have same length");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!hasCalled) {
            hasCalled = true;

            rationalView = hasRational() ? createRationalView() : null;
            if (rationalView != null) {
                windowManager = getActivity().getWindowManager();
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.width = displayMetrics.widthPixels - ((int) (32 * displayMetrics.density));
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.TOP | Gravity.CENTER_VERTICAL;
                windowManager.addView(rationalView, lp);
            }

            // 请求权限（旧方式）
            requestPermissions(permissions, 1001);
        }
    }

    private boolean hasRational() {
        return permissionsName != null && permissionsName.length >= 1;
    }

    private View createRationalView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rationalView = inflater.inflate(R.layout.request_permission_rationale, null, false);
        ViewGroup rational_root = rationalView.findViewById(R.id.rational_root);
        rational_root.removeAllViews();

        int rationaleCount = permissionsName != null ? permissionsName.length : 0;
        for (int index = 0; index < rationaleCount; index++) {
            View item = inflater.inflate(R.layout.request_permission_rationale_item, rational_root, false);
            TextView permissionNameTextView = item.findViewById(R.id.permission_name);
            TextView permissionRationalTextView = item.findViewById(R.id.permission_rational);
            permissionNameTextView.setText(permissionsName[index]);
            permissionRationalTextView.setText(permissionsRationale[index]);
            rational_root.addView(item);
        }

        return rationalView;
    }

     boolean checkPermission(Context context) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (rationalView != null && windowManager != null) {
            windowManager.removeView(rationalView);
            rationalView = null;
        }

        if (requestCode == 1001) {
            List<String> rejectPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    rejectPermissionList.add(permissions[i]);
                }
            }

            if (rejectPermissionList.isEmpty()) {
                if (permissionSuccessCallBack != null) permissionSuccessCallBack.onGranted();
            } else {
                if (permissionFailCallBack != null)
                    permissionFailCallBack.onDenied(rejectPermissionList);
            }

            if (fragmentManager != null) {
                fragmentManager.beginTransaction().remove(this).commitAllowingStateLoss();
            }
        }
    }
}
