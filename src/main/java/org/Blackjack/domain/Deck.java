package org.Blackjack.domain;

import java.util.*;

public class Deck {
    private Deque<Card> cards;
    private DeckState state;
    private Deck(Deque<Card> cards, DeckState state){
        this.cards = cards;
        this.state = state;
    }
//    public Deck() {
//        this.cards = new ArrayDeque<>();
//    }
    public static Deck withUnShuffledCards(){
        Deque<Card> cards = new ArrayDeque<>();
        for(Suit suit : Suit.values()) {
            if(suit == Suit.SECRET) {
                continue;
            }
            for(Rank rank : Rank.values()) {
                if (rank == Rank.SECRET) {
                    continue;
                }
                cards.offerFirst(Card.newInstanceWithFaceSide(suit, rank));
            }
        }
        return new Deck(cards, DeckState.UNSHUFFLED);
    }
    public static Deck withShuffledDeck(List<Card> shuffledCards) {
        return new Deck(new ArrayDeque<>(shuffledCards), DeckState.SHUFFLED);
    }
    public static Deck withShuffledDeck(Deque<Card> shuffledCards) {
        return new Deck(new ArrayDeque<>(shuffledCards), DeckState.SHUFFLED);
    }

    public boolean isShuffled() {
        return state == DeckState.SHUFFLED;
    }
    public DeckState state() {return state;}
    public Deque<Card> cards() {
        return cards;
    }
}
