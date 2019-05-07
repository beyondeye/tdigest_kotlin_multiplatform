import tdigest_kt = require("tdigest-kt");

var a:number =3;
var b:number=2;

const centroid = new tdigest_kt.com.tdunning.math.stats.Centroid(false)
const t=tdigest_kt.com.tdunning.math.stats;
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

var c:number =3;
var d:number=2;
//console.info(perc40)

