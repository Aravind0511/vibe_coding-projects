import socket
import subprocess
import ipaddress
import platform
import json
import re
import time
import os
import urllib.request
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
from concurrent.futures import ThreadPoolExecutor, as_completed

# ── MAC Vendor OUI Database ───────────────────────────────────
OUI_DB = {
    "00:50:56": "VMware", "00:0C:29": "VMware", "00:1A:11": "Google",
    "DC:A6:32": "Raspberry Pi", "B8:27:EB": "Raspberry Pi",
    "F4:5C:89": "Apple", "A4:CF:99": "Apple", "00:1B:63": "Apple",
    "3C:22:FB": "Apple", "F0:18:98": "Apple", "00:1E:C2": "Apple",
    "18:65:90": "Apple", "04:4B:ED": "Apple", "10:02:B5": "Apple",
    "BC:92:6B": "Apple", "00:1D:AA": "Dell", "F8:DB:88": "Dell",
    "18:03:73": "Dell", "18:66:DA": "Dell", "00:14:22": "Dell",
    "00:50:BA": "D-Link", "00:17:9A": "D-Link", "14:D6:4D": "D-Link",
    "28:10:7B": "D-Link", "CC:40:D0": "TP-Link", "50:C7:BF": "TP-Link",
    "E8:48:B8": "TP-Link", "A0:F3:C1": "TP-Link", "54:AF:97": "TP-Link",
    "00:23:69": "Cisco", "00:1C:57": "Cisco", "00:25:84": "Cisco",
    "F8:72:EA": "Cisco", "00:50:0F": "Cisco",
    "B0:FC:36": "Samsung", "00:07:AB": "Samsung", "A0:75:91": "Samsung",
    "9C:02:98": "Samsung", "00:1C:BF": "Huawei", "00:E0:FC": "Huawei",
    "70:72:CF": "Huawei", "88:E3:AB": "Huawei", "AC:84:C9": "Intel",
    "00:1F:3A": "Intel", "00:21:6A": "Intel", "AC:1F:6B": "Supermicro",
    "40:B0:34": "Xiaomi", "28:6C:07": "Xiaomi", "64:09:80": "Xiaomi",
    "F8:A4:5F": "Netgear", "00:14:6C": "Netgear", "1C:3B:6A": "Netgear",
    "00:26:F2": "Netgear", "C8:D3:FF": "OnePlus",
    "60:BE:B5": "Microsoft", "28:18:78": "Microsoft",
    "00:15:5D": "Microsoft (Hyper-V)",
}

_geo_cache = {}

def get_vendor(mac):
    if not mac or mac == "N/A":
        return "Unknown"
    return OUI_DB.get(mac.upper()[:8], "Unknown")

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        return "127.0.0.1"

def get_local_mac():
    try:
        import uuid
        mac = ':'.join(['{:02x}'.format((uuid.getnode() >> i) & 0xff)
                        for i in range(0, 48, 8)][::-1])
        return mac
    except:
        return "N/A"

def get_subnet(local_ip):
    parts = local_ip.split('.')
    return f"{parts[0]}.{parts[1]}.{parts[2]}.0/24"

def is_private_ip(ip):
    try:
        addr = ipaddress.IPv4Address(ip)
        return addr.is_private or addr.is_loopback or addr.is_link_local
    except:
        return True

# ── GEO LOOKUP via ipapi.co (free, no key needed) ────────────
def geo_lookup(ip=None):
    cache_key = ip or "self"
    if cache_key in _geo_cache:
        return _geo_cache[cache_key]
    if ip and is_private_ip(ip):
        return None
    try:
        url = f"https://ipapi.co/{ip}/json/" if ip else "https://ipapi.co/json/"
        req = urllib.request.Request(url, headers={"User-Agent": "NetScanner/2.0"})
        with urllib.request.urlopen(req, timeout=6) as resp:
            data = json.loads(resp.read().decode())
        if data.get("error"):
            return None
        result = {
            "lat": data.get("latitude"),
            "lon": data.get("longitude"),
            "city": data.get("city", "Unknown"),
            "region": data.get("region", ""),
            "country": data.get("country_name", "Unknown"),
            "country_code": data.get("country_code", ""),
            "isp": data.get("org", "Unknown"),
            "timezone": data.get("timezone", ""),
            "public_ip": data.get("ip", ip or ""),
            "asn": data.get("asn", ""),
            "postal": data.get("postal", ""),
        }
        _geo_cache[cache_key] = result
        return result
    except Exception as e:
        print(f"[GEO] Failed for {ip or 'self'}: {e}")
        return None

def get_gateway():
    try:
        if platform.system() == "Windows":
            result = subprocess.run(["ipconfig"], capture_output=True, text=True)
            for line in result.stdout.split('\n'):
                if 'Default Gateway' in line:
                    gw = line.split(':')[-1].strip()
                    if gw:
                        return gw
        else:
            result = subprocess.run(["ip", "route"], capture_output=True, text=True)
            for line in result.stdout.split('\n'):
                if line.startswith('default'):
                    parts = line.split()
                    if 'via' in parts:
                        return parts[parts.index('via') + 1]
    except:
        pass
    return "N/A"

def get_dns():
    dns = []
    try:
        if platform.system() == "Windows":
            result = subprocess.run(["ipconfig", "/all"], capture_output=True, text=True)
            for line in result.stdout.split('\n'):
                if 'DNS Servers' in line:
                    d = line.split(':')[-1].strip()
                    if d:
                        dns.append(d)
        else:
            with open('/etc/resolv.conf') as f:
                for line in f:
                    if line.startswith('nameserver'):
                        dns.append(line.split()[1])
    except:
        pass
    return dns[:3] or ["N/A"]

def get_network_info():
    local_ip = get_local_ip()
    geo = geo_lookup()
    return {
        "local_ip": local_ip,
        "local_mac": get_local_mac(),
        "hostname": socket.gethostname(),
        "subnet": get_subnet(local_ip),
        "gateway": get_gateway(),
        "dns": get_dns(),
        "os": platform.system(),
        "os_version": platform.version()[:60],
        "vendor": get_vendor(get_local_mac()),
        "geo": geo,
    }

# ── NETWORK SCAN ─────────────────────────────────────────────
def ping(ip, timeout=1):
    if platform.system() == "Windows":
        cmd = ["ping", "-n", "1", "-w", str(timeout * 1000), ip]
    else:
        cmd = ["ping", "-c", "1", "-W", str(timeout), ip]
    try:
        result = subprocess.run(cmd, capture_output=True, timeout=timeout + 1)
        return result.returncode == 0
    except:
        return False

def get_mac_from_arp(ip):
    try:
        if platform.system() == "Windows":
            result = subprocess.run(["arp", "-a", ip], capture_output=True, text=True)
            for line in result.stdout.split('\n'):
                if ip in line:
                    for part in line.split():
                        if re.match(r'([0-9a-f]{2}[:-]){5}[0-9a-f]{2}', part.lower()):
                            return part.replace('-', ':').upper()
        else:
            for cmd in [["ip", "neigh", "show", ip], ["arp", "-n", ip]]:
                result = subprocess.run(cmd, capture_output=True, text=True)
                match = re.search(r'([0-9a-f]{2}:){5}[0-9a-f]{2}', result.stdout.lower())
                if match:
                    return match.group(0).upper()
    except:
        pass
    return "N/A"

def resolve_hostname(ip):
    try:
        return socket.gethostbyaddr(ip)[0]
    except:
        return ip

def scan_ip(ip, local_ip, gateway, own_geo):
    if not ping(ip):
        return None
    mac = get_mac_from_arp(ip)
    hostname = resolve_hostname(ip)
    vendor = get_vendor(mac)

    if ip == local_ip:
        device_type = "This Device"
    elif ip == gateway:
        device_type = "Gateway / Router"
    elif vendor == "Apple":
        device_type = "Apple Device"
    elif vendor in ["Cisco", "D-Link", "TP-Link", "Netgear"]:
        device_type = "Network Device"
    elif vendor == "Raspberry Pi":
        device_type = "Raspberry Pi"
    elif vendor in ["VMware", "Microsoft (Hyper-V)"]:
        device_type = "Virtual Machine"
    elif vendor != "Unknown":
        device_type = f"{vendor} Device"
    else:
        device_type = "Host"

    return {
        "ip": ip,
        "mac": mac if mac != "N/A" else "Unavailable",
        "hostname": hostname if hostname != ip else "Unknown",
        "vendor": vendor,
        "device_type": device_type,
        "is_self": ip == local_ip,
        "is_gateway": ip == gateway,
        "timestamp": time.time(),
        # All LAN devices share the same public geo location (they're behind the same router)
        "geo": own_geo,
    }

def scan_network(subnet, local_ip, gateway, own_geo, progress_cb=None):
    network = ipaddress.IPv4Network(subnet, strict=False)
    hosts = [str(h) for h in network.hosts()]
    priority = [ip for ip in hosts if ip in [local_ip, gateway]]
    rest = [ip for ip in hosts if ip not in [local_ip, gateway]]
    all_ips = priority + rest
    results = []

    with ThreadPoolExecutor(max_workers=50) as executor:
        futures = {executor.submit(scan_ip, ip, local_ip, gateway, own_geo): ip for ip in all_ips}
        done = 0
        for future in as_completed(futures):
            done += 1
            r = future.result()
            if r:
                results.append(r)
            if progress_cb:
                progress_cb(done, len(all_ips), results)

    results.sort(key=lambda x: (not x['is_self'], not x['is_gateway'],
                                 [int(p) for p in x['ip'].split('.')]))
    return results

# ── HTTP SERVER ───────────────────────────────────────────────
class Handler(BaseHTTPRequestHandler):
    def log_message(self, format, *args): pass

    def send_json(self, data, status=200):
        body = json.dumps(data).encode()
        self.send_response(status)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', len(body))
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path
        params = parse_qs(parsed.query)

        if path == '/':
            self.serve_file('index.html', 'text/html')

        elif path == '/api/info':
            self.send_json(get_network_info())

        elif path == '/api/geo':
            ip = params.get('ip', [None])[0]
            result = geo_lookup(ip)
            self.send_json(result or {"error": "No geo data"})

        elif path == '/api/scan':
            info = get_network_info()
            own_geo = info.get('geo')
            progress = {}

            def on_progress(done, total, devices):
                progress.update({"done": done, "total": total})

            devices = scan_network(
                info['subnet'], info['local_ip'], info['gateway'],
                own_geo, on_progress
            )
            self.send_json({
                "success": True,
                "devices": devices,
                "total_scanned": progress.get('total', 0),
                "network_info": info,
            })

        else:
            self.send_response(404)
            self.end_headers()

    def serve_file(self, filename, content_type):
        try:
            filepath = os.path.join(os.path.dirname(__file__), filename)
            with open(filepath, 'rb') as f:
                data = f.read()
            self.send_response(200)
            self.send_header('Content-Type', content_type)
            self.send_header('Content-Length', len(data))
            self.end_headers()
            self.wfile.write(data)
        except FileNotFoundError:
            self.send_response(404)
            self.end_headers()

if __name__ == '__main__':
    PORT = 5000
    print(f"\n{'='*55}")
    print(f"  🛰  NetScanner v2 — with Geolocation")
    print(f"  🌐  Open: http://localhost:{PORT}")
    print(f"  📍  Geo lookup: ipapi.co (free, no key needed)")
    print(f"  Press Ctrl+C to stop")
    print(f"{'='*55}\n")
    server = HTTPServer(('0.0.0.0', PORT), Handler)
    server.serve_forever()
