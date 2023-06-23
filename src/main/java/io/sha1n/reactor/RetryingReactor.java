package io.sha1n.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

class RetryingReactor {
    public static void main(String[] args) throws InterruptedException {
        var reactor = new RetryingReactor();
        var sources = reactor.retrieveMessageParts();

        Flux.merge(sources)
                .map(s -> Optional.ofNullable(s).orElse("").trim())
                .filter(s -> !s.isEmpty())
                .reduce((s1, s2) -> s1 + " " + s2)
                .flatMap(reactor::saveMessage)
                .retryWhen(Retry
                        .backoff(10, Duration.ofMillis(1))
                        .maxBackoff(Duration.ofSeconds(1))
                )
                .doOnError(e -> System.err.println("Failed to save message: " + e.getMessage()))
                .subscribe();

        Thread.sleep(1000 * 10);
    }

    private Mono<String> saveMessage(String message) {
        return Mono.fromSupplier(() -> {
            System.out.printf("Attempting to save message: %s (threads=%d) %n", message, ManagementFactory.getThreadMXBean().getThreadCount());
            throw new RuntimeException("Failed to save message");
        });
    }

    private List<Mono<String>> retrieveMessageParts() {
        return List.of(
                Mono.just("Hello"),
                Mono.just("World"),
                Mono.just("!"),
                Mono.just("Goodbye World...")
                        .flatMap(m -> Mono.<String>fromSupplier(() -> {
                            throw new RuntimeException("Failed to save message");
                        }))
                        .onErrorReturn("")
        );
    }
}