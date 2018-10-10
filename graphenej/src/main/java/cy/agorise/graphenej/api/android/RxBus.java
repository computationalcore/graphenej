package cy.agorise.graphenej.api.android;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * Explained here: https://blog.kaush.co/2014/12/24/implementing-an-event-bus-with-rxjava-rxbus/
 */
public class RxBus {

    private static RxBus rxBus;

    public static final RxBus getBusInstance(){
        if(rxBus == null){
            rxBus = new RxBus();
        }
        return rxBus;
    }

    private final Relay<Object> _bus = PublishRelay.create().toSerialized();

    public void send(Object o) {
        _bus.accept(o);
    }

    public Flowable<Object> asFlowable() {
        return _bus.toFlowable(BackpressureStrategy.LATEST);
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }
}