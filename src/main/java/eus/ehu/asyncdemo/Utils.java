package eus.ehu.asyncdemo;

import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;

/**
 * Collection of utility methods for async operations.
 */
public final class Utils {
    private Utils() {}

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
    public interface Consumer<T> {
        void apply(T t);
    }

    @FunctionalInterface
    public interface ProducerWithThrow<R> {
        R apply() throws Throwable;
    }
} 