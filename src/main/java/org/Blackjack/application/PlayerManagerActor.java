package org.Blackjack.application;

import org.Blackjack.application.command.IsUserExist;
import org.Blackjack.application.command.SavePlayer;
import org.Blackjack.domain.DomainException;
import org.Blackjack.domain.Player;
import org.Blackjack.domain.PlayerID;
import org.Blackjack.infrastructure.AbstractActor;
import org.Blackjack.infrastructure.Command;
import org.Blackjack.infrastructure.Response;
import org.Blackjack.infrastructure.SuccessResponse;

import java.util.*;

public class PlayerManagerActor extends AbstractActor {
    private final Map<PlayerID, Player> players = new HashMap<>();
    private final Map<String, PlayerID> usernameToId = new HashMap<>();
    @Override
    public Response onReceive(Command command) {
        return switch (command.message()) {
            case IsUserExist isUserExist -> isUserExist(isUserExist);
            case SavePlayer savePlayer -> savePlayer(savePlayer);
            default -> throw new IllegalArgumentException();
        };
    }

    private Response isUserExist(IsUserExist isUserExist) {
        if(!usernameToId.containsKey(isUserExist.username())){
            throw new ApplicationException("User not exist !");
        }
        Player player = players.get(usernameToId.get(isUserExist.username()));
        return new SuccessResponse<>(player);
    }

    private Response savePlayer(SavePlayer savePlayer) {
        if(usernameToId.containsKey(savePlayer.username())){
            throw new ApplicationException("User already exist !");
        }
        Player newPlayer = new Player(new PlayerID(), savePlayer.username(), false);
        players.put(newPlayer.id(), newPlayer);
        usernameToId.put(newPlayer.username(), newPlayer.id());
        return new SuccessResponse<>(newPlayer);
    }
}
