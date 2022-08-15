package reactive.test;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

public class FluxTest {

    @Test
    public void fluxSubscriber() {
        Flux<String> flux = Flux.just("Ricardo", "Junior", "Maria")
                .log();

        StepVerifier.create(flux)
                .expectNext("Ricardo", "Junior", "Maria")
                .verifyComplete();
    }

    @Test
    public void fluxSubscribeNumbers() {
        Flux<Integer> fluxInteger = Flux.range(1, 3)
                .log();

        StepVerifier.create(fluxInteger)
                .expectNext(1, 2, 3)
                .verifyComplete();

    }

    @Test
    public void fluxSubscribeFromList() {
        Flux<Integer> fluxInteger = Flux.fromIterable(List.of(11, 2, 5, 4))
                .log();
        fluxInteger.subscribe(System.out::println);

        StepVerifier.create(fluxInteger)
                .expectNext(11, 2, 5, 4)
                .verifyComplete();

    }

    @Test
    public void fluxSubscribeNumbersError() {
        Flux<Integer> fluxInteger = Flux.range(1, 5)
                .log()
                .map(i -> {
                    if (i == 4) {
                        throw new IndexOutOfBoundsException("index error");
                    }
                    return i;
                });
        fluxInteger.subscribe(System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("DONE !"),
                subscription -> subscription.request(3)
        );

        StepVerifier.create(fluxInteger)
                .expectNext(1, 2, 3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscribeNumbersUglyBackpressure() {
        Flux<Integer> fluxInteger = Flux.range(1, 10)
                .log();

        fluxInteger.subscribe(new Subscriber<Integer>() {
            private int count = 0;
            private int requestCount = 2;
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(2);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        StepVerifier.create(fluxInteger)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscribeNumbersNotSoUglyBackpressure() {
        Flux<Integer> fluxInteger = Flux.range(1, 10)
                .log();

        fluxInteger.subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private final int requestCount = 2;


            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });

        StepVerifier.create(fluxInteger)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void flexSubscriberIntervalOne() throws Exception {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .log();

        interval.subscribe(i -> System.out.println("Number, " + i));
        Thread.sleep(3000);
    }

    @Test
    public void flexSubscriberIntervalTwoo() throws Exception {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .log();

      //  StepVerifier.withVirtualTime(new Flux<Long>.interval(Duration.ofDays(1)));
    }
}
