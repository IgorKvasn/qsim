package sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.creationdelay;

import sk.stuba.fiit.kvasnicka.qsimdatamodel.data.utils.PacketCreationDelayFunction;
import sk.stuba.fiit.kvasnicka.qsimsimulation.rule.SimulationRuleBean;

import java.util.Random;

/**
 * returns constant delay with some random noise
 * the value of the random noise will affect the result in this way: the result will be constant delay +/- random noise; but never less then 0
 * if random noise equals 0, constant delay will be produced
 *
 * @author Igo
 */
public class ConstantNoiseCreationDelay extends PacketCreationDelayFunction {
    private Random rand = new Random();
    private double noise;

    public ConstantNoiseCreationDelay(double delay, double noise) {
        super(delay, 1);
        this.noise = noise;
    }

    @Override
    public double calculateDelay(SimulationRuleBean rule, double simulationTime) {
        double result = (maxDelay - noise) + rand.nextDouble() * noise;
        if (result < 0) result = 0;
        return result;
    }
}
