import tdigest_kt = require("tdigest-kt");

var t=tdigest_kt.com.tdunning.math.stats.TDigest;
var digest = t.Companion.createAvlTreeDigest(200);
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
//console.info(perc40)
var a:number =3;
var b:number=2;
