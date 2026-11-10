# Lights

A home lighting controller for my apartment: an Android app and a small Python server that talks to the [Shelly](https://www.shelly.com/) switches, dimmers and bulbs on my local network.

I built it because I wanted one place to control everything, with a proper "day" and "night" setup instead of juggling several vendor apps. The server runs on the LAN and does the real work, so the app stays thin and every connected client sees the same state in real time.

## Screenshots

|![Homescreen](https://github.com/user-attachments/assets/36f4cdfe-e702-4ac5-8fe7-a408815e8814)|![Devices](https://github.com/user-attachments/assets/36c63934-dc75-4ee5-a12f-038d932fa25a)|![Device settings](https://github.com/user-attachments/assets/ca5d1b72-cab2-4f85-bcbe-a6d6a4eae49b)|![Schedule screen](https://github.com/user-attachments/assets/68373a3e-f9a4-40b7-b84c-d014c8eae56f)|
|---|---|---|---|

## Features

- **Light groups.** A group is a named room or fixture with a *day* light and a *night* light, which can be different physical devices. For example, a wall switch during the day and a dimmed bulb at night.
- **Day / night mode.** Toggling the mode moves each group's state to the matching light. Night brightness is scaled globally with a single slider and capped per group.
- **Device types.** Relay switches, dimmers, color-temperature (CCT) lights and bulbs.
- **Live sync.** State changes are pushed to all clients over a WebSocket, so the app, wall switches and any HTTP triggers stay consistent.
- **Schedule.** Optionally switch back to day mode and/or turn everything off at a set time.
- **Editable setup.** Add, reorder, edit and delete groups from the app, and save the result back to the server config.
- **Auth.** The server requires a shared secret on every request. The app stores it encrypted with an AES key held in the Android Keystore.

## Architecture

```
┌──────────────┐   REST + WebSocket   ┌────────────────┐   HTTP RPC   ┌────────────┐
│ Android app  │ ───────────────────> │ Python server  │ ───────────> │ Shelly     │
│ (Compose)    │ <─────────────────── │ (Sanic)        │              │ devices    │
└──────────────┘      state push      └────────────────┘              └────────────┘
```

**Server** (`server/`): async [Sanic](https://sanic.dev/) app. It keeps the group state in memory, queries devices with [httpx](https://www.python-httpx.org/) concurrently, and broadcasts changes to subscribers. Groups, schedule and credentials live in `config.json`. A background loop runs the schedule.

**App** (`app/`): Kotlin with Jetpack Compose and Material 3. A single `MainViewModel` holds UI state, Retrofit/OkHttp handles the REST calls, and a WebSocket client keeps the state live. The password is encrypted with the Android Keystore and persisted in DataStore.

## Tech stack

| Part | Technologies |
|---|---|
| Android | Kotlin, Jetpack Compose, Navigation, Retrofit, OkHttp, Gson, Coroutines |
| Server | Python 3, Sanic, httpx |
| Hardware | Shelly switches, dimmers, CCT lights and bulbs |

## Running it

### Server

```bash
cd server
pip install -r requirements.txt
python main.py
```

It listens on port `100` by default (change it at the bottom of `main.py`). Edit `config.json` to match your setup:

```jsonc
{
  "password": "choose-something",
  "night_mode_ip": "192.168.0.203",   // device whose input reports night mode
  "schedule": { "hour": 5, "minute": 30, "all_off": false, "night_mode": false },
  "groups": [
    {
      "id": 0,
      "name": "Living Room",
      "day":   { "ip": "192.168.0.204", "type": "switch", "id": 1 },
      "night": { "ip": "192.168.0.201", "type": "bulb", "brightness": 100 }
    }
  ]
}
```

`type` is one of `switch`, `dimmer`, `cct` or `bulb`.

Note: requests from the `192.168.0.2xx` range (the Shelly devices themselves) skip authentication, so wall switches can call the server directly. Adjust this in `auth_middleware` if your network is laid out differently.

### Android app

Open `app/` in Android Studio, or build from the command line:

```bash
cd app
./gradlew assembleDebug
```

Requires Android 7.0+ (API 24). The server address is set as `BASE_URL` in `RestClient.kt`; on first launch the app asks for the password. The server itself speaks plain HTTP, so put it behind a TLS-terminating reverse proxy if you expose it outside your LAN. Cleartext for the configured host is allowed in `network_security_config.xml`.

## Project layout

```
app/      Android app (Compose UI, ViewModel, REST + WebSocket clients)
server/   Sanic server and config
```

## Notes

This is a personal project, so some things are tailored to my setup (device addresses, the hardcoded server host, the IP-based auth shortcut, no built-in TLS). It's meant to show how it's put together, not to be a drop-in product.

## License

[TWAORPWB](LICENSE) applies.
