package org.bdawg.abode.devices;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bdawg.abode.Abode;
import org.bdawg.abode.exceptions.AbodeException;
import org.bdawg.abode.exceptions.ErrorConstants;
import org.bdawg.abode.internal.AbodeConstants;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class AbodeDevice {
    static final Logger logger = LoggerFactory.getLogger(AbodeDevice.class);

    private Abode abode;
    protected JsonObject json;
    private String deviceId;
    private String deviceUUID;
    private String name;
    private String type;
    private String typeTag;
    private String genericType;

    public AbodeDevice(JsonObject jsonObject, Abode abode) {
        this.abode = abode;
        this.json = jsonObject;
        this.deviceId = jsonObject.get("id").getAsString();
        this.deviceUUID = jsonObject.get("uuid").getAsString();
        this.name = jsonObject.get("name").getAsString();
        this.type = jsonObject.get("type").getAsString();
        this.typeTag = jsonObject.get("type_tag").getAsString();
        this.genericType = jsonObject.get("generic_type").getAsString();
        this.updateName();
    }

    public JsonElement getValue(String name) {
        return this.json.get(name.toLowerCase());
    }

    public JsonElement refresh() throws AbodeException {
        return this.refresh(AbodeConstants.DEVICE_URL);
    }

    public JsonElement refresh(String url) throws AbodeException {
        url = url.replace("$DEVID$", this.deviceId);
        JsonElement responseObject = this.abode.sendRequest("GET", url);


        logger.debug(String.format("Device refresh response: %s", responseObject.toString()));

        if (responseObject != null && !responseObject.isJsonArray()) {
            JsonArray asArray = new JsonArray();
            asArray.add(responseObject);
            responseObject = asArray;
        }

        for (JsonElement device : responseObject.getAsJsonArray()) {
            this.update(device);
        }

        return responseObject;
    }

    public boolean setStatus(JsonPrimitive status) throws AbodeException {
        // """Set device status."""
        if (this.json.has("control_url")) {
            String url = AbodeConstants.BASE_URL + this.json.get("control_url");

            JsonObject statusData = new JsonObject();
            statusData.add("status", status);

            JsonElement response = this.abode.sendRequest("put", url, Collections.emptyMap(), new StringContentProvider(statusData.toString()), false);

            logger.debug(String.format("Set Status Response: %s", response.toString()));

            if (!response.isJsonObject()) {
                throw new AbodeException("Response was not a json object");
            }

            if (!response.getAsJsonObject().get("id").getAsString().equals(this.deviceId)) {
                throw new AbodeException(ErrorConstants.SET_STATUS_DEV_ID);
            }

            if (!response.getAsJsonObject().get("status").equals(status)) {
                throw new AbodeException(ErrorConstants.SET_STATUS_STATE);
            }

//            #Note:
//            Status result is of int type, not of new status of device.
//            #Seriously, why would you do that ?
//            #So, can 't set status here must be done at device level.

            logger.info(String.format("Set device %s status to: %s", this.deviceId, status));

            return true;
        }

        return false;
    }

    public void update(JsonElement jsonState) {
        logger.debug(String.format("Updating state %s", jsonState.toString()));
        if (jsonState.isJsonObject()) {
            JsonObject newStateObject = jsonState.getAsJsonObject();
            newStateObject.keySet().forEach(key -> {
                if (this.json.has(key)) {
                    this.json.add(key, newStateObject.get(key));
                }
            });
        }
        this.updateName();
    }

    public void updateName() {
        this.name = this.json.get("name").getAsString();
        if (this.name == null || this.name.isEmpty()) {
            this.name = this.type + " " + this.deviceId;
        }
    }

    public String status() {
        return this.getValue("status").getAsString();
    }

    public boolean batteryLow() {
        return this.getValue("faults").getAsJsonObject().get("low_battery").getAsInt() == 1;
    }

    public boolean noResponse() {
        return this.getValue("faults").getAsJsonObject().get("no_response").getAsInt() == 1;
    }

    public boolean outOfOrder() {
        return this.getValue("faults").getAsJsonObject().get("out_of_order").getAsInt() == 1;
    }

    public boolean tampered() {
        // 'tempered' - Typo in API?
        return this.getValue("faults").getAsJsonObject().get("tempered").getAsInt() == 1;
    }

    public String name() {
        return this.name;
    }

    public String genericType() {
        return this.genericType;
    }

    public String type() {
        return this.type;
    }

    public String typeTag() {
        return this.typeTag;
    }

    public String deviceId() {
        return this.deviceId;
    }

    public String deviceUUID() {
        return this.deviceUUID;
    }

    public String description() {
        return String.format("%s (ID: %s, UUID: %s) - %s - %s", this.name, this.deviceId, this.deviceUUID, this.type, this.status());
    }


}
