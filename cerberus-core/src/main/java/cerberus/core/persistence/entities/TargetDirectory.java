package cerberus.core.persistence.entities;

import static cerberus.core.persistence.entities.validation.Utils.isEqualSafe;
import static cerberus.core.persistence.entities.validation.Utils.safeHashcode;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "TARGET_DIRECTORIES")
public class TargetDirectory implements Serializable {

	private static final long serialVersionUID = 5129498973083928915L;

	public static final int ABSOLUTE_PATH_LENGTH = 512;

	@Id
	@Column(name = "ID")
	private Long id = null;

	@Column(name = "ABSOLUTE_PATH", length = ABSOLUTE_PATH_LENGTH)
	private String absolutePath = null;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TargetDirectory) {
			TargetDirectory td = (TargetDirectory) obj;
			if (isEqualSafe(td.getAbsolutePath(), getAbsolutePath()))
				return isEqualSafe(td.getId(), getId());

		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 123;
		hash += safeHashcode(getId());
		hash += safeHashcode(getAbsolutePath());
		return hash;
	}
}
