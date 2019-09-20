/* ==================================================================
 * SocketcandCanbusNetwork.java - 19/09/2019 4:13:04 pm
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

package net.solarnetwork.node.io.canbus.socketcand;

import com.github.kayak.core.SocketcandConnection;
import net.solarnetwork.node.io.canbus.CanbusConnection;
import net.solarnetwork.node.io.canbus.support.AbstractCanbusNetwork;

/**
 * CAN bus network implementation using the socketcand server protocol.
 * 
 * @author matt
 * @version 1.0
 * @see <a href=
 *      "https://github.com/linux-can/socketcand">linux-can/socketcand</a>
 */
public class SocketcandCanbusNetwork extends AbstractCanbusNetwork {

	/** The default host value. */
	public static final String DEFAULT_HOST = "localhost";

	/** The default port value. */
	public static final int DEFAULT_PORT = 29536;

	private String host = DEFAULT_HOST;
	private int port = DEFAULT_PORT;

	private int socketTimeout = SocketcandCanbusConnection.DEFAULT_SOCKET_TIMEOUT;
	private boolean socketTcpNoDelay = SocketcandCanbusConnection.DEFAULT_SOCKET_TCP_NO_DELAY;
	private boolean socketReuseAddress = SocketcandCanbusConnection.DEFAULT_SOCKET_REUSE_ADDRESS;
	private int socketLinger = SocketcandCanbusConnection.DEFAULT_SOCKET_LINGER;
	private boolean socketKeepAlive = SocketcandCanbusConnection.DEFAULT_SOCKET_KEEP_ALIVE;

	/**
	 * Constructor.
	 */
	public SocketcandCanbusNetwork() {
		super();
	}

	@Override
	public String getDisplayName() {
		return "TCP CAN bus";
	}

	@Override
	protected String getNetworkDescription() {
		return host + ":" + port;
	}

	@Override
	public CanbusConnection createConnection(String busName) {
		SocketcandCanbusConnection conn = new SocketcandCanbusConnection(getHost(), getPort(), busName);
		conn.setSocketKeepAlive(isSocketKeepAlive());
		conn.setSocketLinger(getSocketLinger());
		conn.setSocketReuseAddress(isSocketReuseAddress());
		conn.setSocketTcpNoDelay(isSocketTcpNoDelay());
		conn.setSocketTimeout(getSocketTimeout());
		return conn;
	}

	/**
	 * Get the host to connect to.
	 * 
	 * @return the host; defaults to {@link #DEFAULT_HOST}
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Set the host to connect to.
	 * 
	 * @param host
	 *        the host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Get the port to connect to.
	 * 
	 * @return the port; defaults to {@link #DEFAULT_PORT}
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set the port to connect to.
	 * 
	 * @param port
	 *        the port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Get the timeout for blocking socket operations like reading from the
	 * socket.
	 * 
	 * @return the socket timeout, in milliseconds; defaults to
	 *         {@link #DEFAULT_SOCKET_TIMEOUT}
	 */
	public int getSocketTimeout() {
		return socketTimeout;
	}

	/**
	 * Set the timeout for blocking socket operations like reading from the
	 * socket.
	 * 
	 * @param socketTimeout
	 *        the socket timeout to use, in milliseconds
	 */
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	/**
	 * Get the TCP "no delay" flag.
	 * 
	 * @return {@literal true} if the TCP "no delay" option should be used;
	 *         defaults to
	 *         {@link SocketcandConnection#DEFAULT_SOCKET_TCP_NO_DELAY}
	 */
	public boolean isSocketTcpNoDelay() {
		return socketTcpNoDelay;
	}

	/**
	 * Set the TCP "no delay" flag.
	 * 
	 * @param socketTcpNoDelay
	 *        {@literal true} if the TCP "no delay" option should be used
	 */
	public void setSocketTcpNoDelay(boolean socketTcpNoDelay) {
		this.socketTcpNoDelay = socketTcpNoDelay;
	}

	/**
	 * Get the socket "reuse address" flag.
	 * 
	 * @return {@literal true} if the socket "reuse address" flag should be
	 *         used; defaults to
	 *         {@link SocketcandConnection#DEFAULT_SOCKET_REUSE_ADDRESS}
	 */
	public boolean isSocketReuseAddress() {
		return socketReuseAddress;
	}

	/**
	 * Set the socket "reuse address" flag.
	 * 
	 * @param socketReuseAddress
	 *        {@literal true} if the socket "reuse address" flag should be used
	 */
	public void setSocketReuseAddress(boolean socketReuseAddress) {
		this.socketReuseAddress = socketReuseAddress;
	}

	/**
	 * Get the socket linger amount.
	 * 
	 * @return the socket linger amount, in seconds, or {@literal 0} to disable;
	 *         defaults to {@link SocketcandConnection#DEFAULT_SOCKET_LINGER}
	 */
	public int getSocketLinger() {
		return socketLinger;
	}

	/**
	 * Set the socket linger amount.
	 * 
	 * @param socketLinger
	 *        the socket linger amount, in seconds, or {@literal 0} to disable
	 */
	public void setSocketLinger(int socketLinger) {
		this.socketLinger = socketLinger;
	}

	/**
	 * Get the socket "keep alive" flag.
	 * 
	 * @return {@literal true} if the socket "keep alive" flag should be used;
	 *         defaults to
	 *         {@link SocketcandConnection#DEFAULT_SOCKET_KEEP_ALIVE}
	 */
	public boolean isSocketKeepAlive() {
		return socketKeepAlive;
	}

	/**
	 * Set the socket "keep alive" flag.
	 * 
	 * @return {@literal true} if the socket "keep alive" flag should be used
	 */
	public void setSocketKeepAlive(boolean socketKeepAlive) {
		this.socketKeepAlive = socketKeepAlive;
	}

}
