/* ==================================================================
 * CanbusDatumDataSourceSimulator.java - 18/12/2019 6:46:31 am
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

package net.solarnetwork.node.datum.canbus;

import static java.util.Collections.singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import net.solarnetwork.node.io.canbus.CanbusFrame;
import net.solarnetwork.node.io.canbus.CanbusFrameFlag;
import net.solarnetwork.node.io.canbus.KcdParser;
import net.solarnetwork.node.io.canbus.support.JaxbSnKcdParser;
import net.solarnetwork.node.settings.FactorySettingSpecifierProvider;
import net.solarnetwork.node.settings.SettingResourceHandler;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.SettingSpecifierProviderFactory;
import net.solarnetwork.node.settings.SettingsBackup;
import net.solarnetwork.node.settings.SettingsCommand;
import net.solarnetwork.node.settings.SettingsImportOptions;
import net.solarnetwork.node.settings.SettingsService;
import net.solarnetwork.node.settings.SettingsUpdates;
import net.solarnetwork.util.ByteUtils;
import net.solarnetwork.util.ClassUtils;
import net.solarnetwork.util.JsonUtils;
import net.solarnetwork.util.StaticOptionalService;

/**
 * Simulate the effects of CAN bus messages on a {@link CanbusDatumDataSource}.
 * 
 * <p>
 * This class has a {@code main()} method so it can be run from the command
 * line. Pass the path to a SolarNetwork KCD file to load, along with the path
 * to a {@literal candump} log file to parse. It will then parse the log file
 * and print out the result of each log message getting processed by a
 * {@link CanbusDatumDataSource} configured from the KCD.
 * </p>
 * 
 * @author matt
 * @version 1.0
 */
public class CanbusDatumDataSourceSimulator {

	/** Pattern to match {@literal candump} log messages. */
	public static final Pattern CANDUMP_LOG_PATTERN = Pattern
			.compile("\\((\\d+)\\.(\\d+)\\)\\s+(\\w+)\\s+([0-9A-F]+)\\#([0-9A-F]+)");

	private final Map<String, CanbusDatumDataSource> dataSources;

	/**
	 * Constructor.
	 */
	public CanbusDatumDataSourceSimulator(Path kcdPath) {
		super();
		try {
			this.dataSources = applyKcdConfiguration(kcdPath);
		} catch ( IOException e ) {
			throw new RuntimeException("Unable to apply KCD configuration: " + e.getMessage(), e);
		}
	}

	private Map<String, CanbusDatumDataSource> applyKcdConfiguration(Path path) throws IOException {
		FileSystemResource r = new FileSystemResource(path.toFile());
		KcdConfigurer configurer = new KcdConfigurer(
				new StaticOptionalService<KcdParser>(new JaxbSnKcdParser(true)),
				new InternalSettingsService());
		SettingsUpdates updates = configurer.applySettingResources(KcdConfigurer.RESOURCE_KEY_KCD_FILE,
				singleton(r));
		Map<String, CanbusDatumDataSource> dsMap = new LinkedHashMap<>(8);
		Map<String, Map<String, Object>> propMap = new LinkedHashMap<>(8);
		if ( updates != null && updates.hasSettingValueUpdates() ) {

			for ( SettingsUpdates.Change c : updates.getSettingValueUpdates() ) {
				Map<String, Object> props = propMap.computeIfAbsent(c.getInstanceKey(),
						k -> new LinkedHashMap<>(64));
				props.put(c.getKey(), c.getValue());
			}
		}
		for ( Map.Entry<String, Map<String, Object>> me : propMap.entrySet() ) {
			CanbusDatumDataSource ds = new CanbusDatumDataSource();
			ds.setUid(me.getKey());
			ClassUtils.setBeanProperties(ds, me.getValue(), true);
			ds.configurationChanged(me.getValue());
			dsMap.put(me.getKey(), ds);
		}
		return dsMap;
	}

	public static final class InternalSettingsService implements SettingsService {

		private int instanceId = -1;

		@Override
		public List<SettingSpecifierProvider> getProviders() {
			return null;
		}

		@Override
		public List<SettingSpecifierProviderFactory> getProviderFactories() {
			return null;
		}

		@Override
		public SettingSpecifierProviderFactory getProviderFactory(String factoryUID) {
			return null;
		}

		@Override
		public String addProviderFactoryInstance(String factoryUID) {
			return String.valueOf(++instanceId);
		}

		@Override
		public void deleteProviderFactoryInstance(String factoryUID, String instanceUID) {
		}

		@Override
		public Map<String, FactorySettingSpecifierProvider> getProvidersForFactory(String factoryUID) {
			return Collections.emptyMap();
		}

		@Override
		public Object getSettingValue(SettingSpecifierProvider provider, SettingSpecifier setting) {
			return null;
		}

		@Override
		public void updateSettings(SettingsCommand command) {
			// ignore
		}

		@Override
		public SettingResourceHandler getSettingResourceHandler(String handlerKey, String instanceKey) {
			return null;
		}

		@Override
		public Iterable<Resource> getSettingResources(String handlerKey, String instanceKey,
				String settingKey) throws IOException {
			return null;
		}

		@Override
		public void importSettingResources(String handlerKey, String instanceKey, String settingKey,
				Iterable<Resource> resources) throws IOException {
			// ignore
		}

		@Override
		public void exportSettingsCSV(Writer out) throws IOException {
			// ignore
		}

		@Override
		public void importSettingsCSV(Reader in) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void importSettingsCSV(Reader in, SettingsImportOptions options) throws IOException {
			// ignore
		}

		@Override
		public SettingsBackup backupSettings() {
			return null;
		}

		@Override
		public Collection<SettingsBackup> getAvailableBackups() {
			return null;
		}

		@Override
		public Reader getReaderForBackup(SettingsBackup backup) {
			return null;
		}

	}

	private static final class InternalCanbusFrame implements CanbusFrame {

		private final int address;
		private final byte[] data;

		private InternalCanbusFrame(int address, byte[] data) {
			super();
			this.address = address;
			this.data = data;
		}

		@Override
		public int getAddress() {
			return address;
		}

		@Override
		public int getDataLength() {
			return (data != null ? data.length : 0);
		}

		@Override
		public byte[] getData() {
			return data;
		}

		@Override
		public boolean isFlagged(CanbusFrameFlag flag) {
			if ( flag == CanbusFrameFlag.ExtendedFormat ) {
				return isExtendedAddress();
			}
			return false;
		}

	}

	private static final class InternalEventAdmin implements EventAdmin {

		private final PrintStream out;
		private int count = 0;

		private InternalEventAdmin(PrintStream out) {
			super();
			this.out = out;
		}

		@Override
		public void postEvent(Event event) {
			sendEvent(event);
		}

		@Override
		public void sendEvent(Event event) {
			Map<String, Object> data = new LinkedHashMap<>(event.getPropertyNames().length);
			for ( String p : event.getPropertyNames() ) {
				if ( p.startsWith("_") || "event.topics".equals(p) || "created".equals(p) ) {
					continue;
				}
				data.put(p, event.getProperty(p));
			}
			if ( data.size() < 2 ) {
				// only contains sourceId; skip
				return;
			}
			String json = JsonUtils.getJSONString(data, "{}");
			out.println(String.format("%05d %s", ++count, json));
		}

	}

	/**
	 * Parse {@literal candump} log messages and print the datum results as JSON
	 * to the output stream.
	 * 
	 * @param int
	 *        the {@literal candump} log data to parse
	 * @param out
	 *        the output stream to write to
	 * @throws IOException
	 *         if any IO error occurs
	 */
	public void parseCandumpMessages(BufferedReader in, PrintStream out) throws IOException {
		String l;
		for ( CanbusDatumDataSource ds : dataSources.values() ) {
			ds.setEventAdmin(new StaticOptionalService<>(new InternalEventAdmin(out)));
		}
		while ( (l = in.readLine()) != null ) {
			Matcher m = CANDUMP_LOG_PATTERN.matcher(l);
			if ( !m.matches() ) {
				continue;
			}
			//long s = Long.parseLong(m.group(1));
			//int ns = Integer.parseInt(m.group(2));
			//String busName = m.group(3);
			int addr = Integer.parseInt(m.group(4), 16);
			byte[] msg = ByteUtils.decodeHexString(m.group(5));
			CanbusFrame f = new InternalCanbusFrame(addr, msg);
			for ( CanbusDatumDataSource ds : dataSources.values() ) {
				ds.canbusFrameReceived(f);
			}
		}
	}

	/**
	 * Main entry point.
	 * 
	 * @param args
	 *        the path to the KCD file and the path to the {@literal candump}
	 *        compatible input data
	 */
	public static void main(String[] args) {
		if ( args == null || args.length < 2 ) {
			System.err.println("Must provide <KCD> <candump> file paths as arguments.");
			return;
		}
		try (BufferedReader in = Files.newBufferedReader(Paths.get(args[1]), Charset.forName("UTF-8"))) {
			new CanbusDatumDataSourceSimulator(Paths.get(args[0])).parseCandumpMessages(in, System.out);
		} catch ( IOException e ) {
			System.err.println("Error opening candump file: " + e.getMessage());
		}
	}

}
