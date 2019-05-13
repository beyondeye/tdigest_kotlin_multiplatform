import tdigest_kt = require("tdigest-kt");
import * as ByteBuffer from "bytebuffer";
import {com} from "tdigest-kt";
import toBinaryOutput = com.basicio.toBinaryOutput;
import toBinaryInput = com.basicio.toBinaryInput;
import * as Long from "long";

var a:number =3;
var b:number=2;

const t=tdigest_kt.com.tdunning.math.stats;
const centroid = new t.Centroid(false);
var digest = t.TDigest.Companion.createAvlTreeDigest(200);
digest.addSample(10);
digest.addSample(20);
digest.addSample(30);
digest.addSample(40);
digest.addSample(50);
digest.addSample(60);
digest.addSample(70);
digest.addSample(80);
digest.addSample(90);
digest.addSample(100);
var perc40=digest.quantile(0.5);

var count = digest.centroidCount();
var digest_size = digest.size();
var low_ = digest_size.low_;
var high_ = digest_size.high_;

var digest_size_l = Long.fromBits(low_,high_);
var digest_size_num = digest_size_l.toNumber();

//now test serialization and deserialization
const size=digest.byteSize();
const buf = new ByteBuffer(size);
const output=toBinaryOutput(buf);
digest.asBytes(output);
const serializedString=output.toB64();
//now try to deserialize
//const input=buildBinaryInputFromB64(serializedString);
const input=toBinaryInput(ByteBuffer.fromBase64(serializedString));

const digest_deserialized=t.AVLTreeDigest.Companion.fromBytes(input);

var perc40_2=digest_deserialized.quantile(0.5);


var c:number =3;
var d:number=2;
//console.info(perc40)

