# Javascan

Javascan is a java implementation of a network port scanner with the following features

- Built using Java 8 core and Apache commons libraries
- IPv4, IPv6, and hostname based network scanning
- Scans are concurrent and dynamically throttled
- Scans individual IP addresses, CIDR blocks, or a combination, with distinct port ranges optional for each target or network.

### Installation
If Java 8 and Maven are already installed, use 'git clone' to clone from Github, and Maven to build and install
```
$ git clone https://github.com/btsomogyi/javascan.git
$ cd javascan
$ mvn package
$ java -cp target/javascan-<ver>-aio.jar javascan.Javascan
```

### Usage
```
usage: Javascan <<host | ip | cidr>[@port[-port]]>...

    Javascan hostname
    Javascan a.b.c.d@10
    Javascan a.b.c.d/x@10-100

```

- hostname is any DNS resolvable host record
- ip is any IP address (IPv4 | IPv6)
- cidr is standard subnet in dot-slash format

One or more complete target specifiers (target@port-port) may be provided

### Design

Javascan uses a straightforward design to express this basic scanner functionality.  Design goals were:
- Implement concurrency with thread reuse
- Minimize object memory consumption in-flight (release objects as soon as payload is output)
- Capture output order using Future<> objects so output can proceed in parallel to scanning process

Without reasonable raw socket access, Java is not the ideal language to implement port scanning utility.

The Javascan application utilizes a dynamic thread pool to enable concurrency for the individual port probes, and will spawn threads as needed to scan as concurrently as possible.  The thread pool reuses threads, but in cases where there are a large number of timing out connections, the thread pool can grow large and exhaust the allowed number of threads per process.  In this case, the thread pool automatically limits itself to the maximum allowed threads and continues processing with this degree of concurrency.

### License
Apache License, Version 2.0

### TODO
- Convert to Quasar lightweight fibers (http://docs.paralleluniverse.co/quasar/)
- Implement command line flags for throttle, include subnet ends (network/broadcast), multi-address lookup, and output filtering
- Implement check for IPv4 & IPv6 interface address check on local system
- Implement source address and port parameters