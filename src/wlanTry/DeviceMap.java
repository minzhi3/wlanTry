package wlanTry;

import java.util.ArrayList;
import java.util.Random;

class Location {
	public double x;
	public double y;
	public Location(double x, double y){
		this.x=x;
		this.y=y;
	}
}

public class DeviceMap {
	final double distAP=50;
	final double areaAP=40;
	public final int carrierSenseRange=50;
	private ArrayList<Location> devices;
	//private ArrayList<Location> accessPoints; 

	/**
	 * The distance of carrier sense.
	 */
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
	/**
	 * Create a map with square;
	 * @param n
	 */
	public void createMap(int n){
		Random r=new Random();
		double size=distAP/2;
		//for (int i=0;i<4;i++){
			this.addDevice(size, size);
			this.addDevice(-size, size);
			this.addDevice(-size, -size);
			this.addDevice(size, -size);
		//}
		for (int i=0;i<n;i++){
			double x;
			double y;
			do{
				x=(r.nextDouble()-0.5)*(this.areaAP*2+this.distAP);
				y=(r.nextDouble()-0.5)*(this.areaAP*2+this.distAP);
			}while (!this.inAreaAP(x, y));
			this.addDevice(x, y);
		}
	}
	public int getAPofIndex(int index){
		double x=this.devices.get(index).x;
		double y=this.devices.get(index).y;
		if (x>0) {
			if (y>0) 
				return 0;
			else return 1;
		}
		else{
			if (y>0)
				return 2;
			else
				return 3;
		}
	}
	private boolean inAreaAP(double x,double y){
		double apX[]=new double[]{0.5,-0.5,-0.5,0.5};
		double apY[]=new double[]{0.5,0.5,-0.5,-0.5};
		for (int i=0;i<4;i++){
			double dx=x-apX[i];
			double dy=y-apY[i];
			if (Math.sqrt(dx*dx+dy*dy)>this.areaAP)
				return false;
		}
		return true;
	}

}
