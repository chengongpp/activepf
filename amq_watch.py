from requests import get
from requests.models import get_auth_from_url
import json
import logging
import time

def watch_qps() -> None:
    INTERVAL = 1
    stats = None
    time_prev = time.time()
    while True:
        r = get("http://192.168.56.103:8161/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=QUEUE",
        headers={'Origin': '192.168.56.1'}, auth=('admin', 'admin'))
        inter = time.time() - time_prev
        time_prev = time.time()
        info = json.loads(r.text)
        if stats is None:
            stats = info['value']
        print("QPS=",(info['value']['EnqueueCount'] - stats['EnqueueCount']) / inter,
         'QP2=', (info['value']['DequeueCount'] - stats['DequeueCount']) / inter,
         'AEQT', info['value']['AverageEnqueueTime'])
        stats = info['value']
        # logging.info(info['value'])
        time.sleep(INTERVAL)

if __name__ == '__main__':
    logging.basicConfig(format='%(asctime)s|%(message)s', datefmt="%H:%M:%S", level=logging.INFO)
    watch_qps()