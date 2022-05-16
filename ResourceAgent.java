package agentTest;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.Color;

import java.awt.Font;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
public class ResourceAgent extends Agent{
	//gets ResName, ResVolume, PlanningHorizon
	private Map<String, String> commands = new HashMap<String,String>(), outputVoc = new HashMap<String, String>();
	private Map<Integer, String> requestStatusCollection = new HashMap <Integer,String>();
	private ArrayList<Integer> resavaliability = new ArrayList<Integer>();
	private ArrayList<Integer> extraResources = new ArrayList<>();
	private ArrayList<ResourceReserve> reserves = new ArrayList<ResourceReserve>();
	//private ArrayList<>
	
	//graph part
	JFrame frame = new JFrame();
	XYSeries series = new XYSeries("2022");
	private JFreeChart reportChart;
	XYDataset dataset = new XYSeriesCollection();
	ChartPanel chartPanel;
	private void initUI() {
		frame.setTitle(this.getAID().getLocalName() + " plot");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setResizable(false); 
        frame.setBounds(100, 100, 650, 220);
        dataset = new XYSeriesCollection();
        ((XYSeriesCollection) dataset).addSeries(series);
		reportChart = ChartFactory.createXYLineChart("Resourse Logging","Time","Value",dataset,
				PlotOrientation.VERTICAL, true, true, false);
		XYPlot plot = reportChart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);
        
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);
        
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);
        
        reportChart.getLegend().setFrame(BlockBorder.NONE);
        
        reportChart.setTitle(new TextTitle("Resourse Logging with Time",
                new Font("Serif", java.awt.Font.BOLD, 18)));
        chartPanel = new ChartPanel(reportChart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);
        frame.add(chartPanel);
	}
	int LogTime = 50;
	private Integer addNextLog() {
		//printReport("" + LogTime);
		Integer ret = getLog();
		series.add(++LogTime,getLog());
		return ret;
	}
	private ResReqPriorQueue requests = new ResReqPriorQueue();
	private String resName, reportTo;
	private int firstres = 0, lastres = 0;// for report testing set lastres 10 else set this 0 at startup. lastres is a very first not affected day.
	private int resVolume;
	private int satisfaction; 
	private static final int LOGGING_FREQUENCY = 10;
	private int loggingtime = 0;
	private boolean startedNegotiations = false;
	private MessagesToSend sendQueue = new MessagesToSend();
	private void addres(int start, int days, int volume) {
		for (int i = 0; i<days;i++) {
			Integer l = resavaliability.get(i+start);
			l+=volume;
			resavaliability.set(i+start, l);
		}
	}
	private void subres(int start, int days, int volume) {
		for (int i = 0; i<days;i++) {
			Integer l = resavaliability.get(i+start);
			l-=volume;
			resavaliability.set(i+start, l);
		}
	}

	//TODO BLOCK
	private int getLog() {
		Integer ret = 100;
		for (int i = 0;i <resavaliability.size();i++) {
			if (ret>resavaliability.get(i))
				ret = resavaliability.get(i);
		}
		return ret;
	}
	Behaviour Logging = new CyclicBehaviour() {
		@Override
		public void action() {
			if (loggingtime == 0)
			{
				loggingtime = LOGGING_FREQUENCY;
				
				sendmes(reportTo,labelToCommand("REPORT_STATUS") + " " + addNextLog());
			}
			else loggingtime--;
		}
	};
	//TODO: make sure this works properly
	private Double countInitial() {
			
		return 0.0;
	}
	private double countfurther() {
		return 0.0;
	}
	
	private void outputVocabularyInit() {
		outputVoc.put("RESERVE_ACCEPTED", "acre");
		outputVoc.put("RESERVE_DECLINED", "dere");
		outputVoc.put("REPORT_STATUS", "rare");
	}
	//TODO ENDS
	
	//REQUEST_RECIEVED = 0, REQUEST_IN_PROCESS = 1, REQUEST_ACCEPTED = 2
	private void requestStatusInitialization() {
		requestStatusCollection.put(0, "REQUEST_RECIEVED");
		requestStatusCollection.put(1, "REQUEST_IN_PROCESS");
		requestStatusCollection.put(2, "REQUEST_ACCEPTED");
	}
	//"rreq" "RESOURSE REQUIRED", "rget" "GET RESERVED", "rref" "GIVE_BACK_RESERVE", "strt" "START_NEGOTIATIONS"
	private void initiateVocabulary() {	
		commands.put("rreq", "RESOURSE_REQUIRED");
		commands.put("rget", "GET_RESERVED");
		commands.put("rref", "GIVE_BACK_RESERVE");
		commands.put("strt", "START_NEGOTIATIONS");
		commands.put("suba", "DEFICITE_EVENT");
		commands.put("adda", "PROFICITE_EVENT");
		commands.put("srep", "REPORT_REQUEST");
		//commands.put("shpl", "SHOW_PLOT");
		commands.put("srep", "SHOW_PLOT");
	};
	String getREquestStatusLabel(Integer n) {
		return requestStatusCollection.get(n);
	}
	String CommandExplain(String command) {
		if (commands.get(command) == null)
			return "UNKNOWN_MESSAGE";
		return commands.get(command);
	}
	String labelToCommand(String label) {
		return outputVoc.get(label);
	}
	private void sendmes(String reciever, String msg) {
		ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
		mes.addReceiver(new AID(reciever, AID.ISGUID));
		mes.setContent(msg);
		send(mes);
	}
	//TODO: incorporate sendmes instead of all another code about sending messages 
	private void printReport(String msg) {
		System.out.println(getAID().getLocalName() + ": "+ msg);
	}
	private Integer reservePos(String name) {
		boolean found = false;
		int i = 0, len = reserves.size();
		while ((!found) & (i<len)) {
			if (reserves.get(i).name.equals(name)) {
				found = true;
			}else i++;	
		}
		if (found) return i;
		return -1;
		
	}
	//search for closest timespan with resources avaliable
	/**
	 * 
	 * @param start starting day
	 * @param volume resource volume
	 * @param longevity for how many days need to reserve
	 * @return possible closest date to make reserve
	 */
	private Integer fetchApprTimespan(Integer start, Integer volume, Integer longevity ) {
		//printReport("RESAVAIL: " + resavaliability.get(1) + " VOLUME: "+ volume);
		boolean found = false;
		Integer shift = -1;
		if (volume == 0)
			return start;
		while (!found) {
			shift++;
			found = true;
			for (int i = 0; i<longevity;i++) {
				if (resavaliability.get(i+start +shift) < volume) {
					found = false;
					break;
				}
			}
		}
		return (start+shift);	
	}
	//ChartFactory.createLineChart(myAgent.getAID().getName() + " max")
	/*createLineChart(String title,
                           String categoryAxisLabel,
                           String valueAxisLabel,
                           CategoryDataset dataset);*/
	public String getLocalName(AgentController ac) {
		String s;
		try {
			s = ac.getName();
			s= s.substring(0,s.indexOf('@'));
		} catch (StaleProxyException e) {
			s = "";
		}
		
		return s;
	}
/*	Behaviour stopReporting = new OneShotBehaviour() {
	 	@Override
		public void action() {
	 		//printReport("invoked stopReporting");
			myAgent.removeBehaviour(IfFinishedRep);
			//send message that report is finished
			ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
			msg1.addReceiver(new AID(reportTo, AID.ISGUID));
			msg1.setContent("rres "+ resName + " " + resVolume);
			myAgent.send(msg1);
		}
	};

	Behaviour IfFinishedRep = new CyclicBehaviour() {
		@Override
		public void action() {
			//printReport("invoked IfFinishedRep");
			//check if report is finished
			if (firstres == lastres) {
				myAgent.removeBehaviour(Reporting);
				myAgent.addBehaviour(stopReporting);
				firstres = 0;
			}
		}
	};
	Behaviour Reporting = new CyclicBehaviour() {
		@Override
		public void action() {
			//printReport("invoked Reporting");
			if (firstres<lastres) {
				//printReport("invoked message assembling");
				//report next piece
				ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
				//printReport("message creation was successful");
				mes.addReceiver(new AID(reportTo, AID.ISGUID));
				//printReport("message start");
				//form message content: resname daynum volume	
				mes.setContent(resName + " " + (firstres) + " " + resavaliability.get(firstres));
				myAgent.send(mes);
				firstres++;
				}
		}
	};*/
	Behaviour LoggingBehaviour = new CyclicBehaviour() {
		@Override
		public void action() {
			
		}
	};
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
	
	Behaviour NextRequestProcessing = new CyclicBehaviour() {
		@Override
		public void action() {
			ResourceRequest tmp = requests.getNext();
			if (tmp!=null) { 
				ArrayList<String> recievers = new ArrayList<>();
				recievers.add(tmp.getName());
				switch (getREquestStatusLabel(tmp.getStatus())) {
					case "REQUEST_RECIEVED":
						{	
							 printReport("REQUEST FROM: " + tmp.getName());
							//смотрим есть ли возможность поставить на нужную дату.
							int posDate = fetchApprTimespan(tmp.getStart(), tmp.volume(), tmp.longevity());//possible closest date to make reserve
							//printReport("timespan: "+posDate);
							if ( posDate == tmp.getStart())
								{//да:
									//меняем статус на REQUEST_IN_PROCESS,
									tmp.setStatus(ResourceRequest.REQUEST_IN_PROCESS);
									//шлём готовность к резерву
									sendQueue.add(new SendingTask(recievers,labelToCommand("RESERVE_ACCEPTED") + " " + posDate));
									//printReport("XX" + "RAC" + recievers.get(0));
									//запихиваем обратно
									requests.updateReq(tmp);
								}
							else//нет: 
								{
									//шлём отказ с новой датой
								printReport("decline reserve: "+ recievers.get(0));
									sendQueue.add(new SendingTask(recievers,labelToCommand("RESERVE_DECLINED") + " " + posDate));
								}
						};
					break;
					case "REQUEST_IN_PROCESS":// = 1
						{
							//printReport("REQUEST_IN_PROCESS");
							requests.updateReq(tmp);
						}
					break;
					case "REQUEST_ACCEPTED":// = 2
					{
						// убираем доступность ресурса.
						//printReport("REQUEST_ACCEPTED");
						int i1, i2,n;
						i1 = tmp.getStart();
						i2 = tmp.longevity();
						n = tmp.volume();
						for (int i = 0 ;i<=i2;i++) {
							int a = resavaliability.get(i+i1);
							//printReport("there's " + a + " at the " + i+i1 + " place");
							resavaliability.set(i+i1, a-n);
						}
					}
				break;
				}
			}
		}
	};
	Behaviour NextMessage = new CyclicBehaviour() {
		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg = myAgent.receive();
			if (msg!=null) {
				String[] items = msg.getContent().split(" ");
				switch (CommandExplain(items[0])) {
					case "RESOURSE_REQUIRED"://reserve recieved: start span volume mark.N1 mark.N2 
						{
							//printReport("rreq");
							ResourceRequest tmp = new ResourceRequest(msg.getSender().getName(),Integer.parseInt(items[1].toString()) ,
									Integer.parseInt(items[2].toString()), Integer.parseInt(items[3].toString()),
									new JobWeight(Integer.parseInt(items[4].toString()),Integer.parseInt(items[5].toString())));
							requests.updateReq(tmp);
							//TODO: uncomment printReport(items[1].toString());
						}
					break;
					case "GET_RESERVED":
						{
							//printReport("rget");
							//достаём по имени отправившего запрос из очереди.
							ResourceRequest tmp = requests.getByName(msg.getSender().getName());
							//если он достался, проверяем статус. Если он REQUEST_IN_PROCCESS, то меняем на REQUEST_ACCEPTED. И в любом случае после этого обратно пихаем.
							if (tmp !=null) {
								if (tmp.getStatus() == ResourceRequest.REQUEST_IN_PROCESS) {
									tmp.setStatus(ResourceRequest.REQUEST_ACCEPTED);
									subres(tmp.getStart(),tmp.longevity(),tmp.volume());
								}
							}
							else
								printReport("Sudden accept! from: " + msg.getSender());//no requests for sender to accept
						}
					break;
					case "GIVE_BACK_RESERVE":
					{
						//смотрим, сколько ресурсов освободилось, на какой срок и подкидываем их в топку
						
						//rref DATE, T, N
						
						int i1, i2,n;
						i1 = Integer.parseInt(items[1]);
						i2 = Integer.parseInt(items[2]);
						n = Integer.parseInt(items[3]);
						/*for (int i = 0 ;i<=i2;i++) {
							int a = resavaliability.get(i+i1);
							//printReport("there's " + a + " at the " + i+i1 + " place");
							resavaliability.set(i+i1, a+n);
						}*/
						addres(i1,i2,n);
					}break;
					case "START_NEGOTIATIONS":{
						if (!startedNegotiations) {//negotiations to start. must work only time. Either way behavious is to be added ONLY if not active
							myAgent.addBehaviour(NextRequestProcessing);
							//send back 
							myAgent.addBehaviour(Logging);
							Integer toSend = resVolume - requests.getMaxNeed();
							startedNegotiations = true;
							reportTo = msg.getSender().getName();
							sendmes(reportTo,labelToCommand("REPORT_STATUS") + " " + toSend.toString());
						}
						else {
							Double toSend = countfurther();	
							//sendmes(msg.getSender().getName(),"rrep " + toSend.toString());
						}
					} break;
					case "REPORT_REQUEST":
						break;
					case "SHOW_PLOT":
					{
						initUI();
						frame.setVisible(true);
						
					}break;
					case "UNKNOWN_MESSAGE": printReport(msg.getContent()); break;
					
				}
			}
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
				String[] items = msg.getContent().split(" ");
				//case statements after switch (switchdef) are equvalent of switchdef.equal() so this should suffice.
				/*switch (items[0]){
				 * case "suba": {// try to get resource: " subtract avaliability"
					int i1 = Integer.parseInt(items[1])-1,
						i2 = Integer.parseInt(items[2])-1,
						n  = Integer.parseInt(items[3]);
					System.out.println("Reserve requested from " + i1 + " till " + i2);
					//lets see if there is needed 
					//int prefplace = i1;
					int i3 = fetchApprTimespan(i1,n,i2-i1);
					i2 += i3 - i1;
					i1 = i3;
					//System.out.println("Trying to reserve from " + (i1+1) + " till " + (i2+1));
					for (int i = i1 ;i<=i2;i++) {
						int a = resavaliability.get(i);
						//printReport("there's " + a + " at the " + i + " place");
						resavaliability.remove(i);
						resavaliability.add(i, a - n);
						//if (i!=0) System.out.print(resavaliability.get(i-1) +" ");
						//System.out.println(resavaliability.get(i) + " " + resavaliability.get(i+1));
					}
					//create reserve	
					reserves.add(new ResourceReserve(msg.getSender().getName(),i1, n, i2-i1+1));
					System.out.println(msg.getSender());
					if (lastres<i2) {
						lastres = i2;
					}//
					ACLMessage mes =  new ACLMessage(ACLMessage.INFORM);
					mes.setContent("resq " + i1 + " " + i2);
					mes.addReceiver(new AID(msg.getSender().getName(),AID.ISGUID));
					myAgent.send(mes);
				}; break; 
				 * case "adda":{
					int i1 = Integer.parseInt(items[1])-1,
						i2 = Integer.parseInt(items[2])-1,
						n  = Integer.parseInt(items[3]);
					System.out.println("Trying to collect to " + i1 + " till " + i2);
					for (int i = i1 ;i<=i2;i++) {
						int a = resavaliability.get(i);
						printReport("there's " + a + " at the " + i + " place");
						resavaliability.remove(i);
						resavaliability.add(i, a + n);
						if (i!=0) System.out.print(resavaliability.get(i-1) +" ");
						System.out.println(resavaliability.get(i) + " " + resavaliability.get(i+1));
					}; break; // give resource back: "add avaliability"
				 * case "acre": {
				//подтверждение резерва	ACcept REserve
					printReport("Recieved acceptance of reserve");
				  //проверяем наличие резерва на имя отправителя полученного сообщения.
				    String resOwner = msg.getSender().getName();//msg.getSender().getName() - имя отправителя.
				    int i = reservePos(resOwner);
				    if (i ==-1) {
				    	//нет резерва, отвечаем, что ошибка.
				    	printReport("No reserve found!");
				    	sendmes(msg.getSender().getName(),"nres");
				    }else {
				    	//есть резерв
				    	printReport("Reserve found, at position " + i);
				    	//шлём сообщение о подтверждённом выделении
				    	
				    	//убираем резерв
				    	reserves.remove(i);
				    	// для удаления по индексу необходимо, чтобы индекс был int, не Integer, т.к. Integer наследуется от object и вызовется remove(object) вместо remove(int) 
				    	
				    }; break; // reservation confirmation : "accept reserve";
				 * case "srep":{//report routine start
					ACLMessage msg1 =  new ACLMessage(ACLMessage.INFORM);
					reportTo = msg.getSender().getName();
					msg1.addReceiver(new AID(reportTo, AID.ISGUID));
					msg1.setContent("rres "+ resName + " " + resVolume);
					myAgent.send(msg1);
					if (lastres == 0) {//nothing to report
						msg1.setContent("rref "+ resName);
						myAgent.send(msg1);
					} else { //we can report something
						myAgent.addBehaviour(Reporting);
						myAgent.addBehaviour(IfFinishedRep);
					}
				}; break; // start routine to send report back to message sender. Only one report at a time is supported!
				case
				 * }
				 * */ 
				if (items[0].equals("suba")) {// try to get resource: " subtract avaliability"
					int i1 = Integer.parseInt(items[1])-1,
						i2 = Integer.parseInt(items[2])-1,
						n  = Integer.parseInt(items[3]);
					System.out.println("Reserve requested from " + i1 + " till " + i2);
					//lets see if there is needed 
					//int prefplace = i1;
					int i3 = fetchApprTimespan(i1,n,i2-i1);
					i2 += i3 - i1;
					i1 = i3;
					System.out.println("Trying to reserve from " + (i1+1) + " till " + (i2+1));
					for (int i = i1 ;i<=i2;i++) {
						int a = resavaliability.get(i);
						printReport("there's " + a + " at the " + i + " place");
						resavaliability.remove(i);
						resavaliability.add(i, a - n);
						//if (i!=0) System.out.print(resavaliability.get(i-1) +" ");
						//System.out.println(resavaliability.get(i) + " " + resavaliability.get(i+1));
					}
					//create reserve	
					reserves.add(new ResourceReserve(msg.getSender().getName(),i1, n, i2-i1+1));
					System.out.println(msg.getSender());
					if (lastres<i2) {
						lastres = i2;
					}//
					ACLMessage mes =  new ACLMessage(ACLMessage.INFORM);
					mes.setContent("resq " + i1 + " " + i2);
					mes.addReceiver(new AID(msg.getSender().getName(),AID.ISGUID));
					myAgent.send(mes);
				} else if (items[0].equals("adda")){
					int i1 = Integer.parseInt(items[1])-1,
						i2 = Integer.parseInt(items[2])-1,
						n  = Integer.parseInt(items[3]);
					System.out.println("Trying to collect to " + i1 + " till " + i2);
					for (int i = i1 ;i<=i2;i++) {
						int a = resavaliability.get(i);
						printReport("there's " + a + " at the " + i + " place");
						resavaliability.remove(i);
						resavaliability.add(i, a + n);
						if (i!=0) System.out.print(resavaliability.get(i-1) +" ");
						System.out.println(resavaliability.get(i) + " " + resavaliability.get(i+1));
					}
					
				} 
				else if (items[0].equals("sbta")) {
					int i1 = Integer.parseInt(items[1])-1,
							i2 = Integer.parseInt(items[2])-1,
							n  = Integer.parseInt(items[3]);
						System.out.println("Trying to reserve from " + i1 + " till " + i2);
						//lets see if there is needed 
						if (n > resVolume) {
							ACLMessage mes3 = new ACLMessage(ACLMessage.INFORM);
							mes3.addReceiver(msg.getSender());
							mes3.setContent("cant");
							myAgent.send(mes3);
						}
						int prefplace = i1;
						prefplace = fetchApprTimespan(i1,n,i2-i1); 
						//printReport("found start in: "+ prefplace);
				}
				else if (items[0].equals("srep")) {//report routine start
					ACLMessage msg1 =  new ACLMessage(ACLMessage.INFORM);
					reportTo = msg.getSender().getName();
					msg1.addReceiver(new AID(reportTo, AID.ISGUID));
					msg1.setContent("rres "+ resName + " " + resVolume);
					myAgent.send(msg1);
					if (lastres == 0) {//nothing to report
						msg1.setContent("rref "+ resName);
						myAgent.send(msg1);
					} //else { //we can report something
						//myAgent.addBehaviour(Reporting);
						//myAgent.addBehaviour(IfFinishedRep);
					}
				//}
				else if (items[0].equals("repr")) {
					for(ResourceReserve i: reserves) {
						printReport(i.volume + " " + i.date + " " + i.days);
					}
				}
				else if (items[0].equals("acre")){
				//подтверждение резерва	ACcept REserve
					//printReport("Recieved acceptance of reserve");
				  //проверяем наличие резерва на имя отправителя полученного сообщения.
				    String resOwner = msg.getSender().getName();//msg.getSender().getName() - имя отправителя.
				    int i = reservePos(resOwner);
				    if (i ==-1) {
				    	//нет резерва, отвечаем, что ошибка.
				    	printReport("No reserve found!");
				    	sendmes(msg.getSender().getName(),"nres");
				    }else {
				    	//есть резерв
				    	printReport("Reserve found, at position " + i);
				    	//шлём сообщение о подтверждённом выделении
				    	
				    	//убираем резерв
				    	reserves.remove(i);
				    	// для удаления по индексу необходимо, чтобы индекс был int, не Integer, т.к. Integer наследуется от object и вызовется remove(object) вместо remove(int) 
				    	
				    }
				    
				    
				}else if (items[0].equals("care")){
					//отмена резерва CAncel REserve
					//care VOLUME TIME
					//printReport("Recieved cancellation of reserve");
					String resOwner = msg.getSender().getName();//msg.getSender().getName() - имя отправителя.
				    int i = reservePos(resOwner);
				    if (i != -1) {
				    	
				    	//add resource from reserve
				    	reserves.remove(i);
				    }
				}
			}
		}
	};
	
	
	@Override
	public void setup() {
		initiateVocabulary();
		outputVocabularyInit();
		requestStatusInitialization();
		//JOptionPane.showMessageDialog(null , "Resource Agent name is:" + getAID().getLocalName());
		//System.out.println("Resource Agent name is: "+ getAID().getName());
		Object args[] = getArguments();
		if (args!=null && args.length>0) {
			//printReport("Invoked args getter");
			//reading arguments.
			//ResName,ResVolume,PlanningHorizon;
			resName = args[0].toString();
			resVolume = Integer.parseInt(args[1].toString());
			int planningHorizon = Integer.parseInt(args[2].toString());
			//printReport("horizon: "+ planningHorizon);
			printReport("Volume: " + resVolume);
			requests = new ResReqPriorQueue(planningHorizon);
			for (int i = 0;i < planningHorizon;i++) {
				resavaliability.add(resVolume); 
				extraResources.add(0);
			}
			//System.out.println(resavaliability.size() + " " + resavaliability.get(0) + " " + resavaliability.size());
			
		}
		else System.out.println("no arguments!");  
		addBehaviour(NextMessage);
		addBehaviour(SendingBehaviour);
	}
}