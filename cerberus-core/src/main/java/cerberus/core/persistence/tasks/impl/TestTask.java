package cerberus.core.persistence.tasks.impl;

import cerberus.core.persistence.tasks.PersistableTask;
import cerberus.core.persistence.tasks.PersistableTasks;

public class TestTask implements PersistableTask {

	@Override
	public void perform() {
		System.out.println("!!!!!!!!!!!!!!!!!!!!!");
		PersistableTasks.scheduleMinutesAfter(1, this);
	}

}
