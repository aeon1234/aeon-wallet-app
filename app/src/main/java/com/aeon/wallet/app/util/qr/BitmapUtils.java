package com.aeon.wallet.app.util.qr;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
public class BitmapUtils {
    public static Bitmap textToImage(String text) throws WriterException, NullPointerException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text,
                BarcodeFormat.QR_CODE,512,  512 );
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
    @Nullable
    public static Bitmap getBitmap(ByteBuffer data, FrameMetadata metadata) {
        data.rewind();
        byte[] imageInBuffer = new byte[data.limit()];
        data.get(imageInBuffer, 0, imageInBuffer.length);
        try {
            YuvImage image =
                    new YuvImage(
                            imageInBuffer, ImageFormat.NV21, metadata.getWidth(), metadata.getHeight(), null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, metadata.getWidth(), metadata.getHeight()), 80, stream);

            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

            stream.close();
            return rotateBitmap(bmp, metadata.getRotation());
        } catch (Exception e) {
            Log.e("VisionProcessorBase", "Error: " + e.getMessage());
        }
        return null;
    }
    private static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        matrix.postScale(1.0f, 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }
}
