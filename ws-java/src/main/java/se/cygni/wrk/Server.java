package se.cygni.wrk;

import com.fasterxml.jackson.databind.JsonNode;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import rx.subjects.PublishSubject;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Server extends WebSocketServer {
    private final Map<WebSocket, Handler> modelsBySocket;

    public Server(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        modelsBySocket = new ConcurrentHashMap<>();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        String address = Util.getAddress(webSocket);
        System.out.println("connect from " + address);
        PublishSubject<JsonNode> messages = PublishSubject.<JsonNode>create();
        messages.subscribe(json -> {
            String jsonString = Util.toString(json);
            System.out.println("sending to " + address + " :" + jsonString);
            webSocket.send(jsonString);
        });
        Handler handler = new Handler(messages);
        modelsBySocket.put(webSocket, handler);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("close from " + Util.getAddress(webSocket));
        modelsBySocket.remove(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println("message from " + Util.getAddress(webSocket) + ": " + s);
        Handler handler = modelsBySocket.get(webSocket);
        JsonNode message = Util.toJson(s);
        String type = message.get("type").textValue();
        switch (type) {
            case "go.click":
                handler.getGoClicks().onNext("");
                break;
            case "query.input":
                handler.getQueryInputs().onNext(message.get("text").textValue());
                break;
            default:
                throw new IllegalStateException("Unknown message type '" + type + "': '" + s + "'");
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        if (webSocket != null) {
            System.err.println("error:" + Util.getAddress(webSocket));
        }
        e.printStackTrace(System.err);
    }

    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        WebSocketImpl.DEBUG = false;
        final Server s = new Server(4739);
        s.start();
        System.out.println("Server started");
        final CountDownLatch shuttingDown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(shuttingDown::countDown));
        shuttingDown.await();
    }
}
