# SolarFlux Upload Service

This project provides SolarNode plugin that posts datum captured by other SolarNode plugins to a
SolarFlux-compatible MQTT server.

![SolarFlux overall settings](docs/solarnode-solarflux-upload-settings.png)

# Install

The plugin is meant for developers and can be manually installed.

# Use

Once installed, a new **SolarFlux Upload Service** component will appear on the **Settings** page on
your SolarNode. Click on the **Manage** button to configure services. You'll need to add one
configuration for each SolarFlux server you want to upload data to.


## MQTT message format

Each datum message is published as a [CBOR][cbor] encoded indefinite-length map. This is
essentially a JSON object. The map keys are the datum property names.

Here's an example datum message, expressed as JSON:

```json
{
  "_DatumType": "net.solarnetwork.node.domain.ACEnergyDatum", 
  "_DatumTypes": [
    "net.solarnetwork.node.domain.ACEnergyDatum", 
    "net.solarnetwork.node.domain.EnergyDatum", 
    "net.solarnetwork.node.domain.Datum", 
    "net.solarnetwork.node.domain.GeneralDatum"
  ], 
  "apparentPower": 2797, 
  "created": 1545167905344, 
  "current": 11.800409317016602, 
  "phase": "PhaseB", 
  "phaseVoltage": 409.89337158203125, 
  "powerFactor": 1.2999000549316406, 
  "reactivePower": -1996, 
  "realPower": 1958, 
  "sourceId": "Ph2", 
  "voltage": 236.9553680419922, 
  "watts": 1958
}
```


# Overall settings

Each component configuration contains the following overall settings:

| Setting | Description |
|---------|-------------|
| Host | The URI for the SolarFlux server to connect to. |
| Username | The MQTT username to use. |
| Password | The MQTT password to use. |
| Exclude Properties | A regular expression to match property names on all datum sources to exclude from publishing. |
| Require Mode | If configured, an operational mode that must be active for any data to be published. |
| Filters | Any number of datum [filter configurations](#filter-settings). |

For TLS-encrypted connections, SolarNode will make the node's own X.509 certificate available for
client authentication.

## Overall settings notes

<dl>
	<dt>Host</dt>
	<dd>The URL to the MQTT server to use. Use <code>mqtts</code> for a TLS encrypted connection,
	or <code>mqtt</code> for no encryption. For example: <code>mqtts://influx.solarnetwork.net:8884</code>.</dd>
	<dt>Password</dt>
	<dd>Note that SolarNode will provide its X.509 certificate on TLS connections, so a password
	might not be necessary.</dd>
	<dt>Exclude Properties</dt>
	<dd>You can exclude all internal datum properties like <code>_DatumType</code> with an expression
	like <code>_.*</code>.</dd>
	<dt>Require Mode</dt>
	<dd>If you would like the ability to control when data is published to SolarFlux you can
	configure an <a href="https://github.com/SolarNetwork/solarnetwork/wiki/SolarNode-Operational-Modes">operational mode</a>,
	and only when that mode is active will data get published to SolarFlux.</dd>
</dl>

# Filter settings

Each component can define any number of filters, which are used to restrict the frequency at which
individual datum sources are posted to SolarFlux, and/or restrict which properties of the datum
are posted. This can be very useful to constrain how much data is sent to SolarFlux, for example
on nodes using mobile internet connections where the cost of posting data is high.

![SolarFlux filter settings](docs/solarnode-solarflux-upload-filter-settings.png)

Each filter configuration contains the following settings:

| Setting | Description |
|---------|-------------|
| Source ID | A case-insensitive regular expression to match against datum source IDs. If defined, this filter will only be applied to datum with matching source ID values. If not defined this filter will be applied to all datum. For example `^solar` would match any source ID starting with _solar_. |
| Limit Seconds | The minimum number of seconds to limit datum that match the configured **Source ID** pattern. If datum are produced faster than this rate, they will be filtered out. Set to `0` or leave empty for no limit. |
| Property Includes | A list of  case-insensitive regular expressions to match against datum property names. If configured, **only** properties that match one of these expressions will be included in the filtered output. For example `^watt` would match any property starting with _watt_.  |
| Property Excludes | A list of  case-insensitive regular expressions to match against datum property names. If configured, **any** property that match one of these expressions will be excluded from the filtered output. For example `^temp` would match any property starting with _temp_. Exclusions are applied **after** property inclusions.  |

> :warning: **WARNING:** the datum `sourceId` and `created` properties will be affected by the 
  property include/exclude filters! If you define **any** include filters, you might want to
  add an include rule for `^created$`. You might like to have `sourceId` removed to conserve
  bandwidth, however, given that value is part of the MQTT topic the datum is posted on and thus
  redundant.

[cbor]: http://cbor.io/
