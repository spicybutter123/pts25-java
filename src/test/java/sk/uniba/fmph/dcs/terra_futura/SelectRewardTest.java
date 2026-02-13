package sk.uniba.fmph.dcs.terra_futura;

import org.junit.Test;
import sk.uniba.fmph.dcs.terra_futura.enums.Resource;
import sk.uniba.fmph.dcs.terra_futura.interfaces.Rewardable;

import java.util.List;
import static org.junit.Assert.*;

public class SelectRewardTest {
    private static class MockRewardable implements Rewardable {
        boolean canPut = true;
        List<Resource> lastPutResources = null;

        @Override
        public boolean canPutResources(List<Resource> resources) {
            return canPut;
        }

        @Override
        public void putResources(List<Resource> resources) {
            lastPutResources = resources;
        }
    }

    @Test
    public void testSelectRewardFlow() {
        SelectReward selectReward = new SelectReward();
        MockRewardable mockCard = new MockRewardable();
        List<Resource> available = List.of(Resource.Green, Resource.Red);

        // Initial state
        assertFalse(selectReward.canSelectReward(Resource.Green));

        // Set reward
        selectReward.setReward(1, mockCard, available);

        // Check canSelect
        assertTrue(selectReward.canSelectReward(Resource.Green));
        assertFalse(selectReward.canSelectReward(Resource.Yellow)); // Not in available

        // Perform selection
        selectReward.selectReward(Resource.Green);

        // Verify mock interaction
        assertNotNull(mockCard.lastPutResources);
        assertEquals(1, mockCard.lastPutResources.size());
        assertEquals(Resource.Green, mockCard.lastPutResources.get(0));
    }

    @Test
    public void testCannotSelectIfCardRefuses() {
        SelectReward selectReward = new SelectReward();
        MockRewardable mockCard = new MockRewardable();
        mockCard.canPut = false;

        selectReward.setReward(1, mockCard, List.of(Resource.Green));

        assertFalse(selectReward.canSelectReward(Resource.Green));
    }
}
