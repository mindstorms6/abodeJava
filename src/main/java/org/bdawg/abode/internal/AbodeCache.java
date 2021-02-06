package org.bdawg.abode.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bdawg.abode.exceptions.AbodeException;
import org.bdawg.abode.helpers.AbodeHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieStore;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class AbodeCache {

    private static final Gson gson;
    static {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(CookieStore.class, new CookieSerializer());
        gson = builder.create();
    }

    public static void saveCache(AbodeCache cache, String filePath) throws IOException, AbodeException {
        Path cachePath = Path.of(filePath);
        if (Files.isDirectory(cachePath)){
            throw new AbodeException(filePath + " is a directory");
        }
        if (!Files.exists(cachePath)) {
            Files.createFile(cachePath);
        }


        Files.write(cachePath, gson.toJson(cache).getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static AbodeCache loadCache(String filePath) throws IOException {
        Path cachePath = Path.of(filePath);
        if (!Files.exists(cachePath)) {
            return null;
        }

        return gson.fromJson(new String(Files.readAllBytes(cachePath), StandardCharsets.UTF_8), AbodeCache.class);
    }

    private static final Logger logger = LoggerFactory.getLogger(AbodeCache.class);

    public String id;
    public String password;
    public String uuid;
    public CookieStore cookies;

    private String cachePath;
    private boolean disabled;

    public AbodeCache() {
        this(null, true);
    }

    public AbodeCache(String cachePath, boolean disabled) {
        this.cachePath = cachePath;
        this.disabled = disabled;
    }

    public void save() throws IOException, AbodeException {
        if (!this.disabled) {
            AbodeCache.saveCache(this, cachePath);
        }

    }

    public void load() throws IOException, AbodeException {
        if (!this.disabled && Files.isReadable(Path.of(this.cachePath)) && Files.isRegularFile(Path.of(this.cachePath))) {
            logger.debug("Cache found at " + this.cachePath);
            AbodeCache cache = AbodeCache.loadCache(this.cachePath);
            if (cache != null) {
                this.update(cache);
            } else {
                logger.debug("Removing invalid cache file: " + this.cachePath);
                try {
                    Files.deleteIfExists(Path.of(this.cachePath));
                } catch (Exception ex) {
                    logger.warn("Failed to remove cache file", ex);
                }
            }
        }
        this.save();
    }

    public void update(AbodeCache other) {
        if (other.cookies != null) {
            this.cookies = other.cookies;
        }
        if (other.password != null) {
            this.password = other.password;
        }
        if (other.id != null) {
            this.id = other.id;
        }
        if (other.uuid != null) {
            this.uuid = other.uuid;
        }
    }
}
