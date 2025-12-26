package org.Blackjack.application;

import org.Blackjack.application.command.CreateRoom;
import org.Blackjack.application.command.RegisterRoomToRegistry;
import org.Blackjack.application.response.RoomCreatedAndRegistered;
import org.Blackjack.application.response.RoomSessionCreatedResponse;
import org.Blackjack.infrastructure.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlackjackTableManagerActor extends AbstractActor {
    private final Map<RoomId, ActorRef> roomRegistry = new HashMap<>();

    @Override
    public Response onReceive(Command command) {
        return switch (command.message()) {
            case CreateRoom createRoom -> createRoom(command);
            case RegisterRoomToRegistry registerRoomToRegistryCommand -> registerRoomToRegistry(registerRoomToRegistryCommand);
            default -> throw new ApplicationException("Unexpected message : " + command.message());
        };
    }

    private Response registerRoomToRegistry(RegisterRoomToRegistry registerRoomToRegistryCommand) {
        roomRegistry.putIfAbsent(new RoomId(registerRoomToRegistryCommand.uuid()), registerRoomToRegistryCommand.actorRef());
        return new SuccessResponse<>(new RoomCreatedAndRegistered(registerRoomToRegistryCommand.uuid(), registerRoomToRegistryCommand.roomOwner(), registerRoomToRegistryCommand.state()));
    }

    private Response createRoom(Command<CreateRoom> command) {
        ActorRef blackjackRoom = context().fork(new BlackjackRoomActor());
        CompletableFuture<Response> responseFuture = blackjackRoom.send(new AsyncCommand<>(command.message()));

        selfEnqueueWithExternalFuture(responseFuture, command.future(),response -> {
            if (response instanceof SuccessResponse<?> success) {
                Object payload = success.message();

                if (payload instanceof RoomSessionCreatedResponse roomCreated) {
                    return new RegisterRoomToRegistry(roomCreated.uuid(), roomCreated.roomOwner(), roomCreated.state(), blackjackRoom);
                }
            }
            if (response instanceof ApplicationErrorResponse  error) {
                return new InternalErrorCommand(error.message());
            }
            if (response instanceof SystemErrorResponse error) {
                return new InternalErrorCommand(error.message());
            }
            return NullResponse.INSTANCE;
        });

        return NullResponse.INSTANCE;
    }
}
