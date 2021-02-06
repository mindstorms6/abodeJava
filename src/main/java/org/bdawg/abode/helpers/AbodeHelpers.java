package org.bdawg.abode.helpers;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import static org.bdawg.abode.internal.AbodeConstants.*;

public abstract class AbodeHelpers {

    private static final Gson gson = new Gson();

    public static String getPanelModeUrl(String area, String mode) {
        // Create panel URL.
        return BASE_URL + "api/v1/panel/mode/" + area + "/" + mode;
    }



    public static String genUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    public static Map<Object, Object> update(Map<Object, Object> dct, Map<Object, Object> dctMerge) {
        dctMerge.entrySet().forEach((entry) -> {
            if (dct.containsKey(entry.getKey()) && dct.get(entry.getKey()).getClass().isAssignableFrom(Map.class)) {
                dct.put(entry.getKey(), AbodeHelpers.update((Map)dct.get(entry.getKey()), (Map)entry.getValue()));
            } else {
                dct.put(entry.getKey(), entry.getValue());
            }
        });
        return dct;
    }

    public static String getGenericType(String typeTag) {

        return new HashMap<String, String>() {{
            put(DEVICE_ALARM, TYPE_ALARM);
            put(DEVICE_GLASS_BREAK, TYPE_CONNECTIVITY);
            put(DEVICE_KEYPAD, TYPE_CONNECTIVITY);
            put(DEVICE_REMOTE_CONTROLLER, TYPE_CONNECTIVITY);
            put(DEVICE_SIREN, TYPE_CONNECTIVITY);
            put(DEVICE_STATUS_DISPLAY, TYPE_CONNECTIVITY);

            put(DEVICE_DOOR_CONTACT, TYPE_OPENING);

            put(DEVICE_MOTION_CAMERA, TYPE_CAMERA);
            put(DEVICE_MOTION_VIDEO_CAMERA, TYPE_CAMERA);
            put(DEVICE_IP_CAM, TYPE_CAMERA);
            put(DEVICE_OUTDOOR_MOTION_CAMERA, TYPE_CAMERA);
            put(DEVICE_OUTDOOR_SMART_CAMERA, TYPE_CAMERA);
            put(DEVICE_SECURE_BARRIER, TYPE_COVER);
            put(DEVICE_DIMMER, TYPE_LIGHT);
            put(DEVICE_DIMMER_METER, TYPE_LIGHT);
            put(DEVICE_HUE, TYPE_LIGHT);
            put(DEVICE_DOOR_LOCK, TYPE_LOCK);

            // #Moisture
            put(DEVICE_WATER_SENSOR, TYPE_CONNECTIVITY);

            // Switches
            put(DEVICE_SWITCH, TYPE_SWITCH);
            put(DEVICE_NIGHT_SWITCH, TYPE_SWITCH);
            put(DEVICE_POWER_SWITCH_SENSOR, TYPE_SWITCH);
            put(DEVICE_POWER_SWITCH_METER, TYPE_SWITCH);

            // Water Valve
            put(DEVICE_VALVE, TYPE_VALVE);

            // Unknown Sensors
            // More data needed to determine type
            put(DEVICE_ROOM_SENSOR, TYPE_UNKNOWN_SENSOR);
            put(DEVICE_TEMPERATURE_SENSOR, TYPE_UNKNOWN_SENSOR);
            put(DEVICE_MULTI_SENSOR, TYPE_UNKNOWN_SENSOR);
            put(DEVICE_PIR, TYPE_UNKNOWN_SENSOR);
            put(DEVICE_POVS, TYPE_UNKNOWN_SENSOR);

        }}.get(typeTag.toLowerCase());
    }
}
