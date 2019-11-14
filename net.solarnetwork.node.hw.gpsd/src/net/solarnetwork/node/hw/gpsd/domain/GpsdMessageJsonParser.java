/* ==================================================================
 * GpsdMessageJsonParser.java - 12/11/2019 9:05:45 am
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

package net.solarnetwork.node.hw.gpsd.domain;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import com.fasterxml.jackson.core.TreeNode;

/**
 * API for parsing JSON data into {@link GpsdMessage} instances.
 * 
 * @param <T>
 *        the supported message type
 * @author matt
 * @version 1.0
 */
public interface GpsdMessageJsonParser<T extends GpsdMessage> {

	/**
	 * Parse a JSON tree into a message instance.
	 * 
	 * @param node
	 *        the JSON to parse
	 * @return the message instance, or {@literal null} if the message cannot be
	 *         parsed
	 */
	T parseJsonTree(TreeNode node);

	/**
	 * Parse an ISO 8601 timestamp value into an {@link Instant}.
	 * 
	 * @param timestamp
	 *        the timestamp value
	 * @return the instant, or {@literal null} if {@code timestamp} is
	 *         {@literal null}, empty, or cannot be parsed
	 */
	static Instant iso8610Timestamp(String timestamp) {
		Instant ts = null;
		if ( timestamp != null && !timestamp.isEmpty() ) {
			try {
				ts = Instant.parse(timestamp);
			} catch ( DateTimeParseException e ) {
				// ignore
			}
		}
		return ts;
	}

}
