package functionalTests.loggingRequests.deadlock;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.multiactivity.MultiActiveService;


/**
 * Created by pkhvoros on 6/5/15.
 */
public class SecondActiveObject implements RunActive,Serializable { 
	
	private static final long serialVersionUID = 1L;

	@Override
    public void runActivity(Body body) {
        MultiActiveService service = new MultiActiveService(body);
        while (body.isActive()) {
            service.multiActiveServing();
        }
    }

    public StringWrapper run(StubObject first){
        return ((FirstActiveObject) first).run(PAActiveObject.getStubOnThis());
    }

}
