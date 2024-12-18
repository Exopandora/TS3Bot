# KardExo Bot

## Build
### TeamSpeak
The teamspeak distribution can be built using the following command:
```shell
gradlew :ts3:shadowJar
```
The resulting jar will be located in `ts3/build/libs`.

### Discord
The discord distribution can be built using the following command:
```shell
gradlew :discord:shadowJar
```
The resulting jar will be located in `discord/build/libs`.

## Configuration
Configuration can be done by modifying config.json.
Some config options are platform specific.

### TeamSpeak
Config options specific for TeamSpeak:
- host_address
- login_name
- login_password
- channel_name
- virtual_server_id

### Discord
Config options specific for Discord:
- token
