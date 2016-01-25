package se.cygni.wrk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by alext on 1/25/16.
 */
public class Util {
    public static JsonNode toJson(String s) {
        try {
            return new ObjectMapper().readTree(s);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    public static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toString(JsonNode json) {
        try {
            return new ObjectMapper().writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static String getAddress(WebSocket webSocket) {
        return webSocket.getRemoteSocketAddress().toString();
    }
}
