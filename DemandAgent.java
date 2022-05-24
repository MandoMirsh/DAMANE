package agentTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class DemandAgent extends Agent{
	private void sendmes(String reciever, String msg) {
		ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
		mes.addReceiver(new AID(reciever, AID.ISGUID));
		mes.setContent(msg);
		send(mes);
	}
	//AGENT STATUS:
	private static final Integer HALT = 0, REINITIATING = 1, RESPONCEWAIT = 2, RESPONSE_POSITIVE = 3, RESPONSE_NEGATIVE = 4, PLANNED = 3;
	int agentStatus = HALT;  //HALT, REINITIATING, RESPONCEWAIT, PLANNED
	//AGENT STATUS END
	
	//REQUEST INTERNALS
	
	//Resources
	ArrayList<String> resNames = new ArrayList<>();
	ArrayList<Integer> resVolumes = new ArrayList<>();
	//Whom to report to
	String jobname, storage, controller;
	Integer start, finish, lateFinish, lateStart, volume;
	//REQUEST INTERNALS END
	
	//MESSAGE MANAGEMENT
	//sending lists
	ArrayList<String> sendToController = new ArrayList<String>(), sendToStorage = new ArrayList<String>(),sendToJob = new ArrayList<String>();
	//management part
	private MessagesToSend sendQueue = new MessagesToSend();
	private Map<String, String> commands = new HashMap<String,String>(), outputVoc = new HashMap<String, String>();
	String CommandExplain(String command) {
		if (commands.get(command) == null)
			return "UNKNOWN_MESSAGE";
		return commands.get(command);
	}
	String labelToCommand(String label) {
		return outputVoc.get(label);
	}
	//input commands
	private void initiateVocabulary() {	
		commands.put("acre","RESERVE_ACCEPTED");
		commands.put("dere","RESERVE_DECLINED");
		commands.put("stop", "HALT");
		commands.put("sneg", "RESUME");
		commands.put("time", "NEW_TIMELINE");
	}
	//output commands
	private void outputVocabularyInit() {
		outputVoc.put("TRY_TO_RESERVE", "rreq");
		outputVoc.put("GET_RESERVED", "rget");
		outputVoc.put("REPORT_STATUS", "jore");
		outputVoc.put("I_AM_READY", "tire");
		outputVoc.put("I_AM_NOT_READY", "tnre");
		outputVoc.put("RELEASING_RESOURSE", "rref");
	}
	//Report to the project controller
	private void sendReady() {
		sendQueue.add(new SendingTask(sendToController, labelToCommand("I_AM_READY") + jobname));
	}
	private void sendNoReady() {
		sendQueue.add(new SendingTask(sendToController, labelToCommand("I_AM_NOT_READY") + jobname));
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
	private void startNegotiations() {
		agentStatus = REINITIATING;
	}
	
	@Override
	public void setup() {
		//args are: start, finish, late finish, volume, controller, storage, jobname, resnum, RESNAMES, RESDEMANDS 
		Object args[] = getArguments();
		start = Integer.parseInt(args[0].toString());
		finish = Integer.parseInt(args[1].toString());
		lateFinish = Integer.parseInt(args[2].toString());
		lateStart = lateFinish - finish + start;
		volume = Integer.parseInt(args[3].toString());
		controller = args[4].toString();
		storage = args[5].toString();
		jobname = args[6].toString();
		int resnum = Integer.parseInt(args[7].toString());
		for (int i = 0; i < resnum; i++) {
			resVolumes.add(Integer.parseInt(args[8+i].toString()));
			resNames.add(args[8+resnum+i].toString());
		}
		sendToController.add(controller);
		sendToStorage.add(storage);
		sendToJob.add(jobname);
		addBehaviour(SendingBehaviour);
		startNegotiations();
	}
}
