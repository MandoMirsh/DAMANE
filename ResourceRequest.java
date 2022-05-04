package agentTest;

public class ResourceRequest implements Comparable<ResourceRequest> {

	private Integer requestStart, requestTime, requestVolume, requestStatus; 
	private String requestersName;
	private JobWeight tightness = new JobWeight(0,0);
	public static final Integer REQUEST_RECIEVED = 0, REQUEST_IN_PROCESS = 1, REQUEST_ACCEPTED = 2;
	
	public ResourceRequest(String name, int start, int span, int volume, JobWeight mark) {
		requestersName = name;
		tightness.setWeights(mark.getWeights().get(0), mark.getWeights().get(0));
		
	}
	public int getStart() {
		return requestStart;
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
	@Override
	public int compareTo(ResourceRequest o) {
		// TODO Auto-generated method stub
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
