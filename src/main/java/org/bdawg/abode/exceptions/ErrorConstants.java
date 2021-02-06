package org.bdawg.abode.exceptions;

public class ErrorConstants {
    
    public static final String USERNAME =  "Username must be a non-empty string";

    public static final String PASSWORD =  "Password must be a non-empty string";

    public static final String REQUEST =  "Request failed";

    public static final String SET_STATUS_DEV_ID =  "Device status/level response ID does not match request ID";

    public static final String SET_STATUS_STATE =  "Device status/level value does not match request value";

    public static final String REFRESH =  "Failed to refresh device";

    public static final String SET_MODE =  "Failed to set alarm mode";

    public static final String SET_MODE_AREA =  "Set mode response area does not match request area";

    public static final String SET_MODE_MODE =  "Set mode response mode does not match request mode";

    public static final String INVALID_ALARM_MODE =  "Mode is not of a known alarm mode value";

    public static final String MISSING_ALARM_MODE =  "No alarm mode found in object";

    public static final String INVALID_DEFAULT_ALARM_MODE =  "Default alarm mode must be one of 'home' or 'away'";

    public static final String INVALID_DEVICE_ID =  "The given value is not a device or valid device ID";

    public static final String INVALID_SETTING =  "Setting is not valid";

    public static final String INVALID_SETTING_VALUE =  "Value for setting is not valid";

    public static final String INVALID_AUTOMATION_REFRESH_RESPONSE =  "Automation refresh response did not match expected values.";

    public static final String INVALID_AUTOMATION_EDIT_RESPONSE =  "Automation edit response did not match expected values.";
    
    public static final String UNABLE_TO_MAP_DEVICE =  "Unable to map device json to device class - no type tag found.";

    public static final String EVENT_CODE_MISSING =  "Event is not valid, start and end event codes are missing.";

    //public static final String EVENT_CODE_MISSING =  "Timeline event is not valid, event code missing.";

    public static final String INVALID_TIMELINE_EVENT =  "Timeline event received missing an event code or type.";

    public static final String EVENT_GROUP_INVALID =  "Timeline event group is not valid.";

    public static final String CAM_IMAGE_REFRESH_NO_FILE =  "Camera image refresh did not have a file path.";

    public static final String CAM_IMAGE_UNEXPECTED_RESPONSE =  "Unknown camera image response.";

    public static final String CAM_IMAGE_NO_LOCATION_HEADER =  "Camera file path did not redirect to image location.";

    public static final String CAM_TIMELINE_EVENT_INVALID =  "Timeline event_code invalid - expected 5001.";

    public static final String CAM_IMAGE_REQUEST_INVALID =  "Received an invalid response from AWS servers for image.";

    public static final String EVENT_DEVICE_INVALID =  "Object given to event registration service is not a device object";

    public static final String SOCKETIO_ERROR =  "SocketIO Error Packet Received";

    public static final String MISSING_CONTROL_URL =  "Control URL does not exist in device JSON.";

    public static final String SET_PRIVACY_MODE =  "Device privacy mode value does not match request value.";

    public static final String MFA_CODE_REQUIRED =  "Multifactor authentication code required for login.";

    public static final String UNKNOWN_MFA_TYPE =  "Unknown multifactor authentication type.";
}
