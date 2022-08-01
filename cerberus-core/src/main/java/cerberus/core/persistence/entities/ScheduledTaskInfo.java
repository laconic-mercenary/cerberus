package cerberus.core.persistence.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import cerberus.core.persistence.tasks.PersistableTask;

// this isn't used because it never ended up having a place to fit
// it was a good idea though. The idea is that you can persist a 'task'
// to the database to be ran at a later time (a java class serialized to a CLOB in the database)
@Entity()
@Table(name = "CERBERUS_TASKS")
@Deprecated
public class ScheduledTaskInfo implements Serializable {

	private static final long serialVersionUID = -1020321277949670268L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id = null;

	@Column(name = "TIME", nullable = true)
	private Long time = null;

	@Lob
	@Column(name = "TASK")
	private PersistableTask task = null;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PersistableTask getTask() {
		return task;
	}

	public void setTask(PersistableTask task) {
		this.task = task;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}
}
