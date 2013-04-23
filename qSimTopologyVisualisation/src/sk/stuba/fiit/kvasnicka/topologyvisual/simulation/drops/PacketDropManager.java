/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.simulation.drops;

import java.util.LinkedList;
import java.util.List;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.drop.PacketDropEvent;
import sk.stuba.fiit.kvasnicka.qsimsimulation.events.drop.PacketDropListener;

/**
 *
 * @author Igor Kvasnicka
 */
public class PacketDropManager implements PacketDropListener {

    private final List<PacketDrop> drops = new LinkedList<PacketDrop>();

    public double[] createHistogramData(double start, double end, String node, String rule, PacketDropEvent.LocationEnum location, int classWidth) {
        List<PacketDrop> filtered = new LinkedList<PacketDrop>();
        double endCorrected = Double.MAX_VALUE;
        if (end != -1) {
            endCorrected = end;
        }
        //filter all drops
        synchronized (drops) {
            for (PacketDrop drop : drops) {
                if (start <= drop.when && drop.when <= endCorrected && drop.nodeName.equals(node) && drop.ruleName.equals(rule) && location == drop.location) {
                    filtered.add(drop);
                }
            }
        }

        //create list of values (times)
        double[] values = new double[filtered.size()];
        for (int i = 0; i < filtered.size(); i++) {
            values[i] = filtered.get(i).when;
        }

        return values;
    }

    public long calculateHistogramBins(int classWidth, int start, double[] data) {
        double end = findEnd(data);
        if ((end - start) % classWidth == 0) {
            return Math.round((end - start) / classWidth);
        } else {
            return Math.round((end - start) / classWidth + 1);
        }
    }

    private double findEnd(double[] drops) {
        double result = 0;
        for (double d : drops) {
            result = Math.max(result, d);
        }
        return result;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    @Override
    public void packetDropOccurred(PacketDropEvent pde) {
        drops.add(new PacketDrop(pde.getWhen(), pde.getWhere().getName(), pde.getRule().getName(), pde.getLocation()));
    }

    private class PacketDrop {

        private final double when;
        private final String nodeName;
        private final String ruleName;
        private final PacketDropEvent.LocationEnum location;

        public PacketDrop(double when, String nodeName, String ruleName, PacketDropEvent.LocationEnum location) {
            this.when = when;
            this.nodeName = nodeName;
            this.ruleName = ruleName;
            this.location = location;
        }
    }
}
