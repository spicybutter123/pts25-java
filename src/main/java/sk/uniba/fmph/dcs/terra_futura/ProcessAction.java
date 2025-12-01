package sk.uniba.fmph.dcs.terra_futura;

import sk.uniba.fmph.dcs.terra_futura.enums.Resource;
import sk.uniba.fmph.dcs.terra_futura.datatypes.GridPosition;

import java.util.List;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;

/**
 * Overí, či sa daná akcia (aktivácia karty) dá vykonať
 * a vykoná ju. Ak sa niečo nedá vykonať, vráti false.
 * Bez podpory Assistance.
 */
public final class ProcessAction {

    public static boolean activateCard (
            final GridPosition cardPosition,
            final Grid grid,
            final List<SimpleEntry<Resource, GridPosition>> inputs,
            final List<Resource> outputs,
            final List<GridPosition> pollution
    ) {
        // Overenie, že karta existuje.
        final Optional<Card> cardOpt = grid.getCard(cardPosition);
        if (cardOpt.isEmpty()) {
            throw new IllegalArgumentException("Card not found at position: " + cardPosition);
        }
        final Card card = cardOpt.get();

        if (!card.canPutResources(outputs)) {
            return false;
        }

        removeResources(grid, inputs);
        card.putResources(outputs);
        placePollution(grid, pollution);
        grid.setActivated(cardPosition);

        return true;
    }

    /** Odobranie vstupných zdrojov. */
    private static void removeResources(Grid grid, List<SimpleEntry<Resource, GridPosition>> inputs) {
        for (final SimpleEntry<Resource, GridPosition> entry : inputs) {
            Resource resource = entry.getKey();
            GridPosition gridPosition = entry.getValue();

            Optional<Card> resourceCardOpt = grid.getCard(gridPosition);
            if (resourceCardOpt.isEmpty()) {
                throw new IllegalArgumentException("Card not found at position: " + gridPosition);
            }
            Card resourceCard = resourceCardOpt.get();

            resourceCard.getResources(List.of(resource));
        }
    }

    /** Uloženie pollution. */
    private static void placePollution(
            final Grid grid,
            final List<GridPosition> pollutionPositions
    ) {
        for (final GridPosition position : pollutionPositions) {
            Optional<Card> pollutionCardOpt = grid.getCard(position);
            if (pollutionCardOpt.isEmpty()) {
                throw new IllegalArgumentException("Card not found at position: " + position);
            }
            Card pollutionCard = pollutionCardOpt.get();
            pollutionCard.putResources(List.of(Resource.Pollution));
        }
    }
}
