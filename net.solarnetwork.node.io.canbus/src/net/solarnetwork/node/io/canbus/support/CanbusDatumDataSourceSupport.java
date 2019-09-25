/* ==================================================================
 * CanbusDatumDataSourceSupport.java - 24/09/2019 8:54:44 pm
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

package net.solarnetwork.node.io.canbus.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.scheduling.TaskScheduler;
import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.io.canbus.CanbusConnection;
import net.solarnetwork.node.io.canbus.CanbusNetwork;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingsChangeObserver;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;
import net.solarnetwork.util.OptionalService;

/**
 * A base helper class to support {@link CanbusNetwork} based
 * {@link DatumDataSource} implementations.
 * 
 * @author matt
 * @version 1.0
 */
public abstract class CanbusDatumDataSourceSupport extends DatumDataSourceSupport
		implements SettingsChangeObserver {

	/** The default value for the {@code connectionCheckFrequency} property. */
	public static final long DEFAULT_CONNECTION_CHECK_FREQUENCY = 60000L;

	private final AtomicReference<CanbusConnection> connection = new AtomicReference<CanbusConnection>();

	private OptionalService<CanbusNetwork> canbusNetwork;
	private String busName;
	private TaskScheduler taskScheduler;
	private long connectionCheckFrequency = DEFAULT_CONNECTION_CHECK_FREQUENCY;

	private ScheduledFuture<?> connectionCheckFuture;

	/**
	 * Get setting specifiers for the
	 * {@literal canbusNetwork.propertyFilters['UID']} and {@literal busName}
	 * properties.
	 * 
	 * @return list of setting specifiers
	 */
	public static List<SettingSpecifier> canbusDatumDataSourceSettingSpecifiers(String prefix) {
		List<SettingSpecifier> results = new ArrayList<SettingSpecifier>(16);
		results.add(
				new BasicTextFieldSettingSpecifier(prefix + "canbusNetwork.propertyFilters['UID']", ""));
		results.add(new BasicTextFieldSettingSpecifier(prefix + "busName", ""));
		return results;
	}

	/**
	 * Call once after properties are configured to start up the service.
	 */
	public synchronized void startup() {
		if ( taskScheduler != null && connectionCheckFuture == null ) {
			log.info("Scheduling CAN bus connectivity check for {}ms", connectionCheckFrequency);
			connectionCheckFuture = taskScheduler.scheduleWithFixedDelay(new ConnectionCheck(),
					new Date(System.currentTimeMillis() + 10000L), connectionCheckFrequency);
		}
	}

	/**
	 * Callback after properties have been changed.
	 * 
	 * <p>
	 * This method closes the shared connection if it is open, so that it is
	 * re-opened with the updated configuration.
	 * </p>
	 * 
	 * @param properties
	 *        the changed properties
	 */
	@Override
	public synchronized void configurationChanged(Map<String, Object> properties) {
		closeSharedCanbusConnection();
	}

	/**
	 * Call when the service is no longer needed to clean up resources.
	 */
	public synchronized void shutdown() {
		if ( connectionCheckFuture != null ) {
			connectionCheckFuture.cancel(true);
			connectionCheckFuture = null;
		}
		closeSharedCanbusConnection();
	}

	/**
	 * Get an <b>open</b> CAN bus connection, creating and opening a new
	 * connection if necessary.
	 * 
	 * <p>
	 * An existing shared connection is returned if possible, but should be
	 * treated like a normal, non-shared connection by calling
	 * {@link CanbusConnection#close()} when finished using it.
	 * </p>
	 * 
	 * @return the open connection, or {@literal null} if the connection could
	 *         not be created
	 */
	protected CanbusConnection canbusConnection() {
		CanbusConnection conn = sharedCanbusConnection();
		return (conn != null ? new NonClosingCanbusConnection(conn) : null);
	}

	@SuppressWarnings("resource")
	private CanbusConnection sharedCanbusConnection() {
		CanbusConnection curr, newConn = null;
		do {
			if ( newConn != null && !newConn.isClosed() ) {
				// previous compareAndSet failed, so close and re-try
				try {
					newConn.close();
				} catch ( Exception e ) {
					// ignore
				}
			}
			curr = connection.get();
			if ( curr != null && !curr.isClosed() ) {
				return curr;
			}

			CanbusNetwork network = canbusNetwork();
			if ( network == null ) {
				log.info("No CanbusNetwork available; cannot open connection to bus {}", getBusName());
				return null;
			}
			newConn = network.createConnection(getBusName());
			if ( newConn == null ) {
				return null;
			}
			try {
				newConn.open();
			} catch ( Exception e ) {
				log.error("Error opening CAN bus connection {}: {}", canbusNetworkName(), e.toString());
				return null;
			}
		} while ( !connection.compareAndSet(curr, newConn) );
		return newConn;
	}

	/**
	 * Close the shared {@link CanbusConnection} if possible.
	 */
	private synchronized void closeSharedCanbusConnection() {
		CanbusConnection conn = connection.get();
		if ( conn != null && !conn.isClosed() ) {
			try {
				conn.close();
			} catch ( Exception e ) {
				log.warn("Error closing CAN bus connection {}: {}", canbusNetworkName(), e.toString());
			} finally {
				connection.compareAndSet(conn, null);
			}
		}
	}

	private class ConnectionCheck implements Runnable {

		@Override
		public void run() {
			try {
				CanbusConnection conn = sharedCanbusConnection();
				if ( conn == null ) {
					log.info("No CAN bus connection available to {} (missing configuration?)",
							canbusNetworkName());
				} else {
					Future<Boolean> verification = conn.verifyConnectivity();
					Boolean result = verification.get(connectionCheckFrequency, TimeUnit.MILLISECONDS);
					if ( result != null && result.booleanValue() ) {
						log.info("Verified CAN bus connectivity to {}", canbusNetworkName());
					} else {
						log.warn("Failed to verify CAN bus connectivity to {}; closing connection now",
								canbusNetworkName());
						closeSharedCanbusConnection();
					}
				}
			} catch ( Exception e ) {
				log.error("Error checking CAN bus connection to {}: {}", canbusNetworkName(),
						e.toString());
			}
		}

	}

	/**
	 * Get the configured CAN bus network name.
	 * 
	 * @return the CAN bus network name
	 */
	public String canbusNetworkName() {
		return getBusName() + "@" + canbusNetwork();
	}

	/**
	 * Get the {@link CanbusNetwork} from the configured {@code canbusNetwork}
	 * service, or {@literal null} if not available or not configured.
	 * 
	 * @return the CanbusNetwork or {@literal null}
	 */
	protected final CanbusNetwork canbusNetwork() {
		return (canbusNetwork == null ? null : canbusNetwork.service());
	}

	/**
	 * Get the CAN bus network.
	 * 
	 * @return the network
	 */
	public OptionalService<CanbusNetwork> getCanbusNetwork() {
		return canbusNetwork;
	}

	/**
	 * Set the CAN bus network.
	 * 
	 * @param canbusNetwork
	 *        the network
	 */
	public void setCanbusNetwork(OptionalService<CanbusNetwork> canbusNetwork) {
		this.canbusNetwork = canbusNetwork;
	}

	/**
	 * Get the CAN bus name to use.
	 * 
	 * @return the CAN bus name
	 */
	public String getBusName() {
		return busName;
	}

	/**
	 * Set the CAN bus name to use.
	 * 
	 * @param busName
	 *        the CAN bus name
	 * @throws IllegalArgumentException
	 *         if {@code busName} is {@literal null} or empty
	 */
	public void setBusName(String busName) {
		if ( busName == null || busName.isEmpty() ) {
			throw new IllegalArgumentException("The CAN bus name must be provided.");
		}
		this.busName = busName;
	}

	/**
	 * Get the task scheduler.
	 * 
	 * @return the task scheduler
	 */
	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	/**
	 * Set the task scheduler.
	 * 
	 * @param taskScheduler
	 *        the task scheduler
	 */
	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	/**
	 * Get the connection check frequency.
	 * 
	 * @return the check frequency, in milliseconds; defaults to
	 *         {@link #DEFAULT_CONNECTION_CHECK_FREQUENCY}
	 */
	public long getConnectionCheckFrequency() {
		return connectionCheckFrequency;
	}

	/**
	 * Set the connection check frequency.
	 * 
	 * <p>
	 * A frequency at which to check that the CAN bus connection is still valid,
	 * or {@literal 0} to disable. Requires the
	 * {@link #setTaskScheduler(TaskScheduler)} to have been configured.
	 * </p>
	 * 
	 * @param connectionCheckFrequency
	 *        the frequency to check for a valid connection, in milliseconds
	 */
	public void setConnectionCheckFrequency(long connectionCheckFrequency) {
		this.connectionCheckFrequency = connectionCheckFrequency;
	}

}
