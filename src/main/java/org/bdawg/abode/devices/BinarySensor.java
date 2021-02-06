package org.bdawg.abode.devices;

import com.google.gson.JsonObject;
import org.bdawg.abode.Abode;
import org.bdawg.abode.internal.AbodeConstants;

import java.util.Arrays;

public class BinarySensor extends AbodeDevice {
    public BinarySensor(JsonObject object, Abode abode) {
        super(object, abode);
    }

    public boolean isOn() {
        if (this.type().equals("Occupancy")) {
            return !this.status().equals(AbodeConstants.STATUS_ONLINE);
        }
        return !Arrays.asList(AbodeConstants.STATUS_OFF, AbodeConstants.STATUS_OFFLINE, AbodeConstants.STATUS_CLOSED).contains(this.status());
    }
}
