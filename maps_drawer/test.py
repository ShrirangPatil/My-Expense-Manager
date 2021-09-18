import requests
import shutil
import os
import random
x = [random.randint(1, 40000) for i in range(1, 40)]
y = ["1/1/2021" for i in range(1, 40)]
r = requests.get('http://192.168.43.199:8000/', json={"val_daily_expen": x, "key_daily_expen": y}, stream=True)
if r.status_code == 200:
    with open("image.png", 'wb') as f:
        r.raw.decode_content = True
        shutil.copyfileobj(r.raw, f)