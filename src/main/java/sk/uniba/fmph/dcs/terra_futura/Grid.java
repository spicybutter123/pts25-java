package sk.uniba.fmph.dcs.terra_futura;

import sk.uniba.fmph.dcs.terra_futura.datatypes.GridPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Grid implements InterfaceActivateGrid {

    private final Map<GridPosition, Card> placedCards = new HashMap<>();
    private final Set<GridPosition> activatedPositions = new HashSet<>();
    private Set<GridPosition> currentActivationPattern = new HashSet<>();
    private boolean patternSet = false;

    public Optional<Card> getCard(GridPosition coordinate) {
        return Optional.ofNullable(placedCards.get(coordinate));
    }

    public void putCard(GridPosition coordinate, Card card) {
        if (!canPutCard(coordinate)) {
            throw new IllegalArgumentException("Cannot put card at " + coordinate);
        }
        placedCards.put(coordinate, card);
    }

    public boolean isFull() {
        return placedCards.size() >= 9;
    }

    public boolean canPutCard(GridPosition coordinate) {
        if(placedCards.containsKey(coordinate)) {
            return false;
        }

        if(placedCards.isEmpty()) {
            return true;
        }

        if(!hasNeighbor(coordinate)) {
            return false;
        }

        return fitsIn3x3(coordinate);
    }

    private boolean hasNeighbor(GridPosition pos) {
        int[][] directions = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
        for (int[] dir : directions) {
            GridPosition neighbor = new GridPosition(pos.x() + dir[0], pos.y() + dir[1]);
            if (placedCards.containsKey(neighbor)) {
                return true;
            }
        }
        return false;
    }

    // dynamicky sledujem, je mozne ze prvu kartu som si mohol dat aj na nejaky zvlastny index
    private boolean fitsIn3x3(GridPosition newPos) {
        int minX = newPos.x();
        int maxX = newPos.x();
        int minY = newPos.y();
        int maxY = newPos.y();

        for (GridPosition pos : placedCards.keySet()) {
            minX = Math.min(minX, pos.x());
            maxX = Math.max(maxX, pos.x());
            minY = Math.min(minY, pos.y());
            maxY = Math.max(maxY, pos.y());
        }

        return (maxX - minX <= 2) && (maxY - minY <= 2);
    }

    public boolean canBeActivated(GridPosition coordinate) {
        if (!currentActivationPattern.contains(coordinate)) {
            return false;
        }
        if (activatedPositions.contains(coordinate)) {
            return false;
        }
        Card card = placedCards.get(coordinate);
        return card != null && card.isClear();
    }

    public void setActivated(GridPosition coordinate) {
        if (canBeActivated(coordinate)) {
            activatedPositions.add(coordinate);
        }
    }

    public Collection<GridPosition> computeActivationPattern(final GridPosition position) {
        Set<GridPosition> pattern = new HashSet<>();
        if(!placedCards.containsKey(position)) {
            return pattern;
        }

        Card placedCard = placedCards.get(position);
        if(placedCard.isClear()) {
            pattern.add(position);
        }

        for(Map.Entry<GridPosition, Card> entry : placedCards.entrySet()) {
            GridPosition pos = entry.getKey();
            Card card = entry.getValue();

            if (pos.equals(position))
                continue;

            boolean sameRow = pos.x() == position.x();
            boolean sameCol = pos.y() == position.y();

            if ((sameRow || sameCol) && card.isClear()) {
                pattern.add(pos);
            }
        }
        return pattern;
    }

    public List<Card> getAllCards() {
        return new ArrayList<>(placedCards.values());
    }

    @Override
    public void setActivationPattern(Collection<GridPosition> pattern) {
        if (patternSet) {
            return;
        }
        this.currentActivationPattern = new HashSet<>(pattern);
        this.patternSet = true;
    }

    public void endTurn() {
        currentActivationPattern.clear();
        activatedPositions.clear();
        patternSet = false;
    }
}
