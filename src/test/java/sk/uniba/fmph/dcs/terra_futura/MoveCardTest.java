package sk.uniba.fmph.dcs.terra_futura;
import org.junit.jupiter.api.Test;
import sk.uniba.fmph.dcs.terra_futura.datatypes.GridPosition;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class MoveCardTest {

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
        assertDoesNotThrow(() -> moveCard.selectCardIndex(0));
        assertDoesNotThrow(() -> moveCard.selectCardIndex(3));
    }

    @Test
    public void selectCardIndex_InvalidLowerBoundTest() {
        moveCard = new MoveCard();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> moveCard.selectCardIndex(-1));
        assertEquals("Card index out of range <0;3>", exception.getMessage());
    }

    @Test
    public void selectCardIndex_InvalidUpperBoundTest() {
        moveCard = new MoveCard();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> moveCard.selectCardIndex(4));
        assertEquals("Card index out of range <0;3>", exception.getMessage());
    }

    @Test
    public void moveCard_IndexNotSetTest() {
        moveCard = new MoveCard();
        pile = new FakePile();
        grid = new FakeGrid();
        gridPosition = new GridPosition(0,0);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> moveCard.moveCard(pile, gridPosition, grid));
        assertEquals("Card index not set", exception.getMessage());
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
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> moveCard.moveCard(pile, gridPosition, grid));
        assertEquals("There is no card at index:" + index + ".", exception.getMessage());
    }

    @Test
    void moveCard_CannotPutCardOnGridTest() {
        moveCard = new MoveCard();
        pile = new FakePile();
        grid = new FakeGrid();
        gridPosition = new GridPosition(0,0);
        int index = 1;
        grid.accept = false;
        moveCard.selectCardIndex(index);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> moveCard.moveCard(pile, gridPosition, grid));
        assertTrue(exception.getMessage().startsWith("Cannot put card at"));
    }

    @Test
    void moveCard_SuccessTest() {
        moveCard = new MoveCard();
        pile = new FakePile();
        grid = new FakeGrid();
        gridPosition = new GridPosition(0,0);
        int index = 2;
        moveCard.selectCardIndex(index);

        boolean result = moveCard.moveCard(pile, gridPosition, grid);
        assertTrue(result);
    }
}
