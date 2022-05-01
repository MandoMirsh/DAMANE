package agentTest;

import java.util.ArrayList;
/**
 * Class for storing message and list of recievers
 * @author MandoMirsh
 *
 */
public class SendingTask {
	private ArrayList<String> SendTo;
	private String msg;
	private Integer sendNow;
	private Integer sendFin;
	public SendingTask(ArrayList<String> to, String mes) {
		SendTo = to;
		msg = mes;
		sendNow = 0;
		sendFin = to.size(); 
	}
	public String getMes() {
		return msg;
	}
	/**
	 * List of recievers starts all over
	 */
	public void reset() {
		sendNow = 0;
	}
	/**
	 * 
	 * @return String reciever, CAN be null, if no recievers left
	 * @see java.lang.String
	 */
	public String next() {
		if (!iffin())
			return SendTo.get(sendNow++);
		return null;
	}
	public boolean iffin()
	{
		return (sendNow >= sendFin);
	}
}
  