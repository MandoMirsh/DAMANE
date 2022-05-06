package agentTest;

public class ResourceDescriptor {
	private String name;
	private boolean completeFlag = false;
	private int date, howMuch;
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
	ResourceDescriptor(String resname, int date, int volume){
		howMuch = volume;
		this.date = date;
		name = resname;	
	}
	ResourceDescriptor(String resname, int date){
		howMuch = 0;
		this.date = date;
		name = resname;
	}
	ResourceDescriptor(String resname){
		howMuch = 0;
		this.date = 0;
		this.name = resname;
	}
	public int volume() {
		return howMuch;
	}
	public void setVolume(int i) {
		howMuch = i;
	}
	public String getName() {
		return name;
	}
}
