package cerberus.core.commands;

import static com.frontier.lib.validation.TextValidator.raiseIfEmptyStr;
import cerberus.core.commands.impl.BasicSenderInformation;

public interface SenderInformation {

	public static class Factory {
		public static SenderInformation make(String name, String address) {
			raiseIfEmptyStr(address);
			raiseIfEmptyStr(name);
			return new BasicSenderInformation(name, address);
		}
	}

	public String getName();

	public String getAddress();

}
