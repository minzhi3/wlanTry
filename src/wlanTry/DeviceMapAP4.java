package wlanTry;
import java.util.Random;



public class DeviceMapAP4 extends DeviceMap {
	public DeviceMapAP4(int n,double error) {
		super(n,error);
	}

	public void createMap(){

		Random r=new Random();
		double size=distAP/2;
		//for (int i=0;i<4;i++){
			super.addDevice(size, size);
			super.addDevice(-size, size);
			super.addDevice(-size, -size);
			super.addDevice(size, -size);
		//}
		for (int i=0;i<numMT*4;i++){
			//if (numMT!=3){
				double x;
				double y;
				do{
					x=(r.nextDouble()-0.5)*(this.areaAP*2+this.distAP);
					y=(r.nextDouble()-0.5)*(this.areaAP*2+this.distAP);
				}while (!this.inAreaAP(x, y));
				this.addDevice(x, y);
			//}else {//Case for hidden nodes
			//	if (i/3==0) this.addDevice(30, 30);
			//	if (i/3==1) this.addDevice(-30, 30);
			//	if (i/3==2) this.addDevice(1, -1);
			//	if (i/3==3) this.addDevice(-30, -30);
			//}
		}
		//for (int i=0;i<super.devices.size();i++){
		//	System.out.println(super.devices.get(i).x);
		//}
	}
	
	public int getAPofIndex(int index){
		double x=this.devices.get(index).x;
		double y=this.devices.get(index).y;
		if (y>0) {
			if (x>0) 
				return 0;
			else return 1;
		}
		else{
			if (x>0)
				return 3;
			else
				return 2;
		}
	}
	
	protected boolean inAreaAP(double x,double y){
		double apX[]=new double[]{0.5,-0.5,-0.5,0.5};
		double apY[]=new double[]{0.5,0.5,-0.5,-0.5};
		for (int i=0;i<4;i++){
			double dx=x-apX[i]*this.distAP;
			double dy=y-apY[i]*this.distAP;
			if (Math.sqrt(dx*dx+dy*dy)<this.areaAP)
				return true;
		}
		return false;
	}

}
