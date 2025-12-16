package org.Blackjack.infrastructure;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class DefaultActorRefTest {
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
    private record UnKnownRequest(String message, CompletableFuture<Response> future)   implements Command{
        @Override
        public void complete(Response responseMessage) {
            future.complete(responseMessage);
        }

        @Override
        public CompletableFuture<Response> future() {
            return future;
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
        @Override
        public Response onReceive(Command message) {
            return switch (message) {
                case HelloRequest ignored -> new HelloResponse("Hello From Game Room");
                default -> throw new IllegalArgumentException("Unknown message request !");
            };

        }
    }

    @Test
    void shouldReturnCompletableFutureAndMessageResponse_WhenSendMessageToMailboxAndCompletableFutureComplete() throws ExecutionException, InterruptedException {
        var actorRef = ActorRef.createDefaultActorRef(
                new TestGameRoom(),
                new ConcurrentLinkedQueue<>(),
                new ThreadYieldWaitStrategy(),
                testGameRoomMessageProcessorCallbackWithYieldWaitStrategy
        );

        actorRef.start();

        CompletableFuture<Response> future = actorRef.send(new HelloRequest(null, new CompletableFuture<>()));

        var response = (HelloResponse) future.join();

        actorRef.stop();

        assertInstanceOf(CompletableFuture.class, future);
        assertEquals("Hello From Game Room", response.message());
    }
    @Test
    void shouldReturnErrorMessage_WhenUnknownGameMessage() {
        var actorRef = ActorRef.createDefaultActorRef(
                new TestGameRoom(),
                new ConcurrentLinkedQueue<>(),
                new ThreadYieldWaitStrategy(),
                testGameRoomMessageProcessorCallbackWithYieldWaitStrategy
        );

        actorRef.start();

        CompletableFuture<Response> future = actorRef.send(new UnKnownRequest(null, new CompletableFuture<>()));

        var response = (ErrorMessage) future.join();

        actorRef.stop();

        assertInstanceOf(CompletableFuture.class, future);
        assertInstanceOf(ErrorMessage.class, response);
        assertEquals("Unknown message request !", response.message());

    }
}