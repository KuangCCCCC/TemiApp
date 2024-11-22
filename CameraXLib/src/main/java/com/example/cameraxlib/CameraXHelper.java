package com.example.cameraxlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraXHelper {
    private Context context;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;

    public interface CameraStartListener {
        void onCameraStarted();

        void onCameraError(Exception e);
    }

    public CameraXHelper(Context context) {
        this.context = context;
    }

    public void setPreviewView(PreviewView previewView) {
        this.previewView = previewView;
    }

    public void startCamera(CameraStartListener listener) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
                if (listener != null) {
                    listener.onCameraStarted();
                }
            } catch (ExecutionException | InterruptedException e) {
                if (listener != null) {
                    listener.onCameraError(e);
                }
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCameraUseCases() {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new Size(1920, 1080))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.unbindAll();

        try {
            cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, imageCapture);
            Log.d("CameraXHelper", "相機已啟動並綁定");
        } catch (Exception e) {
            Log.e("CameraXHelper", "綁定相機使用案例失敗: " + e.getMessage());
        }
    }

    public void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            Log.d("CameraXHelper", "相機已停止");
        }
    }

    public void releaseResources() {
        // 停止相機並釋放其他可能的資源
        stopCamera();
        imageCapture = null;
        cameraProvider = null;
        Log.d("CameraXHelper", "相機資源已完全釋放");
    }

    public void capturePhoto() {
        if (imageCapture != null) {
            Log.d("CameraXHelper", "正在進行拍照...");
            imageCapture.takePicture(ContextCompat.getMainExecutor(context), new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    Log.d("CameraXHelper", "拍照成功，開始處理圖片");
                    Bitmap bitmap = imageProxyToBitmap(image);
                    String fileName = generateImageName();
                    uploadImageToFirebase(bitmap, fileName);
                    image.close();
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e("CameraXHelper", "拍照失敗: " + exception.getMessage());
                }
            });
        } else {
            Log.e("CameraXHelper", "ImageCapture 尚未初始化，無法拍照");
        }
    }

    private String generateImageName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return "IMG_" + sdf.format(new Date()) + ".jpg";
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void uploadImageToFirebase(Bitmap bitmap, String fileName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> Log.d("CameraXHelper", "圖片上傳成功"))
                .addOnFailureListener(e -> Log.e("CameraXHelper", "圖片上傳失敗: " + e.getMessage()));
    }
}
