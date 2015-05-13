package com.sayit.vipulmittal.sayit.listenerModule;

/**
 * Created by vipulmittal on 11/05/15.
 */


public enum Action {
    OPEN_CAMERA, TAKE_PHOTO, LEAVE, WHERE_AM_I, NOT_FOUND, STOP_LISTENING;

    public static Action getAction(String data) {
        if (data.contains("camera")) {
            return OPEN_CAMERA;
        } else if (data.contains("click photo")) {
            return TAKE_PHOTO;
        } else if (data.contains("done taking photo") || data.contains("exit")) {
            return LEAVE;
        } else if (data.toLowerCase().contains("where am i")) {
            return WHERE_AM_I;
        } else if (data.toLowerCase().contains("stop listening"))
            return STOP_LISTENING;
        return NOT_FOUND;
    }
}
