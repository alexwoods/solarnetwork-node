/* ==================================================================
 * BasicIdentifiable.java - 15/05/2019 3:42:21 pm
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

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.MessageSource;
import net.solarnetwork.node.Identifiable;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.support.BasicIdentifiable;

/**
 * Basic implementation of {@link Identifiable} and
 * {@link net.solarnetwork.domain.Identifiable} combined.
 * 
 * <p>
 * This class is meant to be extended by more useful services.
 * </p>
 * 
 * @author matt
 * @version 1.2
 * @since 1.67
 */
public abstract class BaseIdentifiable extends net.solarnetwork.support.BasicIdentifiable
		implements Identifiable, net.solarnetwork.domain.Identifiable {

	/**
	 * Get settings for the configurable properties of
	 * {@link BasicIdentifiable}.
	 * 
	 * <p>
	 * Empty strings are used for the default {@code uid} and {@code groupUid}
	 * setting values.
	 * </p>
	 * 
	 * @param prefix
	 *        an optional prefix to include in all setting keys
	 * @return the settings
	 * @see #baseIdentifiableSettings(String, String, String)
	 */
	public static List<SettingSpecifier> baseIdentifiableSettings(String prefix) {
		return baseIdentifiableSettings(prefix, "", "");
	}

	/**
	 * Get settings for the configurable properties of
	 * {@link BasicIdentifiable}.
	 * 
	 * @param prefix
	 *        an optional prefix to include in all setting keys
	 * @param defaultUid
	 *        the default {@code uid} value to use
	 * @param defaultGroupUid
	 *        the default {@code groupUid} value to use
	 * @return the settings
	 * @since 1.1
	 */
	public static List<SettingSpecifier> baseIdentifiableSettings(String prefix, String defaultUid,
			String defaultGroupUid) {
		if ( prefix == null ) {
			prefix = "";
		}
		List<SettingSpecifier> results = new ArrayList<>(8);
		results.add(new BasicTextFieldSettingSpecifier(prefix + "uid", defaultUid));
		results.add(new BasicTextFieldSettingSpecifier(prefix + "groupUid", defaultGroupUid));
		return results;
	}

	/**
	 * Alias for {@link #getUid()}.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public String getUID() {
		return getUid();
	}

	/**
	 * Set the UID.
	 * 
	 * <p>
	 * This is an alias for {@link #setUid(String)}.
	 * </p>
	 * 
	 * @param uid
	 *        the UID to set
	 */
	public void setUID(String uid) {
		setUid(uid);
	}

	/**
	 * Alias for {@link #getGroupUid()}.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public String getGroupUID() {
		return getGroupUid();
	}

	/**
	 * Set the group UID.
	 * 
	 * <p>
	 * This is an alias for {@link #setGroupUid(String)}.
	 * </p>
	 * 
	 * @param groupUid
	 *        the group UID to set
	 */
	public void setGroupUID(String groupUid) {
		setGroupUid(groupUid);
	}

	/*-----
	 * The following methods are here for package-import
	 * backwards-compatibility, before BasicIdentifiable existed.
	 *----- */

	@Override
	public String getUid() {
		return super.getUid();
	}

	@Override
	public void setUid(String uid) {
		super.setUid(uid);
	}

	@Override
	public String getGroupUid() {
		return super.getGroupUid();
	}

	@Override
	public void setGroupUid(String groupUid) {
		super.setGroupUid(groupUid);
	}

	@Override
	public void setDisplayName(String displayName) {
		super.setDisplayName(displayName);
	}

	@Override
	public MessageSource getMessageSource() {
		return super.getMessageSource();
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		super.setMessageSource(messageSource);
	}

}
