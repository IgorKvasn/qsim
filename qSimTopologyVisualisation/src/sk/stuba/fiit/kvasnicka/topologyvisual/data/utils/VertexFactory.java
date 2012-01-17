package sk.stuba.fiit.kvasnicka.topologyvisual.data.utils;

import org.apache.log4j.Logger;
import org.openide.util.NbBundle;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Computer;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Router;
import sk.stuba.fiit.kvasnicka.topologyvisual.data.Switch;

/**
 * @author Igor Kvasnicka
 */
public class VertexFactory {

    private static Logger logg = Logger.getLogger(VertexFactory.class);
    private int routerCount = 0;
    private int computerCount = 0;
    private int switchCount = 0;

    public VertexFactory() {
    }

    public VertexFactory(int routerCount, int switchCount, int computerCount) {
        this.routerCount = routerCount;
        this.switchCount = switchCount;
        this.computerCount = computerCount;
    }

    /**
     * creates new router object with default values
     *
     * @return Router object
     */
    public Router createRouter() {
        routerCount++;
        return new Router(NbBundle.getMessage(VertexFactory.class, "router") + " #" + routerCount);
    }

    public Switch createSwitch() {
        switchCount++;
        return new Switch(NbBundle.getMessage(VertexFactory.class, "switch") + " #" + switchCount);
    }

    /**
     * creates new computer object with default values
     *
     * @return Computer object
     */
    public Computer createComputer() {
        computerCount++;
        return new Computer(NbBundle.getMessage(VertexFactory.class, "computer") + " #" + computerCount);
    }

    /**
     * decrements counter of computers in topology by 1
     */
    public void decrementNumberOfComputers() {
        computerCount--;
        if (computerCount < 0) {
            computerCount = 0;
            logg.error("Computer count was negative - something is wrong");
        }
    }

    /**
     * decrements counter of routers in topology by 1
     */
    public void decrementNumberOfRouters() {
        routerCount--;
        if (routerCount < 0) {
            routerCount = 0;
            logg.error("Router count was negative - something is wrong");
        }
    }

    /**
     * decrements counter of routers in topology by 1
     */
    public void decrementNumberOfSwitches() {
        switchCount--;
        if (switchCount < 0) {
            switchCount = 0;
            logg.error("Switch count was negative - something is wrong");
        }
    }
}
