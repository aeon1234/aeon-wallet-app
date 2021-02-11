package com.aeon.wallet.app;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.vision.barcode.Barcode;
import com.aeon.wallet.app.util.qr.CameraSource;
import com.aeon.wallet.app.util.qr.CameraSourcePreview;
import com.aeon.wallet.app.util.qr.BarcodeScannerProcessor;
import com.aeon.wallet.app.util.qr.CameraView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.aeon.wallet.app.MainActivity.goToFragment;
@KeepName
public final class QRFragment extends Fragment
     implements OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "QRFragment";
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private CameraView cameraView;
    private static int callback;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr, container, false);
        preview = view.findViewById(R.id.preview_view);
        cameraView = view.findViewById(R.id.graphic_overlay);
        ToggleButton facingSwitch = view.findViewById(R.id.facing_switch);
        if(getArguments()!=null && !getArguments().isEmpty()){
            callback =getArguments().getInt("return");
        }
        facingSwitch.setOnCheckedChangeListener(this);
        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }
        return view;
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        createCameraSource();
        startCameraSource();
    }
    @Override
    public void onPause() {
        super.onPause();
        preview.stop();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }
    private void createCameraSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(getActivity(), cameraView);
        }
        try {
            Log.i(TAG, "Using Barcode Detector Processor");
            cameraSource.setMachineLearningFrameProcessor(new BarcodeScannerProcessor() {
            });
        } catch (RuntimeException e) {
            Log.e(TAG, "Can not create image processor", e);
        }
    }
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (cameraView == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, cameraView);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }
    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    getContext().getPackageManager()
                            .getPackageInfo(getContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (isPermissionDenied(getContext(), permission)) {
                return false;
            }
        }
        return true;
    }
    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (isPermissionDenied(getContext(), permission)) {
                allNeededPermissions.add(permission);
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    getActivity(), allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private static boolean isPermissionDenied(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return false;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return true;
    }
    public static void capture(Barcode barcode,CameraView cameraView){
        Bundle bundle = new Bundle();
        bundle.putString("barcode", barcode.getRawValue());
        goToFragment(callback,cameraView,bundle);
    }
}
