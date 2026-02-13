package sk.uniba.fmph.dcs.terra_futura;

import sk.uniba.fmph.dcs.terra_futura.enums.GameState;
import sk.uniba.fmph.dcs.terra_futura.interfaces.TerraFuturaInterface;
import sk.uniba.fmph.dcs.terra_futura.datatypes.*;
import sk.uniba.fmph.dcs.terra_futura.enums.*;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class Game implements TerraFuturaInterface {

    private final Map<Integer, Player> players = new HashMap<>();
    private final Map<Integer, Grid> grids = new HashMap<>();
    private final Map<Integer, ScoringMethod> scoringMethods = new HashMap<>();
    private final Set<Integer> playersFinishedFinalTurn = new HashSet<>();
    private final Map<Integer, Integer> finalScores = new HashMap<>();

    private final int totalPlayers;
    private final Pile deck1;
    private final Pile deck2;
    private final SelectReward rewardSelector;

    private int currentPlayerId;
    private GameState currentState;
    private int currentRewardPlayerId = -1;
    private boolean finalPhaseTriggered = false;

    public Game(int numberOfPlayers,
        List<Collection<GridPosition>> activationPatterns,
        List<ScoringMethod> scoringMethods,
        Pile deck1, Pile deck2) {

        if (activationPatterns == null || activationPatterns.size() != numberOfPlayers * 2) {
            throw new IllegalArgumentException("Invalid number of activation patterns.");
        }
        if (scoringMethods == null || scoringMethods.size() != numberOfPlayers * 2) {
            throw new IllegalArgumentException("Invalid number of scoring methods.");
        }

        this.totalPlayers = numberOfPlayers;
        this.deck1 = deck1;
        this.deck2 = deck2;
        this.rewardSelector = new SelectReward();
        this.currentState = GameState.TakeCardNoCardDiscarded;
        this.currentPlayerId = 0;

        initializePlayers(numberOfPlayers, activationPatterns, scoringMethods);
    }

    private void initializePlayers(int count, List<Collection<GridPosition>> patterns, List<ScoringMethod> scorings) {
        int patternIdx = 0;
        int scoringIdx = 0;

        for (int i = 0; i < count; i++) {
            Grid grid = new Grid();
            grids.put(i, grid);

            ActivationPattern ap1 = new ActivationPattern(grid, patterns.get(patternIdx++));
            ActivationPattern ap2 = new ActivationPattern(grid, patterns.get(patternIdx++));
            ScoringMethod sm1 = scorings.get(scoringIdx++);
            ScoringMethod sm2 = scorings.get(scoringIdx++);

            players.put(i, new Player(grid, ap1, ap2, sm1, sm2));
        }
    }

    @Override
    public boolean takeCard(int playerId, CardSource source, GridPosition destination) {
        if (playerId != currentPlayerId)
            return false;
        if (currentState != GameState.TakeCardNoCardDiscarded && currentState != GameState.TakeCardCardDiscarded) {
            return false;
        }

        Pile sourceDeck = (source.deck() == Deck.I) ? deck1 : deck2;
        Grid playerGrid = grids.get(playerId);

        if (!MoveCard.moveCard(sourceDeck, source.index(), destination, playerGrid)) {
            return false;
        }

        playerGrid.setActivationPattern(playerGrid.computeActivationPattern(destination));
        currentState = GameState.ActivateCard;
        return true;
    }

    @Override
    public boolean discardLastCardFromDeckPlayerId(int playerId, Deck deck) {
        if (playerId != currentPlayerId)
            return false;
        if (currentState != GameState.TakeCardNoCardDiscarded)
            return false;

        Pile targetDeck = (deck == Deck.I) ? deck1 : deck2;
        targetDeck.removeLastCard();

        currentState = GameState.TakeCardCardDiscarded;
        return true;
    }

    @Override
    public boolean activateCard(int playerId, GridPosition cardPosition,
            List<SimpleEntry<Resource, GridPosition>> inputs,
            List<Resource> outputs,
            List<GridPosition> pollution,
            Optional<Integer> otherPlayerId,
            Optional<GridPosition> otherPos) {

        if (playerId != currentPlayerId)
            return false;
        if (currentState != GameState.ActivateCard)
            return false;

        Grid grid = grids.get(playerId);

        if (otherPlayerId.isEmpty()) {
            return ProcessAction.activateCard(cardPosition, grid, inputs, outputs, pollution);
        } else {
            return handleAssistance(cardPosition, grid, inputs, outputs, pollution, otherPlayerId.get(), otherPos);
        }
    }

    private boolean handleAssistance(GridPosition cardPos, Grid grid,
            List<SimpleEntry<Resource, GridPosition>> inputs,
            List<Resource> outputs,
            List<GridPosition> pollution,
            int targetPlayerId,
            Optional<GridPosition> targetPos) {
        if (targetPos.isEmpty())
            return false;

        Grid otherGrid = grids.get(targetPlayerId);
        Optional<Card> assistingCard = otherGrid.getCard(targetPos.get());

        if (assistingCard.isEmpty())
            return false;

        boolean success = ProcessActionAssistance.activateCard(
                assistingCard.get(), cardPos, grid, inputs, outputs, pollution);

        if (!success)
            return false;

        if (!inputs.isEmpty()) {
            initiateRewardSelection(targetPlayerId, assistingCard.get(), inputs);
        }
        return true;
    }

    private void initiateRewardSelection(int rewardReceiverId, Card card,
            List<SimpleEntry<Resource, GridPosition>> inputs) {
        currentRewardPlayerId = rewardReceiverId;
        List<Resource> resources = new ArrayList<>();
        for (SimpleEntry<Resource, GridPosition> entry : inputs) {
            resources.add(entry.getKey());
        }
        rewardSelector.setReward(rewardReceiverId, card, resources);
        currentState = GameState.SelectReward;
    }

    @Override
    public boolean selectReward(int playerId, Resource resource) {
        if (currentState != GameState.SelectReward)
            return false;
        if (playerId != currentRewardPlayerId)
            return false;

        if (rewardSelector.canSelectReward(resource)) {
            rewardSelector.selectReward(resource);
            currentRewardPlayerId = -1;
            rewardSelector.setReward(-1, null, null);
            currentState = GameState.ActivateCard;
            return true;
        }
        return false;
    }

    @Override
    public boolean turnFinished(int playerId) {
        if (playerId != currentPlayerId)
            return false;

        if (currentState == GameState.SelectReward)
            return false; // Must finish reward selection
        if (currentState == GameState.SelectActivationPattern) {
            return finishActivationPatternSelection();
        }
        if (currentState == GameState.SelectScoringMethod) {
            return finishScoringMethodSelection();
        }

        // Must be in ActivateCard to finish turn nominally
        if (currentState != GameState.ActivateCard)
            return false;

        if (finalPhaseTriggered) {
            playersFinishedFinalTurn.add(playerId);
            if (playersFinishedFinalTurn.size() == totalPlayers) {
                currentState = GameState.SelectScoringMethod;
            }
            passTurn();
            return true;
        }

        grids.get(playerId).endTurn();

        if (checkIfAllGridsFull()) {
            currentState = GameState.SelectActivationPattern;
            finalPhaseTriggered = true;
        } else {
            currentState = GameState.TakeCardNoCardDiscarded;
        }

        passTurn();
        return true;
    }

    private boolean finishActivationPatternSelection() {
        boolean allReady = players.values().stream()
                .allMatch(p -> p.getActivationPattern1().isSelected() || p.getActivationPattern2().isSelected());

        if (allReady) {
            currentState = GameState.ActivateCard;
        }
        passTurn();
        return true;
    }

    private boolean finishScoringMethodSelection() {
        boolean allReady = scoringMethods.size() == totalPlayers;
        if (allReady) {
            calculateFinalScores();
            currentState = GameState.Finish;
        }
        passTurn();
        return true;
    }

    private void passTurn() {
        currentPlayerId = (currentPlayerId + 1) % totalPlayers;
    }

    private boolean checkIfAllGridsFull() {
        for (Grid g : grids.values()) {
            if (!g.isFull())
                return false;
        }
        return true;
    }

    @Override
    public boolean selectActivationPattern(int playerId, int card) {
        if (playerId != currentPlayerId)
            return false;
        if (currentState != GameState.SelectActivationPattern)
            return false;

        Player p = players.get(playerId);
        if (card == 1)
            p.getActivationPattern1().select();
        else if (card == 2)
            p.getActivationPattern2().select();
        else
            return false;

        return true;
    }

    @Override
    public boolean selectScoring(int playerId, int card) {
        if (playerId != currentPlayerId)
            return false;
        if (currentState != GameState.SelectScoringMethod)
            return false;

        Player p = players.get(playerId);
        if (card == 1)
            scoringMethods.put(playerId, p.getScoringMethod1());
        else if (card == 2)
            scoringMethods.put(playerId, p.getScoringMethod2());
        else
            return false;

        return true;
    }

    @Override
    public GameState getGameState() {
        return currentState;
    }

    @Override
    public Map<Integer, Integer> getFinalScores() {
        if (currentState != GameState.Finish) {
            throw new IllegalStateException("Game not finished");
        }
        return finalScores;
    }

    Grid getGrid(int playerId) {
        return grids.get(playerId);
    }

    private void calculateFinalScores() {
        for (int i = 0; i < totalPlayers; i++) {
            Grid g = grids.get(i);
            ScoringMethod sm = scoringMethods.get(i);
            FinalScoreCalculator calculator = new FinalScoreCalculator(sm, g.getAllCards());
            finalScores.put(i, calculator.calculate());
        }
    }
}
