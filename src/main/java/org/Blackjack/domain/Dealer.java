package org.Blackjack.domain;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class Dealer {

    private ActionPolicy actionPolicy = ActionPolicy.SOFT_17;

    public Deck shuffleDeck(Deck deck) {
        Deque<Card> _cards = deck.cards();
        List<Card> cards = new ArrayList<>(_cards);
        var random = new Random();
        for(int i = 0; i < cards.size(); i++) {

            int firstRandomIndex = random.nextInt(52);
            int secondRandomIndex = random.nextInt(52);

            Card firstCard = cards.get(firstRandomIndex);
            Card secondCard = cards.get(secondRandomIndex);

            cards.set(firstRandomIndex, secondCard);
            cards.set(secondRandomIndex, firstCard);
        }
        return Deck.withShuffledDeck(cards);
    }
}
