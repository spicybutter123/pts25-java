package sk.uniba.fmph.dcs.terra_futura;

import sk.uniba.fmph.dcs.terra_futura.datatypes.GridPosition;

class MoveCard {
    private int cardIndex;
    MoveCard() {
        cardIndex = -1;
    }
    void selectCardIndex(final int cardIndex) {
        final int numberOfCards = 4;
        if (cardIndex >= 0 && cardIndex < numberOfCards) {
            this.cardIndex = cardIndex;
        } else {
            throw new IllegalArgumentException("Card index out of range <0;3>");
        }
    }
    boolean moveCard(final Pile pile, final GridPosition coordinate, final Grid grid) {
        if (cardIndex < 0) {
            throw new IllegalStateException("Card index not set");
        }
        if (pile.getCard(cardIndex).isEmpty()) {
            throw new IllegalStateException("There is no card at index:" + cardIndex + ".");
        }
        if (grid.canPutCard(coordinate)) {
            grid.putCard(coordinate, pile.takeCard(cardIndex));
            return true;
        } else {
            throw new IllegalStateException("Cannot put card at " + coordinate);
        }
    }
}

