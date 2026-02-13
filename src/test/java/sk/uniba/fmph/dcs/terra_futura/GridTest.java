package sk.uniba.fmph.dcs.terra_futura;

import org.junit.Test;
import static org.junit.Assert.*;
import sk.uniba.fmph.dcs.terra_futura.datatypes.GridPosition;

import java.util.Optional;

public class GridTest {

    @Test
    public void testPutCardAndGetCard() {
        Grid grid = new Grid();
        GridPosition pos = new GridPosition(0, 0);
        Card card = new Card(Optional.empty(), Optional.empty(), 0);

        assertTrue(grid.canPutCard(pos));
        grid.putCard(pos, card);

        assertEquals(Optional.of(card), grid.getCard(pos));
        assertFalse("Cannot put card on occupied position", grid.canPutCard(pos));
    }

    @Test
    public void testAdjacencyConstraint() {
        Grid grid = new Grid();
        grid.putCard(new GridPosition(0, 0), new Card(Optional.empty(), Optional.empty(), 0));

        assertTrue(grid.canPutCard(new GridPosition(0, 1)));
        assertTrue(grid.canPutCard(new GridPosition(0, -1)));
        assertTrue(grid.canPutCard(new GridPosition(1, 0)));
        assertTrue(grid.canPutCard(new GridPosition(-1, 0)));

        assertFalse(grid.canPutCard(new GridPosition(1, 1))); // Diagonal
        assertFalse(grid.canPutCard(new GridPosition(2, 0))); // Too far
    }

    @Test
    public void test3x3Constraint_Dynamic() {
        Grid grid = new Grid();
        grid.putCard(new GridPosition(0, 0), new Card(Optional.empty(), Optional.empty(), 0));

        grid.putCard(new GridPosition(0, 1), new Card(Optional.empty(), Optional.empty(), 0));

        GridPosition pos02 = new GridPosition(0, 2);
        assertTrue(grid.canPutCard(pos02));
        grid.putCard(pos02, new Card(Optional.empty(), Optional.empty(), 0));

        assertFalse(grid.canPutCard(new GridPosition(0, 3)));

        assertFalse(grid.canPutCard(new GridPosition(0, -1)));
    }

    @Test
    public void test3x3Constraint_X_Axis() {
        Grid grid = new Grid();
        grid.putCard(new GridPosition(0, 0), new Card(Optional.empty(), Optional.empty(), 0));
        grid.putCard(new GridPosition(-1, 0), new Card(Optional.empty(), Optional.empty(), 0));

        assertTrue(grid.canPutCard(new GridPosition(-2, 0)));

        grid.putCard(new GridPosition(-2, 0), new Card(Optional.empty(), Optional.empty(), 0));

        assertFalse(grid.canPutCard(new GridPosition(1, 0)));
    }

    @Test
    public void testActivationPattern() {
        Grid grid = new Grid();
        Card c1 = new Card(Optional.empty(), Optional.empty(), 0); // Clear
        Card c2 = new Card(Optional.empty(), Optional.empty(), 0); // Clear
        Card c3 = new Card(Optional.empty(), Optional.empty(), 0); // Clear

        grid.putCard(new GridPosition(0, 0), c1);
        grid.putCard(new GridPosition(0, 1), c2); // Same row
        grid.putCard(new GridPosition(1, 0), c3); // Same col

        var pattern = grid.computeActivationPattern(new GridPosition(0, 0));

        // Should contain itself (0,0) and neighbors in row/col (0,1) and (1,0)
        assertTrue(pattern.contains(new GridPosition(0, 0)));
        assertTrue(pattern.contains(new GridPosition(0, 1)));
        assertTrue(pattern.contains(new GridPosition(1, 0)));
        assertEquals(3, pattern.size());
    }
}
