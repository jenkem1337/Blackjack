package org.Blackjack;


import org.Blackjack.application.BlackjackTableManagerActor;
import org.Blackjack.application.PlayerManagerActor;
import org.Blackjack.infrastructure.*;

public class Main {
    public static void main(String[] args) {
//        var tcp = new NonBlockingTCPServer(new PlainTextTransport(), new BlackjackProtocolHandler(),9090);
//        tcp.start();
        var actorSystem = new ActorSystem();
        var blackjackTableManagerActor = actorSystem.fork(new BlackjackTableManagerActor());
        var playerManagerActor = actorSystem.fork(new PlayerManagerActor());

        var tcp = actorSystem.fork(new TCPEventLoopActor(new PlainTextTransport(), new BlackjackProtocolHandler(blackjackTableManagerActor, playerManagerActor)));
        tcp.send(new AsyncCommand<>(new IOEvent.Start(9090))).join();
    }
}