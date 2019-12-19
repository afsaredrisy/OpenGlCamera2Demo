package com.ityun.cameraand2;

/**
 * @user xie
 * @date 2019/1/4 0004
 * @email 773675907@qq.com.
 */

public interface CameraFactoryImp {

    /**
     *
     *
     * @param type
     */
    void changeCamera(int type);

    /**
     *
     *
     * @return
     */
    int getCameraMaxZoom();

    /**
     *
     *
     * @param zoom
     */
    void setCameraZoom(int zoom);

    /**
     *
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
}
