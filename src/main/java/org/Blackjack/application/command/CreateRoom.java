package org.Blackjack.application.command;

import org.Blackjack.domain.Player;
import org.Blackjack.infrastructure.Command;
import org.Blackjack.infrastructure.Response;

import java.util.concurrent.CompletableFuture;

public record CreateRoom(Player player, CompletableFuture<Response> future) {}