package agentTest;

public class ResourceReserve {
	String name;
	Integer date, volume,days;

	ResourceReserve(String reservant, int start, int volume, int longevity){
		this.name = reservant;
		this.date = start;
		this.volume = volume;
		this.days = longevity;
	}
	ResourceReserve(String reservant, int start, int volume){
		this.name = reservant;
		this.date = start;
		this.volume = volume;
		this.days = 1;
	}
}
