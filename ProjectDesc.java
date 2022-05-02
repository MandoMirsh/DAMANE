package agentTest;

public class ProjectDesc {
	private String agentName,fileName;
	private Integer ready, nonConstraintFinish, realTimeFinish;
	public static final Integer NOINIT = 0,INITINPROGRESS = 1, INITIALIZATIONPASSED = 2, READY = 5; 
	ProjectDesc(String agent, String file){
		this.agentName = agent;
		this.fileName = file;
		ready = NOINIT;
		nonConstraintFinish = 0;
		realTimeFinish = 0;
	}
	public void setInitFinish(Integer N) {
		nonConstraintFinish = N;	
	}
	public Integer getInitFinish() {
		return nonConstraintFinish;	
	}
	public Integer getFinish() {
		return realTimeFinish;
	}
	public void setFinish(int N) {
		realTimeFinish = N;
	}
	public Integer getReadiness() {
		return ready;
	}
	public void setReadiness(Integer N) {
		ready = N;
	}
	public String getName() {
		return agentName;
	}
	public String getFile() {
		return fileName;
	}
}
