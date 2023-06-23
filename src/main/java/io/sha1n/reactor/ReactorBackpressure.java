package io.sha1n.reactor;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;


class ReactorBackpressure {

    public static void main(String[] args) throws InterruptedException {
        final var subscriber1 = new BackpressureSubscriber(1);
        final var subscriber2 = new BackpressureSubscriber(10);

        var flux = Flux.range(1, 20)
                .log()
                .cache()
                .parallel(2, 10)
                .runOn(Schedulers.parallel());

        flux.subscribe(subscriber1);
        flux.subscribe(subscriber2);

        Thread.sleep(1000);
    }

    static class BackpressureSubscriber extends BaseSubscriber<Integer> {
        private final int requestAmount;
        private int handledCount = 0;

        public BackpressureSubscriber(int requestAmount) {
            this.requestAmount = requestAmount;
        }

        @Override
        public void hookOnSubscribe(Subscription subscription) {
            request(requestAmount);
        }

        @Override
        public void hookOnNext(Integer integer) {
            handledCount++;
            if (handledCount % requestAmount == 0) {
                request(requestAmount);
            }
        }

        @Override
        public void hookOnComplete() {
            System.out.printf("[%d] Completed %d%n", requestAmount, handledCount);
        }
    }
}