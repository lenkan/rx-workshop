package se.cygni.wrk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.AbstractHttpContentHolder;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.Observer;
import rx.functions.FuncN;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Handler {
    private final PublishSubject<String> goClicks;
    private final PublishSubject<String> queryInputs;
    private final JsonNodeFactory nf;
    private final PublishSubject<JsonNode> messages;
    private final PublishSubject<Boolean> instantSearchChanges;
    private final PublishSubject<String> enterPresses;

    public Handler(PublishSubject<JsonNode> messages) {
        nf = JsonNodeFactory.instance;
        this.messages = messages;
        goClicks = PublishSubject.create();
        queryInputs = PublishSubject.create();
        instantSearchChanges = PublishSubject.create();
        enterPresses = PublishSubject.create();
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

    public Observer<Boolean> getInstantSearchChanges() {
        return instantSearchChanges;
    }

    public Observer<String> getEnterPresses() {
        return enterPresses;
    }
}
