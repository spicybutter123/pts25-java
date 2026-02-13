package sk.uniba.fmph.dcs.terra_futura;

import org.junit.Before;
import org.junit.Test;
import sk.uniba.fmph.dcs.terra_futura.interfaces.ShufflingInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class PileTest {

    private List<Card> deck;
    private ShufflingInterface mockShuffling;

    @Before
    public void setUp() {
        deck = new ArrayList<>();
        // Vytvoríme 10 dummy kariet
        for (int i = 0; i < 10; i++) {
            deck.add(new Card(Optional.empty(), Optional.empty(), 0));
        }

        mockShuffling = new ShufflingInterface() {
            @Override
            public void shuffle(List<Card> cards) {
                // Používam Collections.reverse namiesto náhodného miešania aby som v testoch
                // nemal náhodnosť
                Collections.reverse(cards);
            }
        };
    }

    @Test
    public void testInitialization() {
        Pile pile = new Pile(new ArrayList<>(deck), mockShuffling);
        // Balíček má 10 kariet.
        // 4 by mali byť viditeľné.
        // 6 by malo byť v skrytom balíčku.

        // Skontrolujeme viditeľné karty
        for (int i = 1; i <= 4; i++) {
            Optional<Card> card = pile.getCard(i);
            assertTrue("Viditeľná karta na indexe " + i + " by mala byť prítomná", card.isPresent());
        }

        // Skontrolujeme skrytú kartu (vrchnú)
        Optional<Card> topHidden = pile.getCard(0);
        assertTrue("Skrytý balíček by nemal byť prázdny", topHidden.isPresent());
    }

    @Test
    public void testInitializationWithFewCards() {
        List<Card> smallDeck = new ArrayList<>();
        smallDeck.add(new Card(Optional.empty(), Optional.empty(), 0));
        smallDeck.add(new Card(Optional.empty(), Optional.empty(), 0));

        Pile pile = new Pile(smallDeck, mockShuffling);

        assertTrue(pile.getCard(1).isPresent());
        assertTrue(pile.getCard(2).isPresent());
        assertFalse(pile.getCard(3).isPresent());
        assertFalse(pile.getCard(4).isPresent());
        assertFalse("Skrytý balíček by mal byť prázdny", pile.getCard(0).isPresent());
    }

    @Test
    public void testGetCardParams() {
        Pile pile = new Pile(new ArrayList<>(deck), mockShuffling);

        // Index 0 -> skrytá vrchná
        assertTrue(pile.getCard(0).isPresent());

        // Index 1-4 -> viditeľné
        assertTrue(pile.getCard(1).isPresent());
        assertTrue(pile.getCard(4).isPresent());

        // Neplatné indexy
        assertFalse(pile.getCard(5).isPresent());
        assertFalse(pile.getCard(-1).isPresent());
    }

    @Test
    public void testTakeCardVisible() {
        Pile pile = new Pile(new ArrayList<>(deck), mockShuffling);
        // Pôvodne viditeľné: deck[9], deck[8], deck[7], deck[6] (pretože odoberáme z
        // konca)
        // Skryté: deck[0]..deck[5]

        Card card3 = pile.getCard(3).orElseThrow();
        Card taken = pile.takeCard(3);

        assertEquals("Zobraná karta by sa mala zhodovať s 'peeknutou' kartou", card3, taken);

        // Po zobratí by sa miesto 3 malo doplniť zo skrytého balíčka.
        assertTrue("Miesto 3 by sa malo doplniť", pile.getCard(3).isPresent());
    }

    @Test
    public void testTakeCardHidden() {
        Pile pile = new Pile(new ArrayList<>(deck), mockShuffling);

        Optional<Card> topHiddenBefore = pile.getCard(0);
        assertTrue(topHiddenBefore.isPresent());

        Card taken = pile.takeCard(0);
        assertEquals(topHiddenBefore.get(), taken);
    }

    @Test
    public void testTakeCardReturnsNullIfInvalid() {
        Pile pile = new Pile(new ArrayList<>(deck), mockShuffling);
        assertNull(pile.takeCard(5));
        assertNull(pile.takeCard(-1));
    }

    @Test
    public void testRecycling() {
        // Nastavíme balíček s dostatočným počtom kariet na naakumulovanie odstránených
        // kariet
        List<Card> mediumDeck = new ArrayList<>();
        // 4 viditeľné + 2 skryté = 6 kariet celkovo.
        for (int i = 0; i < 6; i++) {
            mediumDeck.add(new Card(Optional.empty(), Optional.empty(), i));
        }
        Pile pile = new Pile(new ArrayList<>(mediumDeck), mockShuffling);

        // Stav: 4 viditeľné, 2 skryté, 0 odstránených.

        // removeLastCard() -> 1 odstránená, 1 skrytá, 4 viditeľné.
        pile.removeLastCard();

        // removeLastCard() -> 2 odstránené, 0 skrytých, 4 viditeľné.
        pile.removeLastCard();

        // Teraz je skrytý balíček prázdny, odstránené sú 2.
        // takeCard(0) by malo spustiť recykláciu.

        Card recycled = pile.takeCard(0);
        // Malo by zrecyklovať 2 odstránené -> do skrytého.
        // Potom zobrať 1 zo skrytého.
        // Zostáva: 1 v skrytom.

        assertNotNull("Malo by zrecyklovať a vrátiť kartu", recycled);
    }

    @Test
    public void testRemoveLastCard() {
        Pile pile = new Pile(new ArrayList<>(deck), mockShuffling);
        int sizeBefore = 0;
        for (int i = 1; i <= 4; i++)
            if (pile.getCard(i).isPresent())
                sizeBefore++;

        pile.removeLastCard();

        int sizeAfter = 0;
        for (int i = 1; i <= 4; i++)
            if (pile.getCard(i).isPresent())
                sizeAfter++;

        assertEquals("Po removeLastCard by sa mal počet kariet zachovať (doplnenie)", sizeBefore, sizeAfter);
        assertEquals(4, sizeAfter);
    }
}
