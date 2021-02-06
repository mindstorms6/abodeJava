package org.bdawg.abode.helpers;

import java.util.Arrays;
import java.util.List;

public class TimelineHelper {

    public static final String ALARM_GROUP = "abode_alarm";
    public static final String ALARM_END_GROUP = "abode_alarm_end";
    public static final String PANEL_FAULT_GROUP = "abode_panel_fault";
    public static final String PANEL_RESTORE_GROUP = "abode_panel_restore";
    public static final String DISARM_GROUP = "abode_disarm";
    public static final String ARM_GROUP = "abode_arm";
    public static final String ARM_FAULT_GROUP = "abode_arm_fault";
    public static final String TEST_GROUP = "abode_test";
    public static final String CAPTURE_GROUP = "abode_capture";
    public static final String DEVICE_GROUP = "abode_device";
    public static final String AUTOMATION_GROUP = "abode_automation";
    public static final String AUTOMATION_EDIT_GROUP = "abode_automation_edited";

    public static final List<String> ALL_EVENT_GROUPS = Arrays.asList(ALARM_GROUP, ALARM_END_GROUP, PANEL_FAULT_GROUP,
    PANEL_RESTORE_GROUP, DISARM_GROUP, ARM_GROUP,
    ARM_FAULT_GROUP, TEST_GROUP, CAPTURE_GROUP, DEVICE_GROUP,
    AUTOMATION_GROUP, AUTOMATION_EDIT_GROUP);

    public static String mapEventCode(String eventCode) {
        int code = Integer.parseInt(eventCode);
        // Honestly, these are just guessing based on the below event list.
        // It could be wrong, I have no idea.
        if (1100 <= code && code <= 1199) {
            return ALARM_GROUP;
        }

        if (3100 <= code && code <= 3199) {
            return ALARM_END_GROUP;
        }

        if (1300 <= code && code <= 1399){
            return PANEL_FAULT_GROUP;
        }

        if (3300 <= code && code <= 3399) {
            return PANEL_RESTORE_GROUP;
        }

        if (1400 <= code && code <= 1499) {
            return DISARM_GROUP;
        }
        if (3400 <= code && code <= 3799) {
            return ARM_GROUP;
        }
        if (1600 <= code && code <= 1699) {
            return TEST_GROUP;
        }
        if (5000 <= code && code <= 5099) {
            return CAPTURE_GROUP;
        }
        if (5100 <= code && code <= 5199) {
            return DEVICE_GROUP;
        }
        if (5200 <= code && code <= 5299) {
            return AUTOMATION_GROUP;
        }
        if (6000 <= code && code <= 6100) {
            return ARM_FAULT_GROUP;
        }

        return null;
    }

}
