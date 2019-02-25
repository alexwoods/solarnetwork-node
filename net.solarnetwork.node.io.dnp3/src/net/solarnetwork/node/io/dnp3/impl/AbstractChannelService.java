/* ==================================================================
 * AbstractChannelService.java - 21/02/2019 5:53:24 pm
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

package net.solarnetwork.node.io.dnp3.impl;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import com.automatak.dnp3.Channel;
import com.automatak.dnp3.ChannelListener;
import com.automatak.dnp3.ChannelStatistics;
import com.automatak.dnp3.DNP3Exception;
import com.automatak.dnp3.DNP3Manager;
import com.automatak.dnp3.LinkStatistics;
import com.automatak.dnp3.enums.ChannelState;
import net.solarnetwork.node.io.dnp3.ChannelService;

/**
 * Abstract implementation of {@link ChannelService}.
 * 
 * @param C
 *        the channel configuration
 * @author matt
 * @version 1.0
 */
public abstract class AbstractChannelService<C extends BaseChannelConfiguration>
		implements ChannelService, ChannelListener {

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final DNP3Manager manager;
	private final C config;
	private String uid = "DNP3 Channel";
	private String groupUID;
	private MessageSource messageSource;

	private Channel channel;
	private ChannelState channelState = ChannelState.CLOSED;

	/**
	 * Constructor.
	 * 
	 * @param manager
	 *        the manager
	 * @param config
	 *        the config
	 */
	public AbstractChannelService(DNP3Manager manager, C config) {
		super();
		this.manager = manager;
		this.config = config;
	}

	/**
	 * Configure and start the channel.
	 */
	public synchronized void startup() {
		configurationChanged(null);
	}

	/**
	 * Callback after properties have been changed.
	 * 
	 * @param properties
	 *        the changed properties
	 */
	public void configurationChanged(Map<String, Object> properties) {
		shutdown();
		try {
			channel = createChannel(config);
		} catch ( DNP3Exception e ) {
			log.error("Error creating DNP3 channel [{}]: {}", uid, e.getMessage(), e);
		}
	}

	/**
	 * Shutdown the channel.
	 */
	public synchronized void shutdown() {
		if ( channel != null ) {
			channel.shutdown();
			channel = null;
		}
	}

	/**
	 * Get the manager.
	 * 
	 * @return the manager
	 */
	protected DNP3Manager getManager() {
		return manager;
	}

	/**
	 * Get the configuration.
	 * 
	 * @return the config
	 */
	public C getConfig() {
		return config;
	}

	@Override
	public final synchronized Channel dnp3Channel() {
		if ( channel == null ) {
			try {
				channel = createChannel(config);
			} catch ( DNP3Exception e ) {
				log.error("Error creating DNP3 channel [{}]: {}", uid, e.getMessage(), e);
			}
		}
		return channel;
	}

	/**
	 * Create a new channel instance using the given configuration.
	 * 
	 * @param configuration
	 *        the configuration
	 * @return the channel
	 */
	protected abstract Channel createChannel(C configuration) throws DNP3Exception;

	@Override
	public void onStateChange(ChannelState state) {
		log.info("Channel [{}] state changed to {}", getUid(), state);
		this.channelState = state;
	}

	/**
	 * Get a simple string status message.
	 * 
	 * @return the message, never {@literal null}
	 */
	protected synchronized String getChannelStatusMessage() {
		StringBuilder buf = new StringBuilder();
		if ( channel == null ) {
			buf.append("N/A");
		} else {
			buf.append(channelState);
			LinkStatistics linkStats = channel.getStatistics();
			ChannelStatistics stats = (linkStats != null ? linkStats.channel : null);
			if ( stats != null ) {
				buf.append("; ").append(stats.numOpen).append(" open");
				buf.append("; ").append(stats.numClose).append(" close");
				buf.append("; ").append(stats.numOpenFail).append(" open fail");
				buf.append("; ").append(stats.numBytesRx / 1024).append(" KB in");
				buf.append("; ").append(stats.numBytesTx / 1024).append(" KB out");
			}
		}
		return buf.toString();
	}

	/**
	 * Alias for the {@link #getUID()} method.
	 * 
	 * @return the unique ID
	 * @see #getUID()
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Set the unique ID to identify this service with.
	 * 
	 * @param uid
	 *        the unique ID; defaults to {@literal Modbus Port}
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
	public String getUID() {
		return uid;
	}

	@Override
	public String getGroupUID() {
		return groupUID;
	}

	/**
	 * Set the group unique ID to identify this service with.
	 * 
	 * @param groupUID
	 *        the group unique ID
	 */
	public void setGroupUID(String groupUID) {
		this.groupUID = groupUID;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

}
