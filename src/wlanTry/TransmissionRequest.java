package wlanTry;

import java.util.ArrayList;
import java.util.Random;

class RequestElement{
	public int time;
	public int id;
	public RequestElement(int time, int id){
		this.time=time;
		this.id=id;
	}
}

public class TransmissionRequest{
	public ArrayList<RequestElement> requests;
	public TransmissionRequest(){
		requests=new ArrayList<RequestElement>();
	}
	/**
	 * build random transmission request in a fixed data rates;
	 * @param dataRates :data rates.
	 * @param a :ID of receiver begin.
	 * @param b :ID of receiver end.
	 */
	public void buildRequestList(double pps, int a, int b){
		
	}
}
