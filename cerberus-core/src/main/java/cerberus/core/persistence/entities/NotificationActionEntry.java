package cerberus.core.persistence.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "NOTIFICATION_ACTIONS")
public class NotificationActionEntry implements Serializable {

	private static final long serialVersionUID = 6314024664902522808L;

	public static final int REQ_CLASSNAME_MAXLEN = 64;

	public static final int ACTION_CLASSNAME_MAXLEN = 64;

	public static final int REQ_KEY_MAXLEN = 64;

	@Id
	@Column(name = "ID")
	private Long id = null;

	@Column(name = "ACTION_CLASSNAME", length = ACTION_CLASSNAME_MAXLEN)
	private String actionClassname = null;

	@Column(name = "REQUESTOR_CLASSNAME", length = REQ_CLASSNAME_MAXLEN)
	private String requestorClassname = null;

	@Column(name = "REQUESTOR_KEY", length = REQ_KEY_MAXLEN)
	private String requestorKey = null;

	@Column(name = "ENABLED")
	private Integer enabled = 0;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getActionClassname() {
		return actionClassname;
	}

	public void setActionClassname(String actionClassname) {
		this.actionClassname = actionClassname;
	}

	public String getRequestorClassname() {
		return requestorClassname;
	}

	public void setRequestorClassname(String requestorClassname) {
		this.requestorClassname = requestorClassname;
	}

	public String getRequestorKey() {
		return requestorKey;
	}

	public void setRequestorKey(String requestorKey) {
		this.requestorKey = requestorKey;
	}

	public boolean getEnabled() {
		return enabled == 1 ? true : false;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled ? 1 : 0;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof NotificationActionEntry) {
			NotificationActionEntry nae = (NotificationActionEntry) obj;
			if (equalsSafe(getId(), nae.getId())) {
				if (equalsSafe(getActionClassname(), nae.getActionClassname())) {
					if (equalsSafe(getRequestorClassname(),
							nae.getRequestorClassname())) {
						if (equalsSafe(getRequestorKey(), nae.getRequestorKey())) {
							if (getEnabled() == nae.getEnabled()) {
								result = true;
							}
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		int code = 9;
		code += genCode(getId());
		code += genCode(getActionClassname());
		code += genCode(getRequestorClassname());
		// make sure it doesn't approach integer max
		code -= genCode(getRequestorKey());
		if (getEnabled())
			code += 1;
		return code;
	}

	private static int genCode(Object obj) {
		int code = 0;
		if (obj != null)
			code = obj.hashCode();
		return code;
	}

	private static boolean equalsSafe(Object obj1, Object obj2) {
		boolean result = false;
		if (obj1 != null && obj2 != null) {
			result = obj1.equals(obj2);
		} else if (obj1 == null && obj2 == null) {
			// in this case, 2 properties that are null
			// may indicate equality - though probably shouldn't
			// if ALL of them are null
			result = true;
		}
		return result;
	}
}
