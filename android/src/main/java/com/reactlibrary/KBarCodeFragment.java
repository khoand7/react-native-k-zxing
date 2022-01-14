package com.reactlibrary;

import static androidx.camera.core.internal.utils.ImageUtil.imageToJpegByteArray;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.core.internal.utils.ImageUtil;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.internal.ImageConvertUtils;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

/**
 * A simple {@link Fragment} subclass.
 */
public class KBarCodeFragment extends Fragment {

    private static final String TAG = "KBarCodeFragment";
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private PreviewView previewView2;
    private Executor executor;
    private ReactContext reactContext;
    private static Reader reader = null;

    public ReactContext getReactContext() {
        return reactContext;
    }

    public void setReactContext(ReactContext reactContext) {
        this.reactContext = reactContext;
    }

    public KBarCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = new DirectExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getActivity()));
    }

    @SuppressLint("UnsafeOptInUsageError")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        previewView = getView().findViewById(R.id.previewView);

        /*
        previewView2 = getView().findViewById(R.id.previewView2);
        Preview preview2 = new Preview.Builder()
                .build();
        preview2.setSurfaceProvider(previewView2.getSurfaceProvider());
        */

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        // enable the following line if RGBA output is needed.
                        //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                Bitmap bitmap = null;
                try {
                    bitmap = ImageConvertUtils.getInstance().getUpRightBitmap(image);
                    String result = scanQRImage(bitmap);
                    Log.d(TAG, result);
                    onReceiveNativeEvent(result);
                } catch (MlKitException e) {
                    Log.e(TAG, MlKitException.class.getName(), e);
                }
                imageProxy.close();
            }
        });

        int width = 2;
        int height = 1;


        ViewPort viewPort = new ViewPort.Builder(new Rational(width, height), Surface.ROTATION_0).build();

        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
//                .addUseCase(preview2)
                .addUseCase(imageAnalysis) //if you are using imageAnalysis
                .setViewPort(viewPort)
                .build();

        cameraProvider.unbindAll();

        cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup);

        cameraProvider.bindToLifecycle(this, cameraSelector, preview);

    }

    public static String scanQRImage(Bitmap bMap) {
        String contents = "";

        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        if (reader == null) {
            reader = new MultiFormatReader();
        } else {
            reader.reset();
        }
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        }
        catch (Exception e) {
            Log.e("QrTest", "Error decoding barcode", e);
        }
        return contents;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_k_bar_code, container, false);
    }

    public void onReceiveNativeEvent(String barCode) {
        WritableMap event = Arguments.createMap();
        event.putString("barCode", barCode);
        ReactContext reactContext = getReactContext();
        reactContext
                .getJSModule(RCTEventEmitter.class)
                .receiveEvent(getId(), "topChange", event);
    }

    class DirectExecutor implements Executor {
        public void execute(Runnable r) {
            r.run();
        }
    }
}