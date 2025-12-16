package org.Blackjack.domain;

public class Card {
    private final Suit suit;
    private final Rank rank;
    private CardFace cardFace;
    private Card(Suit suit, Rank rank, CardFace cardFace) {
        this.rank = rank;
        this.suit = suit;
        this.cardFace = cardFace;
    }
    public static Card newInstanceWithBackSide(Suit suit, Rank rank) {
        return new Card(suit, rank, CardFace.BACK_SIDE);
    }
    public static Card newInstanceWithFaceSide(Suit suit, Rank rank) {
        return new Card(suit, rank, CardFace.FACE_SIDE);
    }
    public Suit suit() {
        if(CardFace.BACK_SIDE == cardFace) {
            return Suit.SECRET;
        }
        return suit;
    }

    public Rank rank() {
        if(CardFace.BACK_SIDE == cardFace) {
            return Rank.SECRET;
        }
        return rank;
    }
    public long rankValue() {
        return rank.rankValue();
    }
    public void flipCardFaceToFace() {
        cardFace = CardFace.FACE_SIDE;
    }
    public void flipCardFaceToBack() {
        cardFace = CardFace.BACK_SIDE;
    }
}
