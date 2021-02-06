package org.bdawg.abode;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.socket.client.SocketIOException;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Socket;
import org.bdawg.abode.exceptions.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AbodeSocketIO {

    private static final Logger logger = LoggerFactory.getLogger(AbodeSocketIO.class);

    private static final Executor threadExecutor = Executors.newSingleThreadExecutor();

    public static final String STARTED = "started";
    public static final String STOPPED = "stopped";
    public static final String CONNECTED = "connected";
    public static final String DISCONNECTED = "disconnected";
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final String POLL = "poll";
    public static final String EVENT = "event";
    public static final String ERROR = "error";

    public static final String PACKET_OPEN = "0";
    public static final String PACKET_CLOSE = "1";
    public static final String PACKET_PING = "2";
    public static final String PACKET_PONG = "3";
    public static final String PACKET_MESSAGE = "4";

    public static final String MESSAGE_CONNECT = "0";
    public static final String MESSAGE_DISCONNECT = "1";
    public static final String MESSAGE_EVENT = "2";
    public static final String MESSAGE_ERROR = "4";

    public static final String PING_INTERVAL = "pingInterval";
    public static final String PING_TIMEOUT = "pingTimeout";

    public static final String COOKIE_HEADER = "Cookie";
    public static final String ORIGIN_HEADER = "Origin";

    public static final String URL_PARAMS = "?EIO=3&transport=websocket";


    private final Gson gson = new Gson();
    private final String url;
    private String origin;
    private List<String> cookies;
    private Thread thread;
    private Socket websocket;
    private Object exitEvent;
    private boolean running;

    private boolean websocketConnected = false;
    private boolean engineIoConnected = false;
    private boolean socketIoConnected = false;

    private Map<String, List<Emitter.Listener>> callbacks;

    private long pingIntervalMs = 25000;
    private long pingTimeoutMs = 60000;

    private Date lastPingTime;
    private Date lastPacketTime;


    public AbodeSocketIO(String url, List<String> cookies, String origin) {
        this.callbacks = new HashMap<>();
        this.url = url + URL_PARAMS;
        if (origin != null) {
            this.origin = origin;
        }
        if (cookies != null) {
            this.cookies = cookies;
        }
        this.thread = null;
        this.websocket = null;
        this.exitEvent = null;
        this.running = false;

        this.lastPingTime = Date.from(Instant.EPOCH);
        this.lastPacketTime = Date.from(Instant.EPOCH);
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;

    }

    public boolean on(String eventName, Emitter.Listener callback) {
        if (eventName == null || eventName.isBlank()) {
            return false;
        }
        if (!this.callbacks.containsKey(eventName)) {
            this.callbacks.put(eventName, new ArrayList<>());
        }
        this.callbacks.get(eventName).add(callback);
        return true;
    }

    public void start() throws URISyntaxException { //TODO: this is not going to work - the threading model is diverged from python
        // TODO: re-adapt to be more java-like
        if (this.thread == null) {
            logger.info("Starting socketIO thread");
            this.thread = new Thread(() -> {
                AbodeSocketIO.this.running = true;
                int minWait = 5;
                int maxWait = 30;

                int retries = 0;

                int randomWait = maxWait - minWait;

                while (AbodeSocketIO.this.running) {
                    logger.info("Attempting to connect to SocketIoServer");
                    try {
                        retries += 1;
                        AbodeSocketIO.this.handleEvent(STARTED, null);
                        Socket.Options options = new Socket.Options();
                        if (this.origin != null) {
                            options.extraHeaders.put(ORIGIN_HEADER, Collections.singletonList(this.origin));
                        }
                        if (this.cookies != null ) {
                            options.extraHeaders.put(COOKIE_HEADER, this.cookies);
                        }
                        this.websocket = new io.socket.engineio.client.Socket(this.url, options);
                        this.websocket.on("connected", this::onWebSocketConnected);
                        this.websocket.on("disconnected", this::onWebSocketDisconnected);
                        this.websocket.on("text", this::onWebSocketText);
                        this.websocket.on("poll", this::onWebSocketPoll);
                        this.websocket.on("backoff", this::onWebSocketBackoff);
                        this.websocket.open();

                        if (!AbodeSocketIO.this.running) {
                            this.websocket.close();
                        }


                    } catch (URISyntaxException e) {
                        logger.warn("Err for uri: ", e);
                    }

                    if (AbodeSocketIO.this.running) {
                        double waitFor = minWait + Math.random() * Math.min(randomWait, Math.pow(2, retries));
                        logger.info("Waiting for seconds: " + waitFor);
                        //TODO: fix this
//                        if (this.exitEvent != null && this.exitEvent.wait(waitFor)) {
//                            break;
//                        }
                    }
                }
                this.handleEvent(STOPPED, null);

            }, "SocketIoThread");
            this.thread.setDaemon(true);
            threadExecutor.execute(this.thread);
        }
    }

    public void stop() throws InterruptedException {
        if (this.thread != null) {
            logger.info("Stopping socket io thread");
            this.running = false;
            if (this.exitEvent != null) {

                // TODO: fix this
                // this.exitEvent.set();
            }
            this.thread.join();
        }
    }

    private void onWebSocketConnected(Object event) {
        this.websocketConnected = true;
        logger.info("Websocket Connected");
        this.handleEvent(CONNECTED, null);
    }

    private void onWebSocketDisconnected(Object event) {
        this.websocketConnected = false;
        this.engineIoConnected = false;
        this.socketIoConnected = false;
        logger.info("Websocket Disconnected");
        this.handleEvent(DISCONNECTED, null);
    }

    private void onWebSocketPoll(Object event) {
        Duration lastPacketDelta = Duration.between(this.lastPacketTime.toInstant(), Instant.now());
        long lastPacketMs = lastPacketDelta.toMillis();

        if (this.engineIoConnected && lastPacketMs > this.pingTimeoutMs) {
            logger.warn("SocketIO Server ping timeout");
            this.websocket.close(); //TODO: this probably isn't the right behavior, we need a "restart"
            return;
        }

        Duration lastPingDelta = Duration.between(this.lastPingTime.toInstant(), Instant.now());
        long lastPingMs = lastPingDelta.toMillis();

        if (this.engineIoConnected && lastPingMs >= this.pingIntervalMs) {
            this.websocket.send(PACKET_PING);
            this.lastPingTime = new Date();
            logger.debug("Client ping");
            this.handleEvent(PING, null);
        }

        this.handleEvent(POLL, null);
    }

    private void onWebSocketText(Object eventObject) {
        String event = eventObject.toString();
        this.lastPacketTime = new Date();
        String packetType = event.substring(0, 1);
        if (packetType == null) {
            logger.warn("Null packet type from websocket");
            return;
        }
        String packetData = event.substring(1);

        try {

            switch (packetType) {
                case PACKET_OPEN:
                    this.onEngineIoOpened(packetData);
                    return;
                case PACKET_CLOSE:
                    this.onEngineIoClosed();
                    return;
                case PACKET_PONG:
                    this.onEngineIoPong();
                    return;
                case PACKET_MESSAGE:
                    this.onEngineIoMessage(packetData);
                    return;
                default:
                    logger.debug("Ignoring EngineIO packet:" + event);
            }
        } catch (Exception e) {
            logger.warn("Failed to handle engine io event", e);
        }
    }

    private void onWebSocketBackoff(Object event) {
        return;
    }

    private void onEngineIoOpened(String packetData) {
        if (packetData == null || packetData.isBlank()) {
            return;
        }
        JsonElement jsonData = gson.toJsonTree(packetData);
        if (jsonData.isJsonObject() && jsonData.getAsJsonObject().has(PING_INTERVAL)) {
            this.pingIntervalMs = jsonData.getAsJsonObject().get(PING_INTERVAL).getAsLong();
            logger.debug("Set ping interal to:" + this.pingIntervalMs);
        }

        if (jsonData.isJsonObject() && jsonData.getAsJsonObject().has(PING_TIMEOUT)) {
            this.pingTimeoutMs = jsonData.getAsJsonObject().get(PING_TIMEOUT).getAsLong();
            logger.debug("Set ping timeout:" + this.pingTimeoutMs);
        }

        this.engineIoConnected = true;
        logger.debug("EngineIo connected");
    }

    private void onEngineIoClosed() {
        this.engineIoConnected = false;
        logger.debug("EngineIo disconnected");
        this.websocket.close();
    }

    private void onEngineIoPong() {
        logger.debug("Server pong");
        this.handleEvent(PONG, null);
    }

    private void onEngineIoMessage(String packetData) throws SocketIOException {
        String messageType = packetData.substring(0, 1);
        String messageData = packetData.substring(1);

        if (messageData == null || messageType == null) {
            return;
        }

        switch (messageType) {
            case MESSAGE_CONNECT:
                this.onSocketIoConnected();
                return;
            case MESSAGE_DISCONNECT:
                this.onSocketIoDisconnected();
                return;
            case MESSAGE_ERROR:
                this.onSocketIoError(messageData);
                return;
            case MESSAGE_EVENT:
                this.onSocketIoEvent(messageData);
                return;
            default:
                logger.debug("Ignoring SocketIo message: " + packetData);
        }
    }

    private void onSocketIoConnected() {
        this.socketIoConnected = true;
        logger.debug("SocketIo Connected");
    }

    private void onSocketIoDisconnected() {
        this.socketIoConnected = false;
        logger.debug("SocketIo disconnected");
    }

    private void onSocketIoError(String messageData) throws SocketIOException {
        this.handleEvent(ERROR, messageData);
        throw new SocketIOException(ErrorConstants.SOCKETIO_ERROR, new Exception(messageData));
    }

    private void onSocketIoEvent(String messageData) {
        int idxL = messageData.indexOf("[");
        int idxR = messageData.indexOf("]");

        if (idxL == -1 || idxR == -1) {
            logger.warn("Unable to find event: " + messageData);
            return;
        }

        String json = messageData.substring(idxL, idxR + 1);
        JsonElement jsonData = gson.toJsonTree(json);

        this.handleEvent(EVENT, messageData);
        if (jsonData.isJsonArray()) {
            JsonElement event = jsonData.getAsJsonArray().remove(0);
            JsonArray data = jsonData.getAsJsonArray();
            this.handleEvent(event.getAsString(), data);
        }

    }

    private void handleEvent(String eventName, Object eventData) {
        if (!this.callbacks.containsKey(eventName)) {
            return;
        }
        this.callbacks.get(eventName).forEach(callback -> {
            try {
                if (eventData != null) {
                    callback.call(eventData);
                } else {
                    callback.call();
                }
            } catch (Exception e) {
                logger.error("Captured exception during SocketIO event callback:", e);
            }
        });
    }

}
