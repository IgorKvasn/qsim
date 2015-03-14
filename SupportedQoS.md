## Packet scheduling ##

Algorithms to determine the order in which packets are to be sent.

  * FIFO
  * Priority Queing
  * (Fair queueing) Round robin
  * Weighted round robin (WRR)
  * Weighted Fair Queing (WFQ)


## Packet classification & marking ##

Algoritms to determine packet priority (and thus its output queue).

  * Best efford (no QoS - all packets has the same marking)
  * IP precedence
  * classification according to Differentiated Services Code Point (DSCP)
  * flow based classification
  * no marking (e.g. when marking should be applied only on edge routers, but not on core routers)


## Active queue management ##

  * Random early detection (RED)
  * Weighted random early detection (WRED)
  * no active queue management


## Other QoS related features ##

  * simplified TCP acknowledgement model
  * TCP congestion avoidance