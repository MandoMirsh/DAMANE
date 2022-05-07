package agentTest;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;

public class DummyAgent extends Agent{
	@Override
	public void setup() {
		addBehaviour(new CyclicBehaviour(){
		@Override 
		public void action() {
			ACLMessage msg = blockingReceive();
			System.out.println(this.getAgent().getName() + ": " +msg.getSender().getName() +": "+ msg.getContent());
		}
	});
	}

}
