package org.bdawg.abode.devices;

import com.google.gson.JsonObject;
import org.bdawg.abode.Abode;
import org.bdawg.abode.internal.AbodeConstants;

import java.util.Arrays;

public class Switch extends AbodeDevice {

    public Switch(JsonObject json, Abode abode) {
        super(json, abode);
    }

    public boolean isOn() {
        return
                !Arrays.asList(AbodeConstants.STATUS_OFF, AbodeConstants.STATUS_OFFLINE)
                .contains(this.status());
    }

    public boolean isDimmable() {
        return false;
    }

    public boolean isColorCapable() {
        return false;
    }

    public boolean hasColor() {
        return false;
    }
}
