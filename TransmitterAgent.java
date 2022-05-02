package agentTest;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
public class TransmitterAgent extends Agent{
	private ArrayList<String> firstGroup = new ArrayList<>(), secondGroup = new ArrayList<>();
	private int firsts = -1, seconds = -1;
	private int send1 = 0, send2 = 0;
	private String message1 = "", message2 = "";
	//time to make sending queue
	private MessagesToSend sendQueue = new MessagesToSend();
	
	private void sendmes(String reciever, String msg) {
		ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
		mes.addReceiver(new AID(reciever, AID.ISGUID));
		mes.setContent(msg);
		send(mes);
	}
	boolean ifFirst(String name) {
		for (int i = 0; i <= firsts;i++) {
			if (firstGroup.get(i).equals(name))
				return true;
		}
		return false;
	}
	
	boolean ifSecond(String name) {
		for (int i = 0; i <= seconds;i++) {
			if (secondGroup.get(i).equals(name))
				return true;
		}
		return false;
	}
	
	private void printReport(String msg) {
		System.out.println(getAID().getLocalName() + ": "+ msg);
	}
	
	/*Behaviour StopSending2 = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(Send2);
			send2 = 0;
		}
	};
	Behaviour Send2 = new CyclicBehaviour() {
		@Override
		public void action() {
			if (send2>seconds)
				myAgent.addBehaviour(StopSending2);
			else{
				printReport("Sending to " + secondGroup.get(send2));
				sendMes(secondGroup.get(send2),message2);
				send2++;
			}
			
		}
	};
	Behaviour StopSending1 = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(Send1);
			send1 = 0;
		}
	};
	Behaviour Send1 = new CyclicBehaviour() {
		@Override
		public void action() {
			if (send1>firsts)
				//printRepobbrt("StoppedSending");
				myAgent.addBehaviour(StopSending1);
			else { 
				//printReport("Sending Continues");
				sendMes(firstGroup.get(send1),message1);
				send1++;
			}
		}
	};*/
	Behaviour SendingBehaviour = new CyclicBehaviour() {
		@Override
		public void action() {
			ArrayList<String> next = sendQueue.getNextToSend();
				
				if (next.size()!=0) {
					String sendTo = next.get(1),
							message = next.get(0);	
					//printReport("output "+ sendTo + " "+ message);
					sendmes(sendTo,message);
				}
			}
	};
	//main behaviour
	Behaviour MainTransmitter = new CyclicBehaviour() {
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg = myAgent.receive();
			if (msg!=null) {
				String sender = msg.getSender().getName();
				if (ifFirst(sender)) {
					message2 = msg.getContent();
					//add sending behaviour 
					//addBehaviour(Send2 );
					sendQueue.add(new SendingTask(secondGroup,message2));
				}
				else if  (ifSecond(sender)) {
					message1 = msg.getContent();
					//add sending behaviour
					//addBehaviour(Send1);
					sendQueue.add(new SendingTask(firstGroup,message1));
				}
				else printReport("Some unexpected sender: " + sender);
			}
		}
	};
	@Override
	public void setup() {
		Object[] obj = this.getArguments();
		//fistnum secondnum FIRSTS SECONDS
		firsts = Integer.parseInt(obj[0].toString());
		seconds = Integer.parseInt(obj[1].toString());
		//now read firsts and seconds
		for (int i = 0;i<firsts;i++) {
			firstGroup.add(obj[2+i].toString());
		}
		for (int i = 0;i<seconds;i++) {
			secondGroup.add(obj[2 + firsts + i].toString());
		}
		firsts--;
		seconds--;
		printReport("First Group");
		for (String s:firstGroup) {
			printReport(s);
		}
		printReport("SecondGroup");
		for (String s:secondGroup) {
			printReport(s);
		}
		//printReport("first " + firstGroup.get(firsts));
		//printReport("second " + secondGroup.get(seconds));
		//start recieving messages
		this.addBehaviour(MainTransmitter);
		this.addBehaviour(SendingBehaviour);
	}

}
