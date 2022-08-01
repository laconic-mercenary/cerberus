package cerberus.core.persistence.entities;

import static cerberus.core.persistence.entities.validation.Utils.isEqualSafe;
import static cerberus.core.persistence.entities.validation.Utils.safeHashcode;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "KVP_PROPERTIES")
public class KeyValuePairProperty implements Serializable {

	private static final long serialVersionUID = 1170558084150044230L;

	public static final int KEY_ENTRY_MAX_LENTH = 512;

	public static final int VALUE_ENTRY_MAX_LENGTH = 1024;

	public static final int DESCRIPTION_MAX_LENGTH = 512;

	@Id
	@Column(name = "KEY_ENTRY", length = KEY_ENTRY_MAX_LENTH)
	private String keyEntry = null;

	@Column(name = "VALUE_ENTRY", length = VALUE_ENTRY_MAX_LENGTH)
	private String valueEntry = null;

	@Column(name = "DESCRIPTION", length = DESCRIPTION_MAX_LENGTH)
	private String description = null;

	public String getKeyEntry() {
		return keyEntry;
	}

	public void setKeyEntry(String keyEntry) {
		this.keyEntry = keyEntry;
	}

	public String getValueEntry() {
		return valueEntry;
	}

	public void setValueEntry(String valueEntry) {
		this.valueEntry = valueEntry;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash += safeHashcode(getKeyEntry());
		hash += safeHashcode(getValueEntry());
		hash += safeHashcode(getDescription());
		hash += DESCRIPTION_MAX_LENGTH;
		hash += KEY_ENTRY_MAX_LENTH;
		hash += VALUE_ENTRY_MAX_LENGTH;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equality = false;
		if (obj instanceof KeyValuePairProperty) {
			KeyValuePairProperty kvp = (KeyValuePairProperty) obj;
			if (isEqualSafe(kvp.getKeyEntry(), getKeyEntry()))
				if (isEqualSafe(kvp.getValueEntry(), getValueEntry()))
					equality = isEqualSafe(kvp.getDescription(),
							getDescription());

		}
		return equality;
	}
}
