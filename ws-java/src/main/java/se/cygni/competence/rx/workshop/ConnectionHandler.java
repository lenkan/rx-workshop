package se.cygni.competence.rx.workshop;

import rx.Observable;
import rx.Observer;

import java.net.URI;
import java.util.List;

/**
 * Created by alext on 2016-02-16.
 */
public interface ConnectionHandler {
    void onConnectionOpen(
            Observable<String> goClicks,
            Observable<String> queryInputs,
            Observable<Boolean> instantSearchChanges,
            Observable<String> enterPresses,
            Observer<List<URI>> links,
            Observer<String> status);
}
