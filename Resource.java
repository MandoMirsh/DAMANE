package agentTest; 
public class Resource {
	private int volume;
	private String type;
	public String getResType() {
		return type;	
	}
	public int getResVolume() {
		return volume;	
	}
	Resource(String type, int volume){
		this.type = type;
		this.volume = volume;
	}
	Resource(){
		this.type = "";
		this.volume = 0;
	}
	public Resource takeSomeRes(int n) {
		if (n >=this.volume) {
			this.volume-=n;
			return new Resource(this.type,n);
		}
		else
			return new Resource (this.type,0);
	}
	public void addRes(int n) {
		this.volume +=n;
	}
}
