package se.cygni.competence.rx.workshop;

import rx.Observable;
import rx.Observer;
import rx.observables.ConnectableObservable;

import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FacitHandler implements ConnectionHandler {

    private final DuckDuckGoClient duckDuckGoClient;

    public FacitHandler(DuckDuckGoClient duckDuckGoClient) {
        this.duckDuckGoClient = duckDuckGoClient;
    }

    /**
     * {@inheritDoc}
     */
    public void onConnectionOpen(
            Observable<String> goClicks,
            Observable<String> queryInputs,
            Observable<Boolean> instantSearchChanges,
            Observable<String> enterPresses,
            Observer<List<String>> links,
            Observer<String> status) {
        Observable<String> textOnGoClick = queryInputs.sample(goClicks);
        final Observable<String> textOnEnterPress = queryInputs.sample(enterPresses);
        final Observable<String> textOnTypeWhenInstantEnabled = Observable.combineLatest(queryInputs,
                instantSearchChanges, (phrase, checked) -> checked ? phrase : "").filter(p -> !p.isEmpty());
        textOnTypeWhenInstantEnabled.map(o -> "listening").subscribe(status);
        Observable<String> debouncedTextOnTypeWhenInstantEnabled = textOnTypeWhenInstantEnabled.debounce(1, TimeUnit.SECONDS);
        Observable<String> textOnAction = textOnGoClick.mergeWith(textOnEnterPress).mergeWith(debouncedTextOnTypeWhenInstantEnabled);
        textOnAction.map(o -> "searching").subscribe(status);
        textOnAction.subscribe(s -> {
            System.out.println("About to search");
        });
        final Observable<List<String>> requests = textOnAction.flatMap((searchTerm) -> {
            Observable<List<String>> dResultLists = duckDuckGoClient.searchRelated(searchTerm);
            Observable<List<String>> wResultLists = new WikipediaSearchClient().searchRelated(searchTerm);
            return dResultLists.zipWith(wResultLists, this::interleave);
        });
        //Don't want two subscribers on requests since it triggers double requests being issued
        final ConnectableObservable<List<String>> doneRequests = requests.publish();
        doneRequests.zipWith(textOnAction, (o, text) -> "search for '" + text + "' done").subscribe(status);
        doneRequests.subscribe(links);
        doneRequests.connect();
        status.onNext("ready");
    }

    private <T> ArrayList<T> interleave(List<? extends T> as, List<? extends T> bs) {
        final ArrayList<T> rs = new ArrayList<>();
        for(Iterator<? extends T> ia = as.iterator(), ib = bs.iterator(); ia.hasNext() || ib.hasNext();) {
            if (ia.hasNext()) {
                rs.add(ia.next());
            }
            if (ib.hasNext()) {
                rs.add(ib.next());
            }
        }
        return rs;
    }

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        Server.startAndServe(new FacitHandler(new DuckDuckGoClient()));
    }
}
