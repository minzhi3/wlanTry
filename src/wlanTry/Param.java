package wlanTry;

public class Param {
	public static final String outputPath = "D:\\mt\\";
	public static final boolean withDownlink = true;
	public static final boolean withUplink = true;
	public static final int simTimeLength=500000;
	public static final int numAP=4;
	public static final int maximumMT=20+1;
	public static final double minError=1e-5;
	public static final double maxError=0.1;
	public static final int simRepeat=5;
	public static final int fixedMT=20;
	public static final double fixedError=0;
	
	public static final int noiseNum = 4;
	
	//public static final boolean isControlChannel = true;
	public static final DeviceType deviceType = DeviceType.ControlChannelRTS;
	public static final boolean isDebug = false;
	public static final boolean allMT = false;
	public static final boolean vsBER = false;
	public static final boolean noiseMT = false;
	
	
	public static final double throughputRequest=3 + ((withDownlink&withUplink)?0:3);  //Mbps
	public static final int timeSIFS=10;
	public static final int timeSlot=9;
	public static final int timeACK=40;
	public static final int timeData=1000;  //microsecond
	public static final int sizeCWmin=16;
	public static final int sizeCWmax=1024;
	public static final int maxRetrans=6;
	public static final double dataRates=72; //Mbps
	public static final int numSubpacket=10;
	
	public static final int carrierSenseRange=40;
	public static final int distAP=50;
	public static final int areaAP=40;

	public static final int timeDIFS=timeSIFS+timeSlot*2;
	public static final int timeEIFS=timeDIFS+timeSIFS+timeACK;
	public static final double sizeData=timeData*dataRates;
	public static final double packetRequestRates=throughputRequest/sizeData;
	public static final int timeSubpacket=timeData/numSubpacket;

	public static final int numRequest = (int)(packetRequestRates*simTimeLength/100000);
	//Parameter form Control channel
	public static final int timeControlSlot=5;
	public static final int timeCRC=5;
	public static final int timeoutCTS=200;
	public static final int DataTimeover = 20000;
}
