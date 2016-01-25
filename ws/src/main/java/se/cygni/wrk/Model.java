package se.cygni.wrk;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

/**
 * Created by alext on 2016-01-25.
 */
public class Model {

    private final PublishSubject<String> goClicks;
    private final PublishSubject<String> queryInputs;

    public Model() {
        goClicks = PublishSubject.create();
        queryInputs = PublishSubject.create();
        String text = "hello";
        Observable<HttpClientResponse<ByteBuf>> test = RxNetty.createHttpGet("www.google.com");
        final Observable<HttpClientResponse<ByteBuf>> requests = goClicks.flatMap(c -> {
            final String url = String.format(
                    "http://api.duckduckgo.com/?q=%s&format=json&pretty=1", Util.urlEncode(text)
            );
            //final String url = "http://durat.io";
            System.out.println("Running request:" + url + " on " + Thread.currentThread().getName());
            final Observable<HttpClientResponse<ByteBuf>> o =  RxNetty.createHttpGet(url);
            System.out.println("Created request");
            return o;
        });
    }

    public PublishSubject<String> getGoClicks() {
        return goClicks;
    }

    public Observer<String> getQueryInputs() {
        return queryInputs;
    }
}
