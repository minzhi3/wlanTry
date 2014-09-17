package wlanTry;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import signal.Signal;

public class DeviceControlRTS extends Device {
	final int numMT;
	final int IDSlot;
	int countIFS;
	int countBackoff;
	int countTransmit;
	int countReply;
	int sizeCW;
	boolean carrierSense;
	int countRTS;
	int countCTS;
	
	public DeviceControlRTS(int id, int AP, CyclicBarrier barrier, Object key,
			Channel ch, Channel controlChannel, DeviceMap dm) {
		super(id, AP, barrier, key, ch, controlChannel, dm);
		this.replyRequests=new RequestsQueue();
		this.numMT=dMap.numMT;
		this.IDSlot=id%(numMT);
	}

	@Override
	protected void receiveProcess() throws Exception {
		// TODO Auto-generated method stub
		if (!dataSignals.isEmpty()){
			Signal receivedSignal=dataSignals.get(0);
			debugOutput.output(receivedSignal.getString()+" Received");
			//Receive for data channel
			if (receivedSignal.IDTo==id){
				debugOutput.output(" --ID OK");
				switch (receivedSignal.type){
					case DATA:  //Reply the NACK when error happenl
						if (receivedSignal.getErrorState()){
							ret.receiveError();
							debugOutput.output(" --Error Detected --Reply NACK");
							replyRequests.addRequest(new Request(
									receivedSignal.IDFrom, 
									dataChannel.currentTime, 
									receivedSignal.IDPacket, 
									PacketType.NACK,
									1,
									Param.timeControlSlot-2));
						}else{//Reply ACK or ENDR
							debugOutput.output(" --Available DATA --Reply ACK");
							ret.receiveDATA();
							PacketType type;
							if (receivedSignal.numSubpacket>1){
								type=PacketType.ACK;
							}else{
								type=PacketType.ENDR;
							}
							replyRequests.addRequest(new Request(
									receivedSignal.IDFrom, 
									dataChannel.currentTime, 
									receivedSignal.IDPacket, 
									type,
									1,
									Param.timeControlSlot-2));
						}
						break;
				default:
					break;
				}
			}
		}
		
		//receive for control channel

		if (!controlSignals.isEmpty()){
			Signal receivedControlSignal=controlSignals.get(0);
			debugOutput.output(receivedControlSignal.getString()+" Received");
			switch (receivedControlSignal.type){
			case ACK:
				if (receivedControlSignal.IDTo==id){
					debugOutput.output(" --Type ACK");
					if (requests.getSubpacket()<=1)
						stateTransmit=0;
					
					ret.receiveACK();
					int beginTime=(int)(this.requests.getTranmitTime());
					boolean wholePacket;
					synchronized(key){
						wholePacket=requests.popSubpacket();
					}
				}
				break;
			case NACK://A NACK stops transmitting
				if (receivedControlSignal.IDTo==id){
					debugOutput.output(" --Type NACK");
					ret.receiveNACK();
					stateTransmit=0;
					synchronized(key){
					dataChannel.retrieveSignal(
							neighbor,
							receivedControlSignal.IDFrom,
							PacketType.DATA
							);
					}
				}
				break;
			case RTS://Reply CTS when received RTS
				if (receivedControlSignal.IDFrom==id) break;
				countRTS++;
				if (receivedControlSignal.IDTo==id){
					replyRequests.addRequest(new Request(
						receivedControlSignal.IDFrom, 
						dataChannel.currentTime, 
						receivedControlSignal.IDPacket, 
						PacketType.CTS,
						1,
						Param.timeControlSlot-2));
				}
				break;
			case CTS:
				if (receivedControlSignal.IDFrom==id) break;
				countCTS++;
				if (receivedControlSignal.IDTo==id){
					stateTransmit=2;//finish waiting CTS
				}
				break;
			case ENDS:
				if (receivedControlSignal.IDFrom==id) break;
				if (receivedControlSignal.IDTo!=id){
					countRTS--;
				}
				if (countRTS<0) throw new Exception("RTS Less than zero");
				break;
			case ENDR:
				if (receivedControlSignal.IDFrom==id) break;
				countCTS--;
				if (receivedControlSignal.IDTo==id){
					stateTransmit=6;
					ret.receiveACK();
				}

				if (countRTS<0) throw new Exception("CTS Less than zero");
				break;
			default:
				break;
			}
			debugOutput.output("|");
		}

	}
	@Override
	protected void transmitProcess() {
		Request dataRequest=null ;
		try{
		dataRequest =requests.getFirst().getClone();
		}catch (Exception e){
			e.printStackTrace();
		}
		switch (stateTransmit){
		
		case 0://Initial

			debugOutput.output("Transmitting Starts");
			stateTransmit=7;
			sizeCW=Param.sizeCWmin;
			break;
		case 7://RTS
			Request rtsRequest=new Request(dataRequest.IDTo, dataChannel.getTime(), dataRequest.IDPacket, PacketType.RTS, 1, Param.timeControlSlot-2);
			replyRequests.addRequest(rtsRequest);
			stateTransmit=1;
			break;
		case 1://wait CTS
			debugOutput.output(" --wait CTS, RTS="+countRTS);

			countIFS=Param.timeSIFS;
			break;
		case 2://SIFS
			debugOutput.output(" --SIFS "+countIFS);
			if (countIFS<=0){
				stateTransmit=3;
				if (countBackoff<=0)
					//countBackoff=70;
					countBackoff=(new Random().nextInt(sizeCW)+1)*Param.timeSlot;
					
			}
			if (!carrierSense){
				countIFS--;
			}else {
				countIFS=Param.timeDIFS;
			}
			break;
			
		case 3://Backoff
			debugOutput.output(" --Backoff "+countBackoff);
			if (countBackoff<=0){
				stateTransmit=4;
				countTransmit=requests.getFirst().getLength();
				synchronized(key){
					dataChannel.addSignal(neighbor, id, requests.getFirst());
				}
				//Access successfully
				ret.accessChannel();
			}
			if (!carrierSense){
				countBackoff--;
			}else{
				stateTransmit=2;
			}
			break;
		case 4://Transmitting Data
			debugOutput.output(" --Transmitting "+countTransmit);
			if (countTransmit<=0){
				stateTransmit=5;
				countIFS=Param.timeEIFS;
			}else{
				countTransmit--;
			}
			break;
		case 5://EIFS
			debugOutput.output(" --Waiting ACK/NACK "+countIFS);
			if (countIFS<=0){
				ret.retransmit();
				stateTransmit=0;
			}
			if (!carrierSense){
				countIFS--;
			}else{
				stateTransmit=4;
			}
			break;
		case 6://ENDS
			Request endsRequest=new Request(dataRequest.IDTo, dataChannel.getTime(), dataRequest.IDPacket, PacketType.ENDS, 1, Param.timeControlSlot-2);
			replyRequests.addRequest(endsRequest);
			stateTransmit=0;
			
			int beginTime=(int)(this.requests.getTranmitTime());
			synchronized(key){
				requests.popSubpacket();
			}
			ret.transmittingEnds(beginTime,dataChannel.getTime());
			
			break;
		}

	}

	@Override
	protected void replyProcess() {
		switch (stateReply){
		case 0://Initial
			debugOutput.output("Replying Starts ");
			stateReply=1;
			break;
		case 1:
			debugOutput.output(" --Waiting Slots");
			int currentSlot=(dataChannel.getTime()/Param.timeControlSlot)%dMap.numMT;
			debugOutput.output(" --Now "+currentSlot+" Need "+this.IDSlot);
			if (currentSlot==this.IDSlot){
				debugOutput.output(" --Reply "+replyRequests.getFirst().type);
				
				synchronized(key){
					controlChannel.addSignal(neighbor, id, replyRequests.getFirst());
				}

				ret.reply(replyRequests.getFirst().type);
				replyRequests.popSubpacket();
				stateReply=2;
				countReply=Param.timeControlSlot;
			}
			break;
		case 2:
			if (countReply<=0){
				stateReply=0;
			}else {
				countReply--;
			}
		}

	}

}
