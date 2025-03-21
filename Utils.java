package eus.ehu.mastoclient.utils;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;


/**
 * Collection of utility methods that does not belong in any other class.
 *
 * Created by Vuzi on 26/09/2015.
 */
public final class Utils {

    private Utils() {}

    /**
     * Open the provided URL in the web browser.
     *
     * @param url The URL (string) to open.
     */
    public static void openWebPage(String url) {
        try {
            openWebPage(new URL(url));
        } catch (MalformedURLException e) {
            // Logger.getLogger(Utils.class.getName()).info("Malformed provided URL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Open the provided URL in the web browser.
     *
     * @param url The URL to open.
     */
    public static void openWebPage(URL url) {
        try {
            openWebPage(url.toURI());
        } catch (URISyntaxException e) {
            // Logger.getLogger(Utils.class.getName()).info("Failed to get the URI from the provided URL : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Open the provided URI in the web browser.
     *
     * @param uri The URI to open.
     */
    public static void openWebPage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
               // Logger.getLogger(Utils.class.getName()).info("Failed to open the web browser : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
         //   Logger.getLogger(Utils.class.getName()).info("Desktop or browsing operation is not supported");
        }
    }

    /**
     * Map by value two observable list, usually a model list (as source) and a children list (as target). The mapper is here to provide
     * a mapping interface between the element type of the first list, and the elements of the second list.
     *
     * @param sourceList The source list.
     * @param targetList Tha target list.
     * @param mapper The mapper between the first list and the second.
     * @param <S> Type contained in the first list.
     * @param <T> Type contained in the second list.
     */
    public static <S, T> void mapByValue(ObservableList<S> sourceList, ObservableList<T> targetList, java.util.function.Function<S, T> mapper) {
        Objects.requireNonNull(sourceList);
        Objects.requireNonNull(targetList);
        Objects.requireNonNull(mapper);

        Map<S, T> sourceToTargetMap = new HashMap<>();

        targetList.clear();

        // Populate targetList by sourceList and mapper
        for (S s : sourceList) {
            T t = mapper.apply(s);
            targetList.add(t);
            sourceToTargetMap.put(s, t);
        }
        // Listen to changes in sourceList and update targetList accordingly
        ListChangeListener<S> sourceListener = c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        int j = c.getPermutation(i);
                        S s = sourceList.get(j);
                        T t = sourceToTargetMap.get(s);
                        targetList.set(i, t);
                    }
                } else {
                    for (S s : c.getRemoved()) {
                        T t = sourceToTargetMap.get(s);
                        targetList.remove(t);
                        sourceToTargetMap.remove(s);
                    }

                    int i = c.getFrom();

                    for (S s : c.getAddedSubList()) {
                        T t = mapper.apply(s);
                        targetList.add(i, t);
                        sourceToTargetMap.put(s, t);
                        i += 1;
                    }
                }
            }
        };
        sourceList.addListener(new WeakListChangeListener<>(sourceListener));
        // Store the listener in targetList to prevent GC
        // The listener should be active as long as targetList exists
        targetList.addListener((InvalidationListener) iv -> {
            Object[] refs = {sourceListener,};
            Objects.requireNonNull(refs);
        });
    }

    /**
     * Parse the provided toot, and return a list of all the elements contained.
     *
     * @param toot The toot to parse.
     * @return The toot parsed.
     */
    public static List<String> parseToot(String toot) {
        if(toot == null)
            toot = "";

        Source htmlSource = new Source(toot);
        Segment segment = new Segment(htmlSource, 0, htmlSource.length());
        Renderer htmlRender = new Renderer(segment).setIncludeHyperlinkURLs(false).setMaxLineLength(1000);
        toot = htmlRender.toString();
        System.out.println(toot);

        List<String> tweetParsed = new ArrayList<>();
        Extractor extractor = new Extractor();
        int index = 0;

        // Elements (ordered by position in the toot)
        List<Extractor.Entity> entities = new ArrayList<>();
        entities.addAll(extractor.extractURLsWithIndices(toot));
        entities.addAll(extractor.extractHashtagsWithIndices(toot));
        entities.addAll(extractor.extractMentionedScreennamesWithIndices(toot));

        Collections.sort(entities, (e1, e2) -> e1.getStart() - e2.getStart());

        // Cut the toot
        for(Extractor.Entity entity : entities) {
            tweetParsed.add(toot.substring(index, entity.getStart()));
            tweetParsed.add(toot.substring(entity.getStart(), entity.getEnd()));
            index = entity.getEnd();
        }

        // Last part
        if(index < toot.length())
            tweetParsed.add(toot.substring(index));

        return tweetParsed;
    }

    /**
     * Create and run an async task using the provided function as the asynchronous operation,
     * and the callback as the success operation. Error are ignored and returned as null values.
     *
     * @param asyncOperation The asynchronous operation.
     * @param callback The success callback.
     * @param <V> The type of value produced asynchronously and provided to the callback as a result.
     */
    public static <V> void asyncTask(ProducerWithThrow<V> asyncOperation, Consumer<V> callback) {

        CompletableFuture.supplyAsync(() -> {
            try {
                return asyncOperation.apply();
            } catch (Throwable throwable) {
                return null;
            }
        }).thenAcceptAsync(v -> {
            if(callback != null)
                Platform.runLater(() -> callback.apply(v));
        });
    }


    @FunctionalInterface
    public interface Function<T, R> {
        R apply(T t);
    }

    @FunctionalInterface
    public interface Consumer<T> {
        void apply(T t);
    }

    @FunctionalInterface
    public interface Producer<R> {
        R apply();
    }

    @FunctionalInterface
    public interface FunctionWithThrow<T, R> {
        R apply(T t) throws Throwable;
    }

    @FunctionalInterface
    public interface ConsumerWithThrow<T> {
        void apply(T t) throws Throwable;
    }

    @FunctionalInterface
    public interface ProducerWithThrow<R> {
        R apply() throws Throwable;
    }

    public static String request(String endpoint) {

        String result = "";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://mastodon.social/api/v1/" + endpoint)
                // .url("http://localhost:3000/api/v1/" + endpoint)
                .get()
                .addHeader("Authorization", "Bearer " + System.getenv("TOKEN"))
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                result = response.body().string();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

}


