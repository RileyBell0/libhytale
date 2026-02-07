package dev.twunk.utils;

import com.hypixel.hytale.logger.HytaleLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public abstract class Benchmark {
    @Nonnull
    private static final HashMap<Supplier<?>, ArrayList<Long>> TIMINGS = new HashMap<>();
    private static final HytaleLogger.Api console = HytaleLogger.forEnclosingClass().atInfo();

    // report every X times the function is called
    private static final int REPORTING_FREQUENCY = 30;
    private static final int SHORT_AVERAGE_TICKS = 6;

    public static final <T> T timeFuncExecution(@Nonnull Supplier<T> func) {
        // run
        var stats = getStats(func);
        var res = _run(func, stats);

        // log
        log(func, stats, REPORTING_FREQUENCY, false);
        return res;
    }

    public static final <T> T timeFuncExecutionWithFrequency(@Nonnull Supplier<T> func, int reportingFrequency) {
        // run
        var stats = getStats(func);
        var res = _run(func, stats);

        // log
        log(func, stats, reportingFrequency, false);
        return res;
    }

    public static final <T> T timeFuncExecutionWithFrequency(@Nonnull Supplier<T> func, int reportingFrequency,
            boolean logEveryTick) {
        // run
        var stats = getStats(func);
        var res = _run(func, stats);

        // log
        log(func, stats, reportingFrequency, logEveryTick);
        return res;
    }

    public static final <T> T timeFuncExecutionWithFrequency(@Nonnull Supplier<T> func, boolean logEveryTick) {
        // run
        var stats = getStats(func);
        var res = _run(func, stats);

        log(func, stats, REPORTING_FREQUENCY, logEveryTick);

        return res;
    }

    private static final void log(@Nonnull Supplier<?> func, @Nonnull ArrayList<Long> data, int reportingFrequency,
            boolean logEveryTick) {
        if (data.size() == 0) {
            return;
        }

        if (data.size() % reportingFrequency != 0) {
            return;
        }

        // compute long average
        var size = data.size();
        var total = 0;
        for (var i : data) {
            total += i;
        }
        double longAverage = total / (double) size;

        // compute short average
        Double shortAverage = null;
        if (size >= SHORT_AVERAGE_TICKS) {
            var shortData = data.subList(size - SHORT_AVERAGE_TICKS - 1, size);
            var shortTotal = 0;
            for (var i : shortData) {
                shortTotal += i;
            }
            shortAverage = shortTotal / (double) SHORT_AVERAGE_TICKS;
        }

        var res = String.format("%10s   |  Long average: %7s", func.toString().substring(0, 10),
                ((Double) longAverage).toString());

        if (shortAverage != null) {
            res = String.format("%s  |  Short average: %7s", res, shortAverage.toString());
        }

        if (logEveryTick) {
            res = String.format("%s  |  TICK: %7s", res, data.get(data.size() - 1));
        }

        console.log(res);
    }

    @Nonnull
    @SuppressWarnings("null")
    private static ArrayList<Long> getStats(@Nonnull Supplier<?> func) {
        return TIMINGS.getOrDefault(func, new ArrayList<Long>());
    }

    private static final <T> T _run(@Nonnull Supplier<T> func, @Nonnull ArrayList<Long> stats) {
        // start timer
        var start = System.nanoTime();

        // === USER FUNC ====
        var res = func.get();
        // ==================

        // end timer
        var end = System.nanoTime();
        var duration = end - start;

        // store stats
        stats.add(duration);

        return res;
    }
}
