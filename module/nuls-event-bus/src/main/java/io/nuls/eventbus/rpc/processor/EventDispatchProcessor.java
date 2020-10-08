package io.nuls.eventbus.rpc.processor;

import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.model.Subscriber;

import static io.nuls.eventbus.util.EbLog.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Queue  for all the published events.
 * <p>Dispatch of the event is handled by separate thread.And separate thread for handling send,retry process for each subscriber</p>
 * @author naveen
 */
@SuppressWarnings("unused")
public class EventDispatchProcessor implements Runnable {

    private Object[] objects;

    public EventDispatchProcessor(Object[] objects){
        this.objects = objects;
    }
    /**
     * Event dispatch thread
     */
    @Override
    public void run() {
        try{
            if(null != objects){
                Log.info("Processing the published event starts..");
                Object data = objects[0];
                Set<Subscriber> subscribers =(Set<Subscriber>) objects[1];
                for (Subscriber subscriber : subscribers){
                    Map<String,Object> params = new HashMap<>(1);
                    params.put(EbConstants.CMD_PARAM_DATA,data);
                    EbConstants.SEND_RETRY_THREAD_POOL.execute(new SendRetryProcessor(new Object[]{subscriber,params}));
                }
                Log.info("Processing the published event Ends..");
            }
        }catch (Exception e){
            Log.error(e);
        }
    }
}
