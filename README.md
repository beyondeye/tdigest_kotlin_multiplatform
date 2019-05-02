# TDigest for kotlin multiplatform

This is a port Ted Dunning  [TDigest algorithm](https://github.com/tdunning/t-digest) to kotlin multiplatform. 

The API (the whole code actually) is identical to the original Java implementation (retrieved 5/2019), with one notable exception:

_Java Serialization for TDigest objects is not currently supported._

### Supported platforms
Supported platforms are JVS and JS (including Node).

Kotlin Native could be also be supported with a small effort (need to implement something equivalent to Java ByteBuffer, or 
use a library like [kotlinx.io](https://github.com/Kotlin/kotlinx-io))