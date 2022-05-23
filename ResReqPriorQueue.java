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
	ArrayList<Integer> resNeeds = new ArrayList<>();
	private int maxNeed = 0;
	/**
	 *  adds request to the queue, most appropriate will be on top
	 * @param a - request to add. of type ResourceRequest
	 */
	private void subResNeed(ResourceRequest a) {
		int i = a.getStart(), i2 =a.longevity() + i, vol = a.volume();
		for (; i < i2;i++)
			resNeeds.set(i, resNeeds.get(i) - vol);
	}
	private void addResNeed(ResourceRequest a) {
		int i = a.getStart(), i2 =a.longevity() + i, vol = a.volume();
		for (; i < i2;i++)
			resNeeds.set(i, resNeeds.get(i) + vol);
	}
	public void add(ResourceRequest a) {
		addResNeed(a);
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
		{	ResourceRequest ret = resReqs.get(0);
			subResNeed(ret);
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
	//
	public void updateReq(ResourceRequest a) {
		int i = posInQueue(a);
		if (i>=0) 
			{
			//
			subResNeed(resReqs.get(i));
			addResNeed(a);
			
			resReqs.set(i,a);
			}
		
			
		else
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
	public ResReqPriorQueue() {}
	public ResReqPriorQueue(int N) {
		for (int i = 0;i<N; i++)
			resNeeds.add(0);
	}
	public int resNeedAt(int N) {
		return resNeeds.get(N);
	}
	public int getMaxNeed() {
		int ret = 0;
		for (int i = 0; i< resNeeds.size();i++)
			if (resNeeds.get(i)>ret)
				ret = resNeeds.get(i);
		return ret;
	}
}
