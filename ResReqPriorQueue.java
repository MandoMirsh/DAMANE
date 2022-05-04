package agentTest;

import java.util.ArrayList;
import java.util.Collections;

public class ResReqPriorQueue {
	ArrayList<ResourceRequest> resReqs = new ArrayList<>();
	public void add(ResourceRequest a) {
		resReqs.add(a);
		Collections.sort(resReqs);
	}
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
	private int posInQueue(ResourceRequest a) {
		for(int i=0; i<resReqs.size();i++) 
			if (resReqs.get(i).getName().equals(a.getName()))
				return i;
		return -1;
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
}
