package org.Blackjack.infrastructure;

public class ThreadYieldWaitStrategy implements WaitStrategy{
    @Override
    public void onWait() {
        Thread.yield();
    }
}
