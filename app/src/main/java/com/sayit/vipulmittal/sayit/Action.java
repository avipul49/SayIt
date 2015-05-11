package com.sayit.vipulmittal.sayit;

import java.util.ArrayList;

/**
 * Created by vipulmittal on 11/05/15.
 */


public enum Action {
    OPEN_CAMERA, TAKE_PHOTO, LEAVE, WHERE_AM_I, NOT_FOUND;

    public static Action getAction(ArrayList<String> data) {
        if (data.contains("open camera")) {
            return OPEN_CAMERA;
        } else if (data.contains("take photo")) {
            return TAKE_PHOTO;
        } else if (data.contains("leave") || data.contains("done")) {
            return LEAVE;
        } else if (data.get(0).toLowerCase().contains("where am i")) {
            return WHERE_AM_I;
        }
        return NOT_FOUND;
    }
}
