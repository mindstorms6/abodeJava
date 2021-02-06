package org.bdawg.abode;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bdawg.abode.devices.AbodeDevice;
import org.bdawg.abode.devices.Alarm;
import org.bdawg.abode.devices.BinarySensor;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.bdawg.abode.exceptions.AbodeAuthenticationException;
import org.bdawg.abode.exceptions.AbodeException;
import org.bdawg.abode.helpers.AbodeHelpers;
import org.bdawg.abode.internal.AbodeConstants;
import org.bdawg.abode.internal.AbodeCache;
import org.bdawg.abode.exceptions.ErrorConstants;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieStore;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Abode {
    private static final Logger logger = LoggerFactory.getLogger(Abode.class);

    private String token;
    private JsonElement panelJson;
    private Alarm panel;
    private JsonObject user;
    private String cachePath;
    private String oauthToken;
    private boolean disableCache;
    private AbodeEventController eventController;
    private Map<String, AbodeDevice> devices;
    private HttpClient session;

    private AbodeCache cache;
    private Gson gson = new Gson();
    // private List<AbodeAutomation> automations;

    // private String defaultAlarmMode;

    public Abode(String username, String password, boolean autoLogin, boolean getDevices, boolean getAutomations, String cachePath, boolean disableCache) throws AbodeException, IOException, InterruptedException, ExecutionException, TimeoutException {
        // this.defaultAlarmMode = AbodeBindingConstants.
        this.session = null;
        this.token = null;
        this.panel = null;
        this.user = null;
//        this.cachePath = cachePath;
//        this.disableCache = disableCache;
        this.devices = new HashMap<>();
        this.session = new HttpClient();


        this.eventController = new AbodeEventController(this, AbodeConstants.SOCKETIO_URL);
        this.cache = new AbodeCache(cachePath, disableCache);

        this.cache.load();

        if (username != null && !username.isBlank()) {
            this.cache.id = username;
        }

        if (password != null && !password.isBlank()) {
            this.cache.password = password;
        }

        this.cache.save();

        if (this.cache.cookies != null) {
            this.session.setCookieStore(this.cache.cookies);
        }

        try {
            this.session.start();
        } catch (Exception ex) {
            throw new AbodeException("Could not create and start an http client", ex);
        }

        if (this.cache.id != null && this.cache.password != null && autoLogin) {
            this.login();
        }

        if (getDevices) {
            this.getDevices();
        }
    }

    private boolean login() throws AbodeException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return this.login(null, null, null);
    }

    private boolean login(String username, String password, String mfaCode) throws AbodeException, IOException, InterruptedException, ExecutionException, TimeoutException {
        if (username != null && !username.isBlank()) {
            this.cache.id = username;
        }
        if (password != null && !password.isBlank()) {
            this.cache.password = password;
        }

        if (this.cache.id == null || this.cache.id.isBlank()) {
            throw new AbodeAuthenticationException("Missing username");
        }

        if (this.cache.password == null || this.cache.password.isBlank()) {
            throw new AbodeAuthenticationException("Missing password");
        }

        if (this.cache.uuid == null) {
            this.cache.uuid = UUID.randomUUID().toString();
        }

        this.cache.save();

        this.token = null;

        Map<String, String> loginData = new HashMap<>();
        loginData.put(AbodeConstants.ID, this.cache.id);
        loginData.put(AbodeConstants.PASSWORD, this.cache.password);
        loginData.put(AbodeConstants.UUID, this.cache.uuid);

        if (mfaCode != null && !mfaCode.isBlank()) {
            loginData.put(AbodeConstants.MFA_CODE, mfaCode);
            loginData.put("remember_me", String.valueOf(1));
        }

        Request loginRequest = this.session.newRequest(AbodeConstants.LOGIN_URL).method(HttpMethod.POST);
        loginRequest = loginRequest.body(new StringRequestContent("application/json", this.gson.toJson(loginData), StandardCharsets.UTF_8));
        ContentResponse response = loginRequest.send();

        byte[] content = response.getContent();
        int status = response.getStatus();
        if (response.getStatus() != 200) {
            throw new AbodeAuthenticationException(status, new String(content, StandardCharsets.UTF_8));
        }


        JsonElement responseJson = JsonParser.parseString(new String(content, StandardCharsets.UTF_8));

        if (responseJson.getAsJsonObject().has("mfa_type")) {
            if ("google_authenticator".equals(responseJson.getAsJsonObject().get("mfa_type").getAsString())) {
                throw new AbodeAuthenticationException("MFA Required");
            } else {
                throw new AbodeAuthenticationException("Unknown MFA Type");
            }
        }


        this.cache.cookies = this.session.getCookieStore();
        this.cache.save();

        ContentResponse oauthResponse = session.GET(AbodeConstants.OAUTH_TOKEN_URL);

        String oauthContent = new String(oauthResponse.getContent(), StandardCharsets.UTF_8);
        if (oauthResponse.getStatus() != 200) {
            throw new AbodeAuthenticationException(oauthResponse.getStatus(), oauthContent);
        }

        JsonElement oauthObject = JsonParser.parseString(oauthContent);

        logger.debug(String.format("Login response %s", oauthContent));

        this.token = responseJson.getAsJsonObject().get("token").getAsString();
        this.panelJson = responseJson.getAsJsonObject().get("panel");
        this.user = responseJson.getAsJsonObject().get("user").getAsJsonObject();
        this.oauthToken = oauthObject.getAsJsonObject().get("access_token").getAsString();

        logger.info("Login successful");

        return true;


    }

    public CookieStore getSessionCookies() {
        return this.session.getCookieStore();
    }

    public JsonElement sendRequest(String method, String url) throws AbodeException {
        return this.sendRequest(method, url, null, null, false);
    }

    public JsonElement sendRequest(String method, String url, Map<String, String> headers, ContentProvider content, Boolean isRetry) throws AbodeException {
        if (this.token == null) {
            try {
                this.login();
            } catch (AbodeAuthenticationException | IOException | InterruptedException | ExecutionException | TimeoutException e) {
                logger.warn("Login failed, not sending request");
                throw new AbodeException("Login failure", e);
            }
        }

        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Authorization", String.format("Bearer %s", this.oauthToken));
        headers.put("ABODE-API-KEY", this.token);

        HttpClient httpClient = new HttpClient();
        Request request = httpClient.newRequest(url);
        request.method(method);
        headers.forEach((key, value) -> {
            request.header(key, value);
        });
        if ((method.equalsIgnoreCase("post") || method.equalsIgnoreCase("put")) && content != null) {
            request.content( content);
        }

        try {
            ContentResponse response = request.send();

            if (response != null && response.getStatus() < 400) {
                return this.gson.toJsonTree(response.getContentAsString());
            }
        } catch ( InterruptedException | ExecutionException | TimeoutException e) {
            logger.info("Abode connection reset...", e);
        }

        if (!isRetry) {
            // Delete our current token and try again-- will force a login
            // attempt.
            this.token = null;

            return this.sendRequest(method, url, headers, content, true);
        }

        throw new AbodeException(ErrorConstants.REQUEST);

    }

    private boolean logout() throws AbodeAuthenticationException {
        Map<String, String> headerData = new HashMap<>();
        if (this.token != null) {
            headerData.put("ABODE-API-KEY", this.token);
        }

        this.session = new HttpClient();
        this.token = null;
        this.panel = null;
        this.user = null;
        this.devices = new HashMap<>();

        JsonElement responseObject;
        ContentResponse response;

        try {
            Request logoutRequest = this.session.POST(AbodeConstants.LOGOUT_URL);
            response = logoutRequest.send();
            responseObject = this.gson.toJsonTree(response.getContentAsString());
        } catch (Exception ex) {
            logger.warn("Failed to log out", ex);
            return false;
        }

        if (response.getStatus() != 200) {
            throw new AbodeAuthenticationException(response.getStatus(), response.getContentAsString());
        }

        logger.debug("Logout Response:" + response.getContentAsString());
        logger.info("Logout successful");

        return true;

    }

    public List<AbodeDevice> refresh() throws AbodeException {
        return this.getDevices(true, null);
    }

    public List<AbodeDevice> getDevices() throws AbodeException {
        return getDevices(false, null);
    }

    public List<AbodeDevice> getDevices(boolean refresh, String genericType) throws AbodeException {
        if (refresh || (this.devices == null || this.devices.isEmpty()) ) {
            if (this.devices == null) {
                this.devices = new HashMap<>();
            }
            logger.info("Updating all devices");
            ContentResponse devices;
            try {
                Request getDevicesRequest = this.session.newRequest(AbodeConstants.DEVICES_URL);
                getDevicesRequest.headers((headerConsumer) -> {
                    headerConsumer.add("Authorization", this.oauthToken);
                    headerConsumer.add("ABODE-API-KEY", this.token);
                });
                devices = getDevicesRequest.send();
            } catch (TimeoutException | InterruptedException | ExecutionException ex) {
                AbodeException e = new AbodeException("Failed to fetch devices", ex);
                logger.warn("Fetch devices failed", e);
                throw e;
            }
            JsonElement devicesObject = JsonParser.parseString(devices.getContentAsString());

            logger.info("Get devices response: " + devices.getContentAsString());

            if (devicesObject.isJsonArray()) {
                for (JsonElement device : devicesObject.getAsJsonArray()) {
                    JsonObject remoteDeviceAsObject = device.getAsJsonObject();
                    AbodeDevice existingDevice = this.devices.get(remoteDeviceAsObject.get("id").getAsString());

                    if (existingDevice != null) {
                        existingDevice.update(device);
                    } else {
                        existingDevice = this.newDevice(remoteDeviceAsObject, this);

                        if (existingDevice == null) {
                            logger.debug("Skipping unknown device: " + remoteDeviceAsObject.toString());
                            continue;
                        }
                        this.devices.put(existingDevice.deviceId(), existingDevice);
                    }
                }
            }

            try {
                Request panelRequest = this.session.newRequest(AbodeConstants.PANEL_URL);
                panelRequest.method(HttpMethod.GET);
                panelRequest.headers((headerConsumer) -> {
                    headerConsumer.add("Authorization", this.oauthToken);
                    headerConsumer.add("ABODE-API-KEY", this.token);
                });
                ContentResponse panelResponse = panelRequest.send();
                JsonElement panelJson = JsonParser.parseString(panelResponse.getContentAsString());
                if (this.panel != null) {
                    this.panel.update(panelJson);
                } else {
                    this.panel = Alarm.createAlarm(panelJson, this);
                }
            } catch (TimeoutException | InterruptedException | ExecutionException ex) {
                AbodeException e = new AbodeException("Failed to fetch panel updates", ex);
                logger.warn("Fetch panel failed", e);
                throw e;
            }

            logger.debug("Panel json:" + panelJson.toString());

            AbodeDevice alarmDevice = this.devices.get(AbodeConstants.ALARM_DEVICE_ID + "1");

            if (alarmDevice != null) {
                alarmDevice.update(panelJson);
            } else {
                alarmDevice = Alarm.createAlarm(this.panelJson, this);
                this.devices.put(AbodeConstants.ALARM_DEVICE_ID, alarmDevice);
            }

            if (genericType != null) {
                return this.devices.values().stream().filter(d -> genericType.equals(d.genericType())).collect(Collectors.toList());
            }

            return new ArrayList<>(this.devices.values());
        }

        return new ArrayList<>(this.devices.values());
    }

    public AbodeDevice getDevice(String deviceId) throws AbodeException {
        return getDevice(deviceId, false);
    }

    public AbodeDevice getDevice(String deviceId, boolean refresh) throws AbodeException {
        if (this.devices == null || this.devices.isEmpty()) {
            this.getDevices();
            refresh = false;
        }
        AbodeDevice device = this.devices.get(deviceId);
        if (device != null && refresh) {
            device.refresh();
        }
        return device;
    }

    public Alarm getPanel() {
        return this.panel;
    }

    public AbodeDevice getAlarm(String area) throws AbodeException {
        return this.getAlarm(area, false);
    }

    public AbodeDevice getAlarm(boolean refresh) throws AbodeException {
        return this.getAlarm("1", refresh);
    }

    public AbodeDevice getAlarm(String area, boolean refresh) throws AbodeException {
        if (this.devices == null || this.devices.isEmpty()) {
            this.getDevices();
            refresh = false;
        }
        return this.getDevice(AbodeConstants.ALARM_DEVICE_ID + area, refresh);
    }

    public AbodeEventController getEventController() {
        return eventController;
    }

    public String getUUID() {
        return this.cache.uuid;
    }

//    public AbodeBinarySensor newSensor() {
//
//    }

    public static AbodeDevice newDevice(JsonObject deviceJson, Abode abode) throws AbodeException {
        String typeTag = deviceJson.get("type_tag").getAsString();
        if (typeTag == null || typeTag.isBlank()) {
            throw new AbodeException("Unable to map device");
        }

        String genericType = AbodeHelpers.getGenericType(typeTag.toLowerCase());
        deviceJson.addProperty("generic_type", genericType);

        if (Arrays.asList(AbodeConstants.TYPE_CONNECTIVITY, AbodeConstants.TYPE_MOISTURE, AbodeConstants.TYPE_OPENING).contains(genericType)) {
            return new BinarySensor(deviceJson, abode);
        }
// TODO: Add the remaining device types
//        if (AbodeBindingConstants.TYPE_CAMERA.equals(genericType)) {
//            return AbodeCamera(deviceJson, abode);
//        }

        return null;
    }
}
