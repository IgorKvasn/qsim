package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.creationdelay;

import lombok.Getter;
import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.PacketCreationDelayFunction;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;

/**
 * returns constant delay with some random noise
 * the value of the random noise will affect the result in this way: the result will be constant delay +/- random noise; but never less then 0
 * if random noise equals 0, constant delay will be produced
 *
 * @author Igo
 */
public class ConstantNoiseCreationDelay extends PacketCreationDelayFunction {
    private static final long serialVersionUID = 2063582603195288259L;
    private transient Random rand = new Random();
    @Getter
    private double noise;

    public ConstantNoiseCreationDelay(double delay, double noise) {
        super(delay, 1);
        this.noise = noise;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        rand = new Random();
    }

    @Override
    public double calculateDelay(SimulationRuleBean rule, double simulationTime) {
        double result = (maxDelay - noise) + rand.nextDouble() * noise;
        if (result < 0) result = 0;
        return result;
    }
}
