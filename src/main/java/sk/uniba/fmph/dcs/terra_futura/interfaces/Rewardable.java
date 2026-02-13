package sk.uniba.fmph.dcs.terra_futura.interfaces;

import sk.uniba.fmph.dcs.terra_futura.enums.Resource;
import java.util.List;

public interface Rewardable {
    boolean canPutResources(List<Resource> resources);

    void putResources(List<Resource> resources);
}
