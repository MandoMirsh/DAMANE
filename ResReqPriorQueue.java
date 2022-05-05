package agentTest;

import java.util.ArrayList;
import java.util.Collections;
/**
 * 
 * @author MandoMirsh
 *
 */
public class ResReqPriorQueue {
	ArrayList<ResourceRequest> resReqs = new ArrayList<>();
	/**
	 *  adds request to the queue, most appropriate will be on top
	 * @param a - request to add. of type ResourceRequest
	 */
	public void add(ResourceRequest a) {
		resReqs.add(a);
		Collections.sort(resReqs);
	}
	/**
	 * @return top request (the request itself IS REMOVED from queue)
	 */
	public ResourceRequest getNext() {
		if (resReqs.size() == 0)
			return null;
		else
		{
			ResourceRequest ret  = resReqs.get(0);
			resReqs.remove(0);
			return ret;
		}
	}
	private int posInQueue (String name) {
		for(int i=0; i<resReqs.size();i++) 
			if (resReqs.get(i).getName().equals(name))
				return i;
		return -1;
	}
	private int posInQueue(ResourceRequest a) {
		return posInQueue(a.getName());
	}
	public boolean ifInQueue(ResourceRequest a) {
		return (posInQueue(a)>=0);
	}
	
	public void updateReq(ResourceRequest a) {
		int i = posInQueue(a);
		if (i>=0) 
			resReqs.remove(i);
		this.add(a);
	}
	public ResourceRequest getByName(String name) {
		int posStated = posInQueue(name);
		if (posStated>=0) {
			ResourceRequest ret  = resReqs.get(0);
			resReqs.remove(0);
			return ret;
		}
		return null;	
	}
}
