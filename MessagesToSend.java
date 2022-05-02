package agentTest;

import java.util.ArrayList;
/**
 * Class for Storing messages between sending
 * @author MandoMirsh
 *
 */
public class MessagesToSend {
	private ArrayList<SendingTask> tasks;
	private Integer sendFrom;
	
	public MessagesToSend() {
		tasks = new ArrayList<>();
		sendFrom = 0;
	}
	/**
	 * 
	 * @return List: messageText,RecieverAddress. CAN be Empty
	 * @see java.util.ArrayList 
	 */
	public ArrayList<String> getNextToSend(){
		ArrayList<String> ret = new ArrayList<>();
		if (sendFrom >= tasks.size()) {
			sendFrom = 0;
		}
		else
		{
			ret.add(tasks.get(sendFrom).getMes());
			ret.add(tasks.get(sendFrom).next());
			
			if (!tasks.get(sendFrom).iffin()){
				sendFrom++;
			}
			else
			{
				tasks.remove((int)sendFrom);
			}
		}

		return ret;
	}
	public void add(SendingTask onemore) {
		tasks.add(onemore);
	}
}
