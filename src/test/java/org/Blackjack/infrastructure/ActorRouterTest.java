package org.Blackjack.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ActorRouterTest {
    private record NullResponse() implements Response {
        public static NullResponse nullable() {return new NullResponse();}
    }
    private record BroadcastMessageSent(boolean isSent) implements Response{}
    private record HelloRequest(String message, CompletableFuture<Response> future) implements Command {
        @Override
        public void complete(Response responseMessage) {
            future.complete(responseMessage);
        }

        @Override
        public CompletableFuture<Response> future() {
            return future;
        }
    }
    private record ErrorMessage  (String message)   implements Response{ }
    private record HelloResponse (String message)   implements Response{ }
    private record BroadcastResponse(String message) implements Response{}
    private record BroadcastRequest(CompletableFuture<Response> future) implements Command{
        @Override
        public void complete(Response responseMessage) {
            future.complete(responseMessage);
        }
    }
    private record BroadcastMessage(Long senderId, CompletableFuture<Response> future) implements Command {
        @Override
        public void complete(Response responseMessage) {
            future.complete(responseMessage);
        }

    }
    private final MessageProcessorFunction testGameRoomMessageProcessorCallbackWithYieldWaitStrategy = (isRunning, room, waitStrategy, mailbox) -> () -> {
        while(isRunning) {
            Command msg = mailbox.poll();
            if(msg == null) {
                waitStrategy.onWait();
                continue;
            }
            try {
                Response response = room.onReceive(msg);
                msg.complete(response);
            } catch (RuntimeException e) {
                msg.complete(new ErrorMessage(e.getMessage()));
            }
        }
    };

    private class TestGameRoom extends AbstractActor<Long> {
        private final long id;
        private static long counter = 0;

        public TestGameRoom() {
            id = ++counter;
        }
        @Override
        public Response onReceive(Command message) {
            return switch (message) {
                case HelloRequest ignored -> new HelloResponse("Hello From Game Room");
                case BroadcastMessage broadcastMessage -> {
                    System.out.println("My id is " + id + " and took this broadcast message from " + broadcastMessage.senderId());
                    yield NullResponse.nullable();
                }
                case BroadcastRequest ignore -> {
                    router().broadcast("/actor/test/", new BroadcastMessage(id, new CompletableFuture<>()));
                    yield new BroadcastMessageSent(true);
                }
                default -> throw new IllegalArgumentException("Unknown message request !");
            };

        }
    }

    static ActorRouter router;
    @AfterEach
    void afterEach () {
        router.clear();
    }
    @BeforeAll
    static void setUp() {
        router = ActorRouter.instance();
    }
    @Test
    void registerActorRefToRouter() {
        router.registerActor("/actor/test/1", ActorRef.createDefaultActorRef(new TestGameRoom(), new ConcurrentLinkedQueue<>(), new ThreadYieldWaitStrategy(), testGameRoomMessageProcessorCallbackWithYieldWaitStrategy));
        router.registerActor("/actor/test/2", ActorRef.createDefaultActorRef(new TestGameRoom(), new ConcurrentLinkedQueue<>(),new ThreadYieldWaitStrategy(), testGameRoomMessageProcessorCallbackWithYieldWaitStrategy));
        router.registerActor("/actor/test/3", ActorRef.createDefaultActorRef(new TestGameRoom(), new ConcurrentLinkedQueue<>(),new ThreadYieldWaitStrategy(), testGameRoomMessageProcessorCallbackWithYieldWaitStrategy));
        var routes = router.router();
        String[] path$ = {"/actor/test/1","/actor/test/2","/actor/test/3"};
        AtomicInteger i = new AtomicInteger();
        routes.forEach((paths, ref) ->
                assertEquals(path$[i.getAndIncrement()], paths));
    }
    @Test
    void sendAMessageToActor() {
        router.registerActor("/actor/test/1", ActorRef.createDefaultActorRefAndStartImmediately(new TestGameRoom(), new ConcurrentLinkedQueue<>(),new ThreadYieldWaitStrategy(), testGameRoomMessageProcessorCallbackWithYieldWaitStrategy));

        var responseFuture = router.send("/actor/test/1", new HelloRequest("Hello Request Message", new CompletableFuture<>()));
        var response = (HelloResponse)responseFuture.join();
        assertEquals("Hello From Game Room", response.message());
    }
    @Test
    void sendBroadcastMessageToActors() {
        var actorRef = ActorRef.createDefaultActorRefAndStartImmediately(new TestGameRoom(), new ConcurrentLinkedQueue<>(),new ThreadYieldWaitStrategy(), testGameRoomMessageProcessorCallbackWithYieldWaitStrategy);
        router.registerActor("/actor/test/1", actorRef);
        router.registerActor("/actor/test/2", ActorRef.createDefaultActorRefAndStartImmediately(new TestGameRoom(), new ConcurrentLinkedQueue<>(),new ThreadYieldWaitStrategy(), testGameRoomMessageProcessorCallbackWithYieldWaitStrategy));
        router.registerActor("/actor/test/3", ActorRef.createDefaultActorRefAndStartImmediately(new TestGameRoom(), new ConcurrentLinkedQueue<>(),new ThreadYieldWaitStrategy(), testGameRoomMessageProcessorCallbackWithYieldWaitStrategy));
        var response = (BroadcastMessageSent) actorRef.send(new BroadcastRequest(new CompletableFuture<>())).join();
        assertTrue(response.isSent());
    }
}