package sk.uniba.fmph.dcs.terra_futura;
import org.junit.Assert;
import org.junit.Test;
import sk.uniba.fmph.dcs.terra_futura.datatypes.GridPosition;
import java.util.Optional;


public class MoveCardTest {

    private MoveCard moveCard;
    private FakePile pile;
    private FakeGrid grid;
    private GridPosition gridPosition;

    private static class FakePile extends Pile {
        boolean cardPresent = true;
        Card testCard = new Card();
        Card takenCard = null;

        @Override
        public Optional<Card> getCard(int index) {
            return cardPresent ? Optional.of(testCard) : Optional.empty();
        }

        @Override
        public Card takeCard(int index) {
            takenCard = testCard;
            return testCard;
        }
    }

    private static class FakeGrid extends Grid {
        boolean accept = true;
        boolean putCalled = false;
        Card receivedCard = null;

        @Override
        public boolean canPutCard(GridPosition pos) {
            return accept;
        }

        @Override
        public void putCard(GridPosition pos, Card card) {
            putCalled = true;
            receivedCard = card;
        }
    }

    @Test
    public void selectCardIndex_ValidTest() {
        moveCard = new MoveCard();
        try {
            moveCard.selectCardIndex(0);
            moveCard.selectCardIndex(3);
        } catch (IllegalArgumentException e) {
            Assert.assertNull(e);
        }

    }

    @Test
    public void selectCardIndex_InvalidLowerBoundTest() {
        moveCard = new MoveCard();
        try {
            moveCard.selectCardIndex(-1);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Card index out of range <0;3>", e.getMessage());
        }
    }

    @Test
    public void selectCardIndex_InvalidUpperBoundTest() {
        moveCard = new MoveCard();
        try {
            moveCard.selectCardIndex(4);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Card index out of range <0;3>", e.getMessage());
        }
    }

    @Test
    public void moveCard_IndexNotSetTest() {
        moveCard = new MoveCard();
        pile = new FakePile();
        grid = new FakeGrid();
        gridPosition = new GridPosition(0,0);
        try {
            moveCard.moveCard(pile, gridPosition, grid);
        } catch (IllegalStateException e) {
            Assert.assertEquals("Card index not set", e.getMessage());
        }
    }

    @Test
    public void moveCard_CardNotPresentInPileTest() {
        moveCard = new MoveCard();
        pile = new FakePile();
        grid = new FakeGrid();
        gridPosition = new GridPosition(0,0);
        pile.cardPresent = false;
        int index = 1;
        moveCard.selectCardIndex(index);
        try {
            moveCard.moveCard(pile, gridPosition, grid);
        } catch (IllegalStateException e) {
            Assert.assertEquals("There is no card at index:" + index + ".", e.getMessage());
        }
    }

    @Test
    public void moveCard_CannotPutCardOnGridTest() {
        moveCard = new MoveCard();
        pile = new FakePile();
        grid = new FakeGrid();
        gridPosition = new GridPosition(0,0);
        int index = 1;
        grid.accept = false;
        moveCard.selectCardIndex(index);
        try {
            moveCard.moveCard(pile, gridPosition, grid);
        } catch (IllegalStateException e) {
            Assert.assertEquals("Cannot put card at " + gridPosition, e.getMessage());
        }
    }

    @Test
    public void moveCard_SuccessTest() {
        moveCard = new MoveCard();
        pile = new FakePile();
        grid = new FakeGrid();
        gridPosition = new GridPosition(0,0);
        int index = 2;
        moveCard.selectCardIndex(index);

        boolean result = moveCard.moveCard(pile, gridPosition, grid);
        Assert.assertTrue(result);
    }
}
