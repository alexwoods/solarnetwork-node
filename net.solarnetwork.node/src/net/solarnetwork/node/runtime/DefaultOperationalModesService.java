/* ==================================================================
 * DefaultOperationalModesService.java - 20/12/2018 10:16:02 AM
 * 
 * Copyright 2018 SolarNetwork.net Dev Team
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

package net.solarnetwork.node.runtime;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import net.solarnetwork.node.OperationalModesService;
import net.solarnetwork.node.dao.SettingDao;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.InstructionStatus.InstructionState;
import net.solarnetwork.node.support.KeyValuePair;
import net.solarnetwork.util.OptionalService;

/**
 * Default implementation of {@link OperationalModesService}.
 * 
 * @author matt
 * @version 1.1
 */
@SuppressWarnings("deprecation")
public class DefaultOperationalModesService implements OperationalModesService, InstructionHandler {

	/** The setting key for operational modes. */
	public static final String SETTING_OP_MODE = "solarnode.opmode";

	/**
	 * The setting key for operational mode expiration dates.
	 *
	 * @since 1.1
	 */
	public static final String SETTING_OP_MODE_EXPIRE = "solarnode.opmode.expire";

	/** The default startup delay value. */
	public static final long DEFAULT_STARTUP_DELAY = TimeUnit.SECONDS.toMillis(10);

	/** The default rate at which to look for auto-expired modes, in seconds. */
	public static final int DEFAULT_AUTO_EXPIRE_MODES_FREQUENCY = 20;

	private final OptionalService<SettingDao> settingDao;
	private final OptionalService<EventAdmin> eventAdmin;
	private long startupDelay = DEFAULT_STARTUP_DELAY;
	private TaskScheduler taskScheduler;
	private int autoExpireModesFrequency = DEFAULT_AUTO_EXPIRE_MODES_FREQUENCY;

	private ScheduledFuture<?> autoExpireScheduledFuture;

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 * 
	 * @param settingDao
	 *        the setting DAO to persist operational mode changes with
	 * @param eventAdmin
	 *        the event service to post notifications with
	 */
	public DefaultOperationalModesService(OptionalService<SettingDao> settingDao,
			OptionalService<EventAdmin> eventAdmin) {
		super();
		this.settingDao = settingDao;
		this.eventAdmin = eventAdmin;
	}

	/**
	 * Call to initialize the service after properties configured.
	 */
	public synchronized void init() {
		// post current modes, i.e. shift from "default" to whatever is active
		Runnable task = new Runnable() {

			@Override
			public void run() {
				Set<String> modes = activeOperationalModes();
				if ( !modes.isEmpty() ) {
					if ( log.isInfoEnabled() ) {
						log.info("Initial active operational modes [{}]",
								commaDelimitedStringFromCollection(modes));
					}
					postOperationalModesChangedEvent(modes);
				}
			}
		};
		if ( taskScheduler != null && startupDelay > 0 ) {
			taskScheduler.schedule(task, new Date(System.currentTimeMillis() + startupDelay));
		} else {
			task.run();
		}
		if ( taskScheduler != null && autoExpireScheduledFuture == null ) {
			autoExpireScheduledFuture = taskScheduler.scheduleWithFixedDelay(new AutoExpireModesTask(),
					new Date(System.currentTimeMillis() + this.autoExpireModesFrequency * 1000L),
					this.autoExpireModesFrequency * 1000L);
		}
	}

	private final class AutoExpireModesTask implements Runnable {

		@Override
		public void run() {
			SettingDao dao = settingDao.service();
			if ( dao == null ) {
				return;
			}
			List<KeyValuePair> modes = dao.getSettings(SETTING_OP_MODE);
			if ( modes == null || modes.isEmpty() ) {
				return;
			}
			Set<String> removed = new LinkedHashSet<>(8);
			for ( Iterator<KeyValuePair> itr = modes.iterator(); itr.hasNext(); ) {
				KeyValuePair mode = itr.next();
				if ( isModeExpired(dao, mode.getValue()) ) {
					dao.deleteSetting(SETTING_OP_MODE, mode.getValue());
					dao.deleteSetting(SETTING_OP_MODE_EXPIRE, mode.getValue());
					itr.remove();
					removed.add(mode.getValue());
				}
			}
			if ( !removed.isEmpty() ) {
				Set<String> newActive = modes.stream().map(m -> m.getValue())
						.sorted(String::compareToIgnoreCase)
						.collect(Collectors.toCollection(LinkedHashSet::new));
				log.info("Expired operational modes [{}]; active modes now [{}]",
						commaDelimitedStringFromCollection(removed),
						commaDelimitedStringFromCollection(newActive));
				postOperationalModesChangedEvent(newActive);
			}
		}
	}

	/**
	 * Call to close internal resources.
	 */
	public synchronized void close() {
		if ( autoExpireScheduledFuture != null ) {
			autoExpireScheduledFuture.cancel(true);
			autoExpireScheduledFuture = null;
		}
	}

	@Override
	public boolean handlesTopic(String topic) {
		return (TOPIC_ENABLE_OPERATIONAL_MODES.equals(topic)
				|| TOPIC_DISABLE_OPERATIONAL_MODES.equals(topic));
	}

	@Override
	public InstructionState processInstruction(Instruction instruction) {
		if ( instruction == null ) {
			return null;
		}
		String topic = instruction.getTopic();
		if ( !handlesTopic(topic) ) {
			return null;
		}
		String[] modes = instruction.getAllParameterValues(INSTRUCTION_PARAM_OPERATIONAL_MODE);
		if ( modes == null || modes.length < 1 ) {
			return InstructionState.Declined;
		}
		DateTime expire = OperationalModesService.expirationDate(instruction);
		Set<String> opModes = new LinkedHashSet<>(Arrays.asList(modes));
		switch (topic) {
			case TOPIC_ENABLE_OPERATIONAL_MODES:
				enableOperationalModes(opModes, expire);
				break;

			case TOPIC_DISABLE_OPERATIONAL_MODES:
				disableOperationalModes(opModes);
				break;

		}
		return InstructionState.Completed;
	}

	@Override
	public boolean isOperationalModeActive(String mode) {
		if ( mode == null || mode.isEmpty() ) {
			return true;
		}
		mode = mode.toLowerCase();
		SettingDao dao = settingDao.service();
		if ( dao == null ) {
			return false;
		}
		String active = dao.getSetting(SETTING_OP_MODE, mode);
		return active != null;
	}

	@Override
	public Set<String> activeOperationalModes() {
		SettingDao dao = settingDao.service();
		if ( dao == null ) {
			return emptySet();
		}
		return activeModesFromSettings(dao);
	}

	private boolean isModeExpired(SettingDao dao, String mode) {
		if ( dao == null ) {
			return false;
		}
		String exp = dao.getSetting(SETTING_OP_MODE_EXPIRE, mode);
		if ( exp != null ) {
			try {
				long date = Long.parseLong(exp);
				if ( date < System.currentTimeMillis() ) {
					return true;
				}
			} catch ( NumberFormatException e ) {
				// ignore
			}
		}
		return false;
	}

	private Set<String> activeModesFromSettings(SettingDao dao) {
		List<KeyValuePair> modes = dao.getSettings(SETTING_OP_MODE);
		if ( modes == null || modes.isEmpty() ) {
			return emptySet();
		}
		return modes.stream().map(m -> m.getValue()).filter(m -> !isModeExpired(dao, m))
				.sorted(String::compareToIgnoreCase)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public Set<String> enableOperationalModes(Set<String> modes) {
		return enableOperationalModes(modes, null);
	}

	@Override
	public Set<String> enableOperationalModes(Set<String> modes, DateTime expire) {
		SettingDao dao = settingDao.service();
		if ( dao == null ) {
			return emptySet();
		}
		if ( modes != null && !modes.isEmpty() ) {
			for ( String mode : modes ) {
				if ( mode == null ) {
					continue;
				}
				mode = mode.toLowerCase();
				dao.storeSetting(SETTING_OP_MODE, mode, mode);
				if ( expire != null ) {
					dao.storeSetting(SETTING_OP_MODE_EXPIRE, mode, String.valueOf(expire.getMillis()));
				} else {
					dao.deleteSetting(SETTING_OP_MODE_EXPIRE, mode);
				}
			}
		}
		Set<String> active = activeModesFromSettings(dao);
		if ( log.isInfoEnabled() ) {
			log.info("Enabled operational modes [{}], expiring [{}]; active modes now [{}]",
					commaDelimitedStringFromCollection(modes), (expire != null ? expire : "never"),
					commaDelimitedStringFromCollection(active));
		}
		postOperationalModesChangedEvent(active);
		return active;
	}

	@Override
	public Set<String> disableOperationalModes(Set<String> modes) {
		SettingDao dao = settingDao.service();
		if ( dao == null ) {
			return emptySet();
		}
		if ( modes != null && !modes.isEmpty() ) {
			for ( String mode : modes ) {
				if ( mode == null ) {
					continue;
				}
				mode = mode.toLowerCase();
				dao.deleteSetting(SETTING_OP_MODE, mode);
			}
		}
		Set<String> active = activeModesFromSettings(dao);
		if ( log.isInfoEnabled() ) {
			log.info("Disabled operational modes [{}]; active modes now [{}]",
					commaDelimitedStringFromCollection(modes),
					commaDelimitedStringFromCollection(active));
		}
		postOperationalModesChangedEvent(active);
		return active;
	}

	private void postOperationalModesChangedEvent(Set<String> activeModes) {
		if ( activeModes == null ) {
			return;
		}
		Event event = createOperationalModesChangedEvent(activeModes);
		postEvent(event);
	}

	protected Event createOperationalModesChangedEvent(Set<String> activeModes) {
		if ( activeModes == null ) {
			activeModes = emptySet();
		}
		Map<String, ?> props = singletonMap(EVENT_PARAM_ACTIVE_OPERATIONAL_MODES, activeModes);
		return new Event(EVENT_TOPIC_OPERATIONAL_MODES_CHANGED, props);
	}

	protected final void postEvent(Event event) {
		EventAdmin ea = (eventAdmin == null ? null : eventAdmin.service());
		if ( ea == null || event == null ) {
			return;
		}
		ea.postEvent(event);
	}

	/**
	 * A startup delay before posting an event of the active operational modes.
	 * 
	 * <p>
	 * Note this requires a {@link #setTaskScheduler(TaskScheduler)} to be
	 * configured if set to anything &gt; {@literal 0}.
	 * </p>
	 * 
	 * @param startupDelay
	 *        a startup delay, in milliseconds, or {@literal 0} for no delay;
	 *        defaults to {@link #DEFAULT_STARTUP_DELAY}
	 */
	public void setStartupDelay(long startupDelay) {
		this.startupDelay = startupDelay;
	}

	/**
	 * Configure a task scheduler.
	 * 
	 * <p>
	 * This is required by {@link #setStartupDelay(long)} as well as for
	 * supporting auto-expiring modes.
	 * </p>
	 * 
	 * @param taskScheduler
	 *        a task executor
	 * @see #setStartupDelay(long)
	 */
	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	/**
	 * Set the frequency, in seconds, at which to look for auto-expired
	 * operational modes.
	 * 
	 * @param autoExpireModesFrequency
	 *        the frequency, in seconds
	 * @throws IllegalArgumentException
	 *         if {@code autoExpireModesFrequency} is less than {@literal 1}
	 * @since 1.1
	 */
	public void setAutoExpireModesFrequency(int autoExpireModesFrequency) {
		if ( autoExpireModesFrequency < 1 ) {
			throw new IllegalArgumentException("autoExpireModesFrequency must be > 0");
		}
		this.autoExpireModesFrequency = autoExpireModesFrequency;
	}

}
