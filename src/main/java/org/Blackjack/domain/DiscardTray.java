package org.Blackjack.domain;

import java.util.ArrayDeque;
import java.util.Deque;

public class DiscardTray {
    private Deque<Card> cards;
    private DiscardTray(){
        this.cards = new ArrayDeque<>();
    }
    public static DiscardTray empty() {
        return new DiscardTray();
    }
    public boolean addUsedCard(Card usedCard){
        return cards.offerFirst(usedCard);
    }
    public Deque<Card> usedCards(){
        return cards;
    }
}
