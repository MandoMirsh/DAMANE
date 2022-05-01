package agentTest;
import java.util.ArrayList;
public class Job {
	private int jobnum;
	public int getJobNum() {
		return jobnum;
	}
	private ArrayList<Integer> successors = new ArrayList<>();
	private ArrayList <Resource> Res;
	Job(int jnum, ArrayList<Integer> succ, ArrayList<String> jobNames,ArrayList<Integer> jobVolumes){
		this.successors = new ArrayList<>(succ);
		for (int i=0;i<jobVolumes.size();i++) {
			Res.add(new Resource(jobNames.get(i),jobVolumes.get(i)));
		}
	};
	
	// Не принимает int, необходимо Integer но почему? - Потому что нужен не примитив, а класс.
	/* Every primitive type in Java has an equivalent wrapper class: byte Byte, short Short, int Integer, long Long, boolean Boolean, char Character,float Float and double has Double
	 * Wrapper classes inherit from Object class, and primitive don't. So it can be used in collections with Object reference or with Generics
	 */
	
	
}
