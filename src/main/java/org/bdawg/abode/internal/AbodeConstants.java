/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.bdawg.abode.internal;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link AbodeConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Breland Miley - Initial contribution
 */
public class AbodeConstants {


    public static final String COOKIES = "cookies";

    public static final String ID = "id";
    public static final String PASSWORD = "password";
    public static final String UUID = "uuid";
    public static final String MFA_CODE = "mfa_code";


    public static final String STATUS_ONLINE = "Online";
    public static final String STATUS_OFFLINE = "Offline";
    public static final String STATUS_CLOSED = "Closed";
    public static final int STATUS_CLOSED_INT = 0;
    public static final String STATUS_OPEN = "Open";
    public static final int STATUS_OPEN_INT = 1;

    public static final String STATUS_ON = "On";
    public static final int STATUS_ON_INT = 1;
    public static final String STATUS_OFF = "Off";
    public static final int STATUS_OFF_INT = 0;

    // GENERIC ABODE DEVICE TYPES
    public static final String TYPE_ALARM = "alarm";
    public static final String TYPE_CAMERA = "camera";
    public static final String TYPE_CONNECTIVITY = "connectivity";
    public static final String TYPE_COVER = "cover";
    public static final String TYPE_LIGHT = "light";
    public static final String TYPE_LOCK = "lock";
    public static final String TYPE_MOISTURE = "moisture";
    public static final String TYPE_MOTION = "motion";
    public static final String TYPE_OCCUPANCY = "occupancy";
    public static final String TYPE_OPENING = "door";
    public static final String TYPE_SENSOR = "sensor";
    public static final String TYPE_SWITCH = "switch";
    public static final String TYPE_VALVE = "valve";

    public static final String TYPE_UNKNOWN_SENSOR = "unknown_sensor";

    public static final List<String> BINARY_SENSOR_TYPES = Arrays.asList(TYPE_CONNECTIVITY, TYPE_MOISTURE, TYPE_MOTION,
    TYPE_OCCUPANCY, TYPE_OPENING);

    public static final String BASE_URL = "https://my.goabode.com/";
    public static final String LOGIN_URL = BASE_URL + "api/auth2/login";
    public static final String LOGOUT_URL = BASE_URL + "api/v1/logout";

    public static final String OAUTH_TOKEN_URL = BASE_URL + "api/auth2/claims";

    public static final String PARAMS_URL = BASE_URL + "api/v1/devices_beta/";

    public static final String PANEL_URL = BASE_URL + "api/v1/panel";

    public static final String INTEGRATIONS_URL = BASE_URL + "integrations/v1/devices/";
    public static final String SOCKETIO_URL = "wss://my.goabode.com/socket.io/";

    public static final Map<String, String> SOCKETIO_HEADERS = new HashMap<>() {{
        put("Origin", "https://my.goabode.com/");
    }};

    public static final String DEVICE_UPDATE_EVENT = "com.goabode.device.update";
    public static final String GATEWAY_MODE_EVENT = "com.goabode.gateway.mode";
    public static final String TIMELINE_EVENT = "com.goabode.gateway.timeline";
    public static final String AUTOMATION_EVENT = "com.goabode.automation";

    public static final String DEVICES_URL = BASE_URL + "api/v1/devices";
    public static final String DEVICE_URL = BASE_URL + "api/v1/devices/$DEVID$";




    // Constants to be used to fill our imaginary alarm device
    public static final String ALARM_NAME = "Abode Alarm";
    public static final String ALARM_DEVICE_ID = "area_";
    public static final String ALARM_TYPE = "Alarm";
    
    // DEVICE TYPE_TAGS
    // Alarm
    public static final String DEVICE_ALARM = "device_type.alarm";

    // Binary Sensors - Connectivity
    public static final String DEVICE_GLASS_BREAK = "device_type.glass";
    public static final String DEVICE_KEYPAD = "device_type.keypad";
    public static final String DEVICE_REMOTE_CONTROLLER = "device_type.remote_controller";
    public static final String DEVICE_SIREN = "device_type.siren";
    public static final String DEVICE_STATUS_DISPLAY = "device_type.bx";

    // Binary Sensors - Opening
    public static final String DEVICE_DOOR_CONTACT = "device_type.door_contact";

    // Cameras
    public static final String DEVICE_MOTION_CAMERA = "device_type.ir_camera";
    public static final String DEVICE_MOTION_VIDEO_CAMERA = "device_type.ir_camcoder";
    public static final String DEVICE_IP_CAM = "device_type.ipcam";
    public static final String DEVICE_OUTDOOR_MOTION_CAMERA = "device_type.out_view";
    public static final String DEVICE_OUTDOOR_SMART_CAMERA = "device_type.vdp";

    // Covers
    public static final String DEVICE_SECURE_BARRIER = "device_type.secure_barrier";

    // Dimmers
    public static final String DEVICE_DIMMER = "device_type.dimmer";
    public static final String DEVICE_DIMMER_METER = "device_type.dimmer_meter";
    public static final String DEVICE_HUE = "device_type.hue";

    // Locks
    public static final String DEVICE_DOOR_LOCK = "device_type.door_lock";

    // Moisture
    public static final String DEVICE_WATER_SENSOR = "device_type.water_sensor";

    // Switches
    public static final String DEVICE_SWITCH = "device_type.switch";
    public static final String DEVICE_NIGHT_SWITCH = "device_type.night_switch";
    public static final String DEVICE_POWER_SWITCH_SENSOR = "device_type.power_switch_sensor";
    public static final String DEVICE_POWER_SWITCH_METER = "device_type.power_switch_meter";

    // Water Valve
    public static final String DEVICE_VALVE = "device_type.valve";

    // Unknown Sensor
    // These device types are all considered "occupancy" but could apparently
    // also be multi-sensors based on their json.
    public static final String DEVICE_ROOM_SENSOR = "device_type.room_sensor";
    public static final String DEVICE_TEMPERATURE_SENSOR = "device_type.temperature_sensor";
    public static final String DEVICE_MULTI_SENSOR = "device_type.lm";  // LM = LIGHT MOTION?
    public static final String DEVICE_PIR = "device_type.pir";  // Passive Infrared Occupancy?
    public static final String DEVICE_POVS = "device_type.povs";

    // # DICTIONARIES
    public static final String MODE_STANDBY = "standby";
    public static final String MODE_HOME = "home";
    public static final String MODE_AWAY = "away";

    public static final List<String> ALL_MODES = Arrays.asList(MODE_STANDBY, MODE_HOME, MODE_AWAY);
}
