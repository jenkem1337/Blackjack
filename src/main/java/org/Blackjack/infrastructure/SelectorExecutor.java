package org.Blackjack.infrastructure;

import java.nio.channels.Selector;
import java.util.Queue;

public class SelectorExecutor {
    private final Selector selector;
    private final Queue<Runnable> tasks;

    public SelectorExecutor(Selector selector, Queue<Runnable> tasks) {
        this.selector = selector;
        this.tasks = tasks;
    }

    public void offerTask(Runnable task) {
        tasks.offer(task);
        selector.wakeup();
    }

    public void executeTasks() {
        Runnable task;
        while ((task = tasks.poll()) != null) {
            task.run();
        }

    }
}
