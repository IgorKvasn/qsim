/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fiit.kvasnicka.topologyvisual.events.multiview;

import java.util.EventListener;

/**
 * listens to changes in active simulation MultiView
 *
 * @author Igor Kvasnicka
 */
public interface MultiviewChangedListener extends EventListener {

    void multiviewChangedOccurred(MultiviewChangedEvent evt);
}
