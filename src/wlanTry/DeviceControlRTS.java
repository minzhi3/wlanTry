package wlanTry;

import java.text.spi.BreakIteratorProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import signal.Signal;
class SignalComparator implements Comparator<Signal> {

	@Override
	public int compare(Signal arg0, Signal arg1) {
		int t1=arg0.getEndTime();
		int t2=arg1.getEndTime();
		if (t1<t2)
			return -1;
		else if (t1>t2)
			return 1;
		else
			return 0;
	}
	
}
public class DeviceControlRTS extends Device {
	final int numMT;
	final int IDSlot;
	int countIFS;
	int countBackoff;
	int countTransmit;
	int countReply;
	int sizeCW;
	boolean carrierSense;
	int oldCountCTS, oldCountRTS;//for debug output
	int countRTS,countCTS,countENDS,countENDR;
	int threshold;
	int timeoutCTS;
	Flag rts,cts;
	Request dataRequest;
	boolean receiving, sending;
	ArrayList<Signal> receiveSignalQueue;
	
	public DeviceControlRTS(int id, int AP, CyclicBarrier barrier, Object key,
			Channel ch, Channel controlChannel, DeviceMap dm) {
		super(id, AP, barrier, key, ch, controlChannel, dm);
		this.replyRequests=new RequestsQueue();
		this.numMT=dMap.numMT;
		this.IDSlot=id%(numMT+1);
		oldCountRTS=oldCountCTS=-1;
		countCTS=countRTS=countENDR=countENDS=0;
		receiving=sending=false;
		receiveSignalQueue=new ArrayList<Signal>();
		
		rts=new Flag(dMap.numDevice);
		cts=new Flag(dMap.numDevice);
		dataRequest=null;
	}

	@Override
	protected void receiveProcess() throws Exception {
		// TODO Auto-generated method stub
		receiveSignalQueue.addAll(dataSignals);
		receiveSignalQueue.addAll(controlSignals);
		ArrayList<Signal> delete=new ArrayList<Signal>();
		boolean multiline=false;
		receiveSignalQueue.sort(new SignalComparator());
		
		boolean rtschecked=false;
		
		for (Signal receivedSignal : receiveSignalQueue) {
			if (multiline)debugOutput.output("\n     ");
			multiline=true;
			debugOutput.output(receivedSignal.getString());
			//Receive for data channel
			switch (receivedSignal.type){
				case DATA:  //Reply the NACK when error happen
					delete.add(receivedSignal);
					if (receivedSignal.IDTo==id){
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
								//receiving=false;//end a signal
							}
							replyRequests.addRequest(new Request(
									receivedSignal.IDFrom, 
									dataChannel.currentTime, 
									receivedSignal.IDPacket, 
									type,
									1,
									Param.timeControlSlot-2));
						}
					}
					break;
				case ACK:
					delete.add(receivedSignal);
					if (receivedSignal.IDTo==id){
						debugOutput.output(" --Type ACK");
						//if (requests.getSubpacket()<=1)
						//	stateTransmit=0;
						
						ret.receiveACK();
						synchronized(key){
							requests.popSubpacket();
						}
					}
					break;
				case NACK://A NACK stops transmitting
					delete.add(receivedSignal);
					debugOutput.output(" --Type NACK");
					
					synchronized(key){
					dataChannel.retrieveSignal(
							neighbor,
							receivedSignal.IDFrom,
							PacketType.DATA
							);
					}
					if (receivedSignal.IDTo==id){
						//sending=false;
						ret.receiveNACK();
						stateTransmit=0;
						//countBackoff=(new Random().nextInt(sizeCW)+1)*Param.timeSlot;
						//debugOutput.output(" -- retransmit");
						Request endsRequest=new Request(receivedSignal.IDFrom, dataChannel.getTime(), receivedSignal.IDPacket, PacketType.ENDS, 1, Param.timeControlSlot-2);
						replyRequests.addRequest(endsRequest);
						
					}
					//}
					break;
				case RTS://Reply CTS when received RTS
					if (receivedSignal.IDFrom==id){
						delete.add(receivedSignal);
						break;
					}
					if (receivedSignal.IDTo==id){
						rtschecked=true;
						if (!receiving){
							delete.add(receivedSignal);
							threshold=countRTS;
							receiving=true;
							replyRequests.addRequest(new Request(
									receivedSignal.IDFrom, 
								dataChannel.currentTime, 
								receivedSignal.IDPacket, 
								PacketType.CTS,
								1,
								Param.timeControlSlot-2));
						}
					}
					if (receivedSignal.IDTo!=id){
						if (!rtschecked){
							delete.add(receivedSignal);
							countRTS++;
							debugOutput.output(" --get RTS from "+receivedSignal.IDFrom+" RTS="+countRTS+" ENDS="+countENDS+" threshold="+threshold);
						}
					}
					//else{
						//debugOutput.output("--waiting current receiving");
						
					//}
					break;
				case CTS:
					delete.add(receivedSignal);
					if (receivedSignal.IDFrom==id) break;

					if (receivedSignal.IDTo==id){
						stateTransmit=3;//finish waiting CTS
					}else{
						countCTS++;
						cts.add(receivedSignal.IDFrom);
						debugOutput.output(" --get CTS from "+receivedSignal.IDFrom+", RTS="+rts.getNum()+" CTS="+cts.getNum());
					}
					break;
				case ENDS:
					delete.add(receivedSignal);
					if (receivedSignal.IDFrom==id) break;
					if (receivedSignal.IDTo!=id){
						countENDS++;
						rts.remove(receivedSignal.IDFrom);
						debugOutput.output(" --get ENDS from "+receivedSignal.IDFrom+" RTS="+countRTS+" ENDS="+countENDS+" threshold="+threshold);
					}
					if (rts.getNum()<0) throw new Exception("RTS Less than zero");
					break;
				case ENDR:
					delete.add(receivedSignal);
					if (receivedSignal.IDFrom==id) break;

					if (receivedSignal.IDTo==id){

						Request endsRequest=new Request(receivedSignal.IDFrom, dataChannel.getTime(), receivedSignal.IDPacket, PacketType.ENDS, 1, Param.timeControlSlot-2);
						replyRequests.addRequest(endsRequest);
						stateTransmit=0;
						
						if (dataRequest!=null){
							int beginTime=(int)(this.dataRequest.time);
	
							debugOutput.output(" --remove request,next_ID="+requests.getFirst().IDPacket);
							ret.transmittingEnds(beginTime,dataChannel.getTime());
						}
						ret.receiveACK();
						synchronized(key){
							requests.pop();
						}
						dataRequest=null;
					}else {
						countENDR++;
						cts.remove(receivedSignal.IDFrom);
						//debugOutput.output(" --get ENDR from "+receivedSignal.IDFrom+", RTS="+rts.getNum()+" CTS="+cts.getNum());
						debugOutput.output(" --get ENDR from "+receivedSignal.IDFrom);
					}

					if (cts.getNum()<0) throw new Exception("CTS Less than zero");
					break;
				default:
					break;
				}
			
			}
		receiveSignalQueue.removeAll(delete);
		
		//receive for control channel
/*
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
					synchronized(key){
						requests.popSubpacket();
					}
				}
				break;
			case NACK://A NACK stops transmitting
				//if (receivedControlSignal.IDTo==id){
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
				//}
				break;
			case RTS://Reply CTS when received RTS
				if (receivedControlSignal.IDFrom==id) break;
				//if (!receiving){//If itself is not receiving
					if (receivedControlSignal.IDTo==id){
						receiving=true;
						replyRequests.addRequest(new Request(
							receivedControlSignal.IDFrom, 
							dataChannel.currentTime, 
							receivedControlSignal.IDPacket, 
							PacketType.CTS,
							1,
							Param.timeControlSlot-2));
					}else{
						rts.add(receivedControlSignal.IDFrom);
						debugOutput.output(" --get RTS from "+receivedControlSignal.IDFrom+", RTS="+rts.getNum()+" CTS="+cts.getNum());
					}
				//}else{
					//debugOutput.output("--waiting current receiving");
					
				//}
				break;
			case CTS:
				if (receivedControlSignal.IDFrom==id) break;

				if (receivedControlSignal.IDTo==id){
					stateTransmit=3;//finish waiting CTS
				}else{
					cts.add(receivedControlSignal.IDFrom);
					debugOutput.output(" --get CTS from "+receivedControlSignal.IDFrom+", RTS="+rts.getNum()+" CTS="+cts.getNum());
				}
				break;
			case ENDS:
				if (receivedControlSignal.IDFrom==id) break;
				if (receivedControlSignal.IDTo!=id){
					rts.remove(receivedControlSignal.IDFrom);
					debugOutput.output(" --get ENDS from "+receivedControlSignal.IDFrom+", RTS="+rts.getNum()+" CTS="+cts.getNum());
				}
				if (rts.getNum()<0) throw new Exception("RTS Less than zero");
				break;
			case ENDR:
				if (receivedControlSignal.IDFrom==id) break;

				if (receivedControlSignal.IDTo==id){
					stateTransmit=8;
					ret.receiveACK();
				}else {
					cts.remove(receivedControlSignal.IDFrom);
					debugOutput.output(" --get ENDR from "+receivedControlSignal.IDFrom+", RTS="+rts.getNum()+" CTS="+cts.getNum());
				}

				if (cts.getNum()<0) throw new Exception("CTS Less than zero");
				break;
			default:
				break;
			}
			debugOutput.output("|");
		}
*/
	}
	@Override
	protected void transmitProcess() {

		switch (stateTransmit){
		
		case 0://Initial

			if (dataRequest==null) dataRequest = requests.getFirst().getClone();
			debugOutput.output("Transmitting Starts");
			stateTransmit=1;
			sizeCW=Param.sizeCWmin;
			break;
		case 1://RTS
			if (!sending){// if itself is not sending
				sending=true;
				Request rtsRequest=new Request(dataRequest.IDTo, dataChannel.getTime(), dataRequest.IDPacket, PacketType.RTS, 1, Param.timeControlSlot-2);
				replyRequests.addRequest(rtsRequest);
				stateTransmit=2;
				timeoutCTS=Param.timeoutCTS;
				countIFS=Param.timeSIFS;

			}else{
				debugOutput.output("--waiting current sending");
			}
			break;
		case 2://wait CTS
			//debugOutput.output(" --wait CTS, RTS="+countRTS+" CTS="+countCTS);
			//timeoutCTS--;
			//if (timeoutCTS<=0){
				//debugOutput.output(" CTS timeout");
				//sending=false;
				//stateTransmit=1;
			//}
			break;
		case 3://SIFS
			debugOutput.output(" --SIFS "+countIFS);
			if (countIFS<=0){
				stateTransmit=4;
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
			
		case 4://Backoff
			debugOutput.output(" --Backoff "+countBackoff);
			if (countBackoff<=0){
				stateTransmit=5;

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
				stateTransmit=3;
			}
			break;
		case 5://Waiting ENDR
			if (cts.getNum()!=oldCountCTS){
				oldCountCTS=cts.getNum();
				debugOutput.output(" --wait RECEIVE, CTS="+cts.getNum());
			}
			if (cts.getNum()<=0){
				stateTransmit=6;
			}
			break;
		case 6://Transmitting Data
			debugOutput.output(" --Transmitting "+countTransmit);
			if (countTransmit<=0){
				stateTransmit=7;
				countIFS=Param.timeControlSlot*dMap.numDevice;
			}else{
				countTransmit--;
			}
			break;
		case 7://EIFS
			debugOutput.output(" --Waiting ACK/NACK "+countIFS);
			if (countIFS<=0){
				//ret.retransmit();
				//stateTransmit=4;
				//countBackoff=(new Random().nextInt(sizeCW)+1)*Param.timeSlot;
				//debugOutput.output(" -- retransmit");
				Request endsRequest=new Request(dataRequest.IDTo, dataChannel.getTime(), dataRequest.IDPacket, PacketType.ENDS, 1, Param.timeControlSlot-2);
				replyRequests.addRequest(endsRequest);
				stateTransmit=0;
				
				synchronized(key){
					requests.pop();
				}
				dataRequest=null;
			}
			if (!carrierSense){
				countIFS--;
			}else{
				stateTransmit=6;
			}
			break;
		}

	}

	@Override
	protected void replyProcess() {
		switch (stateReply){
		case 0://Initial
			debugOutput.output("Replying Starts ");
			switch (replyRequests.getFirst().type){
			case RTS:
				stateReply=1;
				break;
			case CTS:
				stateReply=2;
				break;
			default:
				stateReply=3;
				break;
			}
			break;
		case 1://wait other receiving

				//if (cts.getNum()!=oldCountCTS){
				//	oldCountCTS=cts.getNum();
				//	debugOutput.output(" --wait receiving, CTS="+cts.getNum());
				//}
				//if (cts.getNum()<=0){
					stateReply=3;
				//}
			break;
		case 2://wait other sending
			if (countRTS!=oldCountRTS){
				oldCountRTS=countRTS;
				debugOutput.output(" --wait other sending, RTS="+countRTS+" ENDS="+countENDS+" threshold="+threshold);
			}
			//if (rts.getNum()<=0){
			if (countENDS>=threshold){
				stateReply=3;
			}
			break;
		case 3://waiting slots
			//debugOutput.output(" --Waiting Slots");
			int currentSlot=(dataChannel.getTime()/Param.timeControlSlot)%(dMap.numMT+1);
			//debugOutput.output(" --Now "+currentSlot+" Need "+this.IDSlot);
			if (currentSlot==this.IDSlot && (dataChannel.getTime()%Param.timeControlSlot==0)){
				debugOutput.output(" --Reply "+replyRequests.getFirst().type);
				
				synchronized(key){
					controlChannel.addSignal(neighbor, id, replyRequests.getFirst());
				}

				if (replyRequests.getFirst().type==PacketType.ENDS){
					sending=false;
				}
				if (replyRequests.getFirst().type==PacketType.ENDR ||replyRequests.getFirst().type==PacketType.NACK){
					receiving=false;
				}
				
				ret.reply(replyRequests.getFirst().type);
				replyRequests.popSubpacket();
				stateReply=4;
				countReply=Param.timeControlSlot;
			}
			break;
		case 4://replying
			if (countReply<=0){
				stateReply=0;
			}else {
				countReply--;
			}
		}

	}

}
