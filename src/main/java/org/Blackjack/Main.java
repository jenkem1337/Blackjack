package org.Blackjack;

import org.Blackjack.infrastructure.NonBlockingTCPServer;

public class Main {
    public static void main(String[] args) {
        var tcp = new NonBlockingTCPServer(9090);
        tcp.start();
    }
}