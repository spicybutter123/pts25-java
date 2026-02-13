package sk.uniba.fmph.dcs.terra_futura;

import sk.uniba.fmph.dcs.terra_futura.enums.Resource;
import sk.uniba.fmph.dcs.terra_futura.interfaces.Rewardable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SelectReward {
    private int playerId = -1;
    private Rewardable targetCard;
    private List<Resource> availableResources;

    public void setReward(int playerId, Rewardable card, List<Resource> resources) {
        this.playerId = playerId;
        this.targetCard = card;
        this.availableResources = resources != null ? new ArrayList<>(resources) : new ArrayList<>();
    }

    public boolean canSelectReward(Resource resource) {
        if (playerId == -1 || targetCard == null || availableResources == null) {
            return false;
        }
        if (!availableResources.contains(resource)) {
            return false;
        }
        return targetCard.canPutResources(List.of(resource));
    }

    public void selectReward(Resource resource) {
        if (!canSelectReward(resource)) {
            throw new IllegalStateException("Cannot select reward: invalid resource or state.");
        }
        targetCard.putResources(List.of(resource));
    }

    public String state() {
        if (playerId == -1) {
            return "No reward selection active.";
        }
        return "Reward selection active for player " + playerId + ". Resources: " + availableResources;
    }
}
