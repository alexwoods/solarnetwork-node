/* ==================================================================
 * NativeTarPlatformPackageService.java - 22/05/2019 4:27:43 pm
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

package net.solarnetwork.node.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.node.PlatformPackageService;
import net.solarnetwork.util.ProgressListener;

/**
 * {@link PlatformPackageService} that extracts tar archives using the host's
 * native {@code tar} program.
 * 
 * @author matt
 * @version 1.0
 * @since 1.68
 */
public class NativeTarPlatformPackageService extends BasePlatformPackageService {

	/**
	 * The placeholder string in the {@code syncCommand} for the source
	 * directory path.
	 */
	public static final String SOURCE_FILE_PLACEHOLDER = "__SOURCE_FILE__";

	/**
	 * The placeholder string in the {@code syncCommand} for the destination
	 * directory path.
	 */
	public static final String DESTINATION_DIRECTORY_PLACEHOLDER = "__DEST_DIR__";

	/**
	 * The default value of the {@code tarCommand} property.
	 * 
	 * <p>
	 * The tar command is expected to print the names of the files as it
	 * extracts them, which is usually done with a {@literal -v} argument.
	 * </p>
	 */
	public static final List<String> DEFAULT_TAR_COMMAND = Collections.unmodifiableList(Arrays
			.asList("tar", "xvf", SOURCE_FILE_PLACEHOLDER, "-C", DESTINATION_DIRECTORY_PLACEHOLDER));

	private static final Pattern TARBALL_PAT = Pattern.compile("\\.(tar|tgz|tbz2|txz)$");
	private static final Pattern TAR_LIST_PAT = Pattern.compile("^\\w (.*)$");

	private List<String> tarCommand = DEFAULT_TAR_COMMAND;

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public boolean handlesPackage(String archiveFileName) {
		return archiveFileName != null && TARBALL_PAT.matcher(archiveFileName).find();
	}

	@Override
	public <T> Future<PlatformPackageInstallResult<T>> installPackage(Path archive, Path baseDirectory,
			ProgressListener<T> progressListener, T context) {
		return performTask(createTask(archive, baseDirectory, progressListener, context), context);
	}

	protected <T> Callable<PlatformPackageInstallResult<T>> createTask(Path archive, Path baseDirectory,
			ProgressListener<T> progressListener, T context) {
		return new Callable<PlatformPackageService.PlatformPackageInstallResult<T>>() {

			@Override
			public PlatformPackageInstallResult<T> call() throws Exception {
				List<String> cmd = new ArrayList<>(tarCommand.size());
				String tarballPath = archive.toAbsolutePath().toString();
				for ( String param : tarCommand ) {
					param = param.replace(SOURCE_FILE_PLACEHOLDER, tarballPath);
					param = param.replace(DESTINATION_DIRECTORY_PLACEHOLDER, baseDirectory.toString());
					cmd.add(param);
				}
				if ( log.isDebugEnabled() ) {
					StringBuilder buf = new StringBuilder();
					for ( String p : cmd ) {
						if ( buf.length() > 0 ) {
							buf.append(' ');
						}
						buf.append(p);
					}
					log.debug("Tar command: {}", buf.toString());
				}
				log.info("Extracting tar archive {}", archive);
				List<Path> extractedPaths = new ArrayList<>();
				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.redirectErrorStream(true); // OS X tar output list to STDERR; Linux GNU tar to STDOUT
				Process pr = pb.start();
				try (BufferedReader in = new BufferedReader(
						new InputStreamReader(pr.getInputStream()))) {
					String line = null;
					while ( (line = in.readLine()) != null ) {
						Matcher m = TAR_LIST_PAT.matcher(line);
						if ( m.matches() ) {
							line = m.group(1);
						}
						Path path = FileSystems.getDefault().getPath(line).toAbsolutePath().normalize();
						extractedPaths.add(path);
						log.trace("Installed setup resource: {}", line);
					}
				}
				try {
					pr.waitFor();
				} catch ( InterruptedException e ) {
					log.warn("Interrupted waiting for tar command to complete");
				}
				if ( pr.exitValue() != 0 ) {
					String output = extractedPaths.stream().map(p -> p.toString())
							.collect(Collectors.joining("\n")).trim();
					log.error("Tar command returned non-zero exit code {}: {}", pr.exitValue(), output);
					throw new IOException(
							"Tar command returned non-zero exit code " + pr.exitValue() + ": " + output);
				}

				return new BasicPlatformPackageInstallResult<T>(true, null, null, extractedPaths,
						context);
			}
		};
	}

	@Override
	public Iterable<PlatformPackage> listPackages(String nameFilter, Boolean installedFilter) {
		return Collections.emptyList();
	}

	@Override
	public <T> Future<PlatformPackageInstallResult<T>> installPackage(String name, String version,
			Path baseDirectory, ProgressListener<T> progressListener, T context) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set the command and arguments to use for extracting tar resources.
	 * 
	 * <p>
	 * The arguments support {@literal __SOURCE_FILE__} and
	 * {@literal __DEST_DIR__} placeholders that will be replaced by the input
	 * tar file path and the value of the {@code destinationPath} property.
	 * 
	 * @param tarCommand
	 */
	public void setTarCommand(List<String> tarCommand) {
		this.tarCommand = tarCommand;
	}

}
