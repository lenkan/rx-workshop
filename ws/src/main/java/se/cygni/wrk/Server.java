package se.cygni.wrk;

import com.fasterxml.jackson.databind.JsonNode;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by alext on 2016-01-25.
 */
public class Server extends WebSocketServer {
    private final Model model;

    public Server(Model model, int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.model = model;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("connect from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("close from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(
                "message from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ": " + s
        );
        JsonNode message = Util.toJson(s);
        String type = message.get("type").textValue();
        switch (type) {
            case "go.click":
                model.getGoClicks().onNext("");
                break;
            case "query.input":
                model.getQueryInputs().onNext(message.get("text").textValue());
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
        final Server s = new Server(new Model(), 4739);
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
