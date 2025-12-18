package org.Blackjack.infrastructure;

public record ApplicationErrorResponse(
        String message
) implements Response{
}
