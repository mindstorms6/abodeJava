package org.bdawg.abode.devices;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bdawg.abode.Abode;
import org.bdawg.abode.exceptions.AbodeException;
import org.bdawg.abode.internal.AbodeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbodeDevice {
    private static final Logger logger = LoggerFactory.getLogger(AbodeDevice.class);

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
