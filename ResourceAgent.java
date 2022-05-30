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
	private ArrayList<Integer> resReserved = new ArrayList<Integer>();
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
		commands.put("strn", "START_NEGOTIATIONS");
		commands.put("suba", "DEFICITE_EVENT");
		commands.put("adda", "PROFICITE_EVENT");
		commands.put("srep", "REPORT_REQUEST");
		//commands.put("shpl", "SHOW_PLOT");
		commands.put("srep", "SHOW_PLOT");
		commands.put("abre", "ABORT_NEGOTIATIONS");
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
	
	
	private static final Integer INIT = 0, LOOKUP_NEXT = 1, NEGOTIATION = 2, STOPPING = 3, STOP = 4;
	private static final Integer NEG_STOP = 0, NEG_START = 1, NEG_RESPONCE_WAIT = 2, NEG_WAIT = 3; 
	private String negNow = "";
	
	private Integer negotiationStatus, agentStatus, proposal; 
	ArrayList<String> requestQueue = new ArrayList<>();
	Map<String, Integer> starts = new HashMap<String, Integer>(), longevities = new HashMap<String, Integer>(), volumes = new HashMap<String, Integer>();
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
		return (start+shift);	// return start;
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
	Behaviour Negotiation = new OneShotBehaviour() {
		@Override
		public void action() {
			//if we don't have needed time
			ArrayList<String> recievers = new ArrayList<>();
			recievers.add(negNow);
			int volume = volumes.get(negNow), start = starts.get(negNow), longevity = longevities.get(negNow);
			//Checking if we can give enough resources on time.
			proposal = fetchApprTimespan(start, volume, longevity);
			printReport("trying to give proposal " + proposal + " to: "+ negNow);
			//send proposal. after to-do: remade this into one proposal, so it can be accepted or declined 
			if (proposal == start) {
			//if we can=> sending: ok, we can do this, please confirm.
				printReport("And it's a match, so we wait to give resources");
				negotiationStatus = NEG_RESPONCE_WAIT;
				sendQueue.add(new SendingTask(recievers,labelToCommand("RESERVE_ACCEPTED") + " " + proposal));
			}
			else
			{
				printReport("It's no match, so we must lookup someone else. . .");
				negotiationStatus = NEG_STOP;
				sendQueue.add(new SendingTask(recievers,labelToCommand("RESERVE_DECLINED") + " " + proposal));
			}
		}
	};
	Behaviour NegotiationControl = new CyclicBehaviour() {
		@Override
		public void action() {
			if (negotiationStatus == NEG_START) {
				negotiationStatus = NEG_WAIT;
				myAgent.addBehaviour(Negotiation);
				
			}
			if (negotiationStatus == NEG_RESPONCE_WAIT) {
				//if needed
			}
		}
	};
	
	Behaviour StatusControl = new CyclicBehaviour() {
		@Override
		public void action() {
			if (agentStatus == LOOKUP_NEXT) {
				if (requestQueue.size()>0)
				{
					//we get next one request and process it.
					agentStatus = NEGOTIATION;
					negotiationStatus = NEG_START;
					negNow = requestQueue.get(0);
					requestQueue.remove(0);
				}
				if (agentStatus == NEGOTIATION) {
					if (negotiationStatus == NEG_STOP) {
						printReport("Negotiations with " + negNow + " were stopped. fetching another one!");
						//Failed negotiations, try next
						agentStatus = LOOKUP_NEXT;
					}
				}
				if (agentStatus == STOPPING) {
					//finish everything
					//finish logging
					
					
					agentStatus = STOP;
				}
			}
		}
	};
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
					printReport("output "+ message + " to: " + sendTo);
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
				if (msg.getContent() == null)
					printReport("Unexpected message, sent by: " + msg.getSender());
				else {
					//GOT
					printReport("got " + msg.getContent());
					
				String[] items = msg.getContent().split(" ");
				switch (CommandExplain(items[0])) {
					case "RESOURSE_REQUIRED"://DONE
						//reserve recieved: start span volume mark.N1 mark.N2 
						{
							printReport("rreq from: "+ msg.getSender().getName());
							String sender = msg.getSender().getName();
							Integer start = Integer.parseInt(items[1].toString()), span = Integer.parseInt(items[2].toString()), volume = Integer.parseInt(items[3].toString());
							requestQueue.add(msg.getSender().getName());
							requestQueue.sort(null);
							starts.put(sender, start);
							volumes.put(sender, volume);
							longevities.put(sender, span);
							//requests.updateReq(sender);
						}
					break;
					case "GET_RESERVED":
						{
							//printReport("rget");
							//сверяем имя с текущим
							String sender = msg.getSender().getName();
							if (sender == negNow) {
								printReport("Resources taken, time to move on");
								subres(starts.get(sender),longevities.get(sender),volumes.get(sender));
								agentStatus = LOOKUP_NEXT;
							}
							else
								printReport(sender = " why, do u need extra resources?");
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
						printReport("STARTING. . .");
						agentStatus = LOOKUP_NEXT;
						//myAgent.addBehaviour(Logging);
						myAgent.addBehaviour(NegotiationControl);
						myAgent.addBehaviour(StatusControl);
					} break;
					case "ABORT_NEGOTIATIONS":{
						String sender = msg.getSender().getName();
						printReport("abort from: " + sender);
						if (sender == negNow) {
							agentStatus = LOOKUP_NEXT;
						}
						else {
							int pos = requestQueue.indexOf(sender);
							if (pos>-1) {
								requestQueue.remove(pos);
							}
						}
					}
						break;
					case "REPORT_REQUEST":
						break;
					case "SHOW_PLOT":
					{
						initUI();
						frame.setVisible(true);
						
					}break;
					case "STOP": {
						agentStatus = STOPPING;
						myAgent.removeBehaviour(Logging);
						
					}break;
					case "UNKNOWN_MESSAGE": printReport(msg.getContent()); break;
					
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
			printReport("horizon: "+ planningHorizon);
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