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
 * A client to wikipedia search
 * https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=Albert%20Einstein&utf8=&format=json
 */
public class WikipediaSearchClient {

    private HttpClient<ByteBuf, ByteBuf> client;

    public WikipediaSearchClient() {
        client = HttpClient.newClient("en.wikipedia.org", 443);
    }

    public Observable<List<URI>> searchRelated(String searchTerm) {
        final String relativeUrl = String.format("/w/api.php?action=query&generator=search&gsrsearch=%s&format=json&gsrprop=snippet&prop=info&inprop=url", Util.urlEncode(searchTerm));
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
                .map(WikipediaSearchClient::mkUri)
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
