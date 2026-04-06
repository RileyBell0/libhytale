package dev.twunk.lib;

import com.hypixel.hytale.logger.HytaleLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public abstract class Benchmark {

    private static final HashMap<String, ArrayList<Long>> TIMINGS = new HashMap<>();

    @SuppressWarnings("null")
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    // report every X times the function is called
    private static final int REPORTING_FREQUENCY = 30;
    private static final int SHORT_AVERAGE_TICKS = 6;

    ////////////////////////
    ////////////////////////
    // TONS of overloads
    ////////////////////////
    ////////////////////////

    public static final <T> T timeFuncExecution(final String id, final Supplier<T> func) {
        return timeFuncExecutionWithFrequency(id, func, REPORTING_FREQUENCY, false);
    }

    public static final void timeFuncExecution(final String id, final Runnable func) {
        timeFuncExecutionWithFrequency(id, func, REPORTING_FREQUENCY, false);
    }

    public static final <T> T timeFuncExecutionWithFrequency(
        final String id,
        final Supplier<T> func,
        final int reportingFrequency
    ) {
        return timeFuncExecutionWithFrequency(id, func, reportingFrequency, false);
    }

    public static final void timeFuncExecutionWithFrequency(
        final String id,
        final Runnable func,
        final int reportingFrequency
    ) {
        timeFuncExecutionWithFrequency(id, func, reportingFrequency, false);
    }

    public static final <T> T timeFuncExecutionWithFrequency(
        final String id,
        final Supplier<T> func,
        final boolean logEveryTick
    ) {
        return timeFuncExecutionWithFrequency(id, func, REPORTING_FREQUENCY, logEveryTick);
    }

    public static final void timeFuncExecutionWithFrequency(
        final String id,
        final Runnable func,
        final boolean logEveryTick
    ) {
        timeFuncExecutionWithFrequency(id, func, REPORTING_FREQUENCY, logEveryTick);
    }

    ////////////////////////
    ////////////////////////
    // Actual code for timing func execution
    ////////////////////////
    ////////////////////////

    public static final <T> T timeFuncExecutionWithFrequency(
        final String id,
        final Supplier<T> func,
        final int reportingFrequency,
        final boolean logEveryTick
    ) {
        // run
        final var stats = getStats(id);
        final var res = _run(func, stats);
        TIMINGS.put(id, stats);

        // log
        log(id, stats, reportingFrequency, logEveryTick);
        return res;
    }

    public static final void timeFuncExecutionWithFrequency(
        final String id,
        final Runnable func,
        final int reportingFrequency,
        final boolean logEveryTick
    ) {
        // run
        final var stats = getStats(id);
        _run(func, stats);
        TIMINGS.put(id, stats);

        // log
        log(id, stats, reportingFrequency, logEveryTick);
    }

    private static final void log(
        final String id,
        final ArrayList<Long> stats,
        final int reportingFrequency,
        final boolean logEveryTick
    ) {
        if (stats.size() == 0) {
            return;
        }

        if (stats.size() % reportingFrequency != 0) {
            return;
        }

        // compute long average
        final var size = stats.size();
        var total = 0;
        for (var i : stats) {
            total += i;
        }
        final double longAverage = total / (double) size;

        // compute short average
        Double shortAverage = null;
        if (size >= SHORT_AVERAGE_TICKS) {
            final var shortData = stats.subList(size - SHORT_AVERAGE_TICKS - 1, size);
            var shortTotal = 0;
            for (var i : shortData) {
                shortTotal += i;
            }
            shortAverage = shortTotal / (double) SHORT_AVERAGE_TICKS;
        }

        var res = String.format("%10s   |  Long average: %7s", id, ((Double) longAverage).toString());

        if (shortAverage != null) {
            res = String.format("%s  |  Short average: %7s", res, shortAverage.toString());
        }

        if (logEveryTick) {
            res = String.format("%s  |  TICK: %7s", res, stats.get(stats.size() - 1));
        }

        console.log(res);
    }

    @SuppressWarnings("null")
    private static ArrayList<Long> getStats(String id) {
        return TIMINGS.getOrDefault(id, new ArrayList<Long>());
    }

    private static final <@Nonnull T> T _run(final Supplier<T> func, final ArrayList<Long> stats) {
        // start timer
        final var start = System.nanoTime();

        // === USER FUNC ====
        final var res = func.get();
        // ==================

        // end timer
        final var end = System.nanoTime();
        final var duration = end - start;

        // store stats
        stats.add(duration);

        return res;
    }

    private static final void _run(final Runnable func, final ArrayList<Long> stats) {
        // start timer
        final var start = System.nanoTime();

        // === USER FUNC ====
        func.run();
        // ==================

        // end timer
        final var end = System.nanoTime();
        final var duration = end - start;

        // store stats
        stats.add(duration);
    }
}
