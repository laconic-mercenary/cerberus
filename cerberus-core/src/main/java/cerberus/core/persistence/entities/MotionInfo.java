package cerberus.core.persistence.entities;

import java.io.Serializable;

import com.frontier.lib.time.TimeUtil;

// this technically isn't a true JPA entity
// but I didn't know where else to put it
public class MotionInfo implements Serializable {

	/** sid */
	private static final long serialVersionUID = -5192138804984583943L;

	public static final class Factory {

		public static MotionInfo make(String machineName, String address) {
			return new MotionInfo(machineName, address);
		}

	}

	public MotionInfo() {
	}

	public MotionInfo(String name, String address) {
		this.machineName = name;
		this.machineAddress = address;
		this.timestampMillis = TimeUtil.nowUTC().getTime();
	}

	private String machineName = null;
	private String machineAddress = null;
	private long timestampMillis = -1;

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getMachineAddress() {
		return machineAddress;
	}

	public void setMachineAddress(String machineAddress) {
		this.machineAddress = machineAddress;
	}

	public long getTimestampMillis() {
		return this.timestampMillis;
	}
}
