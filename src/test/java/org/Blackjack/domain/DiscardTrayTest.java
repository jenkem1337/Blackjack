package org.Blackjack.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscardTrayTest {
    @Test
    void shouldPutCardToDiscardTray() {
        var discardTray = DiscardTray.empty();
        discardTray.addUsedCard(Card.newInstanceWithFaceSide(Suit.CLUB, Rank.KING));
        discardTray.addUsedCard(Card.newInstanceWithFaceSide(Suit.CLUB, Rank.ACE));
        var usedCards = discardTray.usedCards();
        var firstCard = usedCards.pop();
        var secondCard = usedCards.pop();
        assertEquals(Rank.ACE, firstCard.rank());
        assertEquals(Rank.KING, secondCard.rank());
    }

}