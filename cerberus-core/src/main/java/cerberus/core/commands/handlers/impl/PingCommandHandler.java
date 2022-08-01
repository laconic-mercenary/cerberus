package cerberus.core.commands.handlers.impl;

import java.util.Random;

import org.apache.log4j.Logger;

import cerberus.core.commands.handlers.CommandHandler;
import cerberus.core.commands.impl.PingCommand;
import cerberus.core.eventbus.CerberusEventBus;
import cerberus.core.persistence.DaoManager;
import cerberus.core.persistence.EntityDao;
import cerberus.core.persistence.entities.PingInfo;
import cerberus.core.persistence.entities.validation.ValidationResult;
import cerberus.core.persistence.entities.validation.Validator;
import cerberus.core.persistence.entities.validation.impl.PingInfoValidation;

import com.frontier.lib.time.TimeUtil;

public class PingCommandHandler implements CommandHandler<PingCommand> {

	private static final Random GENERATOR = new Random();

	private static final int RAND_CEILING = Short.MAX_VALUE;

	private static final Logger LOGGER = Logger
			.getLogger(PingCommandHandler.class);

	private static final String MSG_INVALID_PING = "Received invalid Ping (validation failed) : %s";

	@Override
	public void handle(PingCommand command) {
		// this must be self contained (shouldn't maintain state)
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Handling: " + command.getClass().getName());

		doHandle(command);
	}

	private static long generateId() {
		long id = TimeUtil.nowUTC().getTime();
		long pad = GENERATOR.nextInt(RAND_CEILING);
		return id + pad;
	}

	private static void doHandle(PingCommand cmd) {
		final long nowStamp = TimeUtil.nowUTC().getTime();
		final String addr = cmd.getSender().getAddress();
		final String machine = cmd.getSender().getName();

		PingInfo pi = PingInfo.Factory.make(addr, machine, nowStamp);

		Validator<PingInfo> dataValidator = new PingInfoValidation();
		ValidationResult vr = dataValidator.validate(pi);

		if (vr.isSuccessful()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Received ping: addr=%s name=%s",
						addr, machine));
			}

			try (EntityDao<PingInfo, Long> dao = DaoManager.Factory
					.connect(PingInfo.class)) {
				PingInfo existing = find(dao, pi);
				if (existing == null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(String
								.format("Did not find PingInfo: %s/%s. Treating at new entry.",
										pi.getMachineName(), pi.getAddress()));
					}

					// set the generated id
					pi.setId(generateId());
					
					// insert to persistence layer
					dao.insert(pi);
				} else {
					// in this case, update the
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Received Ping from existing client: "
								+ pi.getMachineName() + ". Updating rx time.");
					}

					// update the time this was received
					existing.setPingReceivedTime(nowStamp);
					dao.update(existing);
					pi = existing;
				}
			} catch (Exception e) {
				LOGGER.error("Failed to insert PingInfo record: "
						+ pi.getClass().getSimpleName());
				e.printStackTrace();
				
				// do not get to the post below if an issue occurred...
				return;
			}

			// post notification
			CerberusEventBus.get().post(pi);
		} else {
			LOGGER.warn(String.format(MSG_INVALID_PING, vr.getMessage()
					.getText()));
		}
	}

	private static PingInfo find(EntityDao<PingInfo, Long> dao, PingInfo pi) {
		for (PingInfo existing : dao.findAll()) {
			if (existing.getMachineName().equals(pi.getMachineName())
					&& existing.getAddress().equals(pi.getAddress())) {
				return existing;
			}
		}
		return null;
	}
}
