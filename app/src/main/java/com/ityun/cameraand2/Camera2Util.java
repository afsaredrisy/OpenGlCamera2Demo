package com.ityun.cameraand2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @user xie
 * @date 2019/1/3 0003
 * @email 773675907@qq.com.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Util implements CameraInterface, SurfaceHolder.Callback {

    private Context context;
    private CameraManager cameraManager;
    private Handler childHandler, mainHandler;
    private SurfaceView surfaceView;
    private CameraDevice mCamera;
    private CaptureRequest.Builder mPreviewBuilder;

    String camaraType = "0";

    private int cameratype;
    private CameraCaptureSession mSession;
    private ImageReader mImageReader;

    private CaptureRequest.Builder captureRequestBuilder;
    CameraCharacteristics characteristics;
    int cameraZoom;

    private boolean isLightOpen;

    public Camera2Util(Context context) {
        this.context = context;
    }


    @Override
    public void startPreview(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        surfaceView.getHolder().addCallback(this);
    }


    private void init() {

        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(context.getMainLooper());
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        surfaceView.getHolder().setKeepScreenOn(true);

        mImageReader = ImageReader.newInstance(1080, 960, ImageFormat.JPEG, 5);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {

                Image image = imageReader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];

                image.close();

            }
        }, mainHandler);
    }

    @Override
    public void setZoom(int zoom) {
        if (zoom == 0) {
            zoom = 1;
        }
        cameraZoom = zoom;
        float maxZoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;
        Rect m = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        int minW = (int) (m.width() / maxZoom);
        int minH = (int) (m.height() / maxZoom);
        int difW = m.width() - minW;
        int difH = m.height() - minH;
        int cropW = difW / 100 * zoom;
        int cropH = difH / 100 * zoom;
        cropW -= cropW & 3;
        cropH -= cropH & 3;
        Rect zooms = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
        mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zooms);
        try {
            updatePreview(mSession);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getMaxZoom() {
        /**
         *
         */
        return (int) (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) * 5);
    }

    @Override
    public int nowZoom() {
        return cameraZoom;
    }

    @Override
    public void setCameraType(int cameraType) {
        cameratype = cameraType;
        cameraZoom = 0;
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
        init();
        try {
            if (cameraType == 1) {
                camaraType = cameraManager.getCameraIdList()[1];
            } else {
                camaraType = cameraManager.getCameraIdList()[0];
            }
//            characteristics = cameraManager.getCameraCharacteristics(camaraType);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//
//            Size[] sizes = map.getOutputSizes(SurfaceHolder.class);
//            Size size = getCloselyPreSize(surfaceView.getHeight(), surfaceView.getWidth(), sizes);
//            surfaceView.getHolder().setFixedSize(size.getWidth(), size.getHeight());
            cameraManager.openCamera(camaraType, mCameraDeviceStateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCameraType() {
        return cameratype;
    }

    @Override
    public void openLight(boolean open) {
        if (cameratype == 1) {

            return;
        }
        if (open) {
            if (!isLightOpen) {
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_TORCH);
            }
        } else {
            if (isLightOpen) {
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF);
            }
        }
        isLightOpen = open;
        try {
            updatePreview(mSession);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isOpenLight() {
        return isLightOpen;
    }

    @Override
    public void capturePicture() {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        init();
        isLightOpen = false;
        try {
            characteristics = cameraManager.getCameraCharacteristics(camaraType);

            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Size[] sizes = map.getOutputSizes(SurfaceHolder.class);
            Size size = getCloselyPreSize(surfaceView.getHeight(), surfaceView.getWidth(), sizes);
            surfaceHolder.setFixedSize(size.getWidth(), size.getHeight());
            cameraManager.openCamera(camaraType, mCameraDeviceStateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isLightOpen = false;
        cameraZoom = 0;
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
    }

    /**
     *
     */
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            try {

                mCamera = camera;

                start(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

            if (mCamera != null) {
                mCamera.close();
                mCamera = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            //发生错误
        }
    };


    /**
     *
     */
    protected Size getCloselyPreSize(int surfaceWidth, int surfaceHeight, Size[] preSizeList) {
        int ReqTmpWidth;
        int ReqTmpHeight;

        ReqTmpWidth = surfaceWidth;
        ReqTmpHeight = surfaceHeight;

        for (Size size : preSizeList) {
            if ((size.getWidth() == ReqTmpWidth) && (size.getHeight() == ReqTmpHeight)) {
                return size;
            }
        }

        float reqRatio = ((float) ReqTmpWidth) / ReqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Size retSize = null;
        for (Size size : preSizeList) {
            curRatio = ((float) size.getWidth()) / size.getHeight();
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }
        return retSize;
    }


    private void start(final CameraDevice camera) throws CameraAccessException {
        try {

            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            mPreviewBuilder.addTarget(surfaceView.getHolder().getSurface());

            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);


            camera.createCaptureSession(Arrays.asList(surfaceView.getHolder().getSurface(), mImageReader.getSurface()), mSessionStateCallback, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            mSession = session;
            if (mCamera != null && captureRequestBuilder == null) {
                try {
                    captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    // 将imageReader的surface作为CaptureRequest.Builder的目标
                    captureRequestBuilder.addTarget(mImageReader.getSurface());

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            try {
                updatePreview(session);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    /**
     *
     *
     * @param session
     * @throws CameraAccessException
     */
    private void updatePreview(CameraCaptureSession session) throws CameraAccessException {
        session.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, childHandler);
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

        }
    };


}
