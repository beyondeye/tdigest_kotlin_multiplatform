# TDigest for kotlin multiplatform

This is a full port of the latest version(24/4/2021) of reference implementation of [TDigest algorithm](https://github.com/tdunning/t-digest) to kotlin multiplatform 
that outputs javascript code, with typescript type definitions
You can install this library  with
```commandline
npm i tdigest-kt
```
You also need to have installed the kotlin runtime peer dependency:
```commandline
npm i kotlin@1.3.60
```


TDigest binary compressed serialization is supported, with the same format used by the reference Java implementation
 
See the [README](https://github.com/beyondeye/tdigest_kotlin_multiplatform/blob/master/README.md) for more details.
