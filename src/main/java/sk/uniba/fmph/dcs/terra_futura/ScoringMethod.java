package sk.uniba.fmph.dcs.terra_futura;

import sk.uniba.fmph.dcs.terra_futura.enums.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScoringMethod {

    private final Map<Resource, Integer> requirements = new HashMap<>();
    private final int rewardPoints;
    private Integer finalScore = null;

    public ScoringMethod(List<Resource> requiredResources, int points) {
        if (requiredResources == null || requiredResources.isEmpty()) {
            throw new IllegalArgumentException("List of required resources cannot be empty.");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Points must be non-negative.");
        }

        this.rewardPoints = points;
        for (Resource r : requiredResources) {
            requirements.put(r, requirements.getOrDefault(r, 0) + 1);
        }
    }

    public void selectThisMethodAndCalculate(List<Resource> resources) {
        if (resources == null) {
            this.finalScore = 0;
            return;
        }

        Map<Resource, Integer> available = new HashMap<>();
        for (Resource r : resources) {
            available.put(r, available.getOrDefault(r, 0) + 1);
        }

        int maxSets = Integer.MAX_VALUE;

        for (Map.Entry<Resource, Integer> entry : requirements.entrySet()) {
            Resource res = entry.getKey();
            int needed = entry.getValue();
            int has = available.getOrDefault(res, 0);

            if (has < needed) {
                maxSets = 0;
                break;
            }

            int sets = has / needed;
            if (sets < maxSets) {
                maxSets = sets;
            }
        }

        if (maxSets == Integer.MAX_VALUE) {
            maxSets = 0;
        }

        this.finalScore = maxSets * rewardPoints;
    }

    public Optional<Integer> getFinalPoints() {
        return Optional.ofNullable(finalScore);
    }

    public String state() {
        return finalScore != null ? finalScore.toString() : "not calculated";
    }
}
