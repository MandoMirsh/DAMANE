package agentTest;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
public class InitialTaskAgent extends Agent{ 
	private ArrayList<String> next;
	private void printReport(String msg) {
		System.out.println(getAID().getLocalName() + ": "+ msg);
	}
	@SuppressWarnings("serial")
	@Override
	public void setup() {
		System.out.println("Initial Task Agent activated. My name is: "+ getAID().getLocalName());
		System.out.println("" + getAID().getLocalName()+": ");
		
		addBehaviour(new OneShotBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("Initial Task Agent "+ getAID().getName() + " ready.");
				msg.addReceiver(new AID("SuperController", AID.ISLOCALNAME));
				send(msg);
				//здесь отсылаем сообщения всем последующим.
			}
		});
		//построить сетевой график на основании предоставленной информации и 
	}  
}
