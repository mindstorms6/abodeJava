package org.bdawg.abode;

import org.bdawg.abode.devices.AbodeDevice;
import org.bdawg.abode.devices.Alarm;
import org.bdawg.abode.exceptions.AbodeException;
import org.bdawg.abode.internal.AbodeConstants;
import org.bdawg.abode.exceptions.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbodeEventController {
    private Abode abode;
    private Thread thread;
    private boolean running;
    private boolean connected;

    private Map<String, List<Function>> connectionStatusCallbacks;
    private Map<String, List<Function>> deviceCallbacks;
    private Map<String, List<Function>> eventCallbacks;
    private Map<String, List<Function>> timelineCallbacks;

    private AbodeSocketIO socketio;

    private static final Logger logger = LoggerFactory.getLogger(AbodeEventController.class);


    public AbodeEventController(Abode abode, String url) {
                this.abode = abode;
                this.connected = false;
                this.running = false;
                this.thread = null;

                if (abode.getSessionCookies() != null) {
                    List<String> cookies = abode.getSessionCookies().getCookies()
                            .stream()
                            .map(c -> c.toString())
                            .collect(Collectors.toList());
                }
//                this.socketio = new AbodeSocketIO(url, cookies, AbodeBindingConstants.BASE_URL);
//
//                this.socketio.on(AbodeSocketIO.CONNECTED, this::onSocketConnected);
//                this.socketio.on(AbodeSocketIO.DISCONNECTED, this::onSocketDisconnected);
//                this.socketio.on(AbodeBindingConstants.DEVICE_UPDATE_EVENT, this::onDeviceUpdate);
//                this.socketio.on(AbodeBindingConstants.GATEWAY_MODE_EVENT, this::onModeChange);
                // this.socketio.on(AbodeBindingConstants.TIMELINE_EVENT, this::onTimelineUpdate);
                // this.socketio.on(AbodeBindingConstants.AUTOMATION_EVENT, this::onAutomationUpdate);
    }

    private void onDeviceUpdate(Object... objects) {
        this.onDeviceUpdate(objects[0].toString());
    }

    private void onSocketDisconnected(Object... objects) {
        this.onSocketDisconnected();
    }

    private void onSocketConnected(Object... objects) {
        this.onSocketConnected();
    }

    // private void onSocketStarted(Object... objects) {
    //    this.onSocketStarted();
    // }



    public void start() throws AbodeException {
        try {
            this.socketio.start();
        } catch (URISyntaxException ex) {
            throw new AbodeException("Invalid socket io url", ex);
        }
    }

    public void stop() throws AbodeException {
        try {
            this.socketio.stop();
        } catch (InterruptedException ex) {
            throw new AbodeException("Timeout error while stopping", ex);
        }
    }

    public boolean addConnectionStatusCallback(String uniqueId, Function callback) {
        if (uniqueId == null) {
            return false;
        }
        logger.debug("Subscribing to Abode connection updates for " + uniqueId);
        if (!this.connectionStatusCallbacks.containsKey(uniqueId)) {
            this.connectionStatusCallbacks.put(uniqueId, new ArrayList<>());
        }
        this.connectionStatusCallbacks.get(uniqueId).add(callback);
        return true;
    }

    public boolean removeConnectionStatusCallback(String uniqueId) {
        if (uniqueId == null) {
            return false;
        }
        logger.debug("Unsubscribing from Abode connection updates for " + uniqueId);
        this.connectionStatusCallbacks.remove(uniqueId);
        return true;
    }

    public boolean addDeviceCallbackDeviceIds(List<String> deviceIds, Function callback) throws AbodeException {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return false;
        }

        for (String deviceId : deviceIds) {
            this.abode.getDevice(deviceId);
            logger.debug("Subscribing to updates for deviceId " + deviceId);
            if (!this.deviceCallbacks.containsKey(deviceId)) {
                this.deviceCallbacks.put(deviceId, new ArrayList<>());
            }
            this.deviceCallbacks.get(deviceId).add(callback);
        }
        return true;
    }

    public boolean addDeviceCallback(List<AbodeDevice> devices, Function callback) throws AbodeException {
        if (devices == null || devices.isEmpty()) {
            return false;
        }
        return this.addDeviceCallbackDeviceIds(devices.stream().map(d -> d.deviceId()).collect(Collectors.toList()), callback);
    }

    public boolean removeAllDeviceCallbacksDeviceId(List<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return false;
        }
        for (String deviceId : deviceIds) {
            if (!this.deviceCallbacks.containsKey(deviceId)) {
                return false;
            }
            logger.debug("Unsubscribing from all updates for deviceId: " + deviceId);
            this.deviceCallbacks.remove(deviceId);
        }
        return true;
    }

    public boolean removeAllDeviceCallbacks(List<AbodeDevice> devices) throws AbodeException {
        if (devices == null || devices.isEmpty()) {
            return false;
        }
        for (AbodeDevice device : devices) {
            this.abode.getDevice(device.deviceId());
        }
        return removeAllDeviceCallbacksDeviceId(devices.stream().map(d -> d.deviceId()).collect(Collectors.toList()));
    }

//    public boolean addEventCallback(List<String> eventGroups, Function callback) throws AbodeException {
//        if (eventGroups == null || eventGroups.isEmpty()) {
//            return false;
//        }
//        for (String eventGroup : eventGroups) {
//            if (!TIMELINE.ALL_EVENT_GROUPS.contains(eventGroup)) {
//                throw new AbodeException("Event group " + eventGroup + " is invalid");
//            }
//            logger.debug("Subscribing to event group: " + eventGroup);
//            if (!this.eventCallbacks.containsKey(eventGroup)) {
//                this.eventCallbacks.put(eventGroup, new ArrayList<>());
//            }
//            this.eventCallbacks.get(eventGroup).add(callback);
//        }
//        return true;
//    }

    public boolean addTimelineCallback(List<Map<String, String>> timelineEvents, Function callback) throws AbodeException {
        if (timelineEvents == null || timelineEvents.isEmpty()) {
            return false;
        }
        for (Map<String, String> timelineEvent : timelineEvents) {
            String eventCode = timelineEvent.get("event_code");
            if (eventCode == null || eventCode.isBlank()) {
                throw new AbodeException(ErrorConstants.EVENT_CODE_MISSING);
            }
            logger.debug("Subscribing to timeline event: " + timelineEvent);
            if (!this.timelineCallbacks.containsKey(eventCode)) {
                this.timelineCallbacks.put(eventCode, new ArrayList<>());
            }
            this.timelineCallbacks.get(eventCode).add(callback);
        }
        return true;
    }

    public boolean connected() {
        return this.connected;
    }

    public AbodeSocketIO socketio() {
        return this.socketio;
    }

    private void onSocketConnected() {
        this.connected = true;
        try {
            this.abode.refresh();
        } catch (AbodeException e) {
            logger.warn("Captured exception during abode refresh: ", e);
        } finally {
            this.connectionStatusCallbacks.forEach((callbackKey, callbackValue) -> {
                callbackValue.forEach(callback -> {
                    this.executeCallback(callback);
                });
            });
        }
    }

    private void onSocketDisconnected() {
        this.connected = false;
        this.connectionStatusCallbacks.forEach((callbackKey, callbackValue) -> {
            callbackValue.forEach(callback -> {
                this.executeCallback(callback);
            });
        });
    }

    private void onDeviceUpdate(String deviceId) {
        if (deviceId == null) {
            logger.warn("Device update with no deviceId");
        }
        logger.debug("Device update event for deviceId: " + deviceId);

        AbodeDevice device = null;
        try {
            this.abode.getDevice(deviceId);
        } catch (AbodeException e) {
            logger.warn("No device for Id: " + deviceId);
        }

        if (device == null) {
            logger.debug("Unknown device " + deviceId);
            return;
        }
        if (!this.deviceCallbacks.containsKey(deviceId)) {
            return;
        }
        this.deviceCallbacks.get(deviceId).forEach(callback -> {
            this.executeCallback(callback);
        });
    }

    public void onModeChange(Object[] inputs){
        for (int i = 0; i < inputs.length; i++) {
            onModeChange(inputs[i].toString());
        }
    }

    public void onModeChange(String mode) {
        if (mode == null || mode.isBlank()) {
            logger.warn("Mode change with no mode");
            return;
        }

        if (!AbodeConstants.ALL_MODES.contains(mode.toLowerCase())) {
            logger.warn("Mode change event with an unknown mode");
            return;
        }

        try {
            AbodeDevice alarmDevice = this.abode.getAlarm(true);

            if (alarmDevice instanceof Alarm) {
                Alarm alarm = (Alarm) alarmDevice;
                alarm.setMode(mode);
            }

            if (this.deviceCallbacks.containsKey(alarmDevice.deviceId())) {
                this.deviceCallbacks.get(alarmDevice.deviceId()).forEach(callback -> {
                    this.executeCallback(callback);
                });
            }
        } catch (AbodeException ex) {
            logger.warn("Failed to execute mode update", ex);
        }
    }

    public void onTimelineUpdate(Map<String, String> event) {
        String eventType = event.get("event_type");
        String eventCode = event.get("event_code");

        if (eventType == null || eventCode == null || eventType.isBlank() || eventCode.isBlank()) {
            logger.warn("Invalid timeline update event: " + event);
            return;
        }

        logger.debug("Timeline event received: " + event.get("event_name") + eventType + eventCode);
        //TODO: finish this
    }

    public void executeCallback(Function function) {
        try {
            function.apply(null);
        } catch (Exception e) {
            logger.warn("Failed to execute callback", e);
        }
    }


}
