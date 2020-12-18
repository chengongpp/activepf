from requests import get
from requests.models import get_auth_from_url
from pprint import pprint
import json
import logging
def watch_qps() -> None:
    r = get("http://192.168.56.103:8161/api/jolokia/read/org.apache.activemq:\
type=Broker,brokerName=localhost,destinationType=Queue,destinationName=QUEUE",
    headers={'Origin': '192.168.56.1'}, auth=('admin', 'admin'))
    info = json.loads(r.text)
    logging.info(info['value'])

if __name__ == '__main__':
    logging.basicConfig(format='%(asctime)s|%(message)s', datefmt="%H:%M:%S", level=logging.INFO)
    watch_qps()