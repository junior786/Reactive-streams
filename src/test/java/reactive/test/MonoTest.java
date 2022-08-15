package reactive.test;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;


public class MonoTest {

    @Test
    public void monoSubscribe() {
        String name = "Ricardo";
        Mono<String> mono = Mono.just(name);
        mono.subscribe();
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();

        System.out.println("Mono " + mono);
    }

    @Test
    public void monoSubscribeConsumer() {
        String name = "Ricardo";
        Mono<String> mono = Mono.just(name);

        mono.subscribe(System.out::println);

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();

        System.out.println("Mono " + mono);
    }

    @Test
    public void monoSubscribeConsumerError() {
        String name = "Ricardo";
        Mono<String> mono = Mono.just(name)
                .map(value -> {
                    throw new RuntimeException("TESTE ERROR");
                });

        mono.subscribe(System.out::println, System.out::println);

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();

        System.out.println("Mono " + mono);
    }

    @Test
    public void monoSubscribeConsumerComplete() {
        String name = "Ricardo";
        Mono<String> mono = Mono.just(name)
                .map(s -> s.toUpperCase(Locale.ROOT));

        mono.subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("FINISH"));

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

        System.out.println("Mono " + mono);
    }

    @Test
    public void monoSubscribeConsumerSubscription() {
        String name = "Ricardo";
        Mono<String> mono = Mono.just(name)
                .map(s -> s.toUpperCase(Locale.ROOT));

        mono.subscribe(System.out::println, Throwable::printStackTrace,
                () -> System.out.println("FINISH"),
                Subscription::cancel);
        System.out.println("-----------------");
       /*
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
*/
        System.out.println("Mono " + mono);
    }

    @Test
    public void monoDoOnMethods() {
        String name = "Ricardo";
        Mono<Object> mono = Mono.just(name)
                .map(s -> s.toUpperCase(Locale.ROOT))
                .doOnSubscribe(subscription -> System.out.println("Subscribe {} "))
                .doOnRequest(longNumber -> System.out.println("Request received, starting doing something... "))
                .doOnNext(s -> System.out.println("VLUE IS HERE doOnNext " + s))
                .flatMap(s -> Mono.empty())
                .doOnSuccess(s -> System.out.println("DoOnSuccess executed " + s));

        //mono.subscribe(System.out::println, Throwable::printStackTrace, () -> System.out.println("FINISH"));

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();

       // System.out.println("Mono " + mono);
    }

    @Test
    public void monoDoOnError() {
        Mono<Object> log = Mono.error(new IllegalArgumentException())
                .doOnError(e -> System.out.println("Error message " + e.getMessage()))
                .doOnNext(s -> System.out.println("executing this doOnNext " + s))
                .log();
        StepVerifier.create(log)
                .expectError(IllegalArgumentException.class)
                .verify();

    }

    @Test
    public void monoDoOnErrorResume() {
        String name = "Ricardo";
        Mono<Object> log = Mono.error(new IllegalArgumentException())
                .doOnError(e -> System.out.println("Error message " + e.getMessage()))
                .onErrorResume(s -> {
                    System.out.println(s);
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(log)
                .expectNext(name)
                .verifyComplete();

    }

    @Test
    public void monoDoOnErrorReturn() {
        String name = "Ricardo";
        Mono<Object> log = Mono.error(new IllegalArgumentException())
                .doOnError(e -> System.out.println("Error message " + e.getMessage()))
                .onErrorResume(s -> {
                    System.out.println(s);
                    return Mono.just(name);
                })
                .onErrorReturn("EMPTY")
                .log();

        StepVerifier.create(log)
                .expectNext(name)
                .verifyComplete();

    }
}
