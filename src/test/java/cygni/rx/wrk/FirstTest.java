package cygni.rx.wrk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;
import rx.observables.ConnectableObservable;
import rx.observables.JavaFxObservable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public class FirstTest extends javafx.application.Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        final Model model = new Model();
        primaryStage.setTitle("AnkaAnkaGå");
        final GridPane gp = new GridPane();
        gp.setAlignment(Pos.CENTER);
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(25));
        final Scene scene = new Scene(gp, 800, 600);
        primaryStage.setScene(scene);
        gp.add(new Label("Query"), 0,0);
        final TextField searchField = new TextField();
        gp.add(searchField, 1, 0);
        final Button searchButton = new Button("Search");
        gp.add(searchButton, 2, 0);
        final ListView<Object> lw = new ListView<>();
        lw.setItems(FXCollections.observableArrayList("a", "b", "c"));
        gp.add(lw, 0, 1, 3, 1);
        primaryStage.show();



        final ConnectableObservable<ActionEvent> searchButtonClicks = JavaFxObservable.fromNodeEvents(searchButton, ActionEvent.ACTION).publish();
        searchButtonClicks.connect();
        final Observable<KeyEvent> searchKeyPresses = JavaFxObservable.fromNodeEvents(searchField, KeyEvent.KEY_TYPED);


        final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();
        httpClient.start();
        searchButtonClicks.subscribe(e -> {
                    System.out.println("search click: " + e);
                });
        searchKeyPresses.subscribe(e -> {
            System.out.println("search key press: " + e);
        });

        final Observable<KeyEvent> stoppedTyping = searchKeyPresses.debounce(300, TimeUnit.MILLISECONDS);

        final Observable<String> searchTexts = JavaFxObservable.fromObservableValue(searchField.textProperty());
        searchTexts.subscribe(e -> {
            System.out.println("Search text changed:" + e);
        });

        final Observable<ObservableHttpResponse> requests = searchButtonClicks.flatMap(c -> {
            final String text = searchField.getText();
            final String url = String.format(
                    "http://api.duckduckgo.com/?q=%s&format=json&pretty=1", urlEncode(text)
            );
            System.out.println("Running request:" + url + " on " + Thread.currentThread().getName());
            final Observable<ObservableHttpResponse> o = ObservableHttp.createGet(url, httpClient).toObservable();
            System.out.println("Created request");
            return o;
        });

        /*
        requests.subscribe(r -> {
            System.out.println("got it");
        });
        */


        requests.flatMap(r -> {
            System.out.println("Got answer on " + Thread.currentThread().getName());
            final HttpEntity e = r.getContent().m
            final JsonNode root = toJson(e);
            Platform.runLater(() -> lw.getItems().add("Blehbleh"));
            //lw.refresh();
        }, e -> {
            System.out.println("error");
            e.printStackTrace(System.err);
        }, () -> {
            System.out.println("complete");
        });




        //1. Make button trigger load. Transform from click to text from field
        //   (make observable with latest text from
        //2. Merge with keypresses
    }

    private JsonNode toJson(HttpEntity e)  {
        try {
            final InputStream is = e.getContent();
            return new ObjectMapper().readTree(is);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    private String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
