package se.cygni.competence.rx.examples;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ManualCallbackTest {

    private ArrayBlockingQueue<String> result;

    @Before
    public void before() {
        result = new ArrayBlockingQueue<String>(1);
    }


    @Test
    public void test() throws InterruptedException {
        doA("", (s, ex) -> {
            if (ex != null) {
                giveUp();
            }
            doB(s, (s2, ex2) -> {
                if (ex2 != null) {
                    giveUp();
                }
                doC(s2, (s3, ex3) -> {
                    if (ex3 != null) {
                        giveUp();
                    }
                    done(s3);
                });
            });
        });
        assertEquals("abc", result.poll(1, TimeUnit.SECONDS));
    }

    private void done(String s3) {
        result.add(s3);
    }

    private void giveUp() {
    }

    private void doA(String param, BiConsumer<String, Exception> continuation) {
        doit(param, "a", continuation);
    }

    private void doB(String param, BiConsumer<String, Exception> continuation) {
        doit(param, "b", continuation);
    }

    private void doC(String param, BiConsumer<String, Exception> continuation) {
        doit(param, "c", continuation);
    }

    private void doit(String param, String suffix, BiConsumer<String, Exception> continuation) {
        continuation.accept(param + suffix, null);
    }

}
