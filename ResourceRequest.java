package agentTest;

public class ResourceRequest implements Comparable<ResourceRequest> {

	private Integer requestStart, requestTime, requestVolume, requestStatus; 
	private String requestersName;
	private JobWeight tightness = new JobWeight(0,0);
	public static final Integer REQUEST_RECIEVED = 0, REQUEST_IN_PROCESS = 1, REQUEST_ACCEPTED = 2, REQUEST_FULLFILLED = 3;
	
	public ResourceRequest(String name, int start, int span, int volume, JobWeight mark) {
		requestersName = name;
		tightness.setWeights(mark.getWeights().get(0), mark.getWeights().get(0));
		
	}
	public int getStart() {
		return requestStart;
	}
	public int longevity() {
		return requestTime;
	}
	public int volume() {
		return requestVolume;
	}
	public JobWeight getWeight() {
		return tightness;
	}
	public String getName() {
		return requestersName;
	}
	public int getStatus() {
		return requestStatus;
	}
	public void setStatus(int stat) {
		if ((stat>=REQUEST_RECIEVED)&(stat<=REQUEST_FULLFILLED))
			requestStatus = stat;
	}
	@Override
	public int compareTo(ResourceRequest o) {
		// status - start - tightness - 
		if (this.requestStatus == o.getStatus())
			if (this.requestStart == o.getStart())
				if (this.tightness.equals(o.getWeight()))
				{
					return this.requestersName.compareTo(o.getName()); 
				}
				else
					return (new JobWeightComparator().compare(this.tightness,o.getWeight()));
			else
				return this.requestStart.compareTo(o.getStart());
		else return this.requestStatus.compareTo(o.getStatus());
	}
}
