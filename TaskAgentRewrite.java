package agentTest;


import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class TaskAgentRewrite extends Agent {
	/**
	 * @param reciever - global JADE agent name
	 * @param msg - message content to send
	 */
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
	private Map<String,Integer> mPrev = new HashMap<String,Integer>(),mNext = new HashMap<String,Integer>();
	private ArrayList<ResourceReserve> reserved = new ArrayList<>(); //resource reserves
	private int earlyStart = -1, earlyFinish, lateStart, lateFinish = 0, timeReq = 0; 
	private int satisfaction = 100;//satisfaction percentage
	private boolean initialFinished = false; //indicator that we have forgone initial boundaries establishment
	private Integer gotMes1 = 0, gotMes2 = 0, mesToGet1 = 0, mesToGet2;//mesToGet1 - how many messages will I get if I go forwards the graph, mesToget2 - backwards.
	private MessagesToSend sendQueue = new MessagesToSend();
	private boolean workInPorgressFlag = false,//the net is initialized and this very task has every right to carry on negotiations with resources
					jobIsPlanned = false,//this one is flag for initialization;
					myStartIsMoved = false;//this one is flag for changes in initialization  
	private Integer resAnswCount = 0;//how many Resourse agents replied so far during negotiations
	private JobWeight myWeight;
	private ResDescStore resourseDescs = new ResDescStore(); 
	private Integer toRecieveFinishes = 0, toRecieveStarts = 0; 
	
	
	private void updateEarly(int newStart) {
		earlyStart = newStart;
		earlyFinish = earlyStart + timeReq;
	}
	private void updateLate(int newFinish) {
		lateFinish = newFinish;
		lateStart = lateFinish - timeReq;
	}
	private boolean needShift() {
		return (earlyFinish>lateFinish);
	}
	
	
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
	
	Behaviour Reporting = new OneShotBehaviour(){
		@Override
		public void action() {
			ArrayList <String> recievers = new ArrayList<>();
			recievers.add(myAgent.getArguments()[0].toString());
			String message = "rrep " + (jobIsPlanned?1:0) + " " + earlyStart + " " + earlyFinish + " "  
							+ myWeight.getWeights().get(0) + " " + myWeight.getWeights().get(1); 
				printReport(message);		
				//sendQueue.add( new SendingTask(recievers, message));
		}
	};
	Behaviour NegotiationStart = new OneShotBehaviour() {
		@Override
		public void action() {
			String mesPrefics = "rreq "+ earlyStart + " " + timeReq + " ", mesPostfix = " "+myWeight.getWeights().get(0) + " "+myWeight.getWeights().get(1);
			ArrayList <String> recievers = new ArrayList<>();
			for (int i = 0;i<resourseDescs.size();i++) {
				ResourceDescriptor tmp = resourseDescs.get(i);
				tmp.removeComplete();
				resourseDescs.set(i, tmp);
				String message = mesPrefics +tmp.volume()+ mesPostfix;	//int start, int span, int volume, JobWeight mark
				recievers.add(tmp.getName());
				sendQueue.add(new SendingTask(recievers,message));
			}
			resAnswCount = 0;
			myAgent.addBehaviour(IfNegoiationEnded);
		}
	};
	Behaviour IfNegoiationEnded = new CyclicBehaviour() {
		@Override
		public void action() {
			if (resAnswCount == resourseDescs.size()) {
				myAgent.addBehaviour(NegotiationStop);
			}
		} 
	};
	Behaviour NegotiationStop = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(IfNegoiationEnded);
			if  (resourseDescs.allSet())
			{ //really ended:
				if (myStartIsMoved)
					myAgent.addBehaviour(NegotiationStart);
				else 
				{
					printReport("Negotiations finished!");
					//sending confirmation to resourse agents
					ArrayList <String> recievers = new ArrayList<>();
					for (int i = 0;i<resourseDescs.size();i++) {
						recievers.add(resourseDescs.get(i).getName());
					}
					sendQueue.add(new SendingTask(recievers,"rget"));
					recievers.clear();
					recievers.add(myAgent.getArguments()[0].toString());
					sendQueue.add(new SendingTask(recievers,"tire"));//tire - Task Is REady, tnre - Task Not REady
					jobIsPlanned = true;// setting flag that the negotiations ended and task Agent is now just listening
					recievers.clear();
				}
			}
			else
			{
				//сменить свой ранний старт и ранний финиш
				updateEarly(resourseDescs.maxDate());
				if (needShift())//если ранний финиш >позднего старта,  
				{
					//извещаем последователей о своём раннем финише
					toRecieveStarts = next.size();
					SendNewFinish();
					// активируем поведение-заглушку, которое ждёт ответа от последователей о своих ранних стартах и только потом рестартит процесс договора.
					myAgent.addBehaviour(startCollection);
				}
				
					 
					
			}
		}
	};
	//смотрим, получили ли ответы о старте от последователей, если да - начинаем переговоры с ресурсами заново.
	Behaviour startCollection = new CyclicBehaviour() {
		@Override
		public void action() {
			if (toRecieveStarts == 0) {
				myAgent.addBehaviour(NegotiationStart);
			}
				
		}
		
	};
	//заготовка поведения для ужимки с конца.( вроде бы нет вероятноститого, что это понадобится, но на всякий)
	Behaviour finishCollection = new CyclicBehaviour() {
		@Override
		public void action() {
			if (toRecieveFinishes == 0) {
				myAgent.addBehaviour(NegotiationStart);
			}
				
		}
		
	};
	void SendNewFinish() {
			ArrayList <String> recievers = new ArrayList<String>(next);
			String message = "meaf "+ (earlyFinish);
			sendQueue.add(new SendingTask(recievers,message));
	};
	Behaviour SendNewStart = new OneShotBehaviour() {
		@Override
		public void action() {
			ArrayList <String> recievers = new ArrayList<String>(prev);
			String message = "meat "+ (lateStart);
			sendQueue.add(new SendingTask(recievers,message));
		}
	};
	
	// 
	Behaviour NxtMSGProc = new CyclicBehaviour() {
		@Override
		public void action() {
			//checking new messages
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg = myAgent.receive();
			if (msg!=null) {
				String[] items = msg.getContent().split(" ");
				switch (items[0].toString()){
				case "meat": //MylatEstArT
					//printReport("gotmeat");
					toRecieveStarts--;
				 	{int l = Integer.parseInt(items[1]);
				 		if (l>lateFinish) {
				 			updateLate(l);
				 			sendQueue.add(new SendingTask(prev, "meat " + lateStart));
				 			toRecieveFinishes += prev.size();
				 			myWeight = new JobWeight(earlyFinish - lateStart,lateFinish - earlyStart);
				 		}
				 	}; break;
				case "meaf":{Integer l = Integer.parseInt(items[1]);
						if (l > earlyStart) {
							updateEarly(l);
							//set flag that start is gonna be changed
							//myAgent.addBehaviour(SendNewFinish); 
							//sendmes(sendTo,"meaf "+earlyFinish);
							if (gotMes1>mesToGet1)
								sendQueue.add(new SendingTask(next,"meaf "+earlyFinish));
					
						}
						if (gotMes1==mesToGet1) {
							sendQueue.add(new SendingTask(next,"meaf "+earlyFinish));
						}
					}; break;
				//negotiations income handler (both acre and dere)
				case "acre":{
					String sender = msg.getSender().getName();
					resourseDescs.setUp(sender);
					resAnswCount++;
				};break;
				case "dere":{
					resAnswCount++;
					printReport("resAnswCount: "+resAnswCount);
					String sender = msg.getSender().getName();
					resourseDescs.setDate(sender, Integer.parseInt(items[1]));
				};break;
				case "srep":{
					//старт поведения-отчётности
				}
				}
				
			}
		}
	};
	//time to start negotiations
	Behaviour ChangeSendingBeh = new OneShotBehaviour() {
		@Override
		public void action() {
			myAgent.removeBehaviour(NextMSGProcess);
			myAgent.addBehaviour(NxtMSGProc);
			myAgent.addBehaviour(NegotiationStart);
			for (String s:next) {
			mNext.put(s, lateFinish);	
			}
			for (String s:prev) {
				mPrev.put(s, earlyStart);
			}
		}
	};
	//MSG Process atStartUp
	Behaviour NextMSGProcess = new CyclicBehaviour() {
		@Override
		public void action() {
			//checking new messages
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg = myAgent.receive();
			//if there's any, process it
			if (msg!=null) {
				String sender = (msg.getSender().getName());
				//printReport("Message not null!");
				String[] items = msg.getContent().split(" ");
				switch (items[0].toString()){
				case "srep":printReport("srep");{String message = "";// rept START FINISH
				ArrayList<String> send = new ArrayList<>();
				send.add(sender);
				sendQueue.add(new SendingTask(send,"rept "+earlyFinish));
				} break;
				case "meaf": //printReport("meaf"); //MyEArlyFinish
					{Integer l = Integer.parseInt(items[1]);
						//count initial run messages
						gotMes1++;
						
						if (l > earlyStart) {
							//printReport("Early Start moved, sending messages. . .");
							updateEarly(l);
							//myAgent.addBehaviour(SendNewFinish); 
							//sendmes(sendTo,"meaf "+earlyFinish);
							if (gotMes1>mesToGet1)
								sendQueue.add(new SendingTask(next,"meaf "+earlyFinish));
							
						}
						if (gotMes1==mesToGet1) {
							sendQueue.add(new SendingTask(next,"meaf "+earlyFinish));
						}
					}; break;
				case "meat": //MylatEstArT
					//printReport("gotmeat");
				 	{int l = Integer.parseInt(items[1]);
				 		gotMes2++;
				 		if (l>lateFinish) {
				 			updateLate(l);
				 			if (gotMes2>mesToGet2)
				 				sendQueue.add(new SendingTask(prev, "meat " + lateStart));
				 		}
				 		if (gotMes2==mesToGet2) {
				 			//printReport("sentmeat");
				 			sendQueue.add(new SendingTask(prev, "meat " + lateStart));
				 			myWeight = new JobWeight(earlyFinish - lateStart,lateFinish - earlyStart);
				 			//sendQueue.add(new SendingTask(prev, "tiko "+ (earlyFinish - lateStart) + " " + (lateFinish - earlyStart)));
				 			
				 		}
			 				
				 	};break;
				 	
				case "mini":{// we need to start two behaviours 1) send strt to all others 2) 
						//Проблема - сообщения с нулями после начальной инициализации избыточны.
						Integer sendTo1 = Integer.parseInt(items[1]);
						//add predecessor
						if (findPredName(sender) == -1) {
							//printReport("new predecessor! Name's: "+ sender);
							prev.add(sender);
							mesToGet1++;
						}
						//printReport("input "+ msg.getSender().getName() + " "+ msg.getContent());
						ArrayList<String> send1 = new ArrayList<>(), send2 = new ArrayList<>(next);
						send1.add(next.get(0));
						send2.remove(0);
						if (!initialFinished) {
							//printReport("initiated!");
							if (send2.size()!=0)//"mini 0" after initial transmission is unnecessary
								sendQueue.add(new SendingTask(send2,"mini 0"));
							initialFinished = true;//initial transmission has been made
							sendTo1++;
							//sendQueue.add(new SendingTask(send1,"mini 1"));
						}
						sendQueue.add(new SendingTask(send1, "mini " + sendTo1));
						//sendQueue.add(new SendingTask(send1, msg.getContent()));
				}
					break;
				case "remp": {

					int i = findPredName(sender); 
					if (i>-1) {
						prev.remove(i);
						mesToGet1--;
					}
					};break;
				case "stup":{
					//printReport("stup");
					sendQueue.add(new SendingTask(next, "stup"));
					myAgent.addBehaviour(ChangeSendingBeh);
				};break;
				//case "tiko":{};break; 
				default: printReport("else" + msg.getContent()); break;
				}
			
			}
		}
	};
	void skipUpForTesting() {
		addBehaviour(NxtMSGProc);
		addBehaviour(NegotiationStart);
		this.addBehaviour(SendingBehaviour);
		next.add("pr1task3@127.0.0.1:80/JADE");
		prev.add("pr1task3@127.0.0.1:80/JADE");
		initialFinished = true;
	}
	void startUp(String controller) {
		sendmes(controller,"stup");
		this.addBehaviour(NextMSGProcess);
		this.addBehaviour(SendingBehaviour);
	}
	@Override
	public void setup() {
		//printReport("Task agent booting. . .");
		//First, we get arguments Those are for initial setup
		//TaskName, numSuc, numRes,timeNeed SUCCESSORS, RESNAMES, RESVOLUMES 
		Object args[] = getArguments();
		int sucNum = Integer.parseInt(args[2].toString()),
			resNum = Integer.parseInt(args[3].toString()),
			sucStart = 5, resNmStart = sucStart + sucNum,  
			resVolStart = resNmStart + resNum;
			timeReq = Integer.parseInt(args[4].toString()); 
				
				//add successors
				for (int i = 0; i<sucNum;i++) {
					next.add(args[sucStart+i].toString());
					//printReport(args[sucStart+i].toString() + " is added to successors");
				}
				
				//add resource descriptors
				ResourceDescriptor tmp;
				for (int i = 0;i<resNum;i++) {
					tmp = new ResourceDescriptor(args[resNmStart+i].toString(), timeReq, Integer.parseInt(args[resVolStart+i].toString()));
					//printReport("res: " + tmp.getName() + " " + tmp.volume());
					resourseDescs.add(tmp);
				}

			mesToGet2 = next.size();
			
			startUp(args[0].toString());
			//printReport("stup");
			/*printReport("now I'll print which arguments were taken");	
			//let's see which ones are there
			System.out.println();
			for(int i=0;i<args.length;i++) {
				System.out.println(args[i].toString());
			}*/
		}
}
