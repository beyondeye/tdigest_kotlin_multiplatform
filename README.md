# TDigest for kotlin multiplatform

This is a full port to Kotlin multiplatform of Ted Dunning reference implementation of the [TDigest algorithm](https://github.com/tdunning/t-digest). 
The API (the whole code actually) is identical to the original Java implementation (retrieved 5/2019), with one notable exception:

_Java Serialization for TDigest objects is not currently supported._

but serialization and deserialization using methods ```asBytes``` and ```fromBytes``` **is** supported.
Also the full original suite of Unit tests is included. (Technically the tests are still in Java, but they run on
the library code converted to kotlin) 

Another difference from the original code is that `AVLTreeDigest` class has experimental support for _updating_ already added samples with method 
```updateSample```. Support for updating samples in ```MergeDigest``` is not yet implemented.
### Supported platforms
Supported platforms are JVM and JS (including Node).

Kotlin Native could be also supported with a small effort (need to implement something equivalent to Java ByteBuffer, or 
use a library like [kotlinx.io](https://github.com/Kotlin/kotlinx-io))

### Using the library with Typescript
Type definitions for the main TDigest classes are included, see [here](npmtemplate/tdigest-kt.d.ts)
There is also some basic typescript test code you can look at [here](typescript_tests/tdigest-kt-tests.ts).
before running this test you need to run ```npm install``` in the [typescript_tests](typescript_tests) directory
and run the tests with node.js
### Using the library in Node.js environment
In theory, all you need to do is to install the library with 

```npm i tdigest-kt```

and also its peer dependency from kotlin runtime

```npm i kotlin@1.3.30```

Because of issues with releases of the ```kotlinx-atomicfu``` dependency, it is recommended to install ```kotlinx-atomicfu```
as a local npm package, like it is done in ```typescript tests```. In other words, copy the content of 
[this directory](typescript_tests/libs/kotlinx-atomicfu) to your ```libs``` directory and define the dependency from
it in your `package.json` as 

```JSON
{
  "dependencies": {
    "tdigest-kt": "file:./libs/tdigest-kt"
  }
}
```

(see the example [here](typescript_tests/package.json))


### License
   Copyright 2019 by Dario Elyasy

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License [here](http://www.apache.org/licenses/LICENSE-2.0)

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.