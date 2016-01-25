package se.cygni.wrk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import rx.Observer;
import rx.subjects.PublishSubject;

import java.util.List;
import java.util.stream.Collectors;

public class Handler {
    private final PublishSubject<String> goClicks;
    private final PublishSubject<String> queryInputs;
    private final PublishSubject<JsonNode> messages;
    private final PublishSubject<Boolean> instantSearchChanges;
    private final PublishSubject<String> enterPresses;

    public Handler(PublishSubject<JsonNode> messages) {
        this.messages = messages;
        goClicks = PublishSubject.create();
        queryInputs = PublishSubject.create();
        instantSearchChanges = PublishSubject.create();
        enterPresses = PublishSubject.create();
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
