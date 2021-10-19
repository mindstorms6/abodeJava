package org.bdawg.abode.devices;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bdawg.abode.Abode;
import org.bdawg.abode.exceptions.AbodeException;
import org.bdawg.abode.internal.AbodeConstants;

import java.util.Arrays;

public class Lock extends AbodeDevice {

    public Lock(JsonObject object, Abode abode) {
        super(object, abode);
    }

    public boolean lock() {
        boolean success = false;
        try {
            success = this.setStatus(new JsonPrimitive(AbodeConstants.STATUS_LOCKCLOSED_INT));
        } catch (AbodeException ex) {
            logger.error("Failed to update state to locked:", ex);
        }
        if (success) {
            this.json.addProperty("status", AbodeConstants.STATUS_LOCKCLOSED);
        }
        return success;
    }

    public boolean unlock() {
        boolean success = false;
        try {
            success = this.setStatus(new JsonPrimitive(AbodeConstants.STATUS_LOCKOPEN_INT));
        } catch (AbodeException ex) {
            logger.error("Failed to update state to unlocked:", ex);
        }

        if (success) {
            this.json.addProperty("status", AbodeConstants.STATUS_LOCKOPEN);
        }

        return success;
    }

    public boolean isLocked() {
        return Arrays.asList(AbodeConstants.STATUS_LOCKCLOSED).contains(this.status());
    }
}
