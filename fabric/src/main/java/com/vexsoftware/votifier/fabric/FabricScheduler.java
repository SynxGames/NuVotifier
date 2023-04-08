package com.vexsoftware.votifier.fabric;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vexsoftware.votifier.platform.scheduler.ScheduledVotifierTask;
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FabricScheduler implements VotifierScheduler {

    private final ScheduledExecutorService executor;

    public FabricScheduler() {
        this.executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("Votifier Scheduler %d").build());
    }

    @Override
    public ScheduledVotifierTask sync(Runnable runnable) {
        return null; // Not Used by Code
    }

    @Override
    public ScheduledVotifierTask onPool(Runnable runnable) {
        return null; // Not Used by Code
    }

    @Override
    public ScheduledVotifierTask delayedSync(Runnable runnable, int delay, TimeUnit unit) {
        return null; // Not Used by Code
    }

    @Override
    public ScheduledVotifierTask delayedOnPool(Runnable runnable, int delay, TimeUnit unit) {
        return new TaskWrapper(this.executor.schedule(runnable, delay, unit));
    }

    @Override
    public ScheduledVotifierTask repeatOnPool(Runnable runnable, int delay, int repeat, TimeUnit unit) {
        return new TaskWrapper(this.executor.scheduleAtFixedRate(runnable, delay, repeat, unit));
    }

    private static class TaskWrapper implements ScheduledVotifierTask {

        private final Future<?> future;

        public TaskWrapper(Future<?> future) {
            this.future = future;
        }

        @Override
        public void cancel() {
            future.cancel(true);
        }

    }

}
