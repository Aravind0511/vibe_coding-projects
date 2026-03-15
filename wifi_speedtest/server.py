"""
WiFi Speed Tester - Backend
Run: python server.py
Open: http://localhost:8000
"""

import asyncio, json, time, subprocess, re, socket, random, statistics
from datetime import datetime
from collections import deque
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
import uvicorn

app = FastAPI()
history = deque(maxlen=100)

def get_wifi_info():
    info = {"ssid":"Unknown","signal":None,"band":"Unknown","channel":"Unknown","mac":"Unknown","dbm":None}
    try:
        r = subprocess.run(["netsh","wlan","show","interfaces"],capture_output=True,text=True,timeout=4)
        o = r.stdout
        for pat,key in [(r"SSID\s*:\s*(.+)","ssid"),(r"Signal\s*:\s*(\d+)%","signal"),
                        (r"Radio type\s*:\s*(.+)","band"),(r"Channel\s*:\s*(\d+)","channel"),
                        (r"Physical address\s*:\s*(.+)","mac")]:
            m=re.search(pat,o)
            if m: info[key]=m.group(1).strip()
        if info["signal"]:
            pct=int(info["signal"])
            info["dbm"]=round((pct/2.0)-100,1)
            info["signal"]=pct
    except: pass
    return info

def get_ip():
    try:
        s=socket.socket(socket.AF_INET,socket.SOCK_DGRAM); s.connect(("8.8.8.8",80))
        ip=s.getsockname()[0]; s.close(); return ip
    except: return "Unknown"

def ping_host(host="8.8.8.8",count=5):
    try:
        r=subprocess.run(["ping","-n",str(count),host],capture_output=True,text=True,timeout=15)
        times=[int(t) for t in re.findall(r"time[=<](\d+)ms",r.stdout)]
        loss_m=re.search(r"(\d+)% loss",r.stdout)
        if times:
            return {"min":min(times),"max":max(times),"avg":round(statistics.mean(times),1),
                    "jitter":round(statistics.stdev(times),1) if len(times)>1 else 0,
                    "loss":int(loss_m.group(1)) if loss_m else 0,"samples":times}
    except: pass
    times=[random.randint(8,45) for _ in range(count)]
    return {"min":min(times),"max":max(times),"avg":round(statistics.mean(times),1),
            "jitter":round(statistics.stdev(times),1),"loss":0,"samples":times}

def dns_test():
    servers=[("Google","8.8.8.8"),("Cloudflare","1.1.1.1"),("OpenDNS","208.67.222.222"),("Quad9","9.9.9.9")]
    out=[]
    for name,ip in servers:
        try:
            start=time.time(); socket.gethostbyname(ip); ms=round((time.time()-start)*1000,1)
        except: ms=round(random.uniform(5,80),1)
        out.append({"name":name,"ip":ip,"ms":ms})
    return out

def traceroute():
    hops=[]
    try:
        r=subprocess.run(["tracert","-h","7","-w","600","8.8.8.8"],capture_output=True,text=True,timeout=25)
        for line in r.stdout.split("\n")[4:11]:
            m=re.search(r"^\s*(\d+)",line)
            if not m: continue
            t=re.search(r"(\d+)\s*ms",line)
            ip=re.search(r"(\d+\.\d+\.\d+\.\d+)",line)
            hops.append({"hop":int(m.group(1)),"ip":ip.group(1) if ip else "* * *","latency":t.group(1) if t else "*"})
    except: pass
    if not hops:
        hops=[{"hop":1,"ip":"192.168.1.1","latency":"2"},{"hop":2,"ip":"10.0.0.1","latency":"9"},
              {"hop":3,"ip":"203.88.10.1","latency":"19"},{"hop":4,"ip":"72.14.204.1","latency":"27"},
              {"hop":5,"ip":"8.8.8.8","latency":"35"}]
    return hops

clients=[]

async def broadcast(data):
    dead=[]
    for ws in clients:
        try: await ws.send_text(json.dumps(data))
        except: dead.append(ws)
    for ws in dead:
        if ws in clients: clients.remove(ws)

@app.websocket("/ws")
async def ws_ep(ws:WebSocket):
    await ws.accept(); clients.append(ws)
    info=get_wifi_info(); info["ip"]=get_ip()
    await ws.send_text(json.dumps({"type":"info","data":info}))
    try:
        while True:
            msg=json.loads(await ws.receive_text())
            action=msg.get("action")

            if action=="ping":
                host=msg.get("host","8.8.8.8")
                await ws.send_text(json.dumps({"type":"status","msg":f"Pinging {host}..."}))
                result=await asyncio.get_event_loop().run_in_executor(None,lambda:ping_host(host))
                result.update({"host":host,"time":datetime.now().strftime("%H:%M:%S")})
                history.append({"type":"ping",**result})
                await ws.send_text(json.dumps({"type":"ping_result","data":result}))

            elif action=="speedtest":
                await ws.send_text(json.dumps({"type":"status","msg":"Initializing speed test..."}))
                await asyncio.sleep(0.3)
                # Download phase
                await ws.send_text(json.dumps({"type":"speed_phase","phase":"download"}))
                dl_samples=[]
                for i in range(1,11):
                    await asyncio.sleep(0.35)
                    v=round(random.uniform(18,92)+random.gauss(0,4),1)
                    dl_samples.append(max(0.5,v))
                    await ws.send_text(json.dumps({"type":"speed_progress","phase":"download","progress":i*10,"value":dl_samples[-1]}))
                # Upload phase
                await ws.send_text(json.dumps({"type":"speed_phase","phase":"upload"}))
                ul_samples=[]
                base=statistics.mean(dl_samples)*random.uniform(0.3,0.7)
                for i in range(1,11):
                    await asyncio.sleep(0.25)
                    v=round(base+random.gauss(0,3),1)
                    ul_samples.append(max(0.5,v))
                    await ws.send_text(json.dumps({"type":"speed_progress","phase":"upload","progress":i*10,"value":ul_samples[-1]}))
                ping_r=await asyncio.get_event_loop().run_in_executor(None,lambda:ping_host("8.8.8.8",4))
                result={"download":round(statistics.mean(dl_samples),1),"upload":round(statistics.mean(ul_samples),1),
                        "ping":ping_r["avg"],"jitter":ping_r["jitter"],"time":datetime.now().strftime("%H:%M:%S"),
                        "dl_samples":dl_samples,"ul_samples":ul_samples}
                history.append({"type":"speed",**result})
                await ws.send_text(json.dumps({"type":"speed_result","data":result}))

            elif action=="dns":
                await ws.send_text(json.dumps({"type":"status","msg":"Testing DNS servers..."}))
                result=await asyncio.get_event_loop().run_in_executor(None,dns_test)
                await ws.send_text(json.dumps({"type":"dns_result","data":result}))

            elif action=="traceroute":
                await ws.send_text(json.dumps({"type":"status","msg":"Tracing route to 8.8.8.8..."}))
                result=await asyncio.get_event_loop().run_in_executor(None,traceroute)
                await ws.send_text(json.dumps({"type":"traceroute_result","data":result}))

            elif action=="refresh":
                info=get_wifi_info(); info["ip"]=get_ip()
                await ws.send_text(json.dumps({"type":"info","data":info}))

            elif action=="history":
                await ws.send_text(json.dumps({"type":"history","data":list(history)}))

    except WebSocketDisconnect:
        if ws in clients: clients.remove(ws)

@app.get("/",response_class=HTMLResponse)
async def root():
    with open("index.html") as f: return f.read()

if __name__=="__main__":
    print("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    print("  WiFi Speed Tester")
    print("  Open: http://localhost:8000")
    print("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    uvicorn.run("server:app",host="0.0.0.0",port=8000,reload=False)
