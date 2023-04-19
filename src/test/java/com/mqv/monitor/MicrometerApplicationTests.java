package com.mqv.monitor;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MicrometerApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    public void givenGlobalRegistry_whenIncrementAnywhere_thenCounted() {
        final var counterName = "objects.instance"; // Naming convention with "." to separate each word

        class CountedObject {
            private CountedObject() {
                Metrics.counter(counterName).increment(1.0);
            }
        }

        Metrics.addRegistry(new SimpleMeterRegistry());
        Metrics.counter(counterName).increment();

        new CountedObject();

        var counterOptional = Optional.ofNullable(Metrics.globalRegistry
                .find(counterName).counter());

        assertTrue(counterOptional.isPresent());
        assertEquals(2.0, counterOptional.get().count());
    }

    @Test
    public void giveGlobalRegistry_whenGetTimerRecord_thenReturnResult() {
        var registry = new SimpleMeterRegistry();
        var timer = registry.timer("get.account");
        timer.record(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(15);
            } catch (InterruptedException ignored) {
            }
        });

        timer.record(30, TimeUnit.MILLISECONDS);

        assertEquals(2, timer.count());
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isBetween(40.0, 55.0);
    }

    @Test
    public void giveGlobalRegistry_whenGetLongTaskTimerRecord_thenReturnResult() {
        var registry = new SimpleMeterRegistry();
        var longTaskTimer = LongTaskTimer
                .builder("3rdPartyService")
                .register(registry);

        LongTaskTimer.Sample currentTaskId = longTaskTimer.start();
        try {
            TimeUnit.MILLISECONDS.sleep(2);
        } catch (InterruptedException ignored) {
        }

        long timeElapsed = currentTaskId.stop();

        assertEquals(2L, timeElapsed / ((int) 1e6), 1L);
    }

    @Test
    public void giveGlobalRegistry_whenGetGauge_thenReturnResult() {
        var registry = new SimpleMeterRegistry();
        var list = new ArrayList<>(4);

        var gauge = Gauge
                .builder("cache.size", list, List::size)
                .register(registry);

        assertEquals(0.0, gauge.value());

        list.add("1");

        assertEquals(1.0, gauge.value());
    }

    @Test
    public void giveGlobalRegistry_whenDistributionSummary_thenReturnResult() {
        var registry = new SimpleMeterRegistry();
        var distributionSummary = DistributionSummary
                .builder("request.size")
                .baseUnit("bytes")
                .register(registry);

        distributionSummary.record(3);
        distributionSummary.record(4);
        distributionSummary.record(5);

        assertEquals(3, distributionSummary.count());
        assertEquals(12, distributionSummary.totalAmount());
    }

    @Test
    public void testPercentile() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        Timer timer = Timer
                .builder("test.timer")
                .publishPercentiles(0.3, 0.5, 0.95)
                .publishPercentileHistogram()
                .register(registry);

        timer.record(2, TimeUnit.SECONDS);
        timer.record(2, TimeUnit.SECONDS);
        timer.record(3, TimeUnit.SECONDS);
        timer.record(4, TimeUnit.SECONDS);
        timer.record(8, TimeUnit.SECONDS);
        timer.record(13, TimeUnit.SECONDS);

        Map<Double, Double> actualMicrometer = new TreeMap<>();
        ValueAtPercentile[] percentiles = timer.takeSnapshot().percentileValues();
        for (ValueAtPercentile percentile : percentiles) {
            actualMicrometer.put(percentile.percentile(), percentile.value(TimeUnit.MILLISECONDS));
        }

        Map<Double, Double> expectedMicrometer = new TreeMap<>();
        expectedMicrometer.put(0.3, 1946.157056);
        expectedMicrometer.put(0.5, 3019.89888);
        expectedMicrometer.put(0.95, 13354.663936);

        assertEquals(expectedMicrometer, actualMicrometer);
    }

    @Test
    public void testServiceLevelObject() {
        var registry = new SimpleMeterRegistry();
        var hist = DistributionSummary
                .builder("summary")
                .serviceLevelObjectives(1, 10, 5)
                .register(registry);

        Map<Integer, Double> actualMicrometer = new TreeMap<>();
        HistogramSnapshot snapshot = hist.takeSnapshot();
        Arrays.stream(snapshot.histogramCounts()).forEach(p -> {
            actualMicrometer.put(((int) p.bucket()), p.count());
        });

        Map<Integer, Double> expectedMicrometer = new TreeMap<>();
        expectedMicrometer.put(1,0D);
        expectedMicrometer.put(10,2D);
        expectedMicrometer.put(5,1D);

        assertEquals(expectedMicrometer, actualMicrometer);

        Duration[] durations = {Duration.ofMillis(25), Duration.ofMillis(300), Duration.ofMillis(600)};
        Timer timer = Timer
                .builder("timer")
                .sla(durations)
                .publishPercentileHistogram()
                .register(registry);
    }


    @Test
    public void test() {
        long stepMillis = 10;

        var clock = Clock.SYSTEM;

        System.out.println(clock.wallTime());

        var initialMillis = stepMillis - clock.wallTime() % stepMillis + 1L;

        System.out.println(initialMillis);
    }
}
