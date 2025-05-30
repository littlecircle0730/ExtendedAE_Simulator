import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;

public class ExtendedAESchedule extends BLESchedule{

    private double T; // epoch length in milliseconds
    private double L; // listen interval length in milliseconds
    private double advertisingInterval; // interval between beacons, in milliseconds
	private boolean addExtraEpoch; // does extra epoch added for fixing WP issue

	private double W;

	private int CHUNKS; // total number of chunks
	private int chunkNumber = 0; // chunk index

	private double totalBeaconTime;

	// Extended Adv propertities
	private double secondB; // the beaconLength of offloading data on secondary channels
	private double AUX_Offset; // the time between the beacons on primary channel and secondary channel
		
    
    private ExtendedAESchedule(){}

    public ExtendedAESchedule(int nodeID, BLEDiscSimulatorOptions options, double simulationTime, double[] startOffsets){
		super(nodeID, options, simulationTime);

		this.T = options.getT();
		this.addExtraEpoch = options.getAddExtra();

		this.secondB = options.getSecondB();
		this.AUX_Offset = options.getAUXOffset();

		this.CHUNKS = options.getChunks();
		this.W = options.getW();

		this.totalBeaconTime = beaconLength + AUX_Offset + secondB;

		this.L = options.getL(); // It is the size of A
		L = L + options.getMaxAdditionalAdvDelay() + totalBeaconTime; // A + s + B

		// FIX: The advertising interval needs to be b smaller  to ensure that a COMPLETE beacon is heard within L
		// however, in the code, we use this as the gap (radio off time) between two successive advertisement events, so it's not
		// really the advertisement *interval* exactly (it's b smaller than that)
		this.advertisingInterval = options.getL();
		
		// if this advertising interval is now LESS than a beacon, we're in trouble. Cry and quit.
		//HC: CHANGING!
		if(advertisingInterval < totalBeaconTime){
			System.err.println("Whoops! Invalid setting! The advertisement interval has to be longer than a beacon. This is probably the fault of the correction for BLE's added random advertising delay");
			System.exit(0);
		}
		setStartOffset(startOffsets, options, 2*W);
    }

	// HC: CHANGING!
	private void setStartOffset(double[] startOffsets, BLEDiscSimulatorOptions options, double range) {
		// just select random number between 0 and  for the schedule's offset
		startOffset = (Math.random() * range);
	}
	
	void createSchedule() {
		if (schedule == null) {
			schedule = new ArrayList<BLEScheduleEvent>();
			double windowStartTime = startOffset;
			// randomly choose one of the three scan channels to start on
			int scanChannel = (int) (Math.random() * 3);

			while (windowStartTime < simulationTime) {
				double windowEndTime = windowStartTime + (W + W * Math.random()); //ExtendedAE
				double epochStartTime = windowStartTime;

				while (epochStartTime < windowStartTime + W) { // the rest of T does not adv or listen
					double epochEndTime = Math.min(windowStartTime + W, epochStartTime + T);  // the rest of T does not adv or listen
					
					createOneEpoch(scanChannel, epochStartTime, 0, chunkNumber, epochEndTime);
					epochStartTime = epochEndTime;
					scanChannel = BLEScheduleEvent.getNextScanChannel(scanChannel);
				}

				chunkNumber = (int) (Math.random() * 1000000); // CHANGE
				windowStartTime = windowEndTime;
			}

		}
	}

	// this creates the schedule for one "epoch" which involves listening on only one channel
	private void createOneEpoch(int channel, double startTime, double extraEpoch, int chunkNumber, double epochEndTime) {

		// Actual listening interval
		BLEListenStartEvent startListen = new BLEListenStartEvent(nodeID, startTime, channel);
		schedule.add(startListen);

		double endListenTime = Math.min(startTime + L, epochEndTime);
		BLEListenEndEvent endListen = new BLEListenEndEvent(nodeID, endListenTime);
		schedule.add(endListen);

		// create the advertisement events and add them to the schedule
		double time = endListenTime;

		double lastBeaconTime = time;

		while (time + totalBeaconTime < epochEndTime) {
			int secondChannel = new Random().nextInt(37) + 3; // In our simulation, 3-39 as secondary channel and 0-2 as primary 

			BLEExtendedAdvertiseStartEvent startAdvertising = new BLEExtendedAdvertiseStartEvent(nodeID, time, secondChannel, chunkNumber);
			schedule.add(startAdvertising);
			BLEExtendedAdvertiseEndEvent endAdvertising = new BLEExtendedAdvertiseEndEvent(nodeID, time + totalBeaconTime, secondChannel, chunkNumber);
			schedule.add(endAdvertising);

			lastBeaconTime = time + totalBeaconTime;

			// right? the time between two start beacons should be the same as a listen (which is the advertising interval + beacon length)
			// the start time for the NEXT beacon should be time + advertisingInterval + the BLE random delay
			time = time + advertisingInterval;

			// select a random number between 0 and the max possible added random delay
			double randomDelay = Math.random() * options.getMaxAdditionalAdvDelay();
			time = time + randomDelay;
		}
		// last beacon //BLEnd
		if (lastBeaconTime + totalBeaconTime < epochEndTime) {
			int secondChannel = new Random().nextInt(37) + 3; // In our simulation, 3-39 as secondary channel and 0-2 as primary 
			BLEExtendedAdvertiseStartEvent startAdvertising = new BLEExtendedAdvertiseStartEvent(nodeID, epochEndTime - totalBeaconTime, secondChannel, chunkNumber);
			schedule.add(startAdvertising);
			BLEExtendedAdvertiseEndEvent endAdvertising = new BLEExtendedAdvertiseEndEvent(nodeID, epochEndTime, secondChannel, chunkNumber);
			schedule.add(endAdvertising);
		}

		// System.out.println("chunkNumber: "+ chunkNumber);

	}

	public void onDiscovery(BLEExtendedAdvertiseEndEvent base, BLEDiscSimulator simulation) {
		// we only activate beacons if we're using the BLEnd half epoch model (which is the usual case)
	}
}