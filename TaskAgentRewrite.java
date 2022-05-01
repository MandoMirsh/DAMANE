package agentTest;


import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.util.ArrayList;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class TaskAgentRewrite extends Agent {
	
	private void sendmes(String reciever, String msg) {
		ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
		mes.addReceiver(new AID(reciever, AID.ISGUID));
		mes.setContent(msg);
		send(mes);
	}
	
	private void printReport(String msg) {
		System.out.println(getAID().getLocalName() + ": "+ msg);
	}
	
	private String constructName(String name, String projname, String agentName) {
		return name + agentName + projname;
	}
	
	private String constructTaskName(String name, String projname) {
		return constructName(name, projname, "task");
	}   
	
	private String constructResName(String name, String projname) {
		return constructName(name, projname, "res");
	}
	private int findPredName(String name) {
		int i = 0, i2 = prev.size();
		while (i <i2){
			prev.get(i).equals(name);
			if (prev.get(i).equals(name)) return i;
			i++;
		}
		
		return -1;
	}
	private String control = "ControlAgent", creator = "SuperControlAgent";
	private String reportTo, projName = "";
	private ArrayList<String> prev = new ArrayList<>(),next = new ArrayList<>();//previous and next ones
	private ArrayList<Resource> resReq = new ArrayList<>(); //resource requirements
	private ArrayList<ResourceReserve> reserved = new ArrayList<>(); //resource reserves
	private int earlyStart = -1, earlyFinish, lateStart, lateFinish, timeReq = 0; 
	private int satisfaction = 100;//satisfaction percentage
	private int sendingnow = 0, sendfinish = -1; //sendinglist forwards
	private int rsendingnow = 0, rsendfinish = -1; //sendlist backwards
	private boolean initialFinished = false; //indicator that we have forgone initial boundaries establishment
	
	Behaviour StopSendingFinish = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(SendNewFinish);
			sendingnow = 0;
		}
	};
	
	Behaviour SendNewFinish = new CyclicBehaviour() {
		@Override
		public void action() {
			//check if messages were sent to all
			if (sendingnow > sendfinish)
				{printReport("Finished sending");
			//if yes call remover
				myAgent.addBehaviour(StopSendingFinish);}
			//send to next one
			else {
				String sendTo = next.get(sendingnow);
				printReport("Sending to: "+ sendTo);
				sendmes(sendTo,"meaf "+earlyFinish);
				sendingnow += 1;
			}
		}
	};
	
	Behaviour StopSendingStart = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(SendNewStart);
			rsendingnow = 0;
		}
	};
	Behaviour SendNewStart = new CyclicBehaviour() {
		@Override
		public void action() {
			if (rsendingnow > rsendfinish) {
				myAgent.addBehaviour(StopSendingStart);
			}
			else {
				//TODO: add else
			}
		}
	};
	Behaviour StartUpNet = new CyclicBehaviour(){
		@Override
		public void action() {
			
		}
	};
	
	
	Behaviour NextMSGProcess = new CyclicBehaviour() {
		@Override
		public void action() {
			//checking new messages
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg = myAgent.receive();
			//if there's any, process it
			if (msg!=null) {
				printReport("Message not null!");
				String[] items = msg.getContent().split(" ");
				switch (items[0].toString()){
				case "srep":printReport("srep"); break;
				case "meaf": printReport("meaf"); //MyEArlyFinish
					{Integer l = Integer.parseInt(items[1]) + 1;
						//add predecessor
						String sender = (msg.getSender().getName());
						if (findPredName(sender) == -1) {
							//printReport("new predecessor! Name's: "+ sender);
							prev.add(sender);
						}
						if (l > earlyStart) {
							//printReport("Early Start moved, sending messages. . .");
							earlyStart = l;
							earlyFinish = earlyStart + timeReq;
							myAgent.addBehaviour(SendNewFinish);
						}
					}; break;
				case "meat": //MyEArlystarT
					printReport("meat");
				 	{int l = Integer.parseInt(items[1])  - 1;
				 		if (l>lateFinish) {
				 			lateFinish = l;
				 			lateStart = lateFinish - timeReq;
				 			myAgent.addBehaviour(SendNewStart);
				 		}
				 	};break;
				case "mini": // we need to start two behaviours 1) send strt to all others 2) 
					break;
				default: printReport("else"); break;
				}
			}
		}
	};
	@Override
	public void setup() {
		//printReport("Task agent booting. . .");
		//First, we get arguments Those are for initial setup
		//TaskName, numSuc, numRes,timeNeed SUCCESSORS, RESNAMES, RESVOLUMES 
		Object args[] = getArguments();
		int sucNum = Integer.parseInt(args[1].toString()),
			resNum = Integer.parseInt(args[2].toString()),
			sucStart = 4, resNmStart = sucStart + sucNum,  
			resVolStart = resNmStart + resNum;
			
			timeReq = Integer.parseInt(args[3].toString());
				//add successors
				for (int i = 0; i<sucNum;i++) {
					next.add(args[sucStart+i].toString());
					//printReport(args[sucStart+i].toString() + " is added to successors");
				}
				//for resources
				for (int i = 0;i<resNum;i++) {
					resReq.add(new Resource(args[resNmStart+i].toString(),Integer.parseInt(args[resVolStart+i].toString())));
				}
			sendfinish = next.size() - 1;	
			this.addBehaviour(NextMSGProcess);	
			/*printReport("now I'll print which arguments were taken");	
			//let's see which ones are there
			System.out.println();
			for(int i=0;i<args.length;i++) {
				System.out.println(args[i].toString());
			}*/
		}
}
