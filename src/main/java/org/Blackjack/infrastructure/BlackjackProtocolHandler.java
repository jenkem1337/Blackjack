package org.Blackjack.infrastructure;

import org.Blackjack.application.command.CreateRoom;
import org.Blackjack.application.command.IsUserExist;
import org.Blackjack.application.command.SavePlayer;
import org.Blackjack.application.response.RoomCreatedAndRegistered;
import org.Blackjack.domain.Player;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.CompletableFuture;

public class BlackjackProtocolHandler implements ProtocolHandler{
    private final ActorRef managerRef;
    private final ActorRef playerManagerRef;

    public BlackjackProtocolHandler(ActorRef managerRef, ActorRef playerManagerRef) {
        this.managerRef = managerRef;
        this.playerManagerRef = playerManagerRef;
    }

    @Override
    public void onData(ClientContext ctx, String message) {

        if (message.startsWith("REGISTER_PLAYER")) {
            registerPlayer(ctx, message.split(":")[1]);
            return;
        }

        if (message.startsWith("CREATE_ROOM")) {
            createRoom(ctx, message.split(":")[1]);
            return;
        }

        ctx.write(ByteBuffer.wrap("UNKNOWN ROUTE\n".getBytes()));
    }
    private void registerPlayer(ClientContext ctx, String username) {
        playerManagerRef
                .send(new AsyncCommand<>(new SavePlayer(username)))
                .whenComplete((response, throwable) -> {

                    if (throwable != null) {
                        ctx.write(
                                ByteBuffer.wrap(("ERROR:" + throwable.getMessage() + "\n").getBytes())
                        );
                        return;
                    }

                    if (response instanceof SuccessResponse<?> success) {
                        Player player = (Player) success.message();
                        String msg =
                                "USER_CREATED:PLAYER_ID=" + player.id().id() +
                                        ":USERNAME=" + player.username() + "\n";
                        ctx.write(ByteBuffer.wrap(msg.getBytes()));
                        return;
                    }

                    if (response instanceof ApplicationErrorResponse err) {
                        ctx.write(
                                ByteBuffer.wrap(("ERROR_RESPONSE:" + err.message() + "\n").getBytes())
                        );
                    }
                });
    }

    private void createRoom(ClientContext ctx, String username) {
        playerManagerRef
                .send(new AsyncCommand<>(new IsUserExist(username)))
                .thenCompose(resp -> {
                    if (resp instanceof SuccessResponse<?> success) {
                        Player p = (Player) success.message();
                        return managerRef.send(new AsyncCommand<>(new CreateRoom(p)));
                    }
                    ctx.write(ByteBuffer.wrap("USER NOT FOUND\n".getBytes()));
                    return CompletableFuture.completedFuture(null);
                })
                .thenAccept(resp -> {
                    if (resp == null) return;

                    if (resp instanceof SuccessResponse<?> success) {
                        RoomCreatedAndRegistered r =
                                (RoomCreatedAndRegistered) success.message();

                        String msg =
                                "ROOM CREATED:ROOM_ID=" + r.uuid() +
                                        ":PLAYER_ID=" + r.playerID().id() + "\n";

                        ctx.write(ByteBuffer.wrap(msg.getBytes()));
                    } else {
                        ctx.write(ByteBuffer.wrap("ERROR\n".getBytes()));
                    }
                });
    }

}
