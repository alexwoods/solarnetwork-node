# SolarNode Advantech ADAM 411x Device Datum Source

This project provides SolarNode plugin that can collect data from Advantech ADAM 4117/4118 devices.

![settings](docs/solarnode-adam411x-device-settings.png)

# Install

The plugin can be installed via the **Plugins** page on your SolarNode. It
appears under the **Datum** category as **Advantech ADAM Data Source**.

# Use

Once installed, a new **Advantech ADAM 411x Module** component will appear on the
**Settings** page on your SolarNode. Click on the **Manage** button to configure
devices. You'll need to add one configuration for each Modbus device you want to
collect data from.

## Overall device settings

Each device configuration contains the following overall settings:

| Setting            | Description                                                                      |
|--------------------|----------------------------------------------------------------------------------|
| Schedule           | A cron schedule that determines when data is collected.                          |
| Service Name       | A unique name to identify this data source with.                                 |
| Service Group      | A group name to associate this data source with.                                 |
| Modbus Port        | The service name of the Modbus port to use.                                      |
| Modbus Unit ID     | The ID of the Modbus device to collect data from, from 1 - 255.                  |
| Sample Maximum Age | A minimum time to cache captured Modbus data, in milliseconds.                   |
| Source ID          | The SolarNetwork unique source ID to assign to datum collected from this device. |

## Overall device settings notes

<dl>
	<dt>Modbus Port</dt>
	<dd>This is the <i>service name</i> of the Modbus component configured elsewhere
	in SolarNode. You must configure that component with the proper connection settings
	for your Modbus network, configure a unique service name on that component, and then
	enter that same service name here.</dd>
	<dt>Source ID</dt>
	<dd>This value unique identifies the data collected from this device, by this node,
	 on SolarNetwork. Each configured device should use a different value.</dd>
	<dt>Sample Maximum Age</dt>
	<dd>SolarNode will cache the data collected from the Modbus device for at least
	this amount of time before refreshing data from the device again. Some devices
	do not refresh their values more than a fixed interval, so this setting can be
	used to avoid reading data unnecessarily. This setting also helps in highly
	dynamic configurations where other plugins request the current values from
	the device frequently.</dd>
</dl>

## Datum property settings

You must configure settings for each datum property you want to collect from each channel of each device.
You can configure as many property settings as you like, using the <kbd>+</kbd> and <kbd>-</kbd>
buttons to add/remove property configurations.

Each property configuration contains the following settings:

| Setting         | Description                                                                     |
|-----------------|---------------------------------------------------------------------------------|
| Name            | An arbitrary name used only for reference.                                      |
| Property        | The name of the datum property to save the channel value as.                    |
| Property Type   | The type of datum property to use.                                              |
| Channel         | The ADAM channel to read from (zero-based).                                     |
| Offset          | An offset to apply to the Modbus value to shift the output range.               |
| Unit Multiplier | A multiplier to apply to the Modbus value to normalize it into a standard unit. |
| Decimal Scale   | A maximum number of decimal places to round decimal numbers to.                 |

## Datum property settings notes

<dl>
	<dt>Property</dt>
	<dd>Property names represent what the associated data value is, and SolarNetwork
	has many standardized names that you should consider using. For example the
	<a href="https://github.com/SolarNetwork/solarnetwork-node/blob/master/net.solarnetwork.node/src/net/solarnetwork/node/domain/EnergyDatum.java">EnergyDatum</a>
	class defines properties such as <code>watts</code> and <code>wattHours</code>
	for electrical energy.</dd>
	<dt>Property Type</dt>
	<dd>Each property must be categorized as <code>Accumulating</code>, <code>Instantaneous</code>,
	or <code>Status</code>. <b>Accumulating</b> is used for properties that record
	a value that accumulates over time, such as <code>wattHours</code> captured from
	a power meter. <b>Instantaneous</b> is used for properties that capture values
	that record independent values over time, such as <code>watts</code>. <b>Status</b>
	is used for non-numeric values such as string status messages.</dd>
	<dt>Offset</dt>
	<dd>This can be used to shift a sensor's input range into a different output range. For example
	for a ±15mV sensor, configure <code>0.015</code> to shift the output to 0-30mV. Note how the
	offsets are specified in <b>standard channel units</b>, so <code>V</code> in this example.</dd>
	<dt>Unit Multiplier</dt>
	<dd>The property values stored in SolarNetwork should be normalized into standard
	base units if possible. For example if a power meter reports power in <i>kilowattts</i>
	then a unit multiplier of <code>1000</code> can be used to convert the values into
	<i>watts</i>. This transformation is applied <b>after</b> the configured <b>Offset</b> value.</dd>
	<dt>Decimal Scale</dt>
	<dd>This setting will round decimal numbers to at most this number of decimal places. Setting
	to <code>0</code> rounds decimals to whole numbers. Setting to <code>-1</code> disables
	rounding completely.</dd>
</dl>
