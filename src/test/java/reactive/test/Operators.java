package reactive.test;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class Operators {


    private Flux<Object> emptyFlux() {
        return Flux.empty();
    }

    @Test
    public void deferOperator() throws Exception {
        Mono<Long> just = Mono.just(System.currentTimeMillis());
        Mono<Long> defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

        defer.subscribe(i -> System.out.println("time " + i));
        defer.subscribe(i -> System.out.println("time " + i));
        defer.subscribe(i -> System.out.println("time " + i));
        defer.subscribe(i -> System.out.println("time " + i));

        AtomicLong atomicLong = new AtomicLong();
        defer.subscribe(atomicLong::set);

        Assertions.assertTrue(atomicLong.get() > 0);
    }

    @Test
    public void switchEmptyOperator() {
        Flux<Object> flux = this.emptyFlux()
                .switchIfEmpty(Flux.just("Not empty anymore"))
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("Not empty anymore")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatOperator() {
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concat(flux1, flux2).log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatWithOperator() {
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = flux1.concatWith(flux2).log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }


    @Test
    public void combineLatestOperator() {
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux
                .combineLatest(flux1, flux2, (s1, s2) -> s1.toUpperCase() + s2.toUpperCase())
                .log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("BC", "BD")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeOperator() {
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> merge = Flux.merge(flux1, flux2).log();

        merge.subscribe(System.out::println);

        StepVerifier.create(merge)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeWithOperator() {
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> merge = flux1.mergeWith(flux2).log();

        merge.subscribe(System.out::println);

        StepVerifier.create(merge)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    public Flux<String> findByName(String name){
        return name.equals("A") ? Flux.just("nameA1", "nameA2") : Flux.just("nameB1", "nameB2");
    }

    @Test
    public void flatMapOperator() throws Exception{
        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(100));

        Flux<String> flux = flux1.map(String::toUpperCase)
                .flatMap(this::findByName)
                .log();

        flux.subscribe(System.out::println);

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("nameA1", "nameA2", "nameB1", "nameB2")
                .verifyComplete();
    }

    @AllArgsConstructor
    @Getter
    @ToString
    @EqualsAndHashCode
    class Anime {
        private String title;
        private String studio;
        private int ep;
    }

    @Test
    public void zipOperator() {
        Flux<String> title = Flux.just("Grand blue", "Baki");
        Flux<String> studio = Flux.just("Zero-G", "TMS entertainment");
        Flux<Integer> epFlux = Flux.just(12,24);

        Flux<Anime> animeFlux = Flux.zip(title, studio, epFlux)
                .flatMap(tuple -> Flux.just(new Anime(tuple.getT1(), tuple.getT2(), tuple.getT3())));

        animeFlux.subscribe(System.out::println);
        StepVerifier.create(animeFlux)
                .expectSubscription()
                .expectNext(new Anime("Grand blue","Zero-G", 12),
                        new Anime("Baki", "TMS entertainment", 24))
                .expectComplete()
                .verify();

    }

}
