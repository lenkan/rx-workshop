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
        System.out.println("connect from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
        PublishSubject<JsonNode> messages = PublishSubject.<JsonNode>create();
        messages.subscribe(json -> webSocket.send(Util.toString(json)));
        modelsBySocket.put(webSocket, new Handler(messages));
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("close from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
        modelsBySocket.remove(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(
                "message from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ": " + s
        );
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
                throw new IllegalStateException("Unknown message type '"+ type +"': '" + s + "'");
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.err.println("error:" + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
        e.printStackTrace(System.err);
    }

    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        WebSocketImpl.DEBUG = false;
        final Server s = new Server(4739);
        s.start();
        System.out.println("Server started");
        final CountDownLatch shuttingDown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shuttingDown.countDown();
            }
        }));
        shuttingDown.await();
    }
}
