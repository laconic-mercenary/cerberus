package cerberus.core.persistence.tasks.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import cerberus.core.persistence.entities.ScheduledTaskInfo;
import cerberus.core.persistence.impl.AbstractDao;
import cerberus.core.persistence.tasks.PersistableTask;
import cerberus.core.persistence.tasks.PersistableTaskingManager;

import com.frontier.lib.time.TimeUtil;

public class EntityTaskingManager implements PersistableTaskingManager {

	private static final long serialVersionUID = -3281286684262055515L;

	private static final Logger LOGGER = Logger
			.getLogger(EntityTaskingManager.class);

	private EntityManager entityManager = null;

	public EntityTaskingManager(EntityManager em) {
		setEntityManager(em);
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public boolean schedule(long nextExecutableTimeUTC, PersistableTask task) {
		boolean inserted = false;
		ScheduledTaskInfo sti = new ScheduledTaskInfo();
		sti.setTask(task);
		sti.setTime(nextExecutableTimeUTC);
		try (AbstractDao<ScheduledTaskInfo, Long> dao = new Dao()) {
			dao.setEntityManager(getEntityManager());
			dao.setClazzInfo(ScheduledTaskInfo.class);
			dao.insert(sti);
			inserted = true;
			LOGGER.info("Persisted new S.T.I. with task: "
					+ task.getClass().getName());
		} catch (Exception e) {
			LOGGER.error("Error occurred when attempting to insert S.T.I. with task: "
					+ task.getClass().getName());
			e.printStackTrace();
		}
		return inserted;
	}

	@Override
	public Collection<PersistableTask> fetchReadyTasks() {
		Collection<PersistableTask> readyList = new LinkedList<>();
		List<ScheduledTaskInfo> scheduleList = Collections.emptyList();

		try (AbstractDao<ScheduledTaskInfo, Long> dao = new Dao()) {
			dao.setEntityManager(getEntityManager());
			dao.setClazzInfo(ScheduledTaskInfo.class);
			scheduleList = dao.findAll();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Found " + scheduleList.size() + " S.T.I.s");
			}
		} catch (Exception e) {
			scheduleList = Collections.emptyList();
			LOGGER.error("Failed to fetch all S.T.I.s");
			e.printStackTrace();
		}

		long now = -1;
		for (ScheduledTaskInfo sti : scheduleList) {
			now = TimeUtil.nowUTC().getTime();
			if (now > sti.getTime()) {
				readyList.add(sti.getTask());
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Found %d S.T.I.s that are ready.",
					readyList.size()));
		}

		return readyList;
	}

	public static class Dao extends AbstractDao<ScheduledTaskInfo, Long> {

		private static final long serialVersionUID = -1104230418798602566L;

	}
}
