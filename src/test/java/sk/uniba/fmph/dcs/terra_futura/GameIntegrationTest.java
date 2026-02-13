package sk.uniba.fmph.dcs.terra_futura;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import sk.uniba.fmph.dcs.terra_futura.datatypes.CardSource;
import sk.uniba.fmph.dcs.terra_futura.datatypes.GridPosition;
import sk.uniba.fmph.dcs.terra_futura.enums.Deck;
import sk.uniba.fmph.dcs.terra_futura.enums.Resource;
import sk.uniba.fmph.dcs.terra_futura.interfaces.Effect;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.ArrayList;

public class GameIntegrationTest {

    private Game game;
    private Pile deck1;
    private Pile deck2;
    private List<Card> cardsDeck1;
    private List<Card> cardsDeck2;

    @Before
    public void setUp() {
        cardsDeck1 = new LinkedList<>();
        cardsDeck2 = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            cardsDeck1.add(createDummyCard("D1_" + i));
            cardsDeck2.add(createDummyCard("D2_" + i));
        }

        deck1 = new Pile(cardsDeck1);
        deck2 = new Pile(cardsDeck2);

        List<Collection<GridPosition>> patterns = new ArrayList<>();
        List<ScoringMethod> scorings = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            patterns.add(Collections.singletonList(new GridPosition(0, 0)));
            scorings.add(new ScoringMethod(Collections.singletonList(Resource.Money), 1));
        }

        game = new Game(2, patterns, scorings, deck1, deck2);
    }

    private Card createDummyCard(String id) {
        return new Card(Optional.empty(), Optional.empty(), 1);
    }

    @Test
    public void testSimpleGameScenario() {
        GridPosition targetPos = new GridPosition(0, 0);

        assertTrue(game.getGrid(0).getAllCards().isEmpty());

        boolean result = game.takeCard(0, new CardSource(Deck.I, 1), targetPos);
        assertTrue("Player 0 should successfully take card", result);

        assertFalse(game.getGrid(0).getAllCards().isEmpty());
        assertTrue(game.getGrid(0).canBeActivated(targetPos));

        game.turnFinished(0);

        GridPosition p1Pos = new GridPosition(0, 0);
        boolean p1Result = game.takeCard(1, new CardSource(Deck.II, 1), p1Pos);
        assertTrue("Player 1 should take card", p1Result);

        game.turnFinished(1);

        boolean p0Next = game.takeCard(0, new CardSource(Deck.I, 2), new GridPosition(0, 1)); // Adjacent
        assertTrue("Player 0 should take next card", p0Next);

        assertFalse(game.getGrid(0).canPutCard(new GridPosition(0, 1)));
        assertTrue(game.getGrid(0).canPutCard(new GridPosition(0, -1)));
    }
}
