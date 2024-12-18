# KardExo Bot

## Build
Requirements:
- JDK 21 or later

### TeamSpeak
The TeamSpeak distribution can be built using the following command:
```shell
gradlew :ts3:shadowJar
```
The resulting jar will be located in `ts3/build/libs`.

### Discord
The Discord distribution can be built using the following command:
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

## Run
Requirements:
- JRE 21 or later

### TeamSpeak
Run the following command in a terminal:
```shell
java -jar ts3bot-all.jar
```

### Discord
Run the following command in a terminal:
```shell
java -jar discordbot-all.jar
```
