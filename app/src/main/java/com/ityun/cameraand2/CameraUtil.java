package com.ityun.cameraand2;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * @user xie
 * @date 2019/1/3 0003
 * @email 773675907@qq.com.
 */

public class CameraUtil implements CameraInterface, SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceView surfaceView;

    OrientationEventListener orientationEventListener;

    boolean flagRecord = false;

    Context context;


    int rotationFlag = 90;


    int rotationRecord = 90;


    int camaraType = 0;

    private int previewWidth, previewHeight;

    private boolean isLightOpen;


    public CameraUtil(Context context) {
        this.context = context;
    }

    @Override
    public void startPreview(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void setZoom(int zoom) {
        if (camera != null && camera.getParameters().isZoomSupported()) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setZoom(zoom);
            camera.setParameters(parameters);
        }
    }

    @Override
    public int getMaxZoom() {
        if (camera != null && camera.getParameters().isZoomSupported()) {
            return camera.getParameters().getMaxZoom();
        }
        return 0;
    }

    @Override
    public int nowZoom() {
        if (camera != null && camera.getParameters().isZoomSupported()) {
            return camera.getParameters().getZoom();
        }
        return 0;
    }

    @Override
    public void setCameraType(int cameraType) {
        this.camaraType = cameraType;
        changeCamara(cameraType);
    }

    @Override
    public int getCameraType() {
        return camaraType;
    }

    @Override
    public void openLight(boolean open) {
        if (camaraType == 1 || camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        if (!open && isLightOpen) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//关闭
            camera.setParameters(parameters);
            isLightOpen = false;
        }
        if (open && !isLightOpen) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开启
            camera.setParameters(parameters);
            isLightOpen = true;
        }
    }

    @Override
    public boolean isOpenLight() {
        if (camera == null)
            return false;
        return camera.getParameters().getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH) ? true : false;
    }
    @Override
    public void capturePicture() {
        if (camera == null)
            return;
        camera.takePicture(new ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {

            }
        }, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {

            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        isLightOpen = false;
        camera = Camera.open(camaraType);
        rotationUIListener();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        doChange(surfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    /**
     *
     */
    public void changeCamara(int camaraType) {

        int cameraCount;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (camaraType == 1) {

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    camera = Camera.open(i);
                    try {
                        camera.setPreviewDisplay(surfaceView.getHolder());
                        camera.setDisplayOrientation(90);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();
                    break;
                }
            } else {

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {

                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    camera = Camera.open(i);
                    try {
                        camera.setPreviewDisplay(surfaceView.getHolder());
                        camera.setDisplayOrientation(90);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();
                    break;
                }
            }
        }
    }



    private void doChange(SurfaceHolder holder) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            previewWidth = getCloselyPreSize(surfaceView.getWidth(), surfaceView.getHeight(), parameters.getSupportedPreviewSizes()).width;
            previewHeight = getCloselyPreSize(surfaceView.getWidth(), surfaceView.getHeight(), parameters.getSupportedPreviewSizes()).height;
            if (camaraType == 0) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.cancelAutoFocus();
            }
            if (rotationRecord == 90) {
                parameters.setPreviewSize(previewWidth, previewHeight);
            }
            camera.setParameters(parameters);
            camera.setPreviewDisplay(holder);

//            if (camaraType == 1) {
//            } else {
            camera.setDisplayOrientation(90);
//            }
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**

     */
    protected Camera.Size getCloselyPreSize(int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
        int ReqTmpWidth;
        int ReqTmpHeight;

        if (rotationRecord == 90) {
            ReqTmpWidth = surfaceHeight;
            ReqTmpHeight = surfaceWidth;
        } else {
            ReqTmpWidth = surfaceWidth;
            ReqTmpHeight = surfaceHeight;
        }

        for (Camera.Size size : preSizeList) {
            if ((size.width == ReqTmpWidth) && (size.height == ReqTmpHeight)) {
                return size;
            }
        }

        float reqRatio = ((float) ReqTmpWidth) / ReqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }
        return retSize;
    }

    /**
     *
     */
    private void rotationUIListener() {
        orientationEventListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int rotation) {
                if (!flagRecord) {
                    if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {

                        if (rotationFlag != 0) {

                            rotationRecord = 90;

                            rotationFlag = 0;
                        }
                    } else if (((rotation >= 230) && (rotation <= 310))) {

                        if (rotationFlag != 90) {

                            // rotationAnimation(rotationFlag, 90);

                            rotationRecord = 0;

                            rotationFlag = 90;
                        }
                    } else if (rotation > 30 && rotation < 95) {

                        if (rotationFlag != 270) {

                            // rotationAnimation(rotationFlag, 270);

                            rotationRecord = 180;

                            rotationFlag = 270;
                        }
                    }
                }
            }
        };
        orientationEventListener.enable();
    }


}
