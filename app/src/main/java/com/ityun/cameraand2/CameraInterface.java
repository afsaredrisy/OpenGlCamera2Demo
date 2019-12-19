package com.ityun.cameraand2;

import android.view.SurfaceView;

/**
 * @user xie
 * @date 2019/1/3 0003
 * @email 773675907@qq.com.
 */

public interface CameraInterface {


    /**
     *
     *
     * @param surfaceView
     */
    void startPreview(SurfaceView surfaceView);

    /**
     *
     *
     * @param zoom
     */
    void setZoom(int zoom);

    /**
     *
     *
     * @return
     */
    int getMaxZoom();


    /**
     *
     *
     * @return
     */
    int nowZoom();

    /**
     *
     *
     * @param cameraType
     */
    void setCameraType(int cameraType);

    /**
     *
     *
     * @return
     */
    int getCameraType();

    /**
     *
     *
     * @param open
     */
    void openLight(boolean open);

    /**
     *
     *
     * @return
     */
    boolean isOpenLight();


    /**
     *
     */
    void  capturePicture();

}
