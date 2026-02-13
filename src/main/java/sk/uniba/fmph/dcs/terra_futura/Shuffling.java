package sk.uniba.fmph.dcs.terra_futura;

import sk.uniba.fmph.dcs.terra_futura.interfaces.ShufflingInterface;
import java.util.Collections;
import java.util.List;

public class Shuffling implements ShufflingInterface {
    @Override
    public void shuffle(List<Card> cards) {
        Collections.shuffle(cards);
    }
}
