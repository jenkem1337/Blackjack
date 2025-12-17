package org.Blackjack.infrastructure;

public interface Actor<C, R> {
    R onReceive(C command);
}
