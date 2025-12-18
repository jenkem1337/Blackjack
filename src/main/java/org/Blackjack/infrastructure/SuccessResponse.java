package org.Blackjack.infrastructure;

public record SuccessResponse<T>(T message) implements Response{
}
