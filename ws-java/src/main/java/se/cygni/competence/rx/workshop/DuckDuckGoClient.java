package se.cygni.competence.rx.workshop;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A client to <a href="https://duckduckgo.com/">https://duckduckgo.com/</a>
 */
public class DuckDuckGoClient {

    private HttpClient<ByteBuf, ByteBuf> client;

    public DuckDuckGoClient() {
        client = HttpClient.newClient("api.duckduckgo.com", 80);
    }

    /**
     * Searches for related topics to the given search term.
     * The request is performed on a background thread and the result is delivered
     * as a {@link Observable} which will emit the result once the request completes, or
     * emit an error if it fails.
     * @param searchTerm the search term to search for
     * @return a list of links, where each link is the DuckDuckGo URL for a related search term.
     */
    public Observable<List<URI>> searchRelated(String searchTerm) {
        final String relativeUrl = String.format("/?q=%s&format=json&pretty=1", Util.urlEncode(searchTerm));
        System.out.println("Running request:" + relativeUrl + " on " + Thread.currentThread().getName());
        final HttpClientRequest<ByteBuf, ByteBuf> req = client.createGet(relativeUrl);
        System.out.println("Created request");
        return req
                .flatMap(HttpClientResponse::getContent)
                .reduce("", (s, byteBuf) -> s+ byteBuf.toString(Charsets.UTF_8))
                .flatMap(all -> Observable.just(parseLinks(all)));
    }

    private static List<URI> parseLinks(String s) {
        final JsonNode j = Util.toJson(s);
        final JsonNode relatedTopics = j.get("RelatedTopics");

        final ArrayList<JsonNode> relatedTopicsList = Lists.newArrayList(relatedTopics);
        return relatedTopicsList.stream().filter(r -> r.has("FirstURL"))
                .map(r -> r.get("FirstURL").textValue())
                .map(DuckDuckGoClient::mkUri)
                .collect(Collectors.toList());
    }

    private static URI mkUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
