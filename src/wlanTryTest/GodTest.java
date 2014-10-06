package wlanTryTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import wlanTry.*;


public class GodTest {

	@Test
	public void test() {
		GodResult gr=new GodResult();
		Future<GodResult> result;
		ExecutorService es = Executors.newCachedThreadPool();
		long begintime = System.nanoTime();
		//result=es.submit(new God(10,1));
		result=es.submit(new GodQueue(Param.maximumMT,Param.numAP));
		try {
			GodResult gr2=result.get();
			gr.add(gr2);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		es.shutdown();
		System.out.println("GOD"+": "+gr.getThroughputRx()/Param.numAP+" "+gr.getThroughputTx()/Param.numAP+" "+gr.getDelayTime());
		long endtime = System.nanoTime();
		double costTime = (endtime - begintime)/1e9;
		System.out.println(costTime);
	}

}
