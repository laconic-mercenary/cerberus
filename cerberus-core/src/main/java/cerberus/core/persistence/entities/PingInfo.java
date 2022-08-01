package cerberus.core.persistence.entities;

import static cerberus.core.persistence.entities.validation.Utils.isEqualSafe;
import static cerberus.core.persistence.entities.validation.Utils.safeHashcode;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "CERBERUS_PING_INFO")
public class PingInfo implements Serializable {

	private static final long serialVersionUID = -3118590266337791016L;

	public static final int MAXLEN_ADDRESS = 256;

	public static final int MAXLEN_MACHINENAME = 256;

	@Id
	@Column(name = "ID")
	// @GeneratedValue(strategy = GenerationType.IDENTITY)
	// formerly used auto-gen primary key, changed to manually generated
	// due to script created schema
	private Long id = null;

	@Column(name = "ADDRESS", length = MAXLEN_ADDRESS)
	private String address = null;

	@Column(name = "MACHINENAME", length = MAXLEN_MACHINENAME)
	private String machineName = null;

	@Column(name = "RECEIVEDTIME")
	private long pingReceivedTime = -1L;

	public static class Factory {

		public static PingInfo make(String addr, String machine, long rxTime) {
			PingInfo pi = new PingInfo();
			pi.setAddress(addr);
			pi.setMachineName(machine);
			pi.setPingReceivedTime(rxTime);
			return pi;
		}
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the machineName
	 */
	public String getMachineName() {
		return machineName;
	}

	/**
	 * @param machineName
	 *            the machineName to set
	 */
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	/**
	 * @return the pingReceivedTime
	 */
	public long getPingReceivedTime() {
		return pingReceivedTime;
	}

	/**
	 * @param pingReceivedTime
	 *            the pingReceivedTime to set
	 */
	public void setPingReceivedTime(long pingReceivedTime) {
		this.pingReceivedTime = pingReceivedTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getMachineName());
		builder.append(":");
		builder.append(getAddress());
		return builder.toString();
	}

	@Override
	public int hashCode() {
		int hash = 13;
		hash += safeHashcode(getMachineName());
		hash += safeHashcode(getAddress());
		hash += safeHashcode(getId());
		hash += MAXLEN_ADDRESS;
		hash += MAXLEN_MACHINENAME;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PingInfo) {
			PingInfo target = (PingInfo) obj;
			if (isEqualSafe(target.getMachineName(), getMachineName()))
				if (isEqualSafe(target.getAddress(), getAddress()))
					if (isEqualSafe(target.getId(), getId()))
						return target.getPingReceivedTime() == getPingReceivedTime();
		}
		return false;
	}
}
