package sk.uniba.fmph.dcs.terra_futura.datatypes.effects;

import sk.uniba.fmph.dcs.terra_futura.enums.Resource;
import sk.uniba.fmph.dcs.terra_futura.interfaces.Effect;

import java.util.Collections;
import java.util.List;

public final class ArbitraryBasic implements Effect {
    private final List<Resource> to;

    public ArbitraryBasic(final List<Resource> to) {
        this.to = to;
    }

    @Override
    public boolean check(final List<Resource> input, final List<Resource> output, final int pollution) {
        for (Resource r : output) {
            if (Collections.frequency(to, r) != Collections.frequency(output, r)) {
                return false;
            }
        }

        return pollution == 0;
    }

    @Override
    public boolean hasAssistance() {
        return false;
    }

    @Override
    public String state() {
        return null;
    }
}
