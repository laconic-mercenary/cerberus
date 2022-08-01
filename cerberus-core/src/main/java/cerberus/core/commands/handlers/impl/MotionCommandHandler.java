package cerberus.core.commands.handlers.impl;

import static com.frontier.lib.validation.TextValidator.isEmptyStr;
import static com.frontier.lib.validation.TextValidator.isOverflowing;

import org.apache.log4j.Logger;

import cerberus.core.commands.SenderInformation;
import cerberus.core.commands.handlers.CommandHandler;
import cerberus.core.commands.impl.MotionCommand;
import cerberus.core.eventbus.CerberusEventBus;
import cerberus.core.persistence.entities.MotionInfo;
import cerberus.core.persistence.entities.validation.ValidationResult;
import cerberus.core.persistence.entities.validation.Validator;
import cerberus.core.persistence.entities.validation.impl.BasicValidationResult;

// these handlers shouldn't have fields in them
// should not be expected to maintain state
public class MotionCommandHandler implements CommandHandler<MotionCommand> {

	private static final Logger LOGGER = Logger
			.getLogger(MotionCommandHandler.class);

	@Override
	public void handle(MotionCommand command) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Handling: " + command.getClass().getName());

		doHandle(command);
	}

	private static void doHandle(MotionCommand cmd) {
		MotionInfo mi = MotionInfo.Factory.make(cmd.getSender().getName(), cmd
				.getSender().getAddress());
		Validator<MotionCommand> validator = MotionCommandVisitor.Factory
				.make();

		ValidationResult result = validator.validate(cmd);
		if (result.isSuccessful()) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("MotionCommand has passed validation, posting on event bus...");

			// give the information to subscribers
			CerberusEventBus.get().post(mi);
		} else {
			LOGGER.warn(result.getMessage().getText());
		}
	}

	private static final class MotionCommandVisitor implements
			Validator<MotionCommand> {

		public static final class Factory {

			public static MotionCommandVisitor make() {
				return new MotionCommandVisitor();
			}

		}

		private static final String MSG_INVALID_SENDER = "Invalid sender machine name: %s";

		private static final String MSG_INVALID_SENDER_ADDRESS = "Invalid sender address: %s";

		private static final int ADDR_MAX_LEN = 75;

		private static final int NAME_MAX_LEN = 75;

		@Override
		public ValidationResult validate(MotionCommand target) {
			// oops... no factory...
			BasicValidationResult result = new BasicValidationResult();
			SenderInformation si = target.getSender();
			if (isEmptyStr(si.getName())
					|| isOverflowing(si.getName(), NAME_MAX_LEN)) {
				result.setMessage(
						String.format(MSG_INVALID_SENDER, si.getName()), false);
			} else {
				if (isEmptyStr(si.getAddress())
						|| isOverflowing(si.getAddress(), ADDR_MAX_LEN)) {
					result.setMessage(
							String.format(MSG_INVALID_SENDER_ADDRESS,
									si.getAddress()), false);
				} else {
					result.makeSuccessful();
				}
			}
			return result;
		}
	}
}
