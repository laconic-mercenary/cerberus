package cerberus.core.persistence.entities;

import static cerberus.core.persistence.entities.validation.Utils.isEqualSafe;
import static cerberus.core.persistence.entities.validation.Utils.safeHashcode;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "FILE_MANAGERS")
public class FileManager implements Serializable {

	private static final long serialVersionUID = -6994122365715011233L;

	private static final int FALSE = 0;

	private static final int TRUE = 1;

	public static final int INTERVAL_MAXLEN = 32;

	public static final int DESCRIPTION_MAXLEN = 512;

	public static final int PARAMETERS_MAXLEN = 512;

	public static final int PATTERN_MAXLEN = 128;

	@Id
	@Column(name = "ID")
	private Long id = null;

	@ManyToOne
	@JoinColumn(name = "TARGET_DIRECTORY")
	private TargetDirectory targetDirectory = null;

	@ManyToOne
	@JoinColumn(name = "FILE_MANAGER_KEY")
	private FileManagerEntry fileManagerEntry = null;

	@Column(name = "INTERVAL", length = INTERVAL_MAXLEN)
	private String interval = null;

	@Column(name = "ENABLED")
	private Integer enabled = 0;

	@Column(name = "REQUIRES_FILES")
	private Integer requiresFiles = 0;

	@Column(name = "DESCRIPTION", length = DESCRIPTION_MAXLEN)
	private String description = null;

	@Column(name = "PARAMETERS", length = PARAMETERS_MAXLEN)
	private String parameters = null;

	@Column(name = "PATTERN", length = PATTERN_MAXLEN)
	private String pattern = null;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TargetDirectory getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(TargetDirectory targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	public FileManagerEntry getFileManagerEntry() {
		return fileManagerEntry;
	}

	public void setFileManagerEntry(FileManagerEntry fileManagerEntry) {
		this.fileManagerEntry = fileManagerEntry;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public boolean getEnabled() {
		return enabled == TRUE;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled ? TRUE : FALSE;
	}

	public boolean getRequiresFiles() {
		return requiresFiles == TRUE;
	}

	public void setRequiresFiles(boolean requiresFiles) {
		this.requiresFiles = requiresFiles ? TRUE : FALSE;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		if (obj instanceof FileManager) {
			FileManager instance = (FileManager) obj;
			if (isEqualSafe(instance.getTargetDirectory(), getTargetDirectory()))
				if (isEqualSafe(instance.getInterval(), getInterval()))
					if (isEqualSafe(instance.getPattern(), getPattern()))
						if (isEqualSafe(instance.getFileManagerEntry(),
								getFileManagerEntry()))
							if (isEqualSafe(instance.getParameters(),
									getParameters()))
								if (instance.getRequiresFiles()
										&& getRequiresFiles())
									equals = instance.getEnabled()
											&& getEnabled();
		}
		return equals;
	}

	@Override
	public int hashCode() {
		int hashCode = 10;
		hashCode += safeHashcode(getTargetDirectory());
		hashCode += safeHashcode(getPattern());
		hashCode += safeHashcode(getParameters());
		hashCode += safeHashcode(getInterval());
		hashCode += safeHashcode(getFileManagerEntry());
		hashCode += safeHashcode(getDescription());
		return hashCode;
	}
}
