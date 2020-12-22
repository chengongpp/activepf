# Simple ActiveMQ tuning demo

参考《ActiveMQ in Action》一书第十三章对ActiveMQ进行调优的各种设置，写出来的用于评估各设置对ActiveMQ性能影响的小测试demo

使用amq_watch.py连接jolokia以观察其性能变化

## TODO

- [x] 细粒度定制发送消息的数量和体积
- [ ] 定制Producer和Consumer的线程数
- [x] 完成amq_watch.py