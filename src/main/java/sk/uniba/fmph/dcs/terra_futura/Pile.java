package sk.uniba.fmph.dcs.terra_futura;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Trieda reprezentujúca balíček kariet.
 * Obsahuje viditeľné karty, skrytý balíček a odkladací balíček.
 */
public class Pile {

    private final List<Card> visibleCards = new LinkedList<>();
    private final List<Card> hiddenCards = new LinkedList<>();
    private final List<Card> removedCards = new LinkedList<>();

    private final sk.uniba.fmph.dcs.terra_futura.interfaces.ShufflingInterface shuffling;

    public Pile() {
        this(new LinkedList<>());
    }

    public Pile(List<Card> cards) {
        this(cards, new sk.uniba.fmph.dcs.terra_futura.interfaces.ShufflingInterface() {
            @Override
            public void shuffle(List<Card> c) {
                java.util.Collections.shuffle(c);
            }
        });
    }

    public Pile(List<Card> cards, sk.uniba.fmph.dcs.terra_futura.interfaces.ShufflingInterface shuffling) {
        this.shuffling = shuffling;
        this.hiddenCards.addAll(cards);
        for (int i = 0; i < 4; i++) {
            if (hiddenCards.isEmpty()) {
                break;
            }
            Card card = hiddenCards.removeLast(); // removeLast
            visibleCards.add(card);
        }
    }

    /**
     * Vráti voliteľnú kartu z balíčka na základe indexu, bez jej odstránenia.
     * 
     * @param cardIndex 0 pre skrytú kartu, 1-4 pre viditeľné karty.
     * @return Optional s kartou, alebo Optional.empty() ak karta nie je dostupná.
     */
    public Optional<Card> getCard(final int cardIndex) {
        if (cardIndex == 0) {
            return hiddenCards.isEmpty() ? Optional.empty() : Optional.of(hiddenCards.getLast());
        }
        int index = cardIndex - 1;
        if (index < 0 || index >= visibleCards.size()) {
            return Optional.empty();
        }
        return Optional.of(visibleCards.get(index));
    }

    /**
     * Vyberie a vráti kartu z balíčka na základe indexu.
     * 
     * @param cardIndex 0 pre vrchnú kartu z balíčka, 1-4 pre karty z ponuky.
     * @return Vybraná karta, alebo null ak karta na danej pozícii neexistuje.
     */
    public Card takeCard(final int cardIndex) {
        if (cardIndex == 0) {
            if (hiddenCards.isEmpty()) {
                recycleRemovedCards();
                if (hiddenCards.isEmpty()) {
                    return null;
                }
            }
            Card card = hiddenCards.removeLast();
            if (hiddenCards.isEmpty()) {
                recycleRemovedCards();
            }
            return card;
        } else {
            int index = cardIndex - 1;
            if (index < 0 || index >= visibleCards.size()) {
                return null;
            }
            Card taken = visibleCards.remove(index);
            refillFromHidden();
            return taken;
        }
    }

    /**
     * Odstráni poslednú kartu z ponuky viditeľných kariet a nahradí ju novou.
     */
    public void removeLastCard() {
        if (!visibleCards.isEmpty()) {
            Card removedCard = visibleCards.removeLast();
            removedCards.add(removedCard);
            refillFromHidden();
        }
    }

    public boolean isEmpty() {
        return visibleCards.isEmpty() && hiddenCards.isEmpty();
    }

    public String state() {
        StringBuilder sb = new StringBuilder("Visible pile contains: ");
        for (int i = visibleCards.size() - 1; i >= 0; i--) {
            sb.append(visibleCards.get(i).state());
            if (i > 0) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private void refillFromHidden() {
        if (hiddenCards.isEmpty()) {
            recycleRemovedCards();
        }
        if (!hiddenCards.isEmpty()) {
            Card newCard = hiddenCards.removeLast(); // removeLast
            visibleCards.addFirst(newCard); // addFirst
        }
    }

    private void recycleRemovedCards() {
        if (!removedCards.isEmpty()) {
            shuffling.shuffle(removedCards);
            hiddenCards.addAll(removedCards);
            removedCards.clear();
        }
    }
}
