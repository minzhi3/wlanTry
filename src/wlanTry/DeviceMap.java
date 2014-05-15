package wlanTry;

import java.util.ArrayList;

class Location {
	public double x;
	public double y;
	public Location(double x, double y){
		this.x=x;
		this.y=y;
	}
}

public class DeviceMap {
	private ArrayList<Location> devices;
	//private ArrayList<Location> accessPoints; 

	/**
	 * The distance of carrier sense.
	 */
	public final int carrierSenseRange=50;
	public DeviceMap (){
		devices=new ArrayList<Location>();
		//accessPoints=new ArrayList<Location>();
	}
	public void addDevice(double x, double y){
		devices.add(new Location(x,y));
	}
	/*
	public void addAccessPoints(double x, double y){
		accessPoints.add(new Location(x,y));
	}*/
	public ArrayList<Integer> getNeighbour(int a){
		ArrayList<Integer> ret=new ArrayList<Integer>();
		double x0=devices.get(a).x;
		double y0=devices.get(a).y;
		for (int i=0;i<devices.size();i++){
			if (i==a) continue; 
			double dx=devices.get(i).x-x0;
			double dy=devices.get(i).y-y0;
			if (Math.sqrt(dx*dx+dy*dy)<carrierSenseRange)
				ret.add(i);
		}
		return ret;
	}

}
