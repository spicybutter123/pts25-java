package sk.uniba.fmph.dcs.terra_futura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.uniba.fmph.dcs.terra_futura.enums.Resource;

public class FinalScoreCalculator {

    private final ScoringMethod scoringMethod;
    private final List<Card> cardsToCheck;

    public FinalScoreCalculator(ScoringMethod scoringMethod, List<Card> cards) {
        if (scoringMethod == null) {
            throw new IllegalArgumentException("ScoringMethod cannot be null");
        }
        if (cards == null) {
            throw new IllegalArgumentException("Cards list cannot be null");
        }
        this.scoringMethod = scoringMethod;
        this.cardsToCheck = new ArrayList<>(cards);
    }

    public int calculate() {
        int basePoints = 0;
        int pollutionPenalty = 0;
        List<Resource> resourcesForBonus = new ArrayList<>();

        for (Card card : cardsToCheck) {
            if (!card.isClear()) {
                pollutionPenalty++;
                continue;
            }

            for (Resource r : card.resourcesOnCard()) {
                if (r == Resource.Pollution) {
                    continue;
                }

                resourcesForBonus.add(r);
                basePoints += getResourceValue(r);
            }
        }

        scoringMethod.selectThisMethodAndCalculate(resourcesForBonus);
        int bonusPoints = scoringMethod.getFinalPoints().orElse(0);

        return basePoints + bonusPoints - pollutionPenalty;
    }

    private int getResourceValue(Resource r) {
        switch (r) {
            case Red:
                return 1;
            case Green:
                return 1;
            case Yellow:
                return 1;
            case Bulb:
                return 5;
            case Gear:
                return 5;
            case Car:
                return 6;
            default:
                return 0;
        }
    }
}
