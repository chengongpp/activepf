from http.client import HTTPConnection
# from httplib import HTTPConnection
from base64 import b64encode
import json
import logging
import time

def watch_qps() -> None:
    INTERVAL = 1
    stats = None
    time_prev = time.time()
    while True:
        host = "192.168.56.103:8161"
        url = "/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=QUEUE"
        headers = {'Origin': '192.168.56.1', 'Authorization': 'Basic {}'.format(b64encode(b'admin:admin').decode('ascii'))}
        conn = HTTPConnection(host)
        conn.request('GET', url=url, headers=headers)
        res = conn.getresponse()
        inter = time.time() - time_prev
        time_prev = time.time()
        info = json.loads(res.read().decode('ascii'))
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