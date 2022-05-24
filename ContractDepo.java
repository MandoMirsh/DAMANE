package agentTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * contract depo, when initialized waits for the common agents to ask for contract.
 * each contract is being produced upon the need and is fullfilled or dropped in the negotiations
 * when the timeparts are changed 
 * @author seriu0007
 *
 */
public class ContractDepo extends Agent{
	//JOB PARAMETERS
	ArrayList<String> jobNames = new ArrayList<>();
	ArrayList<Integer> earlyStarts = new ArrayList<>(), lateStarts = new ArrayList<>();
	
	ArrayList<String> resNames = new ArrayList<>();
	ArrayList<Integer> resVolumes = new ArrayList<>();
	
	//contract status
	
	//MESSAGING
	private Map<String, String> commands = new HashMap<String,String>(), outputVoc = new HashMap<String, String>();
	private MessagesToSend sendQueue = new MessagesToSend();
	String CommandExplain(String command) {
		if (commands.get(command) == null)
			return "UNKNOWN_MESSAGE";
		return commands.get(command);
	}
	String labelToCommand(String label) {
		return outputVoc.get(label);
	}
	

	Behaviour SendingBehaviour = new CyclicBehaviour() {
		@Override
		public void action() {
			ArrayList<String> next = sendQueue.getNextToSend();
				
				if (next.size()!=0) {
					String sendTo = next.get(1),
							message = next.get(0);	
					sendmes(sendTo,message);
				}
			}
	};
	

	Behaviour NextMessage = new CyclicBehaviour() {
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg = myAgent.receive();
			if (msg!=null) {
				if (msg.getContent() ==null)
					printReport("DAFUQ: " + msg.getSender());
				else {
					
				}
			}
		}
	};
	//CONTRACT MANAGEMENT
	//job and contract connection
		Map<String, String> jobToContract = new HashMap<String,String>(), contractToJob = new HashMap<String,String>();
	
		
	
	
	private String newContractName() {
		return "Contract" + 
	}
	
	private void createNewContract(String jobname) {
		
	}
}
