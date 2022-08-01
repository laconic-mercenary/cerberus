package cerberus.core.persistence.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import cerberus.core.persistence.entities.validation.Utils;

@Entity(name = "FILE_MANAGER_ENTRIES")
public class FileManagerEntry implements Serializable {

	private static final long serialVersionUID = 5357470946399000234L;

	public static final int CLASSNAME_MAXLEN = 256;

	@Id
	@Column(name = "ID")
	private Long id = null;

	@Column(name = "CLASSNAME", length = CLASSNAME_MAXLEN)
	private String className = null;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEquals = false;
		if (obj instanceof FileManagerEntry) {
			FileManagerEntry instance = (FileManagerEntry) obj;
			if (Utils.isEqualSafe(instance.getClassName(), getClassName()))
				isEquals = Utils.isEqualSafe(instance.getId(), getId());
		}
		return isEquals;
	}

	@Override
	public int hashCode() {
		int code = 78;
		code += Utils.safeHashcode(getClassName());
		code += Utils.safeHashcode(getId());
		return code;
	}
}
