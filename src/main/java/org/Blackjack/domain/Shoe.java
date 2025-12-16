package org.Blackjack.domain;

import java.util.ArrayDeque;
import java.util.Deque;

public class Shoe {
    private Deck deck;
    private Shoe(Deck deck){
        this.deck = deck;
    }
    private Shoe(){}

    public static Shoe createWithUnShuffledDeck() {
        return new Shoe(Deck.withUnShuffledCards());
    }

    public static Shoe empty() {
        return new Shoe();
    }
    public void putShuffledDeck(Deck deck){
        if(deck.isShuffled()){
            this.deck = deck;
            return;
        }
        throw new IllegalStateException("Deck must be shuffled for putting to shoe!!");
    }
    public Deck deck() {return deck;}
    public Card drawCard() {
        return deck.cards().removeFirst();
    }
}
