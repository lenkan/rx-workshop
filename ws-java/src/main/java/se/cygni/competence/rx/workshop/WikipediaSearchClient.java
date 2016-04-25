package se.cygni.competence.rx.workshop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.functions.Action1;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A client to wikipedia search
 * https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=Albert%20Einstein&utf8=&format=json
 */
public class WikipediaSearchClient {

    private HttpClient<ByteBuf, ByteBuf> client;

    public WikipediaSearchClient() {
        client = RxNetty.<ByteBuf, ByteBuf>newHttpClientBuilder("en.wikipedia.org", 443).config(RxClient.ClientConfig
                .Builder.newDefaultConfig()).build();
    }

    public Observable<List<String>> searchRelated(String searchTerm) {
        final String relativeUrl = String.format("/w/api.php?action=query&generator=search&gsrsearch=%s&format=json&gsrprop=snippet&prop=info&inprop=url", Util.urlEncode(searchTerm));
        System.out.println("Running request:" + relativeUrl + " on " + Thread.currentThread().getName());
        HttpClientRequest<ByteBuf> req = HttpClientRequest.createGet(relativeUrl);
        System.out.println("Created request");
        return client.submit(req)
                .flatMap(HttpClientResponse::getContent)
                .reduce("", (s, byteBuf) -> s+ byteBuf.toString(Charsets.UTF_8))
                .flatMap(all -> Observable.just(parseLinks(all)))
                .doOnError(Throwable::printStackTrace);
    }

    private static List<String> parseLinks(String s) {
        final JsonNode root = Util.toJson(s);

        if (root.has("query")) {
            JsonNode query = root.get("query");
            if (query.has("pages")) {
                JsonNode pages = query.get("pages");
                Iterable<JsonNode> it = pages::elements;
                return StreamSupport.stream(it.spliterator(), false)
                        .map(n -> n.get("canonicalurl").textValue())
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
