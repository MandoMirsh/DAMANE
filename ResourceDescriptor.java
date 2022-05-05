package agentTest;

public class ResourceDescriptor {
	private String name;
	private boolean completeFlag = false;
	private int date;
	public void setDate(int N) {
		date = N;
	}
	public int getDate() {
		return date;
	}
	public boolean complete() {
		return completeFlag;
	}
	public void setComplete() {
		completeFlag = true;
	}
	public void removeComplete() {
		completeFlag = false;
	}
	ResourceDescriptor(String resname, int date){
		this.date = date;
		name = resname;
	}
	ResourceDescriptor(String resname){
		this.date = 0;
		this.name = resname;
	}
}
