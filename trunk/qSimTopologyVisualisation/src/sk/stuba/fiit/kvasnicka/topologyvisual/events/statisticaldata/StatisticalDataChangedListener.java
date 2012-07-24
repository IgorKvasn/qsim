/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.events.statisticaldata;

import java.util.EventListener;

/**
 *
 * @author Igor Kvasnicka
 */
public interface StatisticalDataChangedListener extends EventListener {

    void statisticalDataChangeOccured(StatisticalDataEvent event);
}
