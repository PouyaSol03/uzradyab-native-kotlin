import urllib.request
import json

url = "https://api.neshan.org/v1/map-matching?path=35.766326,51.350320|35.765691,51.350610|35.763442,51.351050|35.760195,51.351673|35.759245,51.351833"
req = urllib.request.Request(url, headers={'Api-Key': 'mobile.1c8fc17211564f62991c301a270f2d31'})
try:
    with urllib.request.urlopen(req) as response:
        print(json.dumps(json.loads(response.read().decode()), indent=2))
except Exception as e:
    print("Error:", e)
    if hasattr(e, 'read'):
        print(e.read().decode())
