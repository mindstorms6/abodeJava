package org.bdawg.abode.devices;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bdawg.abode.Abode;
import org.bdawg.abode.internal.AbodeConstants;

public class Sensor extends BinarySensor {
    public Sensor(JsonObject object, Abode abode) {
        super(object, abode);
    }

    public JsonElement getStatus(String key) {
        JsonElement result = this.json.get(AbodeConstants.STATUSES_KEY);
        if (result == null) {
            result = new JsonObject();
        }
        if (result.isJsonObject() && result.getAsJsonObject().has(key)) {
            return result.getAsJsonObject().get(key);
        }
        return null;
    }

    public Number getNumericStatus(String key) {
        // """Extract the numeric value from the statuses object."""
        JsonElement value = this.getStatus(key);
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            return value.getAsJsonPrimitive().getAsNumber();
        }
        return null;
    }




    public Number temp() {
        // """Get device temp."""
        return this.getNumericStatus(AbodeConstants.TEMP_STATUS_KEY);
    }

    public String tempUnit() {
        // """Get unit of temp."""

        JsonElement tempStatusKey = this.getStatus(AbodeConstants.TEMP_STATUS_KEY);
        if (tempStatusKey == null) {
            return null;
        }
        if (tempStatusKey.toString().contains(AbodeConstants.UNIT_FAHRENHEIT)) {
            return AbodeConstants.UNIT_FAHRENHEIT;
        }
        if (tempStatusKey.toString().contains(AbodeConstants.UNIT_CELSIUS)) {
            return AbodeConstants.UNIT_CELSIUS;
        }
        return null;
    }

    public Number humidity() {
        // """Get device humdity."""
        return this.getNumericStatus(AbodeConstants.HUMI_STATUS_KEY);
    }


    public String humidityUnit() {
        // """Get unit of humidity."""
        JsonElement humidityStatus = this.getStatus(AbodeConstants.HUMI_STATUS_KEY);
        if (humidityStatus == null) {
            return null;
        }
        if (humidityStatus.toString().contains(AbodeConstants.UNIT_PERCENT)) {
            return AbodeConstants.UNIT_PERCENT;
        }
        return null;
    }
//
//    @property
//    def lux(self):
//            """Get device lux."""
//            return self._get_numeric_status(CONST.LUX_STATUS_KEY)
//
//    @property
//    def lux_unit(self):
//            """Get unit of lux."""
//            if CONST.UNIT_LUX in self._get_status(CONST.LUX_STATUS_KEY):
//            return CONST.LUX
//        return None
//
//    @property
//    def has_temp(self):
//            """Device reports temperature."""
//            return self.temp is not None
//
//    @property
//    def has_humidity(self):
//            """Device reports humidity level."""
//            return self.humidity is not None
//
//    @property
//    def has_lux(self):
//            """Device reports light lux level."""
//            return self.lux is not None
}
