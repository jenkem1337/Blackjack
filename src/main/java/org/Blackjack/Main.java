package org.Blackjack;


import org.Blackjack.infrastructure.BlackjackProtocolHandler;
import org.Blackjack.infrastructure.NonBlockingTCPServer;
import org.Blackjack.infrastructure.PlainTextTransport;

public class Main {
    public static void main(String[] args) {
        var tcp = new NonBlockingTCPServer(new PlainTextTransport(), new BlackjackProtocolHandler(),9090);
        tcp.start();
    }
}