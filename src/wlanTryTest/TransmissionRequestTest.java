package wlanTryTest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import wlanTry.TransmissionRequest;

public class TransmissionRequestTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testBuildRequestList() {
		TransmissionRequest r=new TransmissionRequest();
		r.buildRequestList(1000, 1, 4, 10000);
		for (int i=0;i<r.requestList.size();i++){
			if (i>39990) System.out.println(r.requestList.get(i).time+" @ MT"+r.requestList.get(i).id);
		}
	}

}
