package com.aeon.wallet.app.util.qr;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aeon.wallet.app.QRFragment;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.nio.ByteBuffer;
import java.util.List;
public class BarcodeScannerProcessor<T> {
    private static final String TAG = "BarcodeProcessor";
    private final BarcodeScanner barcodeScanner;
    private final ScopedExecutor executor;
    private boolean isShutdown;
    @GuardedBy("this")
    private ByteBuffer latestImage;
    @GuardedBy("this")
    private FrameMetadata latestImageMetaData;
    @GuardedBy("this")
    private ByteBuffer processingImage;
    @GuardedBy("this")
    private FrameMetadata processingMetaData;
    protected BarcodeScannerProcessor() {
        executor = new ScopedExecutor(TaskExecutors.MAIN_THREAD);
        barcodeScanner = BarcodeScanning.getClient();
    }
    public synchronized void processByteBuffer(
            ByteBuffer data, final FrameMetadata frameMetadata, final CameraView cameraView) {
        latestImage = data;
        latestImageMetaData = frameMetadata;
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(cameraView);
        }
    }
    private synchronized void processLatestImage(final CameraView cameraView) {
        processingImage = latestImage;
        processingMetaData = latestImageMetaData;
        latestImage = null;
        latestImageMetaData = null;
        if (processingImage != null && processingMetaData != null && !isShutdown) {
            processImage(processingImage, processingMetaData, cameraView);
        }
    }
    private void processImage(
            ByteBuffer data, final FrameMetadata frameMetadata, final CameraView cameraView) {
        Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);
        requestDetectInImage(
                InputImage.fromByteBuffer(
                        data,
                        frameMetadata.getWidth(),
                        frameMetadata.getHeight(),
                        frameMetadata.getRotation(),
                        InputImage.IMAGE_FORMAT_NV21),
                cameraView,
                bitmap)
                .addOnSuccessListener(executor, results -> processLatestImage(cameraView));
    }
    private Task<List<Barcode>> requestDetectInImage(
            final InputImage image,
            final CameraView cameraView,
            @Nullable final Bitmap originalCameraImage
    ) {
        return barcodeScanner.process(image)
                .addOnSuccessListener(
                        executor,
                        results -> {
                            cameraView.clear();
                            if (originalCameraImage != null) {
                                cameraView.add(new CameraView.Graphic(cameraView, originalCameraImage));
                            }
                            BarcodeScannerProcessor.this.onSuccess(results,cameraView);
                            cameraView.postInvalidate();
                        })
                .addOnFailureListener(
                        executor,
                        e -> {
                            cameraView.clear();
                            cameraView.postInvalidate();
                            String error = "Failed to process. Error: " + e.getLocalizedMessage();
                            Log.d(TAG, error);
                            e.printStackTrace();
                            BarcodeScannerProcessor.this.onFailure(e);
                        });
    }
    public void stop() {
        executor.shutdown();
        isShutdown = true;
        barcodeScanner.close();
    }
    protected void onSuccess(@NonNull List<Barcode> barcodes,CameraView cameraView) {
        if (!barcodes.isEmpty()) {
            Barcode barcode = barcodes.get(0);
            QRFragment.capture(barcode,cameraView);
        }
    }
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);
    }
}
