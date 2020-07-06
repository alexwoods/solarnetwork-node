/* ==================================================================
 * JMBusWMBusNetwork.java - 29/06/2020 12:36:22 pm
 * 
 * Copyright 2020 SolarNetwork.net Dev Team
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

package net.solarnetwork.node.io.mbus.jmbus;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.wireless.WMBusListener;
import net.solarnetwork.node.io.mbus.MBusMessage;
import net.solarnetwork.node.io.mbus.MBusMessageHandler;
import net.solarnetwork.node.io.mbus.MBusSecondaryAddress;
import net.solarnetwork.node.io.mbus.WMBusConnection;
import net.solarnetwork.node.io.mbus.WMBusNetwork;
import net.solarnetwork.node.support.BaseIdentifiable;

/**
 * Abstract jMBus implementation of {@link WMBusNetwork}.
 * 
 * @author alex
 * @version 1.0
 */
public abstract class JMBusWMBusNetwork extends BaseIdentifiable implements WMBusNetwork, WMBusListener {

	private org.openmuc.jmbus.wireless.WMBusConnection connection;
	private ConcurrentMap<org.openmuc.jmbus.SecondaryAddress, Set<JMBusWMBusConnection>> listeners = new ConcurrentHashMap<org.openmuc.jmbus.SecondaryAddress, Set<JMBusWMBusConnection>>();

	protected abstract org.openmuc.jmbus.wireless.WMBusConnection createJMBusConnection()
			throws IOException;

	private synchronized org.openmuc.jmbus.wireless.WMBusConnection getOrCreateConnection()
			throws IOException {
		if ( connection == null ) {
			connection = createJMBusConnection();
		}
		return connection;
	}

	@Override
	public WMBusConnection createConnection(MBusSecondaryAddress address, byte[] key) {
		return new JMBusWMBusConnection(address, key);
	}

	@Override
	public void newMessage(org.openmuc.jmbus.wireless.WMBusMessage message) {
		SecondaryAddress addr = message.getSecondaryAddress();

		// route message to all registered listeners
		if ( addr != null ) {
			Set<JMBusWMBusConnection> conns = listeners.get(addr);
			if ( conns != null ) {
				MBusMessage msg = JMBusConversion.from(message);
				for ( JMBusWMBusConnection conn : conns ) {
					// invoke net.solarnetwork.node.io.mbus.MessageHandler handleMessage() method
					conn.handleMessage(msg);
				}
			}
		}
	}

	@Override
	public void discardedBytes(byte[] bytes) {
	}

	@Override
	public void stoppedListening(IOException cause) {
		// TODO Handle this somehow
	}

	/**
	 * 
	 * Proxying connection class
	 */
	private class JMBusWMBusConnection extends WMBusConnection implements MBusMessageHandler {

		private final SecondaryAddress address;
		private byte[] key;
		private org.openmuc.jmbus.wireless.WMBusConnection conn;

		private JMBusWMBusConnection(MBusSecondaryAddress address, byte[] key) {
			this.address = JMBusConversion.to(address);
			this.key = key;
		}

		@Override
		public void open() throws IOException {
			if ( conn == null ) {
				this.conn = getOrCreateConnection();
				if ( conn != null ) {
					Set<JMBusWMBusConnection> conns = listeners.computeIfAbsent(address,
							k -> new CopyOnWriteArraySet<>());
					conns.add(this);
					conn.addKey(this.address, key);
				}
			}
		}

		@Override
		public synchronized void close() {
			if ( conn != null ) {
				conn.removeKey(address);
				Set<JMBusWMBusConnection> conns = listeners.get(address);
				if ( conns != null ) {
					conns.remove(this);
				}
			}
		}
	}
}
