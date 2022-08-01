package cerberus.core.persistence.entities.validation.impl;

import static cerberus.core.persistence.entities.validation.ValidationMessages.overflowingStr;
import static cerberus.core.persistence.entities.validation.ValidationMessages.required;
import static com.frontier.lib.validation.TextValidator.isEmptyStr;
import static com.frontier.lib.validation.TextValidator.isOverflowing;
import cerberus.core.persistence.entities.PingInfo;
import cerberus.core.persistence.entities.validation.ValidationResult;
import cerberus.core.persistence.entities.validation.Validator;

public class PingInfoValidation implements Validator<PingInfo> {

	@Override
	public ValidationResult validate(PingInfo target) {
		ValidationResult vr = null;
		vr = new Address().validate(target);
		if (!vr.isSuccessful())
			return vr;

		vr = new MachineName().validate(target);
		return vr;
	}

	public static class Address implements Validator<PingInfo> {

		@Override
		public ValidationResult validate(PingInfo target) {
			BasicValidationResult bvr = new BasicValidationResult(false);
			if (isEmptyStr(target.getAddress())) {
				bvr.setMessage(required("Address"), true);
			} else {
				if (isOverflowing(target.getAddress(), PingInfo.MAXLEN_ADDRESS)) {
					bvr.setMessage(
							overflowingStr("Address", PingInfo.MAXLEN_ADDRESS),
							true);
				} else {
					bvr.makeSuccessful();
				}
			}
			return bvr;
		}
	}

	public static class MachineName implements Validator<PingInfo> {

		@Override
		public ValidationResult validate(PingInfo target) {
			BasicValidationResult bvr = new BasicValidationResult(false);
			if (isEmptyStr(target.getMachineName())) {
				bvr.setMessage(required("Machine Name"), true);
			} else {
				if (isOverflowing(target.getMachineName(),
						PingInfo.MAXLEN_MACHINENAME)) {
					bvr.setMessage(
							overflowingStr("Machine Name",
									PingInfo.MAXLEN_MACHINENAME), true);
				} else {
					bvr.makeSuccessful();
				}
			}
			return bvr;
		}
	}
}
