package wlanTry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import signal.Signal;
class SignalBeginComparator implements Comparator<Signal> {

	@Override
	public int compare(Signal arg0, Signal arg1) {
		double t1=arg0.timeBegin;
		double t2=arg1.timeBegin;
		if (t1<t2)
			return -1;
		else if (t1>t2)
			return 1;
		else
			return 0;
	}
	
}
public class GodQueue extends God {
	LinkedList<Signal> allReq;
	LinkedList<Signal> channel;
	int time;
	GodResult gr;
	DeviceResult dr[];

	public GodQueue(int n, int type) {
		super(n, type);
		gr=new GodResult();
		dr=new DeviceResult[super.ThreadNum];
		// TODO Auto-generated constructor stub
	}
	
	private void buildQueue(){
		int IDFrom=0;
		for (RequestsQueue qs:dm.requestsList){
			IDFrom++;
			for (Request rr:qs.requests){
				Signal signal=rr.toSignal(IDFrom, (int)rr.time);
				allReq.add(signal);
			}
		}
		Collections.sort(allReq, new SignalBeginComparator());
	}
	@Override
	public GodResult call() throws Exception {
		if (dm==null) return null;
		buildQueue();
		
		ListIterator<Signal> ib,ie;
		ib=ie=allReq.listIterator();
		double timeMT[]=new double[super.ThreadNum];
		
		for (time=0;time<Param.simTimeLength;time++){
			//From Requeue to Queue
			ListIterator<Signal> it=ie;
			Signal s;
			do{
				s=it.next();
				if (s==null) break;
				if (s.timeBegin<=time){
					ie.next();
				}
			}while(s.timeBegin>time);
			
			ListIterator<Signal> ii=ib;
			while (ii!=ie){
				s=ii.next();
				//From Queue to Channel (1)
				if (timeMT[s.IDFrom]>=time) continue;
				
				//From Queue to Channel (2)
				if (check(s)){
					channel.add(s);
				}
				checkEnd();
			}
		}
		for (DeviceResult drs:dr){
			gr.add(drs);
		}
		return gr;
	}

	private void checkEnd() {
		ArrayList<Signal> delete=new ArrayList<Signal>();
		for (Signal ex:channel){
			if (ex.getEndTime()<=time){
				delete.add(ex);
				dr[ex.IDFrom].receiveACK();
				dr[ex.IDTo].receiveDATA();
			}
		}
		channel.removeAll(delete);
	}

	private boolean check(Signal s) {
		ArrayList<Integer> neighbor=dm.getNeighbour(s.IDFrom);
		boolean interference[]=new boolean[super.ThreadNum];
		for (int i=0;i<dm.getDeviceNum();i++){
			interference[i]=false;
		}
		for (Integer i:neighbor){
			interference[i]=true;
		}
		for (Signal ex:channel){
			if (interference[ex.IDTo]){
				return false;
			}
		}
		return true;
	}

}
