package io.sha1n.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

class ReactorCache {
    private static final ParallelFlux<String> sources = Flux.merge(
                    Mono.just("Hello"),
                    Mono.just("World"),
                    Mono.just("!")
            ).cache(1)
            .parallel()
            .runOn(Schedulers.parallel());

    public static void main(String[] args) throws InterruptedException {
        sources.subscribe(message -> System.out.printf("[%s] Message: %s%n", Thread.currentThread().getName(), message));
        sources.subscribe(message -> System.out.printf("[%s] Message: %s%n", Thread.currentThread().getName(), message));
        sources.subscribe(message -> System.out.printf("[%s] Message: %s%n", Thread.currentThread().getName(), message));

        Thread.sleep(1000);
    }
}