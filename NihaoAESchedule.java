import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class NihaoAESchedule extends BLESchedule{

    private double T; // epoch length in milliseconds
    private double advertisingInterval; // interval between beacons, in milliseconds
	private double slotSize;

	private double totalBeaconTime;
    private double n;

	// Extended Adv propertities
    private double secondB; // the beaconLength of offloading data on secondary channels
    private double AUX_Offset; // the time between the beacons on primary channel and secondary channel

	private int CHUNKS; // total number of chunks
	private int chunkNumber = 0; // chunk index
    
    private NihaoAESchedule(){}

    public NihaoAESchedule(int nodeID, BLEDiscSimulatorOptions options, double simulationTime, double[] startOffsets){
		super(nodeID, options, simulationTime);

		this.T = options.getT();
        this.n = options.getN();
		this.secondB = options.getSecondB();
		this.AUX_Offset = options.getAUXOffset();
		this.CHUNKS = options.getChunks();

		this.totalBeaconTime = beaconLength + AUX_Offset + secondB;

		// FIX: The advertising interval needs to be b smaller  to ensure that a COMPLETE beacon is heard within L
		// however, in the code, we use this as the gap (radio off time) between two successive advertisement events, so it's not
		// really the advertisement *interval* exactly (it's b smaller than that)
		this.advertisingInterval = options.getT();
		
		this.slotSize = T/n;

		if(slotSize < totalBeaconTime){
			//if(advertisingInterval < beaconLength){
			System.err.println("Whoops! Invalid setting! The advertisement interval has to be longer than a beacon. This is probably the fault of the correction for BLE's added random advertising delay");
			System.exit(0);
		}
		setStartOffset(startOffsets, options, 1.5*T);
    }

	// HC: CHANGING!
    private void setStartOffset(double[] startOffsets, BLEDiscSimulatorOptions options, double range){
		// just select random number between 0 and  for the schedule's offset
		startOffset = (Math.random() * range);
    }

    // a schedule has to be three epochs long to represent
    // listening on the three channels in turn
    void createSchedule(){
		if(schedule == null){
			schedule = new ArrayList<BLEScheduleEvent>();
			double epochStartTime = startOffset;
			//int scanChannel = BLEScheduleEvent.ADVERTISEMENT_CHANNEL_ONE;
			// randomly choose one of the three scan channels to start on
			int scanChannel = (int) (Math.random() * 3);
			while(epochStartTime < simulationTime){
				// chunkNumber = 0;
				createOneEpoch(scanChannel, epochStartTime);
				epochStartTime += T;
				scanChannel = BLEScheduleEvent.getNextScanChannel(scanChannel);
			}
		}
    }

    // this creates the schedule for one "epoch" which involves listening on only one channel
    private void createOneEpoch(int channel, double startTime){

        int secondChannel = -1;
        double epochEndTime = startTime + T;

        BLEExtendedAdvertiseStartEvent startAdvertising = new BLEExtendedAdvertiseStartEvent(nodeID, startTime, -1, chunkNumber);
        schedule.add(startAdvertising);
        BLEExtendedAdvertiseEndEvent endAdvertising = new BLEExtendedAdvertiseEndEvent(nodeID, startTime+totalBeaconTime, -1, chunkNumber);
        schedule.add(endAdvertising);

		// Actual listening interval
		BLEListenStartEvent startListen = new BLEListenStartEvent(nodeID, startTime+totalBeaconTime, channel);
		schedule.add(startListen);
		BLEListenEndEvent endListen = new BLEListenEndEvent(nodeID, startTime + T);
		schedule.add(endListen);

        double time = startTime+T;
        while(time+beaconLength < epochEndTime) {
			BLEExtendedAdvertiseStartEvent startAdvertisingCont = new BLEExtendedAdvertiseStartEvent(nodeID, time, -1, chunkNumber);
			schedule.add(startAdvertisingCont);
			BLEExtendedAdvertiseEndEvent endAdvertisingCont = new BLEExtendedAdvertiseEndEvent(nodeID, time + totalBeaconTime, -1, chunkNumber);
			schedule.add(endAdvertisingCont);

            time += slotSize;
        }

		chunkNumber = (chunkNumber + 1) % CHUNKS;
    }

    public void onDiscovery(BLEExtendedAdvertiseEndEvent base, BLEDiscSimulator simulation){
		// we only activate beacons if we're using the BLEnd half epoch model (which is the usual case)
    }
}