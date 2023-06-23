package io.sha1n.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import io.sha1n.domain.DomainException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Consumer;

@Slf4j
public class MonoTest {

    @Test
    public void monoSubscriberTest() {
        String name = "Jimi Hendrix";
        Mono<String> mono = Mono
                .just(name);

        StepVerifier
                .create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerTest() {
        String name = "Jimi Hendrix";
        Mono<String> mono = Mono
                .just(name)
                .log();

        mono.subscribe(log::info);

        StepVerifier
                .create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerErrorTest() {
        String name = "Jimi Hendrix";
        Mono<String> mono = Mono
                .just(name)
                .map(n -> {
                    throw new DomainException(("Oh no... not an error..."));
                });

        mono.subscribe(
                log::info,
                t -> log.error("I'm on it!", t)
        );

        StepVerifier
                .create(mono)
                .expectError(DomainException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerCompleteTest() {
        String name = "Jimi Hendrix";
        Mono<String> mono = Mono
                .just(name);

        mono.subscribe(
                log::info,
                t -> log.error("This is not expected...", t),
                () -> log.info("Done!")
        );

        StepVerifier
                .create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscriptionTest() {
        String name = "Jimi Hendrix";
        Mono<String> mono = Mono
                .just(name);

        mono.subscribe(
                log::info,
                t -> log.error("This is not expected...", t),
                () -> log.info("Done!"),
                Subscription::cancel
        );

        StepVerifier
                .create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethodsTest() {
        String name = "Jimi Hendrix";
        Consumer<String> printOnNext = str -> log.info("onNext: {}", str);
        Mono<String> mono = Mono
                .just(name)
                .doOnSubscribe(s -> log.info("onSubscribe {}", s.hashCode()))
                .doOnRequest(longN -> log.info("onRequest: {}", longN))
                .doOnNext(printOnNext)
                .map(String::toUpperCase)
                .doOnNext(printOnNext)
                .doOnSuccess(str -> log.info("onSuccess: {}", str));


        mono.subscribe(
                log::info,
                t -> log.error("This is not expected...", t),
                () -> log.info("Done!"),
                Subscription::cancel
        );

        StepVerifier
                .create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorTest() {
        Mono<Object> mono = Mono.error(new DomainException("Oh no!"))
                .doOnError(e -> log.error("Error: {}", e.getMessage()));


        StepVerifier
                .create(mono)
                .expectError(DomainException.class)
                .verify();
    }


    @Test
    public void monoDoOnErrorResumeTest() {
        Mono<Object> mono = Mono.error(new DomainException("Oh no! Something bad has happened..."))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorResume(
                        DomainException.class,
                        e -> {
                            log.info("{} but, we're fine!", e.getMessage());

                            return Mono.just("OK!");
                        }
                );


        StepVerifier
                .create(mono)
                .expectNext("OK!")
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturnTest() {
        Mono<Object> mono = Mono.error(new DomainException("Oh no! Something bad has happened..."))
                .doOnError(e -> log.error("Error: {}", e.getMessage()))
                .onErrorReturn("OK!");


        StepVerifier
                .create(mono)
                .expectNext("OK!")
                .verifyComplete();
    }
}
