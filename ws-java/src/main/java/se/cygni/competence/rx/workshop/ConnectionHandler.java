package se.cygni.competence.rx.workshop;

import rx.Observable;
import rx.Observer;

import java.net.URI;
import java.util.List;

public interface ConnectionHandler {
    /**
     * Notifies the handler that a new connection was opened.
     * <p/>
     * You are provided with a number of input {@link Observable}s as
     * well as a number of output {@link Observer}s.
     * The different {@link Observable}s represent different <b>input</b> UI controls and will emit events as these change state.
     * The different {@link Observer}s represent <b>output</b> controls. Pushing to these will change output controls in the UI.
     * <p/>
     * Your task in this handler is to implement the business logic for the UI by wiring the input
     * {@link Observable}s through a Rx pipeline to to the output {@link Observer}s.
     * <p/>
     * The wiring which you perform in this method will have effect throughout the lifetime of the
     * connection. The {@link Server} keeps track of the {@link Observable}s and {@link Observer}s
     * for any given connection, and since they will become wired together through your pipeline,
     * the pipeline will persist.
     *
     * @param goClicks             emits an empty string whenever the "Go" button in the GUI is clicked.
     * @param queryInputs          emits query phrases from the search field. Will emit the complete query phrase whenever
     *                             it is changed in the GUI.
     * @param instantSearchChanges emits a boolean representing the checkbox "instant search" state whenever it changes.
     * @param enterPresses         emits an empty string whenever the enter key is pressed in the search field.
     * @param links                pushing a list of URL strings to this observer will replace the result list with the given links.
     * @param status               pushing a string to this observer will update the "backend status" field.
     */
    void onConnectionOpen(
            Observable<String> goClicks,
            Observable<String> queryInputs,
            Observable<Boolean> instantSearchChanges,
            Observable<String> enterPresses,
            Observer<List<URI>> links,
            Observer<String> status) throws Exception;
}
