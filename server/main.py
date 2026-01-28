from sanic.exceptions import Unauthorized, NotFound
from sanic import Sanic, Request
from sanic.response import empty
import json as json
import asyncio
import base64
import httpx

app = Sanic("Lights")
CONFIG_FILE = "config.json"


async def fetch_state(light, url, selector, type, key):
    try:
        resp = await app.ctx.client.get(url)
        light["on"] = resp.is_success and resp.json()[selector]
        if resp.is_success and (type == "bulb" or type == "dimmer") and "brightness" in resp.json() and app.ctx.night_mode:
            light["brightness"] = resp.json()["brightness"] if key == "night" else 100
    except Exception:
        print(f"Failed to fetch state for light at {light['ip']}")


async def fetch_states(groups):
    tasks = []
    for group in groups:
        for key in ["day", "night"]:
            light = group[key]
            if light["type"] == "switch":
                url = f"http://{light['ip']}/rpc/Switch.GetStatus?id={light['id']}"
                selector = "output"
            elif light["type"] == "dimmer":
                url = f"http://{light['ip']}/rpc/Light.GetStatus?id=0"
                selector = "output"
            else:
                url = f"http://{light['ip']}/light/0/status"
                selector = "ison"

            tasks.append(fetch_state(light, url, selector, light["type"], key))

    await asyncio.gather(*tasks)
    for group in groups:
        if group["day"]["ip"] == group["night"]["ip"] and group["day"]["on"] and group["night"]["on"]:
            group["night"]["on"] = app.ctx.night_mode
            group["day"]["on"] = not app.ctx.night_mode


async def get_night_mode():
    try:
        resp = await app.ctx.client.get(f"http://{app.ctx.night_mode_ip}/rpc/Input.GetStatus?id=0")
        return resp.is_success and resp.json()["state"]
    except Exception:
        print(f"Failed to fetch night mode state from {app.ctx.night_mode_ip}")
        return False


async def schedule_loop():
    seconds_in_day = 60 * 60 * 24
    while True:
        try:
            schedule = app.ctx.schedule
            if schedule["hour"] and schedule["minute"]:
                now = asyncio.get_event_loop().time() % seconds_in_day
                target = schedule["hour"] * 3600 + schedule["minute"] * 60
                if abs(now - target) < 30:
                    if schedule["all_off"]:
                        app.ctx.all_off = True
                        await asyncio.gather(*(update_group(g) for g in app.ctx.groups))
                    if schedule["night_mode"]:
                        app.ctx.night_mode = False
                        for group in app.ctx.groups:
                            group["day"]["on"] = group["day"]["on"] or group["night"]["on"]
                            group["night"]["on"] = False
                        await asyncio.gather(*(update_group(g) for g in app.ctx.groups))
                    asyncio.create_task(broadcast())
        except Exception:
            pass
        await asyncio.sleep(30)


@app.listener("before_server_start")
async def config(app: Sanic):
    app.ctx.client = httpx.AsyncClient(timeout=2)
    app.ctx.scaled_brightness_dict = {}
    app.ctx.update_all = False
    app.ctx.all_off = False
    app.ctx.sockets = set()
    app.ctx.last = None
    with open(CONFIG_FILE) as file:
        data = json.load(file)
        app.ctx.schedule = data["schedule"]
        app.ctx.password = data["password"]
        app.ctx.groups = data["groups"]
        app.ctx.night_mode_ip = data["night_mode_ip"]
        app.ctx.night_mode = await get_night_mode()
    asyncio.create_task(fetch_states(app.ctx.groups))
    asyncio.create_task(schedule_loop())
    update_scaled_brightness(50)


@app.middleware("request")
async def auth_middleware(request: Request):
    if request.ip.startswith("192.168.0.2"):
        if app.ctx.all_off:
            app.ctx.all_off = False
            app.ctx.update_all = True
        return
    header = request.headers.get("auth")
    if not header:
        raise Unauthorized("Missing authorization header")
    try:
        decoded = base64.b64decode(header).decode()
    except Exception:
        raise Unauthorized("Invalid token")
    if decoded != app.ctx.password:
        raise Unauthorized("Forbidden")


@app.get("/")
async def ping(request: Request):
    await request.receive_body()
    return empty()


def update_scaled_brightness(brightness: float):
    app.ctx.brightness = percent = max(0, min(100, brightness))
    app.ctx.scaled_brightness_dict = {
        group["id"]: (
            max(1, int(value * (percent / 50))) if percent <= 50
            else int(value + (100 - value) * ((percent - 50) / 50))
        )
        for group in app.ctx.groups
        for value in (group.get("night", {}).get("brightness"),)
        if isinstance(value, (int, float))
    }


async def update_group(group: dict):
    if "on" not in group["day"] or "on" not in group["night"]:
        await fetch_states([group])

    keys = ["night", "day"] if app.ctx.night_mode else ["day", "night"]
    if group["day"]["ip"] == group["night"]["ip"]:
        if group["day"]["on"] and not group["night"]["on"]:
            keys = ["day"]
        elif group["night"]["on"] and not group["day"]["on"]:
            keys = ["night"]
        else:
            keys = ["night"] if app.ctx.night_mode else ["day"]

    for key in keys:
        light = group[key]
        on = light["on"] and not app.ctx.all_off

        if light["type"] == "switch":
            url = f"http://{light['ip']}/rpc/switch.set?id={light['id']}&on={str(on).lower()}"
        elif light["type"] == "dimmer":
            url = f"http://{light['ip']}/rpc/light.set?id=0&on={str(on).lower()}"
        else:
            url = f"http://{light['ip']}/light/0?turn={'on' if on else 'off'}"

        if light["type"] in ["dimmer", "bulb"]:
            scalable = light.get("scalable", True)
            brightness = app.ctx.scaled_brightness_dict.get(group["id"], 100) if scalable else light.get("brightness", 100)
            brightness = brightness if key == "night" else 100
            url += f"&brightness={brightness}"

        await app.ctx.client.get(url)


async def handle_light_set_request(request: Request, id: int, setting: bool):
    light = request.args.get("light", "night" if app.ctx.night_mode else "day")
    group = next((g for g in app.ctx.groups if g["id"] == id), None)
    alt_light = "day" if light == "night" else "night"
    single = request.args.get("light") is not None

    if not group:
        raise NotFound("Group not found")

    group[light]["on"] = setting if setting is not None else not group[light]["on"]
    if not single or group[light]["ip"] == group[alt_light]["ip"]:
        group[alt_light]["on"] = False

    if app.ctx.update_all:
        await asyncio.gather(*(update_group(group) for group in app.ctx.groups))
        app.ctx.update_all = False
    else:
        asyncio.create_task(update_group(group))
    asyncio.create_task(broadcast())
    await request.receive_body()
    return empty()


@app.get("/on/<id:int>")
async def on(request: Request, id: int):
    return await handle_light_set_request(request, id, True)


@app.get("/off/<id:int>")
async def off(request: Request, id: int):
    return await handle_light_set_request(request, id, False)


@app.get("/toggle/<id:int>")
async def toggle(request: Request, id: int):
    return await handle_light_set_request(request, id, None)


@app.get("/mode/<state>")
async def mode(request: Request, state: str):
    app.ctx.night_mode = state == "night"

    for group in app.ctx.groups:
        light = "night" if app.ctx.night_mode else "day"
        alt_light = "day" if app.ctx.night_mode else "night"
        group[light]["on"] = group["day"]["on"] or group["night"]["on"]
        group[alt_light]["on"] = False

    await asyncio.gather(*(update_group(group) for group in app.ctx.groups))
    asyncio.create_task(broadcast())
    await request.receive_body()
    return empty()


@app.get("/all-off/<on>")
async def all_off(request: Request, on: str):
    app.ctx.all_off = on == "on"
    await asyncio.gather(*(update_group(group) for group in app.ctx.groups))
    asyncio.create_task(broadcast())
    await request.receive_body()
    return empty()


@app.get("/brightness/<value:float>")
async def brightness(request: Request, value: float):
    update_scaled_brightness(value)
    await asyncio.gather(*(update_group(group) for group in app.ctx.groups))
    asyncio.create_task(broadcast())
    await request.receive_body()
    return empty()


@app.get("/refresh")
async def refresh(request: Request):
    await fetch_states(app.ctx.groups)
    asyncio.create_task(broadcast(True))
    await request.receive_body()
    return empty()


@app.get("/reset")
async def reset(request: Request):
    try:
        with open(CONFIG_FILE) as file:
            current_groups = {group["id"]: group for group in app.ctx.groups}
            data = json.load(file)
            groups = {}
            for old_group in data["groups"]:
                if old_group["id"] not in current_groups:
                    groups[old_group["id"]] = old_group
                    await fetch_states([old_group])
                else:
                    groups[old_group["id"]] = {}
                    for key, old_value in old_group.items():
                        group_item = current_groups[old_group["id"]][key]
                        if isinstance(old_value, dict) and isinstance(group_item, dict):
                            groups[old_group["id"]][key] = group_item | old_value
                        else:
                            groups[old_group["id"]][key] = old_value
            app.ctx.groups = list(groups.values())
        update_scaled_brightness(50)
        await asyncio.gather(*(update_group(group) for group in app.ctx.groups))
        asyncio.create_task(broadcast(True))
        await request.receive_body()
        return empty()
    except Exception:
        raise NotFound("Failed to load config file")


@app.get("/save")
async def save(request: Request):
    groups = [
        dict(group, day={k: v for k, v in group["day"].items() if k != "on"},
             night={k: v for k, v in group["night"].items() if k != "on"})
        for group in app.ctx.groups
    ]

    with open(CONFIG_FILE, "w") as file:
        json.dump({
            "password": app.ctx.password,
            "night_mode_ip": app.ctx.night_mode_ip,
            "schedule": app.ctx.schedule,
            "groups": groups
        }, file, indent=4)

    await request.receive_body()
    return empty()


async def broadcast(forceSend: bool = False):
    data = {
        "night_mode": app.ctx.night_mode,
        "brightness": app.ctx.brightness,
        "all_off": app.ctx.all_off,
        "groups": app.ctx.groups,
        "schedule": app.ctx.schedule
    }
    msg = json.dumps(data)
    if app.ctx.sockets and (forceSend or not app.ctx.last or app.ctx.last != msg):
        await asyncio.gather(*(ws.send(msg) for ws in list(app.ctx.sockets)))
        app.ctx.last = msg


@app.websocket("/subscribe")
async def subscribe(request, ws):
    app.ctx.sockets.add(ws)
    await ws.send(json.dumps({
        "night_mode": app.ctx.night_mode,
        "brightness": app.ctx.brightness,
        "all_off": app.ctx.all_off,
        "groups": app.ctx.groups,
        "schedule": app.ctx.schedule
    }))
    try:
        async for _ in ws:
            pass
    finally:
        app.ctx.sockets.remove(ws)


@app.put("/update")
async def update(request: Request):
    current_groups = {group["id"]: group for group in app.ctx.groups}
    groups = []
    for group in request.json:
        current = current_groups[group["id"]]
        current.update({
            key: (current[key] | value if isinstance(value, dict) and isinstance(current[key], dict) else value)
            for key, value in group.items()
        })
        groups.append(current)
    app.ctx.groups = groups
    update_scaled_brightness(app.ctx.brightness)
    await asyncio.gather(*(update_group(group) for group in app.ctx.groups))
    asyncio.create_task(broadcast())
    await request.receive_body()
    return empty()


@app.delete("/delete/<id:int>")
async def delete(request: Request, id: int):
    app.ctx.groups = [group for group in app.ctx.groups if group["id"] != id]
    asyncio.create_task(broadcast())
    await request.receive_body()
    return empty()


@app.post("/schedule")
async def schedule(request: Request):
    body = request.json
    app.ctx.schedule = {
        "hour": int(body.get("hour")),
        "minute": int(body.get("minute")),
        "all_off": bool(body.get("all_off")),
        "night_mode": bool(body.get("night_mode"))
    }
    asyncio.create_task(broadcast())

    with open(CONFIG_FILE, "r+") as file:
        data = json.load(file)
        data["schedule"] = app.ctx.schedule
        file.seek(0)
        file.truncate()
        json.dump(data, file, indent=4)

    await request.receive_body()
    return empty()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=100)
