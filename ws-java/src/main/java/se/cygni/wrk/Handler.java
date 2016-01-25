package se.cygni.wrk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alext on 2016-01-25.
 */
public class Handler {
    private final PublishSubject<String> goClicks;
    private final PublishSubject<String> queryInputs;
    private final JsonNodeFactory nf;
    private final PublishSubject<JsonNode> messages;

    public Handler(PublishSubject<JsonNode> messages) {
        nf = JsonNodeFactory.instance;
        this.messages = messages;
        goClicks = PublishSubject.create();
        queryInputs = PublishSubject.create();
        messages.onNext(createLinksMessage(Collections.singletonList("http://java.sun.com")));
        Observable<String> delayedTexts = queryInputs.sample(goClicks);
        final Observable<HttpClientResponse<ByteBuf>> requests = delayedTexts.flatMap(text -> {
            final String url = String.format(
                    "http://api.duckduckgo.com/?q=%s&format=json&pretty=1", Util.urlEncode(text)
            );
            System.out.println("Running request:" + url + " on " + Thread.currentThread().getName());
            final Observable<HttpClientResponse<ByteBuf>> o = RxNetty.createHttpGet(url);
            System.out.println("Created request");
            return o;
        });
        requests.subscribe(response -> {
            response.getContent().subscribe(bb -> {
                        final String s = bb.toString(Charsets.UTF_8);
                        final JsonNode j = Util.toJson(s);
                        final JsonNode relatedTopics = j.get("RelatedTopics");

                        final ArrayList<JsonNode> relatedTopicsList = Lists.newArrayList(relatedTopics);
                        final List<String> links = relatedTopicsList.stream().filter(r -> r.has("FirstURL")).map(r -> r.get("FirstURL").textValue()).collect(Collectors.toList());
                        System.out.println(links);
                ObjectNode msg = createLinksMessage(links);
                        this.messages.onNext(msg);
                    }, (e) -> {
                        e.printStackTrace(System.err);
                    }, () -> {
                        System.out.println("complete");
                    }
            );
        });
    }
    
    private ObjectNode createLinksMessage(List<String> links) {
        ObjectNode msg = nf.objectNode();
        msg.put("type", "new.links");
        ArrayNode jsonLinks = nf.arrayNode();
        jsonLinks.addAll(links.stream().map(nf::textNode).collect(Collectors.toList()));
        msg.set("links", jsonLinks);
        return msg;
    }

    public PublishSubject<String> getGoClicks() {
        return goClicks;
    }

    public PublishSubject<String> getQueryInputs() {
        return queryInputs;
    }
}
