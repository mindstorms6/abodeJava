package org.bdawg.abode.devices;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bdawg.abode.Abode;
import org.bdawg.abode.exceptions.AbodeException;
import org.bdawg.abode.helpers.AbodeHelpers;
import org.bdawg.abode.internal.AbodeConstants;
import org.bdawg.abode.exceptions.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alarm extends Switch {
    private static final Logger logger = LoggerFactory.getLogger(Alarm.class);

    public static Alarm createAlarm(JsonElement json, Abode abode) {
        return createAlarm(json.getAsJsonObject(), abode,"1");
    }

    public static Alarm createAlarm(JsonObject json, Abode abode) {
        return createAlarm(json, abode,"1");
    }

    public static Alarm createAlarm(JsonObject json, Abode abode, String area) {
        json.addProperty("name", AbodeConstants.ALARM_NAME);
        json.addProperty("id", AbodeConstants.ALARM_DEVICE_ID + area);
        json.addProperty("type", AbodeConstants.ALARM_TYPE);
        json.addProperty("type_tag", AbodeConstants.DEVICE_ALARM);
        json.addProperty("generic_type", AbodeConstants.TYPE_ALARM);
        json.addProperty("uuid", json.get("site_id").getAsString().replace(":", "").toLowerCase());

        return new Alarm(json, abode, area);
    }

    private final String area;
    private final Abode abode;

    public Alarm(JsonObject json, Abode abode) {
        this(json, abode, "1");
    }

    public Alarm(JsonObject json, Abode abode, String area) {
        super(json, abode);
        this.area = area;
        this.abode = abode;
    }

    public JsonElement refresh(String url) throws AbodeException {
        JsonElement refreshed = super.refresh(url);
        this.abode.getPanel().update(refreshed);
        return refreshed;
    }

    public JsonElement refresh() throws AbodeException {
        return this.refresh(AbodeConstants.PANEL_URL);
    }

    public boolean setMode(String mode) throws AbodeException {
        if (mode == null || mode.isBlank()) {
            throw new AbodeException(ErrorConstants.MISSING_ALARM_MODE);
        }
        if (!AbodeConstants.ALL_MODES.contains(mode.toLowerCase())) {
            throw new AbodeException(ErrorConstants.INVALID_ALARM_MODE);
        }

        mode = mode.toLowerCase();

        JsonElement response = this.abode.sendRequest("PUT", AbodeHelpers.getPanelModeUrl(area, mode));
        logger.debug("Set alarm home response: " + response.toString());

        if (!response.getAsJsonObject().get("area").getAsString().equals(this.area)) {
            throw new AbodeException(ErrorConstants.SET_MODE_AREA);
        }

        if (!response.getAsJsonObject().get("mode").equals(mode)) {
            throw new AbodeException(ErrorConstants.SET_MODE_MODE);
        }

        this.json.get("mode").getAsJsonObject().add(this.deviceId(), response.getAsJsonObject().get("mode"));

        logger.info(String.format("Set alarm %s mode to: %s",
                this.deviceId(), response.getAsJsonObject().get("mode")));
        return true;
    }

    public String getMode() {
        return this.getValue("mode").getAsJsonObject().get(this.deviceId()).getAsString();
    }

    public boolean isAway() {
        return AbodeConstants.MODE_AWAY.equals(this.getMode());
    }

    public boolean isHome() {
        return AbodeConstants.MODE_HOME.equals(this.getMode());
    }

    public boolean isStandby() {
        return AbodeConstants.MODE_STANDBY.equals(this.getMode());
    }

    public String getStatus() {
        return this.getMode();
    }

    public String getMacAddress() {
        return this.json.get("mac").getAsString();
    }
}
