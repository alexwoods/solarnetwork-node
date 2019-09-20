/* ==================================================================
 * SubscribeMessageImpl.java - 20/09/2019 2:57:04 pm
 * 
 * Copyright 2019 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.io.canbus.socketcand.msg;

import java.util.List;
import net.solarnetwork.node.io.canbus.socketcand.MessageType;
import net.solarnetwork.node.io.canbus.socketcand.SubscribeMessage;

/**
 * Implementation of {@link SubscribeMessage}.
 * 
 * @author matt
 * @version 1.0
 */
public class SubscribeMessageImpl extends AddressedMessage implements SubscribeMessage {

	private final int seconds;
	private final int microseconds;

	/**
	 * Constructor.
	 * 
	 * @param type
	 *        the message type, or {@literal null} if not known
	 * @param command
	 *        the raw command, if {@code type} is {@literal null}
	 * @param arguments
	 *        the raw command arguments
	 * @throws IllegalArgumentException
	 *         if the arguments are inappropriate for a subscribe message
	 */
	public SubscribeMessageImpl(List<String> arguments) {
		super(MessageType.Subscribe, null, arguments, 2);
		try {
			this.seconds = Integer.parseInt(arguments.get(0));
			this.microseconds = Integer.parseInt(arguments.get(1));
		} catch ( NumberFormatException e ) {
			throw new IllegalArgumentException(
					"The seconds [" + arguments.get(0) + "] and/or microseconds [" + arguments.get(1)
							+ "] arguments could not be parsed as numbers.",
					e);
		}
	}

	@Override
	public int getSeconds() {
		return seconds;
	}

	@Override
	public int getMicroseconds() {
		return microseconds;
	}

}
