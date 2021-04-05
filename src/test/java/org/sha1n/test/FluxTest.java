package org.sha1n.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class FluxTest {

    @Test
    public void fluxSubscriberTest() {
        String firstName = "Jimi", lastName = "Hendrix";
        Flux<String> flux = Flux
                .just(firstName, lastName)
                .log();

        StepVerifier
                .create(flux)
                .expectNext(firstName, lastName)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberRangeTest() {
        Flux<Integer> flux = Flux
                .range(1, 3)
                .log();

        flux.subscribe(i -> log.info("i = {}", i));

        StepVerifier
                .create(flux)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberFromIterableTest() {
        Flux<Integer> flux = Flux
                .fromIterable(List.of(1, 2, 3))
                .log();

        flux.subscribe(i -> log.info("i = {}", i));

        StepVerifier
                .create(flux)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberWithBackpressureTest() {
        Flux<Integer> flux = Flux
                .range(1, 10)
                .log();

        flux.subscribe(new BaseSubscriber<>() {
            private final AtomicInteger count = new AtomicInteger(0);
            private final int REQUEST_COUNT = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(REQUEST_COUNT);
            }

            @Override
            protected void hookOnNext(Integer value) {
                log.info("Got: {}", value);

                count.incrementAndGet();
                if (count.compareAndSet(REQUEST_COUNT, 0)) {
                    super.request(REQUEST_COUNT);
                }

            }
        });

        StepVerifier
                .create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }


    @Test
    public void fluxSubscriberIntervalTest() {
        StepVerifier
                .withVirtualTime(this::createHourlyIntervalFlux)
                .expectSubscription()
                .expectNoEvent(Duration.ofHours(1))
                .thenAwait(Duration.ofHours(1))
                .expectNext(0L)
                .thenAwait(Duration.ofHours(1))
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    private Flux<Long> createHourlyIntervalFlux() {
        return Flux
                .interval(Duration.ofHours(1))
                .log();
    }
}
