package org.bdawg.abode.internal;

import com.google.gson.*;
import org.eclipse.jetty.util.HttpCookieStore;

import java.lang.reflect.Type;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;

public class CookieSerializer implements JsonDeserializer<CookieStore>, JsonSerializer<CookieStore> {

    @Override
    public CookieStore deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        CookieStore cookies = new HttpCookieStore();
        if (!json.isJsonObject() | !json.getAsJsonObject().has("cookieJar") || !json.getAsJsonObject().get("cookieJar").isJsonArray()) {
            return cookies;
        }
        JsonArray cookieJarArray = json.getAsJsonObject().get("cookieJar").getAsJsonArray();
        cookieJarArray.forEach(jsonElement -> {
            HttpCookie cookie = context.deserialize(jsonElement, HttpCookie.class);
            cookies.add(URI.create(cookie.getDomain()), cookie);
        });
        return cookies;
    }

    @Override
    public JsonElement serialize(CookieStore src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return new JsonObject();
        }
        JsonArray cookies = new JsonArray();
        JsonObject cookieJar = new JsonObject();
        cookieJar.add("cookieJar", cookies);
        src.getCookies().forEach(cookie -> {
            JsonElement serialized = context.serialize(cookie);
            cookies.add(serialized);
        });
        return cookieJar;
    }
}
