package agentTest;

public class JobDescriptor {
	private Integer jobStart, timeNeeded;
	private JobWeight jobWeight;
	private String jobName;
	public String alias;
	public String name() {
		return jobName;
	}
	public void setName(String name) {
		jobName = name;
	}
	public Integer getJobStart() {
		return jobStart;
	}
	public void setStart(Integer start) {
		this.jobStart = start;
	}
	public Integer longevity() {
		return timeNeeded;
	}
	public void setLongevity(Integer longevity) {
		this.timeNeeded = longevity;
	}
	public JobWeight weight() {
		return jobWeight;
	}
	public void setWeight(JobWeight weight) {
		this.jobWeight = weight;
	}
}
//идея: вместо усложнения дескриптора использовать два дескриптора на агент: 
	//начальный, который меняется в первый раз
	//и текущий, который апдейтится с каждым изменением.