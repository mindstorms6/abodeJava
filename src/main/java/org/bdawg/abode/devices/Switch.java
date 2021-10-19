package org.bdawg.abode.devices;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bdawg.abode.Abode;
import org.bdawg.abode.exceptions.AbodeException;
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

    public boolean switchOn() {
        boolean success = false;
        // """Turn the switch on."""
        try {
            success = this.setStatus(new JsonPrimitive(AbodeConstants.STATUS_ON_INT));
        } catch (AbodeException ex) {
            logger.error("Failed to update state to on:", ex);
        }

        if (success) {
            this.json.addProperty("status", AbodeConstants.STATUS_ON);
        }

        return success;
    }

    public boolean switchOff() {
        boolean success = false;
        // """Turn the switch off."""
        try {
            success = this.setStatus(new JsonPrimitive(AbodeConstants.STATUS_OFF_INT));
        } catch (AbodeException ex) {
            logger.error("Failed to update state to off:", ex);
        }

        if (success) {
            this.json.addProperty("status", AbodeConstants.STATUS_OFF);
        }

        return success;
    }
}
