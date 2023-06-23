package io.sha1n.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class OperatorsTest {

    @Test
    public void subscribeOnTest() {
        Flux<Integer> flux = Flux
                .range(1, 4)
                .log()
                // all operators of each subscriber execute on the same scheduler thread
                .subscribeOn(Schedulers.boundedElastic());

        StepVerifier
                .create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void publishOnTest() {
        Flux<Integer> flux = Flux
                .range(1, 4)
                .log()
                .map(i -> {
                    log.info("Main thread mapper");
                    return i * 2;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Scheduler thread mapper");
                    return i / 2;
                });

        StepVerifier
                .create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void concurrentSubscribersOnIOTest() {
        Mono<List<String>> lines = Mono
                .fromCallable(() -> Files.readAllLines(Path.of("src/test/resources/test-data.txt")))
                .subscribeOn(Schedulers.boundedElastic());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> t1Name = new AtomicReference<>();

        lines.subscribe(l  -> {
            l.forEach(line -> log.info("{}", line));

            String tName = Thread.currentThread().getName();
            t1Name.set(tName);
            log.info("T1 Name = {}", tName);

            try {
                // this ensures that the same thread is not used for both subscribers
                latch.await(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                log.error("Latch wait interrupted!", e);
            }
        });

        StepVerifier.create(lines)
                .expectSubscription()
                .thenConsumeWhile(l -> {
                    Assertions.assertFalse(l.isEmpty());

                    String tName = Thread.currentThread().getName();
                    Assertions.assertNotEquals(tName, t1Name.get());
                    log.info("T2 Name = {}", tName);

                    latch.countDown();

                    return true;
                })
                .verifyComplete();
    }
}
