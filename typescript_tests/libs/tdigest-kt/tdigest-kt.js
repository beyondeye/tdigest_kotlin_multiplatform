(function (_, Kotlin, $module$kotlinx_atomicfu, $module$bytebuffer, $module$long) {
  'use strict';
  var $$importsForInline$$ = _.$$importsForInline$$ || (_.$$importsForInline$$ = {});
  var toByte = Kotlin.toByte;
  var Kind_INTERFACE = Kotlin.Kind.INTERFACE;
  var shuffle = Kotlin.kotlin.collections.shuffle_9jeydg$;
  var IllegalStateException_init = Kotlin.kotlin.IllegalStateException_init_pdl1vj$;
  var Kind_OBJECT = Kotlin.Kind.OBJECT;
  var Random = Kotlin.kotlin.random.Random_za3lpa$;
  var Kind_CLASS = Kotlin.Kind.CLASS;
  var ArrayList_init = Kotlin.kotlin.collections.ArrayList_init_287e2$;
  var Math_0 = Math;
  var StringBuilder_init = Kotlin.kotlin.text.StringBuilder_init;
  var joinToString = Kotlin.kotlin.collections.joinToString_raq4np$;
  var toString = Kotlin.toString;
  var joinToString_0 = Kotlin.kotlin.collections.joinToString_vk9fgb$;
  var ensureNotNull = Kotlin.ensureNotNull;
  var UnsupportedOperationException_init = Kotlin.kotlin.UnsupportedOperationException_init_pdl1vj$;
  var MutableIterator = Kotlin.kotlin.collections.MutableIterator;
  var AbstractCollection = Kotlin.kotlin.collections.AbstractCollection;
  var copyOf = Kotlin.kotlin.collections.copyOf_xgrzbe$;
  var copyOf_0 = Kotlin.kotlin.collections.copyOf_c03ot6$;
  var copyOf_1 = Kotlin.kotlin.collections.copyOf_8ujjk8$;
  var UnsupportedOperationException_init_0 = Kotlin.kotlin.UnsupportedOperationException_init;
  var IllegalStateException_init_0 = Kotlin.kotlin.IllegalStateException_init;
  var IllegalArgumentException_init = Kotlin.kotlin.IllegalArgumentException_init;
  var MutableList = Kotlin.kotlin.collections.MutableList;
  var throwCCE = Kotlin.throwCCE;
  var kotlin_js_internal_DoubleCompanionObject = Kotlin.kotlin.js.internal.DoubleCompanionObject;
  var Unit = Kotlin.kotlin.Unit;
  var IllegalArgumentException_init_0 = Kotlin.kotlin.IllegalArgumentException_init_pdl1vj$;
  var L0 = Kotlin.Long.ZERO;
  var atomic = $module$kotlinx_atomicfu.kotlinx.atomicfu.atomic$int$;
  var Comparable = Kotlin.kotlin.Comparable;
  var numberToInt = Kotlin.numberToInt;
  var copyOf_2 = Kotlin.kotlin.collections.copyOf_mrm5p$;
  var AssertionError_init = Kotlin.kotlin.AssertionError_init;
  var kotlin_js_internal_ByteCompanionObject = Kotlin.kotlin.js.internal.ByteCompanionObject;
  var abs = Kotlin.kotlin.math.abs_za3lpa$;
  var AssertionError_init_0 = Kotlin.kotlin.AssertionError_init_s8jyv4$;
  var List = Kotlin.kotlin.collections.List;
  var isNaN_0 = Kotlin.kotlin.isNaN_yrwdxr$;
  var listOf = Kotlin.kotlin.collections.listOf_mh5how$;
  var NotImplementedError = Kotlin.kotlin.NotImplementedError;
  var reverse = Kotlin.kotlin.collections.reverse_vvxzk3$;
  var println = Kotlin.kotlin.io.println_s8jyv4$;
  var Enum = Kotlin.kotlin.Enum;
  var throwISE = Kotlin.throwISE;
  var toShort = Kotlin.toShort;
  var math = Kotlin.kotlin.math;
  var endsWith = Kotlin.kotlin.text.endsWith_7epoxm$;
  var arrayCopy = Kotlin.kotlin.collections.arrayCopy;
  var defineInlineFunction = Kotlin.defineInlineFunction;
  var wrapFunction = Kotlin.wrapFunction;
  AbstractTDigest.prototype = Object.create(TDigest.prototype);
  AbstractTDigest.prototype.constructor = AbstractTDigest;
  AVLGroupTree_init$ObjectLiteral.prototype = Object.create(IntAVLTree.prototype);
  AVLGroupTree_init$ObjectLiteral.prototype.constructor = AVLGroupTree_init$ObjectLiteral;
  AVLGroupTree.prototype = Object.create(AbstractCollection.prototype);
  AVLGroupTree.prototype.constructor = AVLGroupTree;
  AVLTreeDigest.prototype = Object.create(AbstractTDigest.prototype);
  AVLTreeDigest.prototype.constructor = AVLTreeDigest;
  MergingDigest$centroids$ObjectLiteral.prototype = Object.create(AbstractCollection.prototype);
  MergingDigest$centroids$ObjectLiteral.prototype.constructor = MergingDigest$centroids$ObjectLiteral;
  MergingDigest$Encoding.prototype = Object.create(Enum.prototype);
  MergingDigest$Encoding.prototype.constructor = MergingDigest$Encoding;
  MergingDigest.prototype = Object.create(AbstractTDigest.prototype);
  MergingDigest.prototype.constructor = MergingDigest;
  ScaleFunction.prototype = Object.create(Enum.prototype);
  ScaleFunction.prototype.constructor = ScaleFunction;
  ScaleFunction$K_0.prototype = Object.create(ScaleFunction.prototype);
  ScaleFunction$K_0.prototype.constructor = ScaleFunction$K_0;
  ScaleFunction$K_1.prototype = Object.create(ScaleFunction.prototype);
  ScaleFunction$K_1.prototype.constructor = ScaleFunction$K_1;
  ScaleFunction$K_1_FAST.prototype = Object.create(ScaleFunction.prototype);
  ScaleFunction$K_1_FAST.prototype.constructor = ScaleFunction$K_1_FAST;
  ScaleFunction$K_2.prototype = Object.create(ScaleFunction.prototype);
  ScaleFunction$K_2.prototype.constructor = ScaleFunction$K_2;
  ScaleFunction$K_3.prototype = Object.create(ScaleFunction.prototype);
  ScaleFunction$K_3.prototype.constructor = ScaleFunction$K_3;
  ScaleFunction$K_2_NO_NORM.prototype = Object.create(ScaleFunction.prototype);
  ScaleFunction$K_2_NO_NORM.prototype.constructor = ScaleFunction$K_2_NO_NORM;
  ScaleFunction$K_3_NO_NORM.prototype = Object.create(ScaleFunction.prototype);
  ScaleFunction$K_3_NO_NORM.prototype.constructor = ScaleFunction$K_3_NO_NORM;
  function BinaryInput() {
  }
  BinaryInput.prototype.readBoolean = function () {
    return this.readByte() === toByte(0) ? false : true;
  };
  BinaryInput.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'BinaryInput',
    interfaces: []
  };
  function BinaryOutput() {
  }
  BinaryOutput.prototype.writeBoolean_6taknv$ = function (v) {
    this.writeByte_s8j3t7$(v ? 1 : 0);
  };
  BinaryOutput.$metadata$ = {
    kind: Kind_INTERFACE,
    simpleName: 'BinaryOutput',
    interfaces: []
  };
  function AbstractTDigest() {
    AbstractTDigest$Companion_getInstance();
    TDigest.call(this);
    this.gen_8be2vx$ = Random(0);
    this.isRecording_zamskc$_0 = false;
  }
  Object.defineProperty(AbstractTDigest.prototype, 'isRecording', {
    get: function () {
      return this.isRecording_zamskc$_0;
    },
    set: function (isRecording) {
      this.isRecording_zamskc$_0 = isRecording;
    }
  });
  AbstractTDigest.prototype.recordAllData = function () {
    this.isRecording = true;
    return this;
  };
  AbstractTDigest.prototype.addSample = function (x) {
    this.addWeightedSample(x, 1);
  };
  AbstractTDigest.prototype.addOtherDigest = function (other) {
    var tmp$, tmp$_0;
    var tmp = ArrayList_init();
    tmp$ = other.centroids().iterator();
    while (tmp$.hasNext()) {
      var centroid = tmp$.next();
      tmp.add_11rb$(centroid);
    }
    shuffle(tmp, this.gen_8be2vx$);
    tmp$_0 = tmp.iterator();
    while (tmp$_0.hasNext()) {
      var centroid_0 = tmp$_0.next();
      this.add_9eljot$(centroid_0.mean(), centroid_0.count(), centroid_0);
    }
  };
  AbstractTDigest.prototype.createCentroid_12fank$ = function (mean, id) {
    return Centroid_init_2(mean, id, this.isRecording);
  };
  function AbstractTDigest$Companion() {
    AbstractTDigest$Companion_instance = this;
  }
  AbstractTDigest$Companion.prototype.weightedAverage_kn9dxl$ = function (x1, w1, x2, w2) {
    var tmp$;
    if (x1 <= x2) {
      tmp$ = this.weightedAverageSorted_0(x1, w1, x2, w2);
    }
     else {
      tmp$ = this.weightedAverageSorted_0(x2, w2, x1, w1);
    }
    return tmp$;
  };
  AbstractTDigest$Companion.prototype.weightedAverageSorted_0 = function (x1, w1, x2, w2) {
    mpassert(x1 <= x2);
    var x = (x1 * w1 + x2 * w2) / (w1 + w2);
    var b = Math_0.min(x, x2);
    return Math_0.max(x1, b);
  };
  AbstractTDigest$Companion.prototype.interpolate_hln2n9$ = function (x, x0, x1) {
    return (x - x0) / (x1 - x0);
  };
  AbstractTDigest$Companion.prototype.encode_ydee4o$ = function (buf, n) {
    var n_0 = n;
    var k = 0;
    while (n_0 < 0 || n_0 > 127) {
      var b = toByte(128 | 127 & n_0);
      buf.writeByte_s8j3t7$(b);
      n_0 = n_0 >>> 7;
      k = k + 1 | 0;
      if (k >= 6) {
        throw IllegalStateException_init('Size is implausibly large');
      }
    }
    buf.writeByte_s8j3t7$(toByte(n_0));
  };
  AbstractTDigest$Companion.prototype.decode_51dx0l$ = function (buf) {
    var v = buf.readByte();
    var z = 127 & v;
    var shift = 7;
    while ((v & 128) !== 0) {
      if (shift > 28) {
        throw IllegalStateException_init('Shift too large in decode');
      }
      v = buf.readByte();
      z = z + ((v & 127) << shift) | 0;
      shift = shift + 7 | 0;
    }
    return z;
  };
  AbstractTDigest$Companion.prototype.quantile_m467d5$ = function (index, previousIndex, nextIndex, previousMean, nextMean) {
    var delta = nextIndex - previousIndex;
    var previousWeight = (nextIndex - index) / delta;
    var nextWeight = (index - previousIndex) / delta;
    return previousMean * previousWeight + nextMean * nextWeight;
  };
  AbstractTDigest$Companion.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Companion',
    interfaces: []
  };
  var AbstractTDigest$Companion_instance = null;
  function AbstractTDigest$Companion_getInstance() {
    if (AbstractTDigest$Companion_instance === null) {
      new AbstractTDigest$Companion();
    }
    return AbstractTDigest$Companion_instance;
  }
  AbstractTDigest.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'AbstractTDigest',
    interfaces: [TDigest]
  };
  function AVLGroupTree(record) {
    if (record === void 0)
      record = false;
    AbstractCollection.call(this);
    this.centroid_0 = 0;
    this.count_0 = 0;
    this.data_0 = null;
    this.centroids_0 = null;
    this.counts_0 = null;
    this.datas_0 = null;
    this.aggregatedCounts_0 = null;
    this.tree_0 = null;
    this.tree_0 = new AVLGroupTree_init$ObjectLiteral(this);
    this.centroids_0 = new Float64Array(this.tree_0.capacity());
    this.counts_0 = new Int32Array(this.tree_0.capacity());
    this.aggregatedCounts_0 = new Int32Array(this.tree_0.capacity());
    if (record) {
      var datas = Kotlin.newArray(this.tree_0.capacity(), null);
      this.datas_0 = datas;
    }
  }
  AVLGroupTree.prototype.toString = function () {
    var tmp$, tmp$_0;
    var sb = StringBuilder_init();
    sb.append_gw00v9$('centroids=' + toString((tmp$ = this.centroids_0) != null ? joinToString(tmp$) : null) + ';');
    sb.append_gw00v9$('counts=' + toString((tmp$_0 = this.counts_0) != null ? joinToString_0(tmp$_0) : null) + ';');
    return sb.toString();
  };
  Object.defineProperty(AVLGroupTree.prototype, 'size', {
    get: function () {
      return this.tree_0.size();
    }
  });
  AVLGroupTree.prototype.prev_za3lpa$ = function (node) {
    return this.tree_0.prev_za3lpa$(node);
  };
  AVLGroupTree.prototype.next_za3lpa$ = function (node) {
    return this.tree_0.next_za3lpa$(node);
  };
  AVLGroupTree.prototype.mean_za3lpa$ = function (node) {
    return ensureNotNull(this.centroids_0)[node];
  };
  AVLGroupTree.prototype.count_za3lpa$ = function (node) {
    return ensureNotNull(this.counts_0)[node];
  };
  AVLGroupTree.prototype.data_za3lpa$ = function (node) {
    return this.datas_0 == null ? null : ensureNotNull(this.datas_0)[node];
  };
  AVLGroupTree.prototype.add_lswtur$ = function (centroid, count, data) {
    this.centroid_0 = centroid;
    this.count_0 = count;
    this.data_0 = data;
    this.tree_0.add();
  };
  AVLGroupTree.prototype.add_lv3ymp$ = function (centroid) {
    this.add_lswtur$(centroid.mean(), centroid.count(), centroid.data());
    return true;
  };
  AVLGroupTree.prototype.update_yovgz0$ = function (node, centroid, count, data, forceInPlace) {
    if (centroid === ensureNotNull(this.centroids_0)[node] || forceInPlace) {
      ensureNotNull(this.centroids_0)[node] = centroid;
      ensureNotNull(this.counts_0)[node] = count;
      if (this.datas_0 != null) {
        ensureNotNull(this.datas_0)[node] = data;
      }
    }
     else {
      this.centroid_0 = centroid;
      this.count_0 = count;
      this.data_0 = data;
      this.tree_0.update_za3lpa$(node);
    }
  };
  AVLGroupTree.prototype.remove_za3lpa$ = function (node) {
    this.tree_0.remove_za3lpa$(node);
  };
  AVLGroupTree.prototype.floor_14dthe$ = function (centroid) {
    var floor = IntAVLTree$Companion_getInstance().NIL;
    var node = this.tree_0.root();
    while (node !== IntAVLTree$Companion_getInstance().NIL) {
      var cmp = Kotlin.compareTo(centroid, this.mean_za3lpa$(node));
      if (cmp <= 0) {
        node = this.tree_0.left_za3lpa$(node);
      }
       else {
        floor = node;
        node = this.tree_0.right_za3lpa$(node);
      }
    }
    return floor;
  };
  AVLGroupTree.prototype.floorSum_s8cxhz$ = function (sum) {
    var sum_0 = sum;
    var floor = IntAVLTree$Companion_getInstance().NIL;
    var node = this.tree_0.root();
    while (node !== IntAVLTree$Companion_getInstance().NIL) {
      var left = this.tree_0.left_za3lpa$(node);
      var leftCount = Kotlin.Long.fromInt(ensureNotNull(this.aggregatedCounts_0)[left]);
      if (leftCount.compareTo_11rb$(sum_0) <= 0) {
        floor = node;
        sum_0 = sum_0.subtract(leftCount.add(Kotlin.Long.fromInt(this.count_za3lpa$(node))));
        node = this.tree_0.right_za3lpa$(node);
      }
       else {
        node = this.tree_0.left_za3lpa$(node);
      }
    }
    return floor;
  };
  AVLGroupTree.prototype.first = function () {
    return this.tree_0.first_za3lpa$(this.tree_0.root());
  };
  AVLGroupTree.prototype.last = function () {
    return this.tree_0.last_za3lpa$(this.tree_0.root());
  };
  AVLGroupTree.prototype.headSum_za3lpa$ = function (node) {
    var left = this.tree_0.left_za3lpa$(node);
    var sum = Kotlin.Long.fromInt(ensureNotNull(this.aggregatedCounts_0)[left]);
    var n = node;
    var p = this.tree_0.parent_za3lpa$(node);
    while (p !== IntAVLTree$Companion_getInstance().NIL) {
      if (n === this.tree_0.right_za3lpa$(p)) {
        var leftP = this.tree_0.left_za3lpa$(p);
        sum = sum.add(Kotlin.Long.fromInt(ensureNotNull(this.counts_0)[p] + ensureNotNull(this.aggregatedCounts_0)[leftP] | 0));
      }
      n = p;
      p = this.tree_0.parent_za3lpa$(n);
    }
    return sum;
  };
  AVLGroupTree.prototype.iterator = function () {
    return this.iterator_0(this.first());
  };
  function AVLGroupTree$iterator$ObjectLiteral(this$AVLGroupTree, closure$startNode) {
    this.this$AVLGroupTree = this$AVLGroupTree;
    this.nextNode = closure$startNode;
  }
  AVLGroupTree$iterator$ObjectLiteral.prototype.hasNext = function () {
    return this.nextNode !== IntAVLTree$Companion_getInstance().NIL;
  };
  AVLGroupTree$iterator$ObjectLiteral.prototype.next = function () {
    var tmp$;
    var next = Centroid_init_0(this.this$AVLGroupTree.mean_za3lpa$(this.nextNode), this.this$AVLGroupTree.count_za3lpa$(this.nextNode));
    var data = this.this$AVLGroupTree.data_za3lpa$(this.nextNode);
    if (data != null) {
      tmp$ = data.iterator();
      while (tmp$.hasNext()) {
        var x = tmp$.next();
        next.insertData_14dthe$(x);
      }
    }
    this.nextNode = this.this$AVLGroupTree.tree_0.next_za3lpa$(this.nextNode);
    return next;
  };
  AVLGroupTree$iterator$ObjectLiteral.prototype.remove = function () {
    throw UnsupportedOperationException_init('Read-only iterator');
  };
  AVLGroupTree$iterator$ObjectLiteral.$metadata$ = {
    kind: Kind_CLASS,
    interfaces: [MutableIterator]
  };
  AVLGroupTree.prototype.iterator_0 = function (startNode) {
    return new AVLGroupTree$iterator$ObjectLiteral(this, startNode);
  };
  AVLGroupTree.prototype.sum = function () {
    return ensureNotNull(this.aggregatedCounts_0)[this.tree_0.root()];
  };
  AVLGroupTree.prototype.checkBalance = function () {
    this.tree_0.checkBalance_za3lpa$(this.tree_0.root());
  };
  AVLGroupTree.prototype.checkAggregates = function () {
    this.checkAggregates_0(this.tree_0.root());
  };
  AVLGroupTree.prototype.checkAggregates_0 = function (node) {
    mpassert(ensureNotNull(this.aggregatedCounts_0)[node] === (ensureNotNull(this.counts_0)[node] + ensureNotNull(this.aggregatedCounts_0)[this.tree_0.left_za3lpa$(node)] + ensureNotNull(this.aggregatedCounts_0)[this.tree_0.right_za3lpa$(node)] | 0));
    if (node !== IntAVLTree$Companion_getInstance().NIL) {
      this.checkAggregates_0(this.tree_0.left_za3lpa$(node));
      this.checkAggregates_0(this.tree_0.right_za3lpa$(node));
    }
  };
  function AVLGroupTree_init$ObjectLiteral(this$AVLGroupTree, initialCapacity) {
    this.this$AVLGroupTree = this$AVLGroupTree;
    IntAVLTree.call(this, initialCapacity);
  }
  AVLGroupTree_init$ObjectLiteral.prototype.resize_za3lpa$ = function (newCapacity) {
    IntAVLTree.prototype.resize_za3lpa$.call(this, newCapacity);
    this.this$AVLGroupTree.centroids_0 = copyOf(ensureNotNull(this.this$AVLGroupTree.centroids_0), newCapacity);
    this.this$AVLGroupTree.counts_0 = copyOf_0(ensureNotNull(this.this$AVLGroupTree.counts_0), newCapacity);
    this.this$AVLGroupTree.aggregatedCounts_0 = copyOf_0(ensureNotNull(this.this$AVLGroupTree.aggregatedCounts_0), newCapacity);
    if (this.this$AVLGroupTree.datas_0 != null) {
      this.this$AVLGroupTree.datas_0 = copyOf_1(ensureNotNull(this.this$AVLGroupTree.datas_0), newCapacity);
    }
  };
  AVLGroupTree_init$ObjectLiteral.prototype.merge_za3lpa$ = function (node) {
    throw UnsupportedOperationException_init_0();
  };
  AVLGroupTree_init$ObjectLiteral.prototype.copy_za3lpa$ = function (node) {
    ensureNotNull(this.this$AVLGroupTree.centroids_0)[node] = this.this$AVLGroupTree.centroid_0;
    ensureNotNull(this.this$AVLGroupTree.counts_0)[node] = this.this$AVLGroupTree.count_0;
    if (this.this$AVLGroupTree.datas_0 != null) {
      if (this.this$AVLGroupTree.data_0 == null) {
        if (this.this$AVLGroupTree.count_0 !== 1) {
          throw IllegalStateException_init_0();
        }
        this.this$AVLGroupTree.data_0 = ArrayList_init();
        ensureNotNull(this.this$AVLGroupTree.data_0).add_11rb$(this.this$AVLGroupTree.centroid_0);
      }
      ensureNotNull(this.this$AVLGroupTree.datas_0)[node] = ensureNotNull(this.this$AVLGroupTree.data_0);
    }
  };
  AVLGroupTree_init$ObjectLiteral.prototype.compare_za3lpa$ = function (node) {
    var tmp$;
    if (this.this$AVLGroupTree.centroid_0 < ensureNotNull(this.this$AVLGroupTree.centroids_0)[node]) {
      tmp$ = -1;
    }
     else {
      tmp$ = 1;
    }
    return tmp$;
  };
  AVLGroupTree_init$ObjectLiteral.prototype.fixAggregates_za3lpa$ = function (node) {
    IntAVLTree.prototype.fixAggregates_za3lpa$.call(this, node);
    ensureNotNull(this.this$AVLGroupTree.aggregatedCounts_0)[node] = ensureNotNull(this.this$AVLGroupTree.counts_0)[node] + ensureNotNull(this.this$AVLGroupTree.aggregatedCounts_0)[this.left_za3lpa$(node)] + ensureNotNull(this.this$AVLGroupTree.aggregatedCounts_0)[this.right_za3lpa$(node)] | 0;
  };
  AVLGroupTree_init$ObjectLiteral.$metadata$ = {
    kind: Kind_CLASS,
    interfaces: [IntAVLTree]
  };
  AVLGroupTree.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'AVLGroupTree',
    interfaces: [AbstractCollection]
  };
  function AVLTreeDigest() {
    AVLTreeDigest$Companion_getInstance();
    this.compression_0 = 0;
    this.summary_0 = null;
    this.count_0 = L0;
  }
  AVLTreeDigest.prototype.toString = function () {
    var tmp$, tmp$_0;
    return (tmp$_0 = (tmp$ = this.summary_0) != null ? tmp$.toString() : null) != null ? tmp$_0 : '';
  };
  AVLTreeDigest.prototype.recordAllData = function () {
    if (ensureNotNull(this.summary_0).size !== 0) {
      throw IllegalStateException_init('Can only ask to record added data on an empty summary');
    }
    this.summary_0 = new AVLGroupTree(true);
    return AbstractTDigest.prototype.recordAllData.call(this);
  };
  AVLTreeDigest.prototype.centroidCount = function () {
    return ensureNotNull(this.summary_0).size;
  };
  AVLTreeDigest.prototype.add_9eljot$ = function (x, w, base) {
    if (x !== base.mean() || w !== base.count()) {
      throw IllegalArgumentException_init();
    }
    this.add_lswtur$(x, w, base.data());
  };
  AVLTreeDigest.prototype.addWeightedSample = function (x, w) {
    var tmp$;
    this.add_lswtur$(x, w, (tmp$ = null) == null || Kotlin.isType(tmp$, MutableList) ? tmp$ : throwCCE());
  };
  AVLTreeDigest.prototype.add_ixnl3q$ = function (others) {
    var tmp$, tmp$_0;
    tmp$ = others.iterator();
    while (tmp$.hasNext()) {
      var other = tmp$.next();
      var a = this.min;
      var b = other.min;
      var tmp$_1 = Math_0.min(a, b);
      var a_0 = this.max;
      var b_0 = other.max;
      this.setMinMax_sdh6z7$(tmp$_1, Math_0.max(a_0, b_0));
      tmp$_0 = other.centroids().iterator();
      while (tmp$_0.hasNext()) {
        var centroid = tmp$_0.next();
        this.add_lswtur$(centroid.mean(), centroid.count(), this.isRecording ? centroid.data() : null);
      }
    }
  };
  AVLTreeDigest.prototype.add_lswtur$ = function (x, w, data) {
    this.checkValue_tq0o01$(x);
    if (x < this.min) {
      this.min = x;
    }
    if (x > this.max) {
      this.max = x;
    }
    var start = {v: ensureNotNull(this.summary_0).floor_14dthe$(x)};
    if (start.v === IntAVLTree$Companion_getInstance().NIL) {
      start.v = ensureNotNull(this.summary_0).first();
    }
    if (start.v === IntAVLTree$Companion_getInstance().NIL) {
      mpassert(ensureNotNull(this.summary_0).size === 0);
      ensureNotNull(this.summary_0).add_lswtur$(x, w, data);
      this.count_0 = Kotlin.Long.fromInt(w);
    }
     else {
      var minDistance = {v: kotlin_js_internal_DoubleCompanionObject.MAX_VALUE};
      var lastNeighbor = {v: IntAVLTree$Companion_getInstance().NIL};
      var neighbor = start.v;
      while (neighbor !== IntAVLTree$Companion_getInstance().NIL) {
        var x_0 = ensureNotNull(this.summary_0).mean_za3lpa$(neighbor) - x;
        var z = Math_0.abs(x_0);
        if (z < minDistance.v) {
          start.v = neighbor;
          minDistance.v = z;
        }
         else if (z > minDistance.v) {
          lastNeighbor.v = neighbor;
          break;
        }
        neighbor = ensureNotNull(this.summary_0).next_za3lpa$(neighbor);
      }
      var closest = IntAVLTree$Companion_getInstance().NIL;
      var n = 0.0;
      var neighbor_0 = start.v;
      while (neighbor_0 !== lastNeighbor.v) {
        var tmp$ = minDistance.v;
        var x_1 = ensureNotNull(this.summary_0).mean_za3lpa$(neighbor_0) - x;
        mpassert(tmp$ === Math_0.abs(x_1));
        var q0 = ensureNotNull(this.summary_0).headSum_za3lpa$(neighbor_0).toNumber() / this.count_0.toNumber();
        var q1 = q0 + ensureNotNull(this.summary_0).count_za3lpa$(neighbor_0) / this.count_0.toNumber();
        var tmp$_0 = this.count_0.toNumber();
        var a = this.scale.max_yvo9jy$(q0, this.compression_0, this.count_0.toNumber());
        var b = this.scale.max_yvo9jy$(q1, this.compression_0, this.count_0.toNumber());
        var k = tmp$_0 * Math_0.min(a, b);
        if ((ensureNotNull(this.summary_0).count_za3lpa$(neighbor_0) + w | 0) <= k) {
          n = n + 1;
          if (this.gen_8be2vx$.nextDouble() < 1 / n) {
            closest = neighbor_0;
          }
        }
        neighbor_0 = ensureNotNull(this.summary_0).next_za3lpa$(neighbor_0);
      }
      if (closest === IntAVLTree$Companion_getInstance().NIL) {
        ensureNotNull(this.summary_0).add_lswtur$(x, w, data);
      }
       else {
        var centroid = ensureNotNull(this.summary_0).mean_za3lpa$(closest);
        var count = ensureNotNull(this.summary_0).count_za3lpa$(closest);
        var d = ensureNotNull(this.summary_0).data_za3lpa$(closest);
        if (d != null) {
          if (w === 1) {
            d.add_11rb$(x);
          }
           else {
            d.addAll_brywnq$(ensureNotNull(data));
          }
        }
        centroid = AbstractTDigest$Companion_getInstance().weightedAverage_kn9dxl$(centroid, count, x, w);
        count = count + w | 0;
        ensureNotNull(this.summary_0).update_yovgz0$(closest, centroid, count, d, false);
      }
      this.count_0 = this.count_0.add(Kotlin.Long.fromInt(w));
      if (ensureNotNull(this.summary_0).size > 20 * this.compression_0) {
        this.compress();
      }
    }
  };
  AVLTreeDigest.prototype.updateSample_lu1900$ = function (oldValue, newValue) {
    this.checkValue_tq0o01$(oldValue);
    this.checkValue_tq0o01$(newValue);
    if (oldValue > this.max || oldValue < this.min) {
      throw IllegalArgumentException_init_0('oldValue not in range');
    }
    var x = oldValue;
    var start = {v: ensureNotNull(this.summary_0).floor_14dthe$(x)};
    if (start.v === IntAVLTree$Companion_getInstance().NIL) {
      start.v = ensureNotNull(this.summary_0).first();
    }
    var minDistance = {v: kotlin_js_internal_DoubleCompanionObject.MAX_VALUE};
    var lastNeighbor = {v: IntAVLTree$Companion_getInstance().NIL};
    var neighbor = start.v;
    while (neighbor !== IntAVLTree$Companion_getInstance().NIL) {
      var x_0 = ensureNotNull(this.summary_0).mean_za3lpa$(neighbor) - x;
      var z = Math_0.abs(x_0);
      if (z < minDistance.v) {
        start.v = neighbor;
        minDistance.v = z;
      }
       else if (z > minDistance.v) {
        lastNeighbor.v = neighbor;
        break;
      }
      neighbor = ensureNotNull(this.summary_0).next_za3lpa$(neighbor);
    }
    var closest = IntAVLTree$Companion_getInstance().NIL;
    var n = 0.0;
    var neighbor_0 = start.v;
    while (neighbor_0 !== lastNeighbor.v) {
      var tmp$ = minDistance.v;
      var x_1 = ensureNotNull(this.summary_0).mean_za3lpa$(neighbor_0) - x;
      mpassert(tmp$ === Math_0.abs(x_1));
      n = n + 1;
      if (this.gen_8be2vx$.nextDouble() < 1 / n) {
        closest = neighbor_0;
      }
      neighbor_0 = ensureNotNull(this.summary_0).next_za3lpa$(neighbor_0);
    }
    mpassert(closest !== IntAVLTree$Companion_getInstance().NIL);
    var centroid_closest = ensureNotNull(this.summary_0).mean_za3lpa$(closest);
    var count_closest = ensureNotNull(this.summary_0).count_za3lpa$(closest);
    var d_closest = ensureNotNull(this.summary_0).data_za3lpa$(closest);
    if (d_closest != null) {
      d_closest.remove_11rb$(x);
    }
    if (count_closest > 1) {
      centroid_closest = AbstractTDigest$Companion_getInstance().weightedAverage_kn9dxl$(centroid_closest, count_closest, x, -1.0);
      count_closest = count_closest + -1 | 0;
      ensureNotNull(this.summary_0).update_yovgz0$(closest, centroid_closest, count_closest, d_closest, false);
    }
     else {
      mpassert(minDistance.v === 0.0);
      ensureNotNull(this.summary_0).remove_za3lpa$(closest);
    }
    this.count_0 = this.count_0.add(Kotlin.Long.fromInt(-1));
    if (ensureNotNull(this.summary_0).size > 20 * this.compression_0) {
      this.compress();
    }
    this.add_lswtur$(newValue, 1, null);
  };
  AVLTreeDigest.prototype.compress = function () {
    if (ensureNotNull(this.summary_0).size <= 1) {
      return;
    }
    var n0 = 0.0;
    var k0 = this.count_0.toNumber() * this.scale.max_yvo9jy$(n0 / this.count_0.toNumber(), this.compression_0, this.count_0.toNumber());
    var node = ensureNotNull(this.summary_0).first();
    var w0 = ensureNotNull(this.summary_0).count_za3lpa$(node);
    var n1 = n0 + ensureNotNull(this.summary_0).count_za3lpa$(node);
    var w1 = 0;
    var k1;
    while (node !== IntAVLTree$Companion_getInstance().NIL) {
      var after = ensureNotNull(this.summary_0).next_za3lpa$(node);
      while (after !== IntAVLTree$Companion_getInstance().NIL) {
        w1 = ensureNotNull(this.summary_0).count_za3lpa$(after);
        k1 = this.count_0.toNumber() * this.scale.max_yvo9jy$((n1 + w1) / this.count_0.toNumber(), this.compression_0, this.count_0.toNumber());
        var tmp$ = w0 + w1 | 0;
        var a = k0;
        if (tmp$ > Math_0.min(a, k1)) {
          break;
        }
         else {
          var mean = AbstractTDigest$Companion_getInstance().weightedAverage_kn9dxl$(ensureNotNull(this.summary_0).mean_za3lpa$(node), w0, ensureNotNull(this.summary_0).mean_za3lpa$(after), w1);
          var d1 = ensureNotNull(this.summary_0).data_za3lpa$(node);
          var d2 = ensureNotNull(this.summary_0).data_za3lpa$(after);
          if (d1 != null && d2 != null) {
            d1.addAll_brywnq$(d2);
          }
          ensureNotNull(this.summary_0).update_yovgz0$(node, mean, w0 + w1 | 0, d1, true);
          var tmp = ensureNotNull(this.summary_0).next_za3lpa$(after);
          ensureNotNull(this.summary_0).remove_za3lpa$(after);
          after = tmp;
          n1 += w1;
          w0 = w0 + w1 | 0;
        }
      }
      node = after;
      if (node !== IntAVLTree$Companion_getInstance().NIL) {
        n0 = n1;
        k0 = this.count_0.toNumber() * this.scale.max_yvo9jy$(n0 / this.count_0.toNumber(), this.compression_0, this.count_0.toNumber());
        w0 = w1;
        n1 = n0 + w0;
      }
    }
  };
  AVLTreeDigest.prototype.size = function () {
    return this.count_0;
  };
  AVLTreeDigest.prototype.cdf_14dthe$ = function (x) {
    var values = this.summary_0;
    if (ensureNotNull(values).size === 0) {
      return kotlin_js_internal_DoubleCompanionObject.NaN;
    }
     else if (values.size === 1) {
      return x < values.mean_za3lpa$(values.first()) ? 0.0 : x > values.mean_za3lpa$(values.first()) ? 1.0 : 0.5;
    }
     else {
      if (x < this.min) {
        return 0.0;
      }
       else if (x === this.min) {
        return 0.5 / this.size().toNumber();
      }
      mpassert(x > this.min);
      if (x > this.max) {
        return 1.0;
      }
       else if (x === this.max) {
        var n = this.size();
        return (n.toNumber() - 0.5) / n.toNumber();
      }
      mpassert(x < this.max);
      var first = values.first();
      var firstMean = values.mean_za3lpa$(first);
      if (x > this.min && x < firstMean) {
        return this.interpolateTail_0(values, x, first, firstMean, this.min);
      }
      var last = values.last();
      var lastMean = values.mean_za3lpa$(last);
      if (x < this.max && x > lastMean) {
        return 1 - this.interpolateTail_0(values, x, last, lastMean, this.max);
      }
      mpassert(values.size >= 2);
      mpassert(x >= firstMean);
      mpassert(x <= lastMean);
      var it = values.iterator();
      var a = it.next();
      var aMean = a.mean();
      var aWeight = a.count();
      if (x === aMean) {
        return aWeight / 2.0 / this.size().toNumber();
      }
      mpassert(x > aMean);
      var b = it.next();
      var bMean = b.mean();
      var bWeight = b.count();
      mpassert(bMean >= aMean);
      var weightSoFar = 0.0;
      while (bWeight > 0) {
        mpassert(x > aMean);
        if (x === bMean) {
          mpassert(bMean > aMean);
          weightSoFar += aWeight;
          while (it.hasNext()) {
            b = it.next();
            if (x === b.mean()) {
              bWeight += b.count();
            }
             else {
              break;
            }
          }
          return (weightSoFar + aWeight + bWeight / 2.0) / this.size().toNumber();
        }
        mpassert(x < bMean || x > bMean);
        if (x < bMean) {
          mpassert(aMean < bMean);
          if (aWeight === 1.0) {
            if (bWeight === 1.0) {
              return (weightSoFar + 1.0) / this.size().toNumber();
            }
             else {
              var partialWeight = (x - aMean) / (bMean - aMean) * bWeight / 2.0;
              return (weightSoFar + 1.0 + partialWeight) / this.size().toNumber();
            }
          }
           else if (bWeight === 1.0) {
            var partialWeight_0 = (x - aMean) / (bMean - aMean) * aWeight / 2.0;
            return (weightSoFar + aWeight / 2.0 + partialWeight_0) / this.size().toNumber();
          }
           else {
            var partialWeight_1 = (x - aMean) / (bMean - aMean) * (aWeight + bWeight) / 2.0;
            return (weightSoFar + aWeight / 2.0 + partialWeight_1) / this.size().toNumber();
          }
        }
        weightSoFar += aWeight;
        mpassert(x > bMean);
        if (it.hasNext()) {
          aMean = bMean;
          aWeight = bWeight;
          b = it.next();
          bMean = b.mean();
          bWeight = b.count();
          mpassert(bMean >= aMean);
        }
         else {
          bWeight = 0.0;
        }
      }
      throw IllegalStateException_init('Ran out of centroids');
    }
  };
  AVLTreeDigest.prototype.interpolateTail_0 = function (values, x, node, mean, extremeValue) {
    var count = values.count_za3lpa$(node);
    mpassert(count > 1);
    if (count === 2) {
      return 1.0 / this.size().toNumber();
    }
     else {
      var weight = count / 2.0 - 1;
      var partialWeight = (extremeValue - x) / (extremeValue - mean) * weight;
      return (partialWeight + 1.0) / this.size().toNumber();
    }
  };
  AVLTreeDigest.prototype.quantile = function (q) {
    var tmp$;
    if (q < 0 || q > 1) {
      throw IllegalArgumentException_init_0('q should be in [0,1], got ' + q);
    }
    var values = this.summary_0;
    if (ensureNotNull(values).size === 0) {
      return kotlin_js_internal_DoubleCompanionObject.NaN;
    }
     else if (values.size === 1) {
      return values.iterator().next().mean();
    }
    var index = q * this.count_0.toNumber();
    if (index < 1) {
      return this.min;
    }
    if (index >= this.count_0.subtract(Kotlin.Long.fromInt(1)).toNumber()) {
      return this.max;
    }
    var currentNode = values.first();
    var currentWeight = values.count_za3lpa$(currentNode);
    if (currentWeight === 2 && index <= 2) {
      return 2 * values.mean_za3lpa$(currentNode) - this.min;
    }
    if (values.count_za3lpa$(values.last()) === 2 && index > this.count_0.subtract(Kotlin.Long.fromInt(2)).toNumber()) {
      return 2 * values.mean_za3lpa$(values.last()) - this.max;
    }
    var weightSoFar = currentWeight / 2.0;
    if (index < weightSoFar) {
      return AbstractTDigest$Companion_getInstance().weightedAverage_kn9dxl$(this.min, weightSoFar - index, values.mean_za3lpa$(currentNode), index - 1);
    }
    tmp$ = values.size - 1 | 0;
    for (var i = 0; i < tmp$; i++) {
      var nextNode = values.next_za3lpa$(currentNode);
      var nextWeight = values.count_za3lpa$(nextNode);
      var dw = (currentWeight + nextWeight | 0) / 2.0;
      if (index < weightSoFar + dw) {
        var leftExclusion = 0.0;
        var rightExclusion = 0.0;
        if (currentWeight === 1) {
          if (index < weightSoFar + 0.5) {
            return values.mean_za3lpa$(currentNode);
          }
           else {
            leftExclusion = 0.5;
          }
        }
        if (nextWeight === 1) {
          if (index >= weightSoFar + dw - 0.5) {
            return values.mean_za3lpa$(nextNode);
          }
           else {
            rightExclusion = 0.5;
          }
        }
        mpassert(leftExclusion + rightExclusion < 1);
        mpassert(dw > 1);
        var w1 = index - weightSoFar - leftExclusion;
        var w2 = weightSoFar + dw - index - rightExclusion;
        return AbstractTDigest$Companion_getInstance().weightedAverage_kn9dxl$(values.mean_za3lpa$(currentNode), w2, values.mean_za3lpa$(nextNode), w1);
      }
      weightSoFar += dw;
      currentNode = nextNode;
      currentWeight = nextWeight;
    }
    mpassert(currentWeight > 1);
    mpassert(index - weightSoFar < ((currentWeight / 2 | 0) - 1 | 0));
    mpassert(this.count_0.toNumber() - weightSoFar > 0.5);
    var w1_0 = index - weightSoFar;
    var w2_0 = this.count_0.toNumber() - 1.0 - index;
    return AbstractTDigest$Companion_getInstance().weightedAverage_kn9dxl$(values.mean_za3lpa$(currentNode), w2_0, this.max, w1_0);
  };
  AVLTreeDigest.prototype.centroids = function () {
    return ensureNotNull(this.summary_0);
  };
  AVLTreeDigest.prototype.compression = function () {
    return this.compression_0;
  };
  AVLTreeDigest.prototype.byteSize = function () {
    this.compress();
    return 32 + (ensureNotNull(this.summary_0).size * 12 | 0) | 0;
  };
  function AVLTreeDigest$smallByteSize$lambda(this$AVLTreeDigest, closure$res) {
    return function ($receiver) {
      this$AVLTreeDigest.asSmallBytes($receiver);
      closure$res.v = $receiver.size;
      return Unit;
    };
  }
  AVLTreeDigest.prototype.smallByteSize = function () {
    var bound = this.byteSize();
    var res = {v: 0};
    var buf = buildBinaryOutput(bound, AVLTreeDigest$smallByteSize$lambda(this, res));
    buf.release();
    return res.v;
  };
  AVLTreeDigest.prototype.asBytes = function (buf) {
    var tmp$, tmp$_0;
    buf.writeInt_za3lpa$(AVLTreeDigest$Companion_getInstance().VERBOSE_ENCODING_0);
    buf.writeDouble_14dthe$(this.min);
    buf.writeDouble_14dthe$(this.max);
    buf.writeDouble_14dthe$(this.compression());
    buf.writeInt_za3lpa$(ensureNotNull(this.summary_0).size);
    tmp$ = ensureNotNull(this.summary_0).iterator();
    while (tmp$.hasNext()) {
      var centroid = tmp$.next();
      buf.writeDouble_14dthe$(centroid.mean());
    }
    tmp$_0 = ensureNotNull(this.summary_0).iterator();
    while (tmp$_0.hasNext()) {
      var centroid_0 = tmp$_0.next();
      buf.writeInt_za3lpa$(centroid_0.count());
    }
  };
  AVLTreeDigest.prototype.asSmallBytes = function (buf) {
    var tmp$, tmp$_0;
    buf.writeInt_za3lpa$(AVLTreeDigest$Companion_getInstance().SMALL_ENCODING_0);
    buf.writeDouble_14dthe$(this.min);
    buf.writeDouble_14dthe$(this.max);
    buf.writeDouble_14dthe$(this.compression());
    buf.writeInt_za3lpa$(ensureNotNull(this.summary_0).size);
    var x = 0.0;
    tmp$ = ensureNotNull(this.summary_0).iterator();
    while (tmp$.hasNext()) {
      var centroid = tmp$.next();
      var delta = centroid.mean() - x;
      x = centroid.mean();
      buf.writeFloat_mx4ult$(delta);
    }
    tmp$_0 = ensureNotNull(this.summary_0).iterator();
    while (tmp$_0.hasNext()) {
      var centroid_0 = tmp$_0.next();
      var n = centroid_0.count();
      AbstractTDigest$Companion_getInstance().encode_ydee4o$(buf, n);
    }
  };
  function AVLTreeDigest$Companion() {
    AVLTreeDigest$Companion_instance = this;
    this.VERBOSE_ENCODING_0 = 1;
    this.SMALL_ENCODING_0 = 2;
  }
  AVLTreeDigest$Companion.prototype.fromBytes = function (buf) {
    var encoding = buf.readInt();
    if (encoding === this.VERBOSE_ENCODING_0) {
      var min = buf.readDouble();
      var max = buf.readDouble();
      var compression = buf.readDouble();
      var r = AVLTreeDigest_init(compression);
      r.setMinMax_sdh6z7$(min, max);
      var n = buf.readInt();
      var means = new Float64Array(n);
      for (var i = 0; i < n; i++) {
        means[i] = buf.readDouble();
      }
      for (var i_0 = 0; i_0 < n; i_0++) {
        r.addWeightedSample(means[i_0], buf.readInt());
      }
      return r;
    }
     else if (encoding === this.SMALL_ENCODING_0) {
      var min_0 = buf.readDouble();
      var max_0 = buf.readDouble();
      var compression_0 = buf.readDouble();
      var r_0 = AVLTreeDigest_init(compression_0);
      r_0.setMinMax_sdh6z7$(min_0, max_0);
      var n_0 = buf.readInt();
      var means_0 = new Float64Array(n_0);
      var x = 0.0;
      for (var i_1 = 0; i_1 < n_0; i_1++) {
        var delta = buf.readFloat();
        x += delta;
        means_0[i_1] = x;
      }
      for (var i_2 = 0; i_2 < n_0; i_2++) {
        var z = AbstractTDigest$Companion_getInstance().decode_51dx0l$(buf);
        r_0.addWeightedSample(means_0[i_2], z);
      }
      return r_0;
    }
     else {
      throw IllegalStateException_init('Invalid format for serialized histogram');
    }
  };
  AVLTreeDigest$Companion.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Companion',
    interfaces: []
  };
  var AVLTreeDigest$Companion_instance = null;
  function AVLTreeDigest$Companion_getInstance() {
    if (AVLTreeDigest$Companion_instance === null) {
      new AVLTreeDigest$Companion();
    }
    return AVLTreeDigest$Companion_instance;
  }
  AVLTreeDigest.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'AVLTreeDigest',
    interfaces: [AbstractTDigest]
  };
  function AVLTreeDigest_init(compression, $this) {
    $this = $this || Object.create(AVLTreeDigest.prototype);
    AbstractTDigest.call($this);
    AVLTreeDigest.call($this);
    $this.compression_0 = compression;
    $this.summary_0 = new AVLGroupTree(false);
    return $this;
  }
  function Centroid(record) {
    Centroid$Companion_getInstance();
    this.centroid_0 = 0.0;
    this.count_0 = 0;
    this.id_0 = 0;
    this.actualData_0 = null;
    this.id_0 = Centroid$Companion_getInstance().uniqueCount_0.getAndIncrement$atomicfu();
    if (record) {
      this.actualData_0 = ArrayList_init();
    }
  }
  Centroid.prototype.start_0 = function (x, w, id) {
    this.id_0 = id;
    this.add_12fank$(x, w);
  };
  Centroid.prototype.add_12fank$ = function (x, w) {
    if (this.actualData_0 != null) {
      ensureNotNull(this.actualData_0).add_11rb$(x);
    }
    this.count_0 = this.count_0 + w | 0;
    this.centroid_0 += w * (x - this.centroid_0) / this.count_0;
  };
  Centroid.prototype.mean = function () {
    return this.centroid_0;
  };
  Centroid.prototype.count = function () {
    return this.count_0;
  };
  Centroid.prototype.id = function () {
    return this.id_0;
  };
  Centroid.prototype.toString = function () {
    return 'Centroid{' + 'centroid=' + toString(this.centroid_0) + ', count=' + toString(this.count_0) + String.fromCharCode(125);
  };
  Centroid.prototype.hashCode = function () {
    return this.id_0;
  };
  Centroid.prototype.compareTo_11rb$ = function (o) {
    var r = Kotlin.compareTo(this.centroid_0, o.centroid_0);
    if (r === 0) {
      r = this.id_0 - o.id_0 | 0;
    }
    return r;
  };
  Centroid.prototype.data = function () {
    return this.actualData_0;
  };
  Centroid.prototype.insertData_14dthe$ = function (x) {
    if (this.actualData_0 == null) {
      this.actualData_0 = ArrayList_init();
    }
    ensureNotNull(this.actualData_0).add_11rb$(x);
  };
  Centroid.prototype.add_a3f1dg$ = function (x, w, data) {
    var tmp$;
    if (this.actualData_0 != null) {
      if (data != null) {
        tmp$ = data.iterator();
        while (tmp$.hasNext()) {
          var old = tmp$.next();
          ensureNotNull(this.actualData_0).add_11rb$(old);
        }
      }
       else {
        ensureNotNull(this.actualData_0).add_11rb$(x);
      }
    }
    this.centroid_0 = AbstractTDigest$Companion_getInstance().weightedAverage_kn9dxl$(this.centroid_0, this.count_0, x, w);
    this.count_0 = this.count_0 + w | 0;
  };
  function Centroid$Companion() {
    Centroid$Companion_instance = this;
    this.uniqueCount_0 = atomic(1);
  }
  Centroid$Companion.prototype.createWeighted_a3f1dg$ = function (x, w, data) {
    var r = new Centroid(data != null);
    r.add_a3f1dg$(x, w, data);
    return r;
  };
  Centroid$Companion.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Companion',
    interfaces: []
  };
  var Centroid$Companion_instance = null;
  function Centroid$Companion_getInstance() {
    if (Centroid$Companion_instance === null) {
      new Centroid$Companion();
    }
    return Centroid$Companion_instance;
  }
  Centroid.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Centroid',
    interfaces: [Comparable]
  };
  function Centroid_init(x, $this) {
    $this = $this || Object.create(Centroid.prototype);
    Centroid.call($this, false);
    $this.start_0(x, 1, Centroid$Companion_getInstance().uniqueCount_0.getAndIncrement$atomicfu());
    return $this;
  }
  function Centroid_init_0(x, w, $this) {
    $this = $this || Object.create(Centroid.prototype);
    Centroid.call($this, false);
    $this.start_0(x, w, Centroid$Companion_getInstance().uniqueCount_0.getAndIncrement$atomicfu());
    return $this;
  }
  function Centroid_init_1(x, w, id, $this) {
    $this = $this || Object.create(Centroid.prototype);
    Centroid.call($this, false);
    $this.start_0(x, w, id);
    return $this;
  }
  function Centroid_init_2(x, id, record, $this) {
    $this = $this || Object.create(Centroid.prototype);
    Centroid.call($this, record);
    $this.start_0(x, 1, id);
    return $this;
  }
  function Centroid_init_3(x, w, data, $this) {
    $this = $this || Object.create(Centroid.prototype);
    Centroid_init_0(x, w, $this);
    $this.actualData_0 = data;
    return $this;
  }
  function Dist() {
    Dist_instance = this;
  }
  Dist.prototype.cdf_4avpt5$ = function (x, data) {
    var tmp$;
    var n1 = 0;
    var n2 = 0;
    for (tmp$ = 0; tmp$ !== data.length; ++tmp$) {
      var v = data[tmp$];
      n1 = n1 + (v < x ? 1 : 0) | 0;
      n2 = n2 + (v === x ? 1 : 0) | 0;
    }
    return (n1 + n2 / 2.0) / data.length;
  };
  Dist.prototype.cdf_7si1j9$ = function (x, data) {
    var tmp$;
    var n1 = 0;
    var n2 = 0;
    tmp$ = data.iterator();
    while (tmp$.hasNext()) {
      var v = tmp$.next();
      n1 = n1 + (v < x ? 1 : 0) | 0;
      n2 = n2 + (v === x ? 1 : 0) | 0;
    }
    return (n1 + n2 / 2.0) / data.size;
  };
  Dist.prototype.quantile_4avpt5$ = function (q, data) {
    var n = data.length;
    if (n === 0) {
      return kotlin_js_internal_DoubleCompanionObject.NaN;
    }
    var index = q * n;
    if (index < 0) {
      index = 0.0;
    }
    if (index > (n - 1 | 0)) {
      index = n - 1 | 0;
    }
    var x = index;
    return data[numberToInt(Math_0.floor(x))];
  };
  Dist.prototype.quantile_9iknyd$ = function (q, data) {
    var n = data.size;
    if (n === 0) {
      return kotlin_js_internal_DoubleCompanionObject.NaN;
    }
    var index = q * n;
    if (index < 0) {
      index = 0.0;
    }
    if (index > (n - 1 | 0)) {
      index = n - 1 | 0;
    }
    var x = index;
    return data.get_za3lpa$(numberToInt(Math_0.floor(x)));
  };
  Dist.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Dist',
    interfaces: []
  };
  var Dist_instance = null;
  function Dist_getInstance() {
    if (Dist_instance === null) {
      new Dist();
    }
    return Dist_instance;
  }
  function IntAVLTree(initialCapacity) {
    IntAVLTree$Companion_getInstance();
    if (initialCapacity === void 0)
      initialCapacity = 16;
    this.nodeAllocator_0 = null;
    this.troot_0 = 0;
    this.parent_0 = null;
    this.left_0 = null;
    this.right_0 = null;
    this.depth_0 = null;
    this.nodeAllocator_0 = new IntAVLTree$NodeAllocator();
    this.troot_0 = IntAVLTree$Companion_getInstance().NIL;
    this.parent_0 = new Int32Array(initialCapacity);
    this.left_0 = new Int32Array(initialCapacity);
    this.right_0 = new Int32Array(initialCapacity);
    this.depth_0 = new Int8Array(initialCapacity);
  }
  IntAVLTree.prototype.root = function () {
    return this.troot_0;
  };
  IntAVLTree.prototype.capacity = function () {
    return this.parent_0.length;
  };
  IntAVLTree.prototype.resize_za3lpa$ = function (newCapacity) {
    this.parent_0 = copyOf_0(this.parent_0, newCapacity);
    this.left_0 = copyOf_0(this.left_0, newCapacity);
    this.right_0 = copyOf_0(this.right_0, newCapacity);
    this.depth_0 = copyOf_2(this.depth_0, newCapacity);
  };
  IntAVLTree.prototype.size = function () {
    return this.nodeAllocator_0.size_8be2vx$();
  };
  IntAVLTree.prototype.parent_za3lpa$ = function (node) {
    return this.parent_0[node];
  };
  IntAVLTree.prototype.left_za3lpa$ = function (node) {
    return this.left_0[node];
  };
  IntAVLTree.prototype.right_za3lpa$ = function (node) {
    return this.right_0[node];
  };
  IntAVLTree.prototype.depth_za3lpa$ = function (node) {
    return this.depth_0[node];
  };
  IntAVLTree.prototype.first_za3lpa$ = function (node) {
    var node_0 = node;
    if (node_0 === IntAVLTree$Companion_getInstance().NIL) {
      return IntAVLTree$Companion_getInstance().NIL;
    }
    while (true) {
      var left = this.left_za3lpa$(node_0);
      if (left === IntAVLTree$Companion_getInstance().NIL) {
        break;
      }
      node_0 = left;
    }
    return node_0;
  };
  IntAVLTree.prototype.last_za3lpa$ = function (node) {
    var node_0 = node;
    while (true) {
      var right = this.right_za3lpa$(node_0);
      if (right === IntAVLTree$Companion_getInstance().NIL) {
        break;
      }
      node_0 = right;
    }
    return node_0;
  };
  IntAVLTree.prototype.next_za3lpa$ = function (node) {
    var node_0 = node;
    var right = this.right_za3lpa$(node_0);
    if (right !== IntAVLTree$Companion_getInstance().NIL) {
      return this.first_za3lpa$(right);
    }
     else {
      var parent = this.parent_za3lpa$(node_0);
      while (parent !== IntAVLTree$Companion_getInstance().NIL && node_0 === this.right_za3lpa$(parent)) {
        node_0 = parent;
        parent = this.parent_za3lpa$(parent);
      }
      return parent;
    }
  };
  IntAVLTree.prototype.prev_za3lpa$ = function (node) {
    var node_0 = node;
    var left = this.left_za3lpa$(node_0);
    if (left !== IntAVLTree$Companion_getInstance().NIL) {
      return this.last_za3lpa$(left);
    }
     else {
      var parent = this.parent_za3lpa$(node_0);
      while (parent !== IntAVLTree$Companion_getInstance().NIL && node_0 === this.left_za3lpa$(parent)) {
        node_0 = parent;
        parent = this.parent_za3lpa$(parent);
      }
      return parent;
    }
  };
  IntAVLTree.prototype.add = function () {
    if (this.troot_0 === IntAVLTree$Companion_getInstance().NIL) {
      this.troot_0 = this.nodeAllocator_0.newNode_8be2vx$();
      this.copy_za3lpa$(this.troot_0);
      this.fixAggregates_za3lpa$(this.troot_0);
      return true;
    }
     else {
      var node = this.troot_0;
      mpassert(this.parent_za3lpa$(this.troot_0) === IntAVLTree$Companion_getInstance().NIL);
      var parent;
      var cmp;
      do {
        cmp = this.compare_za3lpa$(node);
        if (cmp < 0) {
          parent = node;
          node = this.left_za3lpa$(node);
        }
         else if (cmp > 0) {
          parent = node;
          node = this.right_za3lpa$(node);
        }
         else {
          this.merge_za3lpa$(node);
          return false;
        }
      }
       while (node !== IntAVLTree$Companion_getInstance().NIL);
      node = this.nodeAllocator_0.newNode_8be2vx$();
      if (node >= this.capacity()) {
        this.resize_za3lpa$(IntAVLTree$Companion_getInstance().oversize_za3lpa$(node + 1 | 0));
      }
      this.copy_za3lpa$(node);
      this.parent_1(node, parent);
      if (cmp < 0) {
        this.left_1(parent, node);
      }
       else {
        mpassert(cmp > 0);
        this.right_1(parent, node);
      }
      this.rebalance_0(node);
      return true;
    }
  };
  IntAVLTree.prototype.find = function () {
    var node = this.troot_0;
    while (node !== IntAVLTree$Companion_getInstance().NIL) {
      var cmp = this.compare_za3lpa$(node);
      if (cmp < 0) {
        node = this.left_za3lpa$(node);
      }
       else if (cmp > 0) {
        node = this.right_za3lpa$(node);
      }
       else {
        return node;
      }
    }
    return IntAVLTree$Companion_getInstance().NIL;
  };
  IntAVLTree.prototype.update_za3lpa$ = function (node) {
    var prev = this.prev_za3lpa$(node);
    var next = this.next_za3lpa$(node);
    if ((prev === IntAVLTree$Companion_getInstance().NIL || this.compare_za3lpa$(prev) > 0) && (next === IntAVLTree$Companion_getInstance().NIL || this.compare_za3lpa$(next) < 0)) {
      this.copy_za3lpa$(node);
      var n = node;
      while (n !== IntAVLTree$Companion_getInstance().NIL) {
        this.fixAggregates_za3lpa$(n);
        n = this.parent_za3lpa$(n);
      }
    }
     else {
      this.remove_za3lpa$(node);
      this.add();
    }
  };
  IntAVLTree.prototype.remove_za3lpa$ = function (node) {
    if (node === IntAVLTree$Companion_getInstance().NIL) {
      throw IllegalArgumentException_init();
    }
    if (this.left_za3lpa$(node) !== IntAVLTree$Companion_getInstance().NIL && this.right_za3lpa$(node) !== IntAVLTree$Companion_getInstance().NIL) {
      var next = this.next_za3lpa$(node);
      mpassert(next !== IntAVLTree$Companion_getInstance().NIL);
      this.swap_0(node, next);
    }
    mpassert(this.left_za3lpa$(node) === IntAVLTree$Companion_getInstance().NIL || this.right_za3lpa$(node) === IntAVLTree$Companion_getInstance().NIL);
    var parent = this.parent_za3lpa$(node);
    var child = this.left_za3lpa$(node);
    if (child === IntAVLTree$Companion_getInstance().NIL) {
      child = this.right_za3lpa$(node);
    }
    if (child === IntAVLTree$Companion_getInstance().NIL) {
      if (node === this.troot_0) {
        if (!(this.size() === 1)) {
          var message = this.size();
          throw AssertionError_init_0(message);
        }
        this.troot_0 = IntAVLTree$Companion_getInstance().NIL;
      }
       else {
        if (node === this.left_za3lpa$(parent)) {
          this.left_1(parent, IntAVLTree$Companion_getInstance().NIL);
        }
         else {
          mpassert(node === this.right_za3lpa$(parent));
          this.right_1(parent, IntAVLTree$Companion_getInstance().NIL);
        }
      }
    }
     else {
      if (node === this.troot_0) {
        mpassert(this.size() === 2);
        this.troot_0 = child;
      }
       else if (node === this.left_za3lpa$(parent)) {
        this.left_1(parent, child);
      }
       else {
        mpassert(node === this.right_za3lpa$(parent));
        this.right_1(parent, child);
      }
      this.parent_1(child, parent);
    }
    this.release_0(node);
    this.rebalance_0(parent);
  };
  IntAVLTree.prototype.release_0 = function (node) {
    this.left_1(node, IntAVLTree$Companion_getInstance().NIL);
    this.right_1(node, IntAVLTree$Companion_getInstance().NIL);
    this.parent_1(node, IntAVLTree$Companion_getInstance().NIL);
    this.nodeAllocator_0.release_kcn2v3$(node);
  };
  IntAVLTree.prototype.swap_0 = function (node1, node2) {
    var parent1 = this.parent_za3lpa$(node1);
    var parent2 = this.parent_za3lpa$(node2);
    if (parent1 !== IntAVLTree$Companion_getInstance().NIL) {
      if (node1 === this.left_za3lpa$(parent1)) {
        this.left_1(parent1, node2);
      }
       else {
        mpassert(node1 === this.right_za3lpa$(parent1));
        this.right_1(parent1, node2);
      }
    }
     else {
      mpassert(this.troot_0 === node1);
      this.troot_0 = node2;
    }
    if (parent2 !== IntAVLTree$Companion_getInstance().NIL) {
      if (node2 === this.left_za3lpa$(parent2)) {
        this.left_1(parent2, node1);
      }
       else {
        mpassert(node2 === this.right_za3lpa$(parent2));
        this.right_1(parent2, node1);
      }
    }
     else {
      mpassert(this.troot_0 === node2);
      this.troot_0 = node1;
    }
    this.parent_1(node1, parent2);
    this.parent_1(node2, parent1);
    var left1 = this.left_za3lpa$(node1);
    var left2 = this.left_za3lpa$(node2);
    this.left_1(node1, left2);
    if (left2 !== IntAVLTree$Companion_getInstance().NIL) {
      this.parent_1(left2, node1);
    }
    this.left_1(node2, left1);
    if (left1 !== IntAVLTree$Companion_getInstance().NIL) {
      this.parent_1(left1, node2);
    }
    var right1 = this.right_za3lpa$(node1);
    var right2 = this.right_za3lpa$(node2);
    this.right_1(node1, right2);
    if (right2 !== IntAVLTree$Companion_getInstance().NIL) {
      this.parent_1(right2, node1);
    }
    this.right_1(node2, right1);
    if (right1 !== IntAVLTree$Companion_getInstance().NIL) {
      this.parent_1(right1, node2);
    }
    var depth1 = this.depth_za3lpa$(node1);
    var depth2 = this.depth_za3lpa$(node2);
    this.depth_1(node1, depth2);
    this.depth_1(node2, depth1);
  };
  IntAVLTree.prototype.balanceFactor_0 = function (node) {
    return this.depth_za3lpa$(this.left_za3lpa$(node)) - this.depth_za3lpa$(this.right_za3lpa$(node)) | 0;
  };
  IntAVLTree.prototype.rebalance_0 = function (node) {
    var n = node;
    while (n !== IntAVLTree$Companion_getInstance().NIL) {
      var p = this.parent_za3lpa$(n);
      this.fixAggregates_za3lpa$(n);
      switch (this.balanceFactor_0(n)) {
        case -2:
          var right = this.right_za3lpa$(n);
          if (this.balanceFactor_0(right) === 1) {
            this.rotateRight_0(right);
          }

          this.rotateLeft_0(n);
          break;
        case 2:
          var left = this.left_za3lpa$(n);
          if (this.balanceFactor_0(left) === -1) {
            this.rotateLeft_0(left);
          }

          this.rotateRight_0(n);
          break;
        case -1:
        case 0:
        case 1:
          break;
        default:throw AssertionError_init();
      }
      n = p;
    }
  };
  IntAVLTree.prototype.fixAggregates_za3lpa$ = function (node) {
    var a = this.depth_za3lpa$(this.left_za3lpa$(node));
    var b = this.depth_za3lpa$(this.right_za3lpa$(node));
    this.depth_1(node, 1 + Math_0.max(a, b) | 0);
  };
  IntAVLTree.prototype.rotateLeft_0 = function (n) {
    var r = this.right_za3lpa$(n);
    var lr = this.left_za3lpa$(r);
    this.right_1(n, lr);
    if (lr !== IntAVLTree$Companion_getInstance().NIL) {
      this.parent_1(lr, n);
    }
    var p = this.parent_za3lpa$(n);
    this.parent_1(r, p);
    if (p === IntAVLTree$Companion_getInstance().NIL) {
      this.troot_0 = r;
    }
     else if (this.left_za3lpa$(p) === n) {
      this.left_1(p, r);
    }
     else {
      mpassert(this.right_za3lpa$(p) === n);
      this.right_1(p, r);
    }
    this.left_1(r, n);
    this.parent_1(n, r);
    this.fixAggregates_za3lpa$(n);
    this.fixAggregates_za3lpa$(this.parent_za3lpa$(n));
  };
  IntAVLTree.prototype.rotateRight_0 = function (n) {
    var l = this.left_za3lpa$(n);
    var rl = this.right_za3lpa$(l);
    this.left_1(n, rl);
    if (rl !== IntAVLTree$Companion_getInstance().NIL) {
      this.parent_1(rl, n);
    }
    var p = this.parent_za3lpa$(n);
    this.parent_1(l, p);
    if (p === IntAVLTree$Companion_getInstance().NIL) {
      this.troot_0 = l;
    }
     else if (this.right_za3lpa$(p) === n) {
      this.right_1(p, l);
    }
     else {
      mpassert(this.left_za3lpa$(p) === n);
      this.left_1(p, l);
    }
    this.right_1(l, n);
    this.parent_1(n, l);
    this.fixAggregates_za3lpa$(n);
    this.fixAggregates_za3lpa$(this.parent_za3lpa$(n));
  };
  IntAVLTree.prototype.parent_1 = function (node, parent) {
    mpassert(node !== IntAVLTree$Companion_getInstance().NIL);
    this.parent_0[node] = parent;
  };
  IntAVLTree.prototype.left_1 = function (node, left) {
    mpassert(node !== IntAVLTree$Companion_getInstance().NIL);
    this.left_0[node] = left;
  };
  IntAVLTree.prototype.right_1 = function (node, right) {
    mpassert(node !== IntAVLTree$Companion_getInstance().NIL);
    this.right_0[node] = right;
  };
  IntAVLTree.prototype.depth_1 = function (node, depth) {
    mpassert(node !== IntAVLTree$Companion_getInstance().NIL);
    mpassert(depth >= 0 && depth <= kotlin_js_internal_ByteCompanionObject.MAX_VALUE);
    this.depth_0[node] = toByte(depth);
  };
  IntAVLTree.prototype.checkBalance_za3lpa$ = function (node) {
    if (node === IntAVLTree$Companion_getInstance().NIL) {
      mpassert(this.depth_za3lpa$(node) === 0);
    }
     else {
      var tmp$ = this.depth_za3lpa$(node);
      var a = this.depth_za3lpa$(this.left_za3lpa$(node));
      var b = this.depth_za3lpa$(this.right_za3lpa$(node));
      mpassert(tmp$ === (1 + Math_0.max(a, b) | 0));
      mpassert(abs(this.depth_za3lpa$(this.left_za3lpa$(node)) - this.depth_za3lpa$(this.right_za3lpa$(node)) | 0) <= 1);
      this.checkBalance_za3lpa$(this.left_za3lpa$(node));
      this.checkBalance_za3lpa$(this.right_za3lpa$(node));
    }
  };
  function IntAVLTree$IntStack() {
    this.stack_0 = null;
    this.size_0 = 0;
    this.stack_0 = new Int32Array(0);
    this.size_0 = 0;
  }
  IntAVLTree$IntStack.prototype.size_8be2vx$ = function () {
    return this.size_0;
  };
  IntAVLTree$IntStack.prototype.pop_8be2vx$ = function () {
    return this.stack_0[this.size_0 = this.size_0 - 1 | 0, this.size_0];
  };
  IntAVLTree$IntStack.prototype.push_kcn2v3$ = function (v) {
    var tmp$;
    if (this.size_0 >= this.stack_0.length) {
      var newLength = IntAVLTree$Companion_getInstance().oversize_za3lpa$(this.size_0 + 1 | 0);
      this.stack_0 = copyOf_0(this.stack_0, newLength);
    }
    this.stack_0[tmp$ = this.size_0, this.size_0 = tmp$ + 1 | 0, tmp$] = v;
  };
  IntAVLTree$IntStack.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'IntStack',
    interfaces: []
  };
  function IntAVLTree$NodeAllocator() {
    this.nextNode_0 = 0;
    this.releasedNodes_0 = null;
    this.nextNode_0 = IntAVLTree$Companion_getInstance().NIL + 1 | 0;
    this.releasedNodes_0 = new IntAVLTree$IntStack();
  }
  IntAVLTree$NodeAllocator.prototype.newNode_8be2vx$ = function () {
    var tmp$, tmp$_0;
    if (this.releasedNodes_0.size_8be2vx$() > 0) {
      tmp$_0 = this.releasedNodes_0.pop_8be2vx$();
    }
     else {
      tmp$_0 = (tmp$ = this.nextNode_0, this.nextNode_0 = tmp$ + 1 | 0, tmp$);
    }
    return tmp$_0;
  };
  IntAVLTree$NodeAllocator.prototype.release_kcn2v3$ = function (node) {
    mpassert(node < this.nextNode_0);
    this.releasedNodes_0.push_kcn2v3$(node);
  };
  IntAVLTree$NodeAllocator.prototype.size_8be2vx$ = function () {
    return this.nextNode_0 - this.releasedNodes_0.size_8be2vx$() - 1 | 0;
  };
  IntAVLTree$NodeAllocator.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'NodeAllocator',
    interfaces: []
  };
  function IntAVLTree$Companion() {
    IntAVLTree$Companion_instance = this;
    this.NIL = 0;
  }
  IntAVLTree$Companion.prototype.oversize_za3lpa$ = function (size) {
    return size + (size >>> 3) | 0;
  };
  IntAVLTree$Companion.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Companion',
    interfaces: []
  };
  var IntAVLTree$Companion_instance = null;
  function IntAVLTree$Companion_getInstance() {
    if (IntAVLTree$Companion_instance === null) {
      new IntAVLTree$Companion();
    }
    return IntAVLTree$Companion_instance;
  }
  IntAVLTree.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'IntAVLTree',
    interfaces: []
  };
  function MergingDigest(compression, bufferSize, size) {
    MergingDigest$Companion_getInstance();
    if (bufferSize === void 0)
      bufferSize = -1;
    if (size === void 0)
      size = -1;
    AbstractTDigest.call(this);
    this.mergeCount_0 = 0;
    this.publicCompression_0 = 0;
    this.compression_0 = 0;
    this.lastUsedCell_0 = 0;
    this.totalWeight_0 = 0.0;
    this.weight_0 = null;
    this.mean_0 = null;
    this.data_0 = null;
    this.unmergedWeight_0 = 0.0;
    this.tempUsed_0 = 0;
    this.tempWeight_0 = null;
    this.tempMean_0 = null;
    this.tempData_0 = null;
    this.order_0 = null;
    this.useAlternatingSort = true;
    this.useTwoLevelCompression = true;
    var compression_0 = compression;
    var bufferSize_0 = bufferSize;
    var size_0 = size;
    if (compression_0 < 10) {
      compression_0 = 10.0;
    }
    var sizeFudge = 0.0;
    if (MergingDigest$Companion_getInstance().useWeightLimit) {
      sizeFudge = 10.0;
      if (compression_0 < 30)
        sizeFudge += 20.0;
    }
    var a = 2 * compression_0 + sizeFudge;
    var b = size_0;
    size_0 = numberToInt(Math_0.max(a, b));
    if (bufferSize_0 === -1) {
      bufferSize_0 = 5 * size_0 | 0;
    }
    if (bufferSize_0 <= (2 * size_0 | 0)) {
      bufferSize_0 = 2 * size_0 | 0;
    }
    var b_0 = (bufferSize_0 / size_0 | 0) - 1 | 0;
    var scale = Math_0.max(1, b_0);
    if (!this.useTwoLevelCompression) {
      scale = 1.0;
    }
    this.publicCompression_0 = compression_0;
    var x = scale;
    this.compression_0 = Math_0.sqrt(x) * this.publicCompression_0;
    if (size_0 < this.compression_0 + sizeFudge) {
      var x_0 = this.compression_0 + sizeFudge;
      size_0 = numberToInt(Math_0.ceil(x_0));
    }
    if (bufferSize_0 <= (2 * size_0 | 0)) {
      bufferSize_0 = 2 * size_0 | 0;
    }
    this.weight_0 = new Float64Array(size_0);
    this.mean_0 = new Float64Array(size_0);
    this.tempWeight_0 = new Float64Array(bufferSize_0);
    this.tempMean_0 = new Float64Array(bufferSize_0);
    this.order_0 = new Int32Array(bufferSize_0);
    this.lastUsedCell_0 = 0;
  }
  Object.defineProperty(MergingDigest.prototype, 'scaleFunction', {
    get: function () {
      return this.scale;
    }
  });
  MergingDigest.prototype.recordAllData = function () {
    AbstractTDigest.prototype.recordAllData.call(this);
    this.data_0 = ArrayList_init();
    this.tempData_0 = ArrayList_init();
    return this;
  };
  MergingDigest.prototype.add_9eljot$ = function (x, w, base) {
    this.add_0(x, w, base.data());
  };
  MergingDigest.prototype.addWeightedSample = function (x, w) {
    var tmp$;
    this.add_0(x, w, (tmp$ = null) == null || Kotlin.isType(tmp$, List) ? tmp$ : throwCCE());
  };
  MergingDigest.prototype.add_0 = function (x, w, history) {
    var tmp$;
    var history_0 = history;
    if (isNaN_0(x)) {
      throw IllegalArgumentException_init_0('Cannot add NaN to t-digest');
    }
    if (this.tempUsed_0 >= (this.tempWeight_0.length - this.lastUsedCell_0 - 1 | 0)) {
      this.mergeNewValues_0();
    }
    var where = (tmp$ = this.tempUsed_0, this.tempUsed_0 = tmp$ + 1 | 0, tmp$);
    this.tempWeight_0[where] = w;
    this.tempMean_0[where] = x;
    this.unmergedWeight_0 += w;
    if (x < this.min) {
      this.min = x;
    }
    if (x > this.max) {
      this.max = x;
    }
    if (this.data_0 != null) {
      if (this.tempData_0 == null) {
        this.tempData_0 = ArrayList_init();
      }
      while (ensureNotNull(this.tempData_0).size <= where) {
        ensureNotNull(this.tempData_0).add_11rb$(ArrayList_init());
      }
      if (history_0 == null) {
        history_0 = listOf(x);
      }
      ensureNotNull(this.tempData_0).get_za3lpa$(where).addAll_brywnq$(history_0);
    }
  };
  MergingDigest.prototype.updateSample_lu1900$ = function (oldValue, newValue) {
    throw new NotImplementedError('updateSample not implemented for MergingDigest');
  };
  MergingDigest.prototype.add_1 = function (m, w, count, data) {
    var m_0 = m;
    var w_0 = w;
    if (m_0.length !== w_0.length) {
      throw IllegalArgumentException_init_0('Arrays not same length');
    }
    if (m_0.length < (count + this.lastUsedCell_0 | 0)) {
      var m1 = new Float64Array(count + this.lastUsedCell_0 | 0);
      Utils_getInstance().arraycopy_dgpv4k$(m_0, 0, m1, 0, count);
      m_0 = m1;
      var w1 = new Float64Array(count + this.lastUsedCell_0 | 0);
      Utils_getInstance().arraycopy_dgpv4k$(w_0, 0, w1, 0, count);
      w_0 = w1;
    }
    var total = 0.0;
    for (var i = 0; i < count; i++) {
      total += w_0[i];
    }
    this.merge_0(m_0, w_0, count, data, null, total, false, this.compression_0);
  };
  MergingDigest.prototype.add_ixnl3q$ = function (others) {
    var tmp$, tmp$_0, tmp$_1, tmp$_2;
    if (others.size === 0) {
      return;
    }
    var size = this.lastUsedCell_0;
    tmp$ = others.iterator();
    while (tmp$.hasNext()) {
      var other = tmp$.next();
      other.compress();
      size = size + other.centroidCount() | 0;
    }
    var m = new Float64Array(size);
    var w = new Float64Array(size);
    var data;
    if (this.isRecording) {
      data = ArrayList_init();
    }
     else {
      data = null;
    }
    var offset = 0;
    tmp$_0 = others.iterator();
    while (tmp$_0.hasNext()) {
      var other_0 = tmp$_0.next();
      if (Kotlin.isType(other_0, MergingDigest)) {
        Utils_getInstance().arraycopy_dgpv4k$(other_0.mean_0, 0, m, offset, other_0.lastUsedCell_0);
        Utils_getInstance().arraycopy_dgpv4k$(other_0.weight_0, 0, w, offset, other_0.lastUsedCell_0);
        if (data != null) {
          tmp$_1 = other_0.centroids().iterator();
          while (tmp$_1.hasNext()) {
            var centroid = tmp$_1.next();
            data.add_11rb$(ensureNotNull(centroid.data()));
          }
        }
        offset = offset + other_0.lastUsedCell_0 | 0;
      }
       else {
        tmp$_2 = other_0.centroids().iterator();
        while (tmp$_2.hasNext()) {
          var centroid_0 = tmp$_2.next();
          m[offset] = centroid_0.mean();
          w[offset] = centroid_0.count();
          if (this.isRecording) {
            mpassert(data != null);
            ensureNotNull(data).add_11rb$(ensureNotNull(centroid_0.data()));
          }
          offset = offset + 1 | 0;
        }
      }
    }
    this.add_1(m, w, size, data);
  };
  MergingDigest.prototype.mergeNewValues_0 = function (force, compression) {
    if (force === void 0)
      force = false;
    if (compression === void 0)
      compression = this.compression_0;
    if (this.totalWeight_0 === 0.0 && this.unmergedWeight_0 === 0.0) {
      return;
    }
    if (force || this.unmergedWeight_0 > 0) {
      this.merge_0(this.tempMean_0, this.tempWeight_0, this.tempUsed_0, this.tempData_0, this.order_0, this.unmergedWeight_0, this.useAlternatingSort & this.mergeCount_0 % 2 === 1, compression);
      this.mergeCount_0 = this.mergeCount_0 + 1 | 0;
      this.tempUsed_0 = 0;
      this.unmergedWeight_0 = 0.0;
      if (this.data_0 != null) {
        this.tempData_0 = ArrayList_init();
      }
    }
  };
  MergingDigest.prototype.merge_0 = function (incomingMean, incomingWeight, incomingCount, incomingData, incomingOrder, unmergedWeight, runBackwards, compression) {
    var tmp$, tmp$_0, tmp$_1;
    var incomingCount_0 = incomingCount;
    var incomingOrder_0 = incomingOrder;
    Utils_getInstance().arraycopy_dgpv4k$(this.mean_0, 0, incomingMean, incomingCount_0, this.lastUsedCell_0);
    Utils_getInstance().arraycopy_dgpv4k$(this.weight_0, 0, incomingWeight, incomingCount_0, this.lastUsedCell_0);
    incomingCount_0 = incomingCount_0 + this.lastUsedCell_0 | 0;
    if (incomingData != null) {
      tmp$ = this.lastUsedCell_0;
      for (var i = 0; i < tmp$; i++) {
        mpassert(this.data_0 != null);
        incomingData.add_11rb$(ensureNotNull(this.data_0).get_za3lpa$(i));
      }
      this.data_0 = ArrayList_init();
    }
    if (incomingOrder_0 == null) {
      incomingOrder_0 = new Int32Array(incomingCount_0);
    }
    Sort_getInstance().sort_kbza6$(incomingOrder_0, incomingMean, incomingCount_0);
    if (runBackwards) {
      Sort_getInstance().reverse_nd5v6f$(incomingOrder_0, 0, incomingCount_0);
    }
    this.totalWeight_0 += unmergedWeight;
    mpassert((this.lastUsedCell_0 + incomingCount_0 | 0) > 0);
    this.lastUsedCell_0 = 0;
    this.mean_0[this.lastUsedCell_0] = incomingMean[incomingOrder_0[0]];
    this.weight_0[this.lastUsedCell_0] = incomingWeight[incomingOrder_0[0]];
    var wSoFar = 0.0;
    if (this.data_0 != null) {
      mpassert(incomingData != null);
      ensureNotNull(this.data_0).add_11rb$(ensureNotNull(incomingData).get_za3lpa$(incomingOrder_0[0]));
    }
    var normalizer = this.scale.normalizer_lu1900$(compression, this.totalWeight_0);
    var k1 = this.scale.k_lu1900$(0.0, normalizer);
    var wLimit = this.totalWeight_0 * this.scale.q_lu1900$(k1 + 1, normalizer);
    tmp$_0 = incomingCount_0;
    for (var i_0 = 1; i_0 < tmp$_0; i_0++) {
      var ix = incomingOrder_0[i_0];
      var proposedWeight = this.weight_0[this.lastUsedCell_0] + incomingWeight[ix];
      var projectedW = wSoFar + proposedWeight;
      var addThis;
      if (MergingDigest$Companion_getInstance().useWeightLimit) {
        var q0 = wSoFar / this.totalWeight_0;
        var q2 = (wSoFar + proposedWeight) / this.totalWeight_0;
        var tmp$_2 = this.totalWeight_0;
        var a = this.scale.max_lu1900$(q0, normalizer);
        var b = this.scale.max_lu1900$(q2, normalizer);
        addThis = proposedWeight <= tmp$_2 * Math_0.min(a, b);
      }
       else {
        addThis = projectedW <= wLimit;
      }
      if (addThis) {
        this.weight_0[this.lastUsedCell_0] = this.weight_0[this.lastUsedCell_0] + incomingWeight[ix];
        this.mean_0[this.lastUsedCell_0] = this.mean_0[this.lastUsedCell_0] + (incomingMean[ix] - this.mean_0[this.lastUsedCell_0]) * incomingWeight[ix] / this.weight_0[this.lastUsedCell_0];
        incomingWeight[ix] = 0.0;
        if (this.data_0 != null) {
          while (ensureNotNull(this.data_0).size <= this.lastUsedCell_0) {
            ensureNotNull(this.data_0).add_11rb$(ArrayList_init());
          }
          mpassert(incomingData != null);
          mpassert(ensureNotNull(this.data_0).get_za3lpa$(this.lastUsedCell_0) !== ensureNotNull(incomingData).get_za3lpa$(ix));
          ensureNotNull(this.data_0).get_za3lpa$(this.lastUsedCell_0).addAll_brywnq$(ensureNotNull(incomingData).get_za3lpa$(ix));
        }
      }
       else {
        wSoFar += this.weight_0[this.lastUsedCell_0];
        if (!MergingDigest$Companion_getInstance().useWeightLimit) {
          k1 = this.scale.k_lu1900$(wSoFar / this.totalWeight_0, normalizer);
          wLimit = this.totalWeight_0 * this.scale.q_lu1900$(k1 + 1, normalizer);
        }
        this.lastUsedCell_0 = this.lastUsedCell_0 + 1 | 0;
        this.mean_0[this.lastUsedCell_0] = incomingMean[ix];
        this.weight_0[this.lastUsedCell_0] = incomingWeight[ix];
        incomingWeight[ix] = 0.0;
        if (this.data_0 != null) {
          mpassert(incomingData != null);
          mpassert(ensureNotNull(this.data_0).size === this.lastUsedCell_0);
          ensureNotNull(this.data_0).add_11rb$(ensureNotNull(incomingData).get_za3lpa$(ix));
        }
      }
    }
    this.lastUsedCell_0 = this.lastUsedCell_0 + 1 | 0;
    var sum = 0.0;
    tmp$_1 = this.lastUsedCell_0;
    for (var i_1 = 0; i_1 < tmp$_1; i_1++) {
      sum += this.weight_0[i_1];
    }
    mpassert(sum === this.totalWeight_0);
    if (runBackwards) {
      Sort_getInstance().reverse_6icyh1$(this.mean_0, 0, this.lastUsedCell_0);
      Sort_getInstance().reverse_6icyh1$(this.weight_0, 0, this.lastUsedCell_0);
      if (this.data_0 != null) {
        reverse(ensureNotNull(this.data_0));
      }
    }
    if (this.totalWeight_0 > 0) {
      var a_0 = this.min;
      var b_0 = this.mean_0[0];
      this.min = Math_0.min(a_0, b_0);
      var a_1 = this.max;
      var b_1 = this.mean_0[this.lastUsedCell_0 - 1 | 0];
      this.max = Math_0.max(a_1, b_1);
    }
  };
  MergingDigest.prototype.checkWeights_8be2vx$ = function () {
    return this.checkWeights_0(this.weight_0, this.totalWeight_0, this.lastUsedCell_0);
  };
  MergingDigest.prototype.checkWeights_0 = function (w, total, last) {
    var tmp$;
    var badCount = 0;
    var n = last;
    if (w[n] > 0) {
      n = n + 1 | 0;
    }
    var normalizer = this.scale.normalizer_lu1900$(this.publicCompression_0, this.totalWeight_0);
    var k1 = this.scale.k_lu1900$(0.0, normalizer);
    var q = 0.0;
    var left = 0.0;
    var header = '\n';
    tmp$ = n;
    for (var i = 0; i < tmp$; i++) {
      var dq = w[i] / total;
      var k2 = this.scale.k_lu1900$(q + dq, normalizer);
      q += dq / 2;
      if (k2 - k1 > 1 && w[i] !== 1.0) {
        println(header + 'Oversize centroid at ' + i + ', k0=' + k1 + ', k1=' + k2 + ', dk=' + (k2 - k1) + ', w=' + w[i] + ', q=' + q + ', dq=' + dq + ', left=' + left + ', current=' + w[i] + ' maxw=' + this.scale.max_lu1900$(q, normalizer));
        header = '';
        badCount = badCount + 1 | 0;
      }
      if (k2 - k1 > 4 && w[i] !== 1.0) {
        throw IllegalStateException_init('Egregiously oversized centroid at ' + i + ', k0=' + k1 + ', k1=' + k2 + ', dk=' + (k2 - k1) + ', w=' + w[i] + ', q=' + q + ', dq=' + dq + ', left=' + left + ', current=' + w[i] + ' maxw=' + this.scale.max_lu1900$(q, normalizer));
      }
      q += dq / 2;
      left += w[i];
      k1 = k2;
    }
    return badCount;
  };
  MergingDigest.prototype.compress = function () {
    this.mergeNewValues_0(true, this.publicCompression_0);
  };
  MergingDigest.prototype.size = function () {
    return Kotlin.Long.fromNumber(this.totalWeight_0 + this.unmergedWeight_0);
  };
  MergingDigest.prototype.cdf_14dthe$ = function (x) {
    var tmp$, tmp$_0, tmp$_1;
    this.mergeNewValues_0();
    if (this.lastUsedCell_0 === 0) {
      return kotlin_js_internal_DoubleCompanionObject.NaN;
    }
     else if (this.lastUsedCell_0 === 1) {
      var width = this.max - this.min;
      if (x < this.min) {
        tmp$ = 0.0;
      }
       else if (x > this.max) {
        tmp$ = 1.0;
      }
       else if (x - this.min <= width) {
        tmp$ = 0.5;
      }
       else {
        tmp$ = (x - this.min) / (this.max - this.min);
      }
      return tmp$;
    }
     else {
      var n = this.lastUsedCell_0;
      if (x < this.min) {
        return 0.0;
      }
      if (x > this.max) {
        return 1.0;
      }
      if (x < this.mean_0[0]) {
        if (this.mean_0[0] - this.min > 0) {
          if (x === this.min) {
            tmp$_0 = 0.5 / this.totalWeight_0;
          }
           else {
            tmp$_0 = (1 + (x - this.min) / (this.mean_0[0] - this.min) * (this.weight_0[0] / 2 - 1)) / this.totalWeight_0;
          }
        }
         else {
          tmp$_0 = 0.0;
        }
        return tmp$_0;
      }
      mpassert(x >= this.mean_0[0]);
      if (x > this.mean_0[n - 1 | 0]) {
        if (this.max - this.mean_0[n - 1 | 0] > 0) {
          if (x === this.max) {
            return 1 - 0.5 / this.totalWeight_0;
          }
           else {
            var dq = (1 + (this.max - x) / (this.max - this.mean_0[n - 1 | 0]) * (this.weight_0[n - 1 | 0] / 2 - 1)) / this.totalWeight_0;
            return 1 - dq;
          }
        }
         else {
          return 1.0;
        }
      }
      var weightSoFar = 0.0;
      var it = 0;
      while (it < (n - 1 | 0)) {
        if (this.mean_0[it] === x) {
          var dw = 0.0;
          while (it < n && this.mean_0[it] === x) {
            dw += this.weight_0[it];
            it = it + 1 | 0;
          }
          return (weightSoFar + dw / 2) / this.totalWeight_0;
        }
         else if (this.mean_0[it] <= x && x < this.mean_0[it + 1 | 0]) {
          if (this.mean_0[it + 1 | 0] - this.mean_0[it] > 0) {
            var leftExcludedW = 0.0;
            var rightExcludedW = 0.0;
            if (this.weight_0[it] === 1.0) {
              if (this.weight_0[it + 1 | 0] === 1.0) {
                return (weightSoFar + 1) / this.totalWeight_0;
              }
               else {
                leftExcludedW = 0.5;
              }
            }
             else if (this.weight_0[it + 1 | 0] === 1.0) {
              rightExcludedW = 0.5;
            }
            var dw_0 = (this.weight_0[it] + this.weight_0[it + 1 | 0]) / 2;
            mpassert(dw_0 > 1);
            mpassert(leftExcludedW + rightExcludedW <= 0.5);
            var left = this.mean_0[it];
            var right = this.mean_0[it + 1 | 0];
            var dwNoSingleton = dw_0 - leftExcludedW - rightExcludedW;
            mpassert(dwNoSingleton > dw_0 / 2);
            mpassert(right - left > 0);
            var base = weightSoFar + this.weight_0[it] / 2 + leftExcludedW;
            return (base + dwNoSingleton * (x - left) / (right - left)) / this.totalWeight_0;
          }
           else {
            var dw_1 = (this.weight_0[it] + this.weight_0[it + 1 | 0]) / 2;
            return (weightSoFar + dw_1) / this.totalWeight_0;
          }
        }
         else {
          weightSoFar += this.weight_0[it];
        }
        it = it + 1 | 0;
      }
      if (x === this.mean_0[n - 1 | 0]) {
        tmp$_1 = 1 - 0.5 / this.totalWeight_0;
      }
       else {
        throw IllegalStateException_init("Can't happen ... loop fell through");
      }
      return tmp$_1;
    }
  };
  MergingDigest.prototype.quantile = function (q) {
    var tmp$;
    if (q < 0 || q > 1) {
      throw IllegalArgumentException_init_0('q should be in [0,1], got ' + q);
    }
    this.mergeNewValues_0();
    if (this.lastUsedCell_0 === 0) {
      return kotlin_js_internal_DoubleCompanionObject.NaN;
    }
     else if (this.lastUsedCell_0 === 1) {
      return this.mean_0[0];
    }
    var n = this.lastUsedCell_0;
    var index = q * this.totalWeight_0;
    if (index < 1) {
      return this.min;
    }
    if (this.weight_0[0] > 1 && index < this.weight_0[0] / 2) {
      return this.min + (index - 1) / (this.weight_0[0] / 2 - 1) * (this.mean_0[0] - this.min);
    }
    if (index > this.totalWeight_0 - 1) {
      return this.max;
    }
    if (this.weight_0[n - 1 | 0] > 1 && this.totalWeight_0 - index <= this.weight_0[n - 1 | 0] / 2) {
      return this.max - (this.totalWeight_0 - index - 1.0) / (this.weight_0[n - 1 | 0] / 2 - 1) * (this.max - this.mean_0[n - 1 | 0]);
    }
    var weightSoFar = this.weight_0[0] / 2;
    tmp$ = n - 1 | 0;
    for (var i = 0; i < tmp$; i++) {
      var dw = (this.weight_0[i] + this.weight_0[i + 1 | 0]) / 2;
      if (weightSoFar + dw > index) {
        var leftUnit = 0.0;
        if (this.weight_0[i] === 1.0) {
          if (index - weightSoFar < 0.5) {
            return this.mean_0[i];
          }
           else {
            leftUnit = 0.5;
          }
        }
        var rightUnit = 0.0;
        if (this.weight_0[i + 1 | 0] === 1.0) {
          if (weightSoFar + dw - index <= 0.5) {
            return this.mean_0[i + 1 | 0];
          }
          rightUnit = 0.5;
        }
        var z1 = index - weightSoFar - leftUnit;
        var z2 = weightSoFar + dw - index - rightUnit;
        return AbstractTDigest$Companion_getInstance().weightedAverage_kn9dxl$(this.mean_0[i], z2, this.mean_0[i + 1 | 0], z1);
      }
      weightSoFar += dw;
    }
    mpassert(this.weight_0[n - 1 | 0] > 1);
    mpassert(index <= this.totalWeight_0);
    mpassert(index >= this.totalWeight_0 - this.weight_0[n - 1 | 0] / 2);
    var z1_0 = index - this.totalWeight_0 - this.weight_0[n - 1 | 0] / 2.0;
    var z2_0 = this.weight_0[n - 1 | 0] / 2 - z1_0;
    return AbstractTDigest$Companion_getInstance().weightedAverage_kn9dxl$(this.mean_0[n - 1 | 0], z1_0, this.max, z2_0);
  };
  MergingDigest.prototype.centroidCount = function () {
    this.mergeNewValues_0();
    return this.lastUsedCell_0;
  };
  function MergingDigest$centroids$ObjectLiteral(this$MergingDigest) {
    this.this$MergingDigest = this$MergingDigest;
    AbstractCollection.call(this);
  }
  function MergingDigest$centroids$ObjectLiteral$iterator$ObjectLiteral(this$MergingDigest) {
    this.this$MergingDigest = this$MergingDigest;
    this.i = 0;
  }
  MergingDigest$centroids$ObjectLiteral$iterator$ObjectLiteral.prototype.hasNext = function () {
    return this.i < this.this$MergingDigest.lastUsedCell_0;
  };
  MergingDigest$centroids$ObjectLiteral$iterator$ObjectLiteral.prototype.next = function () {
    var rc = Centroid_init_3(this.this$MergingDigest.mean_0[this.i], numberToInt(this.this$MergingDigest.weight_0[this.i]), this.this$MergingDigest.data_0 != null ? ensureNotNull(this.this$MergingDigest.data_0).get_za3lpa$(this.i) : null);
    this.i = this.i + 1 | 0;
    return rc;
  };
  MergingDigest$centroids$ObjectLiteral$iterator$ObjectLiteral.prototype.remove = function () {
    throw UnsupportedOperationException_init('Default operation');
  };
  MergingDigest$centroids$ObjectLiteral$iterator$ObjectLiteral.$metadata$ = {
    kind: Kind_CLASS,
    interfaces: [MutableIterator]
  };
  MergingDigest$centroids$ObjectLiteral.prototype.iterator = function () {
    return new MergingDigest$centroids$ObjectLiteral$iterator$ObjectLiteral(this.this$MergingDigest);
  };
  Object.defineProperty(MergingDigest$centroids$ObjectLiteral.prototype, 'size', {
    get: function () {
      return this.this$MergingDigest.lastUsedCell_0;
    }
  });
  MergingDigest$centroids$ObjectLiteral.$metadata$ = {
    kind: Kind_CLASS,
    interfaces: [AbstractCollection]
  };
  MergingDigest.prototype.centroids = function () {
    this.compress();
    return new MergingDigest$centroids$ObjectLiteral(this);
  };
  MergingDigest.prototype.compression = function () {
    return this.publicCompression_0;
  };
  MergingDigest.prototype.byteSize = function () {
    this.compress();
    return (this.lastUsedCell_0 * 16 | 0) + 32 | 0;
  };
  MergingDigest.prototype.smallByteSize = function () {
    this.compress();
    return (this.lastUsedCell_0 * 8 | 0) + 30 | 0;
  };
  function MergingDigest$Encoding(name, ordinal, code) {
    Enum.call(this);
    this.code = code;
    this.name$ = name;
    this.ordinal$ = ordinal;
  }
  function MergingDigest$Encoding_initFields() {
    MergingDigest$Encoding_initFields = function () {
    };
    MergingDigest$Encoding$VERBOSE_ENCODING_instance = new MergingDigest$Encoding('VERBOSE_ENCODING', 0, 1);
    MergingDigest$Encoding$SMALL_ENCODING_instance = new MergingDigest$Encoding('SMALL_ENCODING', 1, 2);
  }
  var MergingDigest$Encoding$VERBOSE_ENCODING_instance;
  function MergingDigest$Encoding$VERBOSE_ENCODING_getInstance() {
    MergingDigest$Encoding_initFields();
    return MergingDigest$Encoding$VERBOSE_ENCODING_instance;
  }
  var MergingDigest$Encoding$SMALL_ENCODING_instance;
  function MergingDigest$Encoding$SMALL_ENCODING_getInstance() {
    MergingDigest$Encoding_initFields();
    return MergingDigest$Encoding$SMALL_ENCODING_instance;
  }
  MergingDigest$Encoding.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Encoding',
    interfaces: [Enum]
  };
  function MergingDigest$Encoding$values() {
    return [MergingDigest$Encoding$VERBOSE_ENCODING_getInstance(), MergingDigest$Encoding$SMALL_ENCODING_getInstance()];
  }
  MergingDigest$Encoding.values = MergingDigest$Encoding$values;
  function MergingDigest$Encoding$valueOf(name) {
    switch (name) {
      case 'VERBOSE_ENCODING':
        return MergingDigest$Encoding$VERBOSE_ENCODING_getInstance();
      case 'SMALL_ENCODING':
        return MergingDigest$Encoding$SMALL_ENCODING_getInstance();
      default:throwISE('No enum constant com.tdunning.math.stats.MergingDigest.Encoding.' + name);
    }
  }
  MergingDigest$Encoding.valueOf_61zpoe$ = MergingDigest$Encoding$valueOf;
  MergingDigest.prototype.asBytes = function (buf) {
    var tmp$;
    this.compress();
    buf.writeInt_za3lpa$(MergingDigest$Encoding$VERBOSE_ENCODING_getInstance().code);
    buf.writeDouble_14dthe$(this.min);
    buf.writeDouble_14dthe$(this.max);
    buf.writeDouble_14dthe$(this.publicCompression_0);
    buf.writeInt_za3lpa$(this.lastUsedCell_0);
    tmp$ = this.lastUsedCell_0;
    for (var i = 0; i < tmp$; i++) {
      buf.writeDouble_14dthe$(this.weight_0[i]);
      buf.writeDouble_14dthe$(this.mean_0[i]);
    }
  };
  MergingDigest.prototype.asSmallBytes = function (buf) {
    var tmp$;
    this.compress();
    buf.writeInt_za3lpa$(MergingDigest$Encoding$SMALL_ENCODING_getInstance().code);
    buf.writeDouble_14dthe$(this.min);
    buf.writeDouble_14dthe$(this.max);
    buf.writeFloat_mx4ult$(this.publicCompression_0);
    buf.writeShort_mq22fl$(toShort(this.mean_0.length));
    buf.writeShort_mq22fl$(toShort(this.tempMean_0.length));
    buf.writeShort_mq22fl$(toShort(this.lastUsedCell_0));
    tmp$ = this.lastUsedCell_0;
    for (var i = 0; i < tmp$; i++) {
      buf.writeFloat_mx4ult$(this.weight_0[i]);
      buf.writeFloat_mx4ult$(this.mean_0[i]);
    }
  };
  MergingDigest.prototype.toString = function () {
    return 'MergingDigest' + '-' + toString(this.scaleFunction) + '-' + (MergingDigest$Companion_getInstance().useWeightLimit ? 'weight' : 'kSize') + '-' + (this.useAlternatingSort ? 'alternating' : 'stable') + '-' + (this.useTwoLevelCompression ? 'twoLevel' : 'oneLevel');
  };
  function MergingDigest$Companion() {
    MergingDigest$Companion_instance = this;
    this.useWeightLimit = true;
  }
  MergingDigest$Companion.prototype.fromBytes = function (buf) {
    var tmp$;
    var encoding = buf.readInt();
    if (encoding === MergingDigest$Encoding$VERBOSE_ENCODING_getInstance().code) {
      var min = buf.readDouble();
      var max = buf.readDouble();
      var compression = buf.readDouble();
      var n = buf.readInt();
      var r = new MergingDigest(compression);
      r.setMinMax_sdh6z7$(min, max);
      r.lastUsedCell_0 = n;
      for (var i = 0; i < n; i++) {
        r.weight_0[i] = buf.readDouble();
        r.mean_0[i] = buf.readDouble();
        r.totalWeight_0 = r.totalWeight_0 + r.weight_0[i];
      }
      return r;
    }
     else if (encoding === MergingDigest$Encoding$SMALL_ENCODING_getInstance().code) {
      var min_0 = buf.readDouble();
      var max_0 = buf.readDouble();
      var compression_0 = buf.readFloat();
      var n_0 = buf.readShort();
      var bufferSize = buf.readShort();
      var r_0 = new MergingDigest(compression_0, bufferSize, n_0);
      r_0.setMinMax_sdh6z7$(min_0, max_0);
      r_0.lastUsedCell_0 = buf.readShort();
      tmp$ = r_0.lastUsedCell_0;
      for (var i_0 = 0; i_0 < tmp$; i_0++) {
        r_0.weight_0[i_0] = buf.readFloat();
        r_0.mean_0[i_0] = buf.readFloat();
        r_0.totalWeight_0 = r_0.totalWeight_0 + r_0.weight_0[i_0];
      }
      return r_0;
    }
     else {
      throw IllegalStateException_init('Invalid format for serialized histogram');
    }
  };
  MergingDigest$Companion.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Companion',
    interfaces: []
  };
  var MergingDigest$Companion_instance = null;
  function MergingDigest$Companion_getInstance() {
    if (MergingDigest$Companion_instance === null) {
      new MergingDigest$Companion();
    }
    return MergingDigest$Companion_instance;
  }
  MergingDigest.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'MergingDigest',
    interfaces: [AbstractTDigest]
  };
  function ScaleFunction(name, ordinal) {
    Enum.call(this);
    this.name$ = name;
    this.ordinal$ = ordinal;
  }
  function ScaleFunction_initFields() {
    ScaleFunction_initFields = function () {
    };
    new ScaleFunction$K_0();
    new ScaleFunction$K_1();
    new ScaleFunction$K_1_FAST();
    new ScaleFunction$K_2();
    new ScaleFunction$K_3();
    new ScaleFunction$K_2_NO_NORM();
    new ScaleFunction$K_3_NO_NORM();
    ScaleFunction$Companion_getInstance();
  }
  function ScaleFunction$K_0() {
    ScaleFunction$K_0_instance = this;
    ScaleFunction.call(this, 'K_0', 0);
  }
  ScaleFunction$K_0.prototype.k_yvo9jy$ = function (q, compression, n) {
    return compression * q / 2;
  };
  ScaleFunction$K_0.prototype.k_lu1900$ = function (q, normalizer) {
    return normalizer * q;
  };
  ScaleFunction$K_0.prototype.q_yvo9jy$ = function (k, compression, n) {
    return 2 * k / compression;
  };
  ScaleFunction$K_0.prototype.q_lu1900$ = function (k, normalizer) {
    return k / normalizer;
  };
  ScaleFunction$K_0.prototype.max_yvo9jy$ = function (q, compression, n) {
    return 2 / compression;
  };
  ScaleFunction$K_0.prototype.max_lu1900$ = function (q, normalizer) {
    return 1 / normalizer;
  };
  ScaleFunction$K_0.prototype.normalizer_lu1900$ = function (compression, n) {
    return compression / 2;
  };
  ScaleFunction$K_0.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'K_0',
    interfaces: [ScaleFunction]
  };
  var ScaleFunction$K_0_instance = null;
  function ScaleFunction$K_0_getInstance() {
    ScaleFunction_initFields();
    return ScaleFunction$K_0_instance;
  }
  function ScaleFunction$K_1() {
    ScaleFunction$K_1_instance = this;
    ScaleFunction.call(this, 'K_1', 1);
  }
  ScaleFunction$K_1.prototype.k_yvo9jy$ = function (q, compression, n) {
    var x = 2 * q - 1;
    return compression * Math_0.asin(x) / (2 * math.PI);
  };
  ScaleFunction$K_1.prototype.k_lu1900$ = function (q, normalizer) {
    var x = 2 * q - 1;
    return normalizer * Math_0.asin(x);
  };
  ScaleFunction$K_1.prototype.q_yvo9jy$ = function (k, compression, n) {
    var x = k * (2 * math.PI / compression);
    return (Math_0.sin(x) + 1) / 2;
  };
  ScaleFunction$K_1.prototype.q_lu1900$ = function (k, normalizer) {
    var x = k / normalizer;
    return (Math_0.sin(x) + 1) / 2;
  };
  ScaleFunction$K_1.prototype.max_yvo9jy$ = function (q, compression, n) {
    var tmp$;
    if (q <= 0) {
      tmp$ = 0.0;
    }
     else if (q >= 1) {
      tmp$ = 0.0;
    }
     else {
      var x = math.PI / compression;
      var tmp$_0 = 2 * Math_0.sin(x);
      var x_0 = q * (1 - q);
      tmp$ = tmp$_0 * Math_0.sqrt(x_0);
    }
    return tmp$;
  };
  ScaleFunction$K_1.prototype.max_lu1900$ = function (q, normalizer) {
    var tmp$;
    if (q <= 0) {
      tmp$ = 0.0;
    }
     else if (q >= 1) {
      tmp$ = 0.0;
    }
     else {
      var x = 0.5 / normalizer;
      var tmp$_0 = 2 * Math_0.sin(x);
      var x_0 = q * (1 - q);
      tmp$ = tmp$_0 * Math_0.sqrt(x_0);
    }
    return tmp$;
  };
  ScaleFunction$K_1.prototype.normalizer_lu1900$ = function (compression, n) {
    return compression / (2 * math.PI);
  };
  ScaleFunction$K_1.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'K_1',
    interfaces: [ScaleFunction]
  };
  var ScaleFunction$K_1_instance = null;
  function ScaleFunction$K_1_getInstance() {
    ScaleFunction_initFields();
    return ScaleFunction$K_1_instance;
  }
  function ScaleFunction$K_1_FAST() {
    ScaleFunction$K_1_FAST_instance = this;
    ScaleFunction.call(this, 'K_1_FAST', 2);
  }
  ScaleFunction$K_1_FAST.prototype.k_yvo9jy$ = function (q, compression, n) {
    return compression * ScaleFunction$Companion_getInstance().fastAsin_tq0o01$(2 * q - 1) / (2 * math.PI);
  };
  ScaleFunction$K_1_FAST.prototype.k_lu1900$ = function (q, normalizer) {
    return normalizer * ScaleFunction$Companion_getInstance().fastAsin_tq0o01$(2 * q - 1);
  };
  ScaleFunction$K_1_FAST.prototype.q_yvo9jy$ = function (k, compression, n) {
    var x = k * (2 * math.PI / compression);
    return (Math_0.sin(x) + 1) / 2;
  };
  ScaleFunction$K_1_FAST.prototype.q_lu1900$ = function (k, normalizer) {
    var x = k / normalizer;
    return (Math_0.sin(x) + 1) / 2;
  };
  ScaleFunction$K_1_FAST.prototype.max_yvo9jy$ = function (q, compression, n) {
    var tmp$;
    if (q <= 0) {
      tmp$ = 0.0;
    }
     else if (q >= 1) {
      tmp$ = 0.0;
    }
     else {
      var x = math.PI / compression;
      var tmp$_0 = 2 * Math_0.sin(x);
      var x_0 = q * (1 - q);
      tmp$ = tmp$_0 * Math_0.sqrt(x_0);
    }
    return tmp$;
  };
  ScaleFunction$K_1_FAST.prototype.max_lu1900$ = function (q, normalizer) {
    var tmp$;
    if (q <= 0) {
      tmp$ = 0.0;
    }
     else if (q >= 1) {
      tmp$ = 0.0;
    }
     else {
      var x = 0.5 / normalizer;
      var tmp$_0 = 2 * Math_0.sin(x);
      var x_0 = q * (1 - q);
      tmp$ = tmp$_0 * Math_0.sqrt(x_0);
    }
    return tmp$;
  };
  ScaleFunction$K_1_FAST.prototype.normalizer_lu1900$ = function (compression, n) {
    return compression / (2 * math.PI);
  };
  ScaleFunction$K_1_FAST.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'K_1_FAST',
    interfaces: [ScaleFunction]
  };
  var ScaleFunction$K_1_FAST_instance = null;
  function ScaleFunction$K_1_FAST_getInstance() {
    ScaleFunction_initFields();
    return ScaleFunction$K_1_FAST_instance;
  }
  function ScaleFunction$K_2() {
    ScaleFunction$K_2_instance = this;
    ScaleFunction.call(this, 'K_2', 3);
  }
  ScaleFunction$K_2.prototype.k_yvo9jy$ = function (q, compression, n) {
    var tmp$, tmp$_0;
    if (n <= 1) {
      if (q <= 0) {
        tmp$ = -10.0;
      }
       else if (q >= 1) {
        tmp$ = 10.0;
      }
       else {
        tmp$ = 0.0;
      }
      return tmp$;
    }
    if (q === 0.0) {
      tmp$_0 = 2 * this.k_yvo9jy$(1 / n, compression, n);
    }
     else if (q === 1.0) {
      tmp$_0 = 2 * this.k_yvo9jy$((n - 1) / n, compression, n);
    }
     else {
      var x = q / (1 - q);
      tmp$_0 = compression * Math_0.log(x) / this.Z_0(compression, n);
    }
    return tmp$_0;
  };
  ScaleFunction$K_2.prototype.k_lu1900$ = function (q, normalizer) {
    var tmp$;
    if (q < 1.0E-15) {
      tmp$ = 2 * this.k_lu1900$(1.0E-15, normalizer);
    }
     else if (q > 1 - 1.0E-15) {
      tmp$ = 2 * this.k_lu1900$(1 - 1.0E-15, normalizer);
    }
     else {
      var x = q / (1 - q);
      tmp$ = Math_0.log(x) * normalizer;
    }
    return tmp$;
  };
  ScaleFunction$K_2.prototype.q_yvo9jy$ = function (k, compression, n) {
    var x = k * this.Z_0(compression, n) / compression;
    var w = Math_0.exp(x);
    return w / (1 + w);
  };
  ScaleFunction$K_2.prototype.q_lu1900$ = function (k, normalizer) {
    var x = k / normalizer;
    var w = Math_0.exp(x);
    return w / (1 + w);
  };
  ScaleFunction$K_2.prototype.max_yvo9jy$ = function (q, compression, n) {
    return this.Z_0(compression, n) * q * (1 - q) / compression;
  };
  ScaleFunction$K_2.prototype.max_lu1900$ = function (q, normalizer) {
    return q * (1 - q) / normalizer;
  };
  ScaleFunction$K_2.prototype.normalizer_lu1900$ = function (compression, n) {
    return compression / this.Z_0(compression, n);
  };
  ScaleFunction$K_2.prototype.Z_0 = function (compression, n) {
    var x = n / compression;
    return 4 * Math_0.log(x) + 24;
  };
  ScaleFunction$K_2.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'K_2',
    interfaces: [ScaleFunction]
  };
  var ScaleFunction$K_2_instance = null;
  function ScaleFunction$K_2_getInstance() {
    ScaleFunction_initFields();
    return ScaleFunction$K_2_instance;
  }
  function ScaleFunction$K_3() {
    ScaleFunction$K_3_instance = this;
    ScaleFunction.call(this, 'K_3', 4);
  }
  ScaleFunction$K_3.prototype.k_yvo9jy$ = function (q, compression, n) {
    var tmp$;
    if (q < 0.9 / n) {
      tmp$ = 10 * this.k_yvo9jy$(1 / n, compression, n);
    }
     else if (q > 1 - 0.9 / n) {
      tmp$ = 10 * this.k_yvo9jy$((n - 1) / n, compression, n);
    }
     else {
      if (q <= 0.5) {
        var x = 2 * q;
        tmp$ = compression * Math_0.log(x) / this.Z_0(compression, n);
      }
       else {
        tmp$ = -this.k_yvo9jy$(1 - q, compression, n);
      }
    }
    return tmp$;
  };
  ScaleFunction$K_3.prototype.k_lu1900$ = function (q, normalizer) {
    var tmp$;
    if (q < 1.0E-15) {
      tmp$ = 10 * this.k_lu1900$(1.0E-15, normalizer);
    }
     else if (q > 1 - 1.0E-15) {
      tmp$ = 10 * this.k_lu1900$(1 - 1.0E-15, normalizer);
    }
     else {
      if (q <= 0.5) {
        var x = 2 * q;
        tmp$ = Math_0.log(x) / normalizer;
      }
       else {
        tmp$ = -this.k_lu1900$(1 - q, normalizer);
      }
    }
    return tmp$;
  };
  ScaleFunction$K_3.prototype.q_yvo9jy$ = function (k, compression, n) {
    var tmp$;
    if (k <= 0) {
      var x = k * this.Z_0(compression, n) / compression;
      tmp$ = Math_0.exp(x) / 2;
    }
     else {
      tmp$ = 1 - this.q_yvo9jy$(-k, compression, n);
    }
    return tmp$;
  };
  ScaleFunction$K_3.prototype.q_lu1900$ = function (k, normalizer) {
    var tmp$;
    if (k <= 0) {
      var x = k / normalizer;
      tmp$ = Math_0.exp(x) / 2;
    }
     else {
      tmp$ = 1 - this.q_lu1900$(-k, normalizer);
    }
    return tmp$;
  };
  ScaleFunction$K_3.prototype.max_yvo9jy$ = function (q, compression, n) {
    var tmp$ = this.Z_0(compression, n);
    var b = 1 - q;
    return tmp$ * Math_0.min(q, b) / compression;
  };
  ScaleFunction$K_3.prototype.max_lu1900$ = function (q, normalizer) {
    var b = 1 - q;
    return Math_0.min(q, b) / normalizer;
  };
  ScaleFunction$K_3.prototype.normalizer_lu1900$ = function (compression, n) {
    return compression / this.Z_0(compression, n);
  };
  ScaleFunction$K_3.prototype.Z_0 = function (compression, n) {
    var x = n / compression;
    return 4 * Math_0.log(x) + 21;
  };
  ScaleFunction$K_3.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'K_3',
    interfaces: [ScaleFunction]
  };
  var ScaleFunction$K_3_instance = null;
  function ScaleFunction$K_3_getInstance() {
    ScaleFunction_initFields();
    return ScaleFunction$K_3_instance;
  }
  function ScaleFunction$K_2_NO_NORM() {
    ScaleFunction$K_2_NO_NORM_instance = this;
    ScaleFunction.call(this, 'K_2_NO_NORM', 5);
  }
  ScaleFunction$K_2_NO_NORM.prototype.k_yvo9jy$ = function (q, compression, n) {
    var tmp$;
    if (q === 0.0) {
      tmp$ = 2 * this.k_yvo9jy$(1 / n, compression, n);
    }
     else if (q === 1.0) {
      tmp$ = 2 * this.k_yvo9jy$((n - 1) / n, compression, n);
    }
     else {
      var x = q / (1 - q);
      tmp$ = compression * Math_0.log(x);
    }
    return tmp$;
  };
  ScaleFunction$K_2_NO_NORM.prototype.k_lu1900$ = function (q, normalizer) {
    var tmp$;
    if (q <= 1.0E-15) {
      tmp$ = 2 * this.k_lu1900$(1.0E-15, normalizer);
    }
     else if (q >= 1 - 1.0E-15) {
      tmp$ = 2 * this.k_lu1900$(1 - 1.0E-15, normalizer);
    }
     else {
      var x = q / (1 - q);
      tmp$ = normalizer * Math_0.log(x);
    }
    return tmp$;
  };
  ScaleFunction$K_2_NO_NORM.prototype.q_yvo9jy$ = function (k, compression, n) {
    var x = k / compression;
    var w = Math_0.exp(x);
    return w / (1 + w);
  };
  ScaleFunction$K_2_NO_NORM.prototype.q_lu1900$ = function (k, normalizer) {
    var x = k / normalizer;
    var w = Math_0.exp(x);
    return w / (1 + w);
  };
  ScaleFunction$K_2_NO_NORM.prototype.max_yvo9jy$ = function (q, compression, n) {
    return q * (1 - q) / compression;
  };
  ScaleFunction$K_2_NO_NORM.prototype.max_lu1900$ = function (q, normalizer) {
    return q * (1 - q) / normalizer;
  };
  ScaleFunction$K_2_NO_NORM.prototype.normalizer_lu1900$ = function (compression, n) {
    return compression;
  };
  ScaleFunction$K_2_NO_NORM.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'K_2_NO_NORM',
    interfaces: [ScaleFunction]
  };
  var ScaleFunction$K_2_NO_NORM_instance = null;
  function ScaleFunction$K_2_NO_NORM_getInstance() {
    ScaleFunction_initFields();
    return ScaleFunction$K_2_NO_NORM_instance;
  }
  function ScaleFunction$K_3_NO_NORM() {
    ScaleFunction$K_3_NO_NORM_instance = this;
    ScaleFunction.call(this, 'K_3_NO_NORM', 6);
  }
  ScaleFunction$K_3_NO_NORM.prototype.k_yvo9jy$ = function (q, compression, n) {
    var tmp$;
    if (q < 0.9 / n) {
      tmp$ = 10 * this.k_yvo9jy$(1 / n, compression, n);
    }
     else if (q > 1 - 0.9 / n) {
      tmp$ = 10 * this.k_yvo9jy$((n - 1) / n, compression, n);
    }
     else {
      if (q <= 0.5) {
        var x = 2 * q;
        tmp$ = compression * Math_0.log(x);
      }
       else {
        tmp$ = -this.k_yvo9jy$(1 - q, compression, n);
      }
    }
    return tmp$;
  };
  ScaleFunction$K_3_NO_NORM.prototype.k_lu1900$ = function (q, normalizer) {
    var tmp$;
    if (q <= 1.0E-15) {
      tmp$ = 10 * this.k_lu1900$(1.0E-15, normalizer);
    }
     else if (q > 1 - 1.0E-15) {
      tmp$ = 10 * this.k_lu1900$(1 - 1.0E-15, normalizer);
    }
     else {
      if (q <= 0.5) {
        var x = 2 * q;
        tmp$ = normalizer * Math_0.log(x);
      }
       else {
        tmp$ = -this.k_lu1900$(1 - q, normalizer);
      }
    }
    return tmp$;
  };
  ScaleFunction$K_3_NO_NORM.prototype.q_yvo9jy$ = function (k, compression, n) {
    var tmp$;
    if (k <= 0) {
      var x = k / compression;
      tmp$ = Math_0.exp(x) / 2;
    }
     else {
      tmp$ = 1 - this.q_yvo9jy$(-k, compression, n);
    }
    return tmp$;
  };
  ScaleFunction$K_3_NO_NORM.prototype.q_lu1900$ = function (k, normalizer) {
    var tmp$;
    if (k <= 0) {
      var x = k / normalizer;
      tmp$ = Math_0.exp(x) / 2;
    }
     else {
      tmp$ = 1 - this.q_lu1900$(-k, normalizer);
    }
    return tmp$;
  };
  ScaleFunction$K_3_NO_NORM.prototype.max_yvo9jy$ = function (q, compression, n) {
    var b = 1 - q;
    return Math_0.min(q, b) / compression;
  };
  ScaleFunction$K_3_NO_NORM.prototype.max_lu1900$ = function (q, normalizer) {
    var b = 1 - q;
    return Math_0.min(q, b) / normalizer;
  };
  ScaleFunction$K_3_NO_NORM.prototype.normalizer_lu1900$ = function (compression, n) {
    return compression;
  };
  ScaleFunction$K_3_NO_NORM.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'K_3_NO_NORM',
    interfaces: [ScaleFunction]
  };
  var ScaleFunction$K_3_NO_NORM_instance = null;
  function ScaleFunction$K_3_NO_NORM_getInstance() {
    ScaleFunction_initFields();
    return ScaleFunction$K_3_NO_NORM_instance;
  }
  function ScaleFunction$Companion() {
    ScaleFunction$Companion_instance = this;
  }
  ScaleFunction$Companion.prototype.fastAsin_tq0o01$ = function (x) {
    if (x < 0) {
      return -this.fastAsin_tq0o01$(-x);
    }
     else if (x > 1) {
      return kotlin_js_internal_DoubleCompanionObject.NaN;
    }
     else {
      var c0High = 0.1;
      var c1High = 0.55;
      var c2Low = 0.5;
      var c2High = 0.8;
      var c3Low = 0.75;
      var c3High = 0.9;
      var c4Low = 0.87;
      if (x > c3High) {
        return Math_0.asin(x);
      }
       else {
        var m0 = new Float64Array([0.2955302411, 1.2221903614, 0.1488583743, 0.2422015816, -0.3688700895, 0.0733398445]);
        var m1 = new Float64Array([-0.043099192, 0.959403575, -0.0362312299, 0.1204623351, 0.045702962, -0.0026025285]);
        var m2 = new Float64Array([-0.034873933724, 1.054796752703, -0.194127063385, 0.283963735636, 0.023800124916, -8.72727381E-4]);
        var m3 = new Float64Array([-0.37588391875, 2.61991859025, -2.48835406886, 1.48605387425, 0.00857627492, -1.5802871E-4]);
        var vars = new Float64Array([1.0, x, x * x, x * x * x, 1 / (1 - x), 1.0 / (1 - x) / (1 - x)]);
        var x0 = this.bound_0((c0High - x) / c0High);
        var x1 = this.bound_0((c1High - x) / (c1High - c2Low));
        var x2 = this.bound_0((c2High - x) / (c2High - c3Low));
        var x3 = this.bound_0((c3High - x) / (c3High - c4Low));
        var mix1 = (1 - x0) * x1;
        var mix2 = (1 - x1) * x2;
        var mix3 = (1 - x2) * x3;
        var mix4 = 1 - x3;
        var r = 0.0;
        if (x0 > 0) {
          r += x0 * this.eval_0(m0, vars);
        }
        if (mix1 > 0) {
          r += mix1 * this.eval_0(m1, vars);
        }
        if (mix2 > 0) {
          r += mix2 * this.eval_0(m2, vars);
        }
        if (mix3 > 0) {
          r += mix3 * this.eval_0(m3, vars);
        }
        if (mix4 > 0) {
          r += mix4 * Math_0.asin(x);
        }
        return r;
      }
    }
  };
  ScaleFunction$Companion.prototype.eval_0 = function (model, vars) {
    var r = 0.0;
    for (var i = 0; i !== model.length; ++i) {
      r += model[i] * vars[i];
    }
    return r;
  };
  ScaleFunction$Companion.prototype.bound_0 = function (v) {
    var tmp$;
    if (v <= 0) {
      tmp$ = 0.0;
    }
     else if (v >= 1) {
      tmp$ = 1.0;
    }
     else {
      tmp$ = v;
    }
    return tmp$;
  };
  ScaleFunction$Companion.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Companion',
    interfaces: []
  };
  var ScaleFunction$Companion_instance = null;
  function ScaleFunction$Companion_getInstance() {
    ScaleFunction_initFields();
    if (ScaleFunction$Companion_instance === null) {
      new ScaleFunction$Companion();
    }
    return ScaleFunction$Companion_instance;
  }
  ScaleFunction.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'ScaleFunction',
    interfaces: [Enum]
  };
  function ScaleFunction$values() {
    return [ScaleFunction$K_0_getInstance(), ScaleFunction$K_1_getInstance(), ScaleFunction$K_1_FAST_getInstance(), ScaleFunction$K_2_getInstance(), ScaleFunction$K_3_getInstance(), ScaleFunction$K_2_NO_NORM_getInstance(), ScaleFunction$K_3_NO_NORM_getInstance()];
  }
  ScaleFunction.values = ScaleFunction$values;
  function ScaleFunction$valueOf(name) {
    switch (name) {
      case 'K_0':
        return ScaleFunction$K_0_getInstance();
      case 'K_1':
        return ScaleFunction$K_1_getInstance();
      case 'K_1_FAST':
        return ScaleFunction$K_1_FAST_getInstance();
      case 'K_2':
        return ScaleFunction$K_2_getInstance();
      case 'K_3':
        return ScaleFunction$K_3_getInstance();
      case 'K_2_NO_NORM':
        return ScaleFunction$K_2_NO_NORM_getInstance();
      case 'K_3_NO_NORM':
        return ScaleFunction$K_3_NO_NORM_getInstance();
      default:throwISE('No enum constant com.tdunning.math.stats.ScaleFunction.' + name);
    }
  }
  ScaleFunction.valueOf_61zpoe$ = ScaleFunction$valueOf;
  function Sort() {
    Sort_instance = this;
    this.prng_0 = Random(0);
  }
  Sort.prototype.sort_kbza6$ = function (order, values, n) {
    this.sort_f11c34$(order, values, 0, n);
  };
  Sort.prototype.sort_f11c34$ = function (order, values, start, n) {
    if (start === void 0)
      start = 0;
    if (n === void 0)
      n = values.length;
    var tmp$;
    tmp$ = start + n | 0;
    for (var i = start; i < tmp$; i++) {
      order[i] = i;
    }
    this.quickSort_0(order, values, start, start + n | 0, 64);
    this.insertionSort_0(order, values, start, start + n | 0, 64);
  };
  Sort.prototype.quickSort_0 = function (order, values, start, end, limit) {
    var tmp$, tmp$_0;
    var start_0 = start;
    var end_0 = end;
    while ((end_0 - start_0 | 0) > limit) {
      var pivotIndex = start_0 + this.prng_0.nextInt_za3lpa$(end_0 - start_0 | 0) | 0;
      var pivotValue = values[order[pivotIndex]];
      this.swap_0(order, start_0, pivotIndex);
      var low = start_0 + 1 | 0;
      var high = end_0;
      var i = low;
      while (i < high) {
        var vi = values[order[i]];
        if (vi === pivotValue) {
          if (low !== i) {
            this.swap_0(order, low, i);
          }
           else {
            i = i + 1 | 0;
          }
          low = low + 1 | 0;
        }
         else if (vi > pivotValue) {
          high = high - 1 | 0;
          this.swap_0(order, i, high);
        }
         else {
          i = i + 1 | 0;
        }
      }
      var from = start_0;
      var to = high - 1 | 0;
      i = 0;
      while (from < low && to >= low) {
        this.swap_0(order, (tmp$ = from, from = tmp$ + 1 | 0, tmp$), (tmp$_0 = to, to = tmp$_0 - 1 | 0, tmp$_0));
        i = i + 1 | 0;
      }
      if (from === low) {
        low = to + 1 | 0;
      }
       else {
        low = from;
      }
      if ((low - start_0 | 0) < (end_0 - high | 0)) {
        this.quickSort_0(order, values, start_0, low, limit);
        start_0 = high;
      }
       else {
        this.quickSort_0(order, values, high, end_0, limit);
        end_0 = low;
      }
    }
  };
  Sort.prototype.sort_808vjj$ = function (key, values) {
    this.sort_tgjelr$(key, 0, key.length, values.slice());
  };
  Sort.prototype.sort_tgjelr$ = function (key, start, n, values) {
    this.quickSort_1(key, values, start, start + n | 0, 8);
    this.insertionSort_1(key, values, start, start + n | 0, 8);
  };
  Sort.prototype.quickSort_1 = function (key, values, start, end, limit) {
    var tmp$, tmp$_0;
    var start_0 = start;
    var end_0 = end;
    while ((end_0 - start_0 | 0) > limit) {
      var a = start_0;
      var b = (start_0 + end_0 | 0) / 2 | 0;
      var c = end_0 - 1 | 0;
      var pivotIndex;
      var pivotValue;
      var va = key[a];
      var vb = key[b];
      var vc = key[c];
      if (va > vb) {
        if (vc > va) {
          pivotIndex = a;
          pivotValue = va;
        }
         else {
          if (vc < vb) {
            pivotIndex = b;
            pivotValue = vb;
          }
           else {
            pivotIndex = c;
            pivotValue = vc;
          }
        }
      }
       else {
        if (vc > vb) {
          pivotIndex = b;
          pivotValue = vb;
        }
         else {
          if (vc < va) {
            pivotIndex = a;
            pivotValue = va;
          }
           else {
            pivotIndex = c;
            pivotValue = vc;
          }
        }
      }
      this.swap_1(start_0, pivotIndex, key, values.slice());
      var low = start_0 + 1 | 0;
      var high = end_0;
      var i = low;
      while (i < high) {
        var vi = key[i];
        if (vi === pivotValue) {
          if (low !== i) {
            this.swap_1(low, i, key, values.slice());
          }
           else {
            i = i + 1 | 0;
          }
          low = low + 1 | 0;
        }
         else if (vi > pivotValue) {
          high = high - 1 | 0;
          this.swap_1(i, high, key, values.slice());
        }
         else {
          i = i + 1 | 0;
        }
      }
      var from = start_0;
      var to = high - 1 | 0;
      i = 0;
      while (from < low && to >= low) {
        this.swap_1((tmp$ = from, from = tmp$ + 1 | 0, tmp$), (tmp$_0 = to, to = tmp$_0 - 1 | 0, tmp$_0), key, values.slice());
        i = i + 1 | 0;
      }
      if (from === low) {
        low = to + 1 | 0;
      }
       else {
        low = from;
      }
      if ((low - start_0 | 0) < (end_0 - high | 0)) {
        this.quickSort_1(key, values, start_0, low, limit);
        start_0 = high;
      }
       else {
        this.quickSort_1(key, values, high, end_0, limit);
        end_0 = low;
      }
    }
  };
  Sort.prototype.insertionSort_1 = function (key, values, start, end, limit) {
    var tmp$;
    for (var i = start + 1 | 0; i < end; i++) {
      var v = key[i];
      var a = i - limit | 0;
      var m = Math_0.max(a, start);
      for (var j = i; j >= m; j--) {
        if (j === m || key[j - 1 | 0] <= v) {
          if (j < i) {
            Utils_getInstance().arraycopy_dgpv4k$(key, j, key, j + 1 | 0, i - j | 0);
            key[j] = v;
            for (tmp$ = 0; tmp$ !== values.length; ++tmp$) {
              var value = values[tmp$];
              var tmp = value[i];
              Utils_getInstance().arraycopy_dgpv4k$(value, j, value, j + 1 | 0, i - j | 0);
              value[j] = tmp;
            }
          }
          break;
        }
      }
    }
  };
  Sort.prototype.swap_0 = function (order, i, j) {
    var t = order[i];
    order[i] = order[j];
    order[j] = t;
  };
  Sort.prototype.swap_1 = function (i, j, key, values) {
    var t = key[i];
    key[i] = key[j];
    key[j] = t;
    for (var k = 0; k !== values.length; ++k) {
      t = values[k][i];
      values[k][i] = values[k][j];
      values[k][j] = t;
    }
  };
  Sort.prototype.checkPartition_bmjou6$ = function (order, values, pivotValue, start, low, high, end) {
    if (order.length !== values.length) {
      throw IllegalArgumentException_init_0('Arguments must be same size');
    }
    if (!(start >= 0 && low >= start && high >= low && end >= high)) {
      throw IllegalArgumentException_init_0('Invalid indices ' + start + ', ' + low + ', ' + high + ', ' + end);
    }
    for (var i = 0; i < low; i++) {
      var v = values[order[i]];
      if (v >= pivotValue) {
        throw IllegalArgumentException_init_0('Value greater than pivot at ' + i);
      }
    }
    for (var i_0 = low; i_0 < high; i_0++) {
      if (values[order[i_0]] !== pivotValue) {
        throw IllegalArgumentException_init_0('Non-pivot at ' + i_0);
      }
    }
    for (var i_1 = high; i_1 < end; i_1++) {
      var v_0 = values[order[i_1]];
      if (v_0 <= pivotValue) {
        throw IllegalArgumentException_init_0('Value less than pivot at ' + i_1);
      }
    }
  };
  Sort.prototype.insertionSort_0 = function (order, values, start, n, limit) {
    for (var i = start + 1 | 0; i < n; i++) {
      var t = order[i];
      var v = values[order[i]];
      var a = i - limit | 0;
      var m = Math_0.max(a, start);
      for (var j = i; j >= m; j--) {
        if (j === 0 || values[order[j - 1 | 0]] <= v) {
          if (j < i) {
            Utils_getInstance().arraycopy_lvhpry$(order, j, order, j + 1 | 0, i - j | 0);
            order[j] = t;
          }
          break;
        }
      }
    }
  };
  Sort.prototype.reverse_nd5v6f$ = function (order, offset, length) {
    if (offset === void 0)
      offset = 0;
    if (length === void 0)
      length = order.length;
    var tmp$;
    tmp$ = length / 2 | 0;
    for (var i = 0; i < tmp$; i++) {
      var t = order[offset + i | 0];
      order[offset + i | 0] = order[offset + length - i - 1 | 0];
      order[offset + length - i - 1 | 0] = t;
    }
  };
  Sort.prototype.reverse_6icyh1$ = function (order, offset, length) {
    var tmp$;
    tmp$ = length / 2 | 0;
    for (var i = 0; i < tmp$; i++) {
      var t = order[offset + i | 0];
      order[offset + i | 0] = order[offset + length - i - 1 | 0];
      order[offset + length - i - 1 | 0] = t;
    }
  };
  Sort.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Sort',
    interfaces: []
  };
  var Sort_instance = null;
  function Sort_getInstance() {
    if (Sort_instance === null) {
      new Sort();
    }
    return Sort_instance;
  }
  function TDigest() {
    TDigest$Companion_getInstance();
    this.scale = ScaleFunction$K_2_getInstance();
    this.min_ykq3ht$_0 = kotlin_js_internal_DoubleCompanionObject.POSITIVE_INFINITY;
    this.max_ykq96r$_0 = kotlin_js_internal_DoubleCompanionObject.NEGATIVE_INFINITY;
  }
  Object.defineProperty(TDigest.prototype, 'min', {
    get: function () {
      return this.min_ykq3ht$_0;
    },
    set: function (min) {
      this.min_ykq3ht$_0 = min;
    }
  });
  Object.defineProperty(TDigest.prototype, 'max', {
    get: function () {
      return this.max_ykq96r$_0;
    },
    set: function (max) {
      this.max_ykq96r$_0 = max;
    }
  });
  TDigest.prototype.checkValue_tq0o01$ = function (x) {
    if (isNaN_0(x)) {
      throw IllegalArgumentException_init_0('Cannot add NaN');
    }
  };
  TDigest.prototype.setScaleFunction = function (scaleFunction) {
    if (endsWith(scaleFunction.toString(), 'NO_NORM')) {
      throw IllegalArgumentException_init_0("Can't use " + scaleFunction + ' as scale ');
    }
    this.scale = scaleFunction;
  };
  TDigest.prototype.setMinMax_sdh6z7$ = function (min, max) {
    this.min = min;
    this.max = max;
  };
  function TDigest$Companion() {
    TDigest$Companion_instance = this;
  }
  TDigest$Companion.prototype.createMergingDigest = function (compression) {
    return new MergingDigest(compression);
  };
  TDigest$Companion.prototype.createAvlTreeDigest = function (compression) {
    return AVLTreeDigest_init(compression);
  };
  TDigest$Companion.prototype.createDigest = function (compression) {
    return this.createMergingDigest(compression);
  };
  TDigest$Companion.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Companion',
    interfaces: []
  };
  var TDigest$Companion_instance = null;
  function TDigest$Companion_getInstance() {
    if (TDigest$Companion_instance === null) {
      new TDigest$Companion();
    }
    return TDigest$Companion_instance;
  }
  TDigest.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'TDigest',
    interfaces: []
  };
  function Utils() {
    Utils_instance = this;
  }
  Utils.prototype.arraycopy_dgpv4k$ = function (src, srcPos, dest, destPos, length) {
    arrayCopy(src, dest, destPos, srcPos, srcPos + length | 0);
    return dest;
  };
  Utils.prototype.arraycopy_lvhpry$ = function (src, srcPos, dest, destPos, length) {
    arrayCopy(src, dest, destPos, srcPos, srcPos + length | 0);
    return dest;
  };
  Utils.prototype.arraycopy_m70dtq$ = function (src, srcPos, dest, destPos, length) {
    arrayCopy(src, dest, destPos, srcPos, srcPos + length | 0);
    return dest;
  };
  Utils.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Utils',
    interfaces: []
  };
  var Utils_instance = null;
  function Utils_getInstance() {
    if (Utils_instance === null) {
      new Utils();
    }
    return Utils_instance;
  }
  function mpassert(expectedTrue) {
    if (!expectedTrue) {
      var message = 'assertion failed';
      throw AssertionError_init_0(message);
    }
  }
  function hello() {
    return 'Hello from JS';
  }
  function KotlinLongFromBits(lowBits, highBits) {
    var tmp$;
    return Kotlin.isType(tmp$ = Kotlin.Long.fromBits(lowBits, highBits), Kotlin.Long) ? tmp$ : throwCCE();
  }
  function BinaryInputFromByteBuffer(bb) {
    this.bb = bb;
  }
  BinaryInputFromByteBuffer.prototype.readByte = function () {
    return this.bb.readByte();
  };
  BinaryInputFromByteBuffer.prototype.readShort = function () {
    return this.bb.readShort();
  };
  BinaryInputFromByteBuffer.prototype.readInt = function () {
    return this.bb.readInt();
  };
  BinaryInputFromByteBuffer.prototype.readLong = function () {
    var value = this.bb.readLong();
    return KotlinLongFromBits(value.getLowBits(), value.getHighBits());
  };
  BinaryInputFromByteBuffer.prototype.readFloat = function () {
    return this.bb.readFloat();
  };
  BinaryInputFromByteBuffer.prototype.readDouble = function () {
    return this.bb.readDouble();
  };
  BinaryInputFromByteBuffer.prototype.release = function () {
  };
  BinaryInputFromByteBuffer.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'BinaryInputFromByteBuffer',
    interfaces: [BinaryInput]
  };
  function toBinaryInput($receiver) {
    return new BinaryInputFromByteBuffer($receiver);
  }
  function buildBinaryInputFromB64(b64string) {
    return toBinaryInput($module$bytebuffer.fromBase64(b64string));
  }
  function BinaryOutputFromByteBuffer(bb) {
    this.bb = bb;
  }
  Object.defineProperty(BinaryOutputFromByteBuffer.prototype, 'size', {
    get: function () {
      return this.bb.offset;
    }
  });
  BinaryOutputFromByteBuffer.prototype.writeByte_s8j3t7$ = function (v) {
    this.bb.writeByte(v);
  };
  BinaryOutputFromByteBuffer.prototype.writeShort_mq22fl$ = function (v) {
    this.bb.writeShort(v);
  };
  BinaryOutputFromByteBuffer.prototype.writeInt_za3lpa$ = function (v) {
    this.bb.writeInt(v);
  };
  BinaryOutputFromByteBuffer.prototype.writeLong_s8cxhz$ = function (v) {
    var v_ = v;
    this.bb.writeLong($module$long.fromBits(v_.low_, v_.high_));
  };
  BinaryOutputFromByteBuffer.prototype.writeFloat_mx4ult$ = function (v) {
    this.bb.writeFloat(v);
  };
  BinaryOutputFromByteBuffer.prototype.writeDouble_14dthe$ = function (v) {
    this.bb.writeDouble(v);
  };
  BinaryOutputFromByteBuffer.prototype.toB64 = function () {
    this.bb.flip();
    return this.bb.toBase64();
  };
  BinaryOutputFromByteBuffer.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'BinaryOutputFromByteBuffer',
    interfaces: [BinaryOutput]
  };
  function toBinaryOutput($receiver) {
    return new BinaryOutputFromByteBuffer($receiver);
  }
  function buildBinaryOutput(initialSize, block) {
    var buf = new $module$bytebuffer(initialSize);
    block(toBinaryOutput(buf));
    return toBinaryInput(buf.flip());
  }
  var mpassert_0 = defineInlineFunction('tdigest-kt.com.tdunning.math.stats.mpassert_4ina18$', wrapFunction(function () {
    var AssertionError_init = Kotlin.kotlin.AssertionError_init_s8jyv4$;
    return function (value, lazyMessage) {
      if (!value) {
        var message = lazyMessage();
        throw AssertionError_init(message);
      }
    };
  }));
  function Sample() {
  }
  Sample.prototype.checkMe = function () {
    return 12;
  };
  Sample.$metadata$ = {
    kind: Kind_CLASS,
    simpleName: 'Sample',
    interfaces: []
  };
  function Platform() {
    Platform_instance = this;
    this.name = 'JS';
  }
  Platform.$metadata$ = {
    kind: Kind_OBJECT,
    simpleName: 'Platform',
    interfaces: []
  };
  var Platform_instance = null;
  function Platform_getInstance() {
    if (Platform_instance === null) {
      new Platform();
    }
    return Platform_instance;
  }
  var package$com = _.com || (_.com = {});
  var package$basicio = package$com.basicio || (package$com.basicio = {});
  package$basicio.BinaryInput = BinaryInput;
  package$basicio.BinaryOutput = BinaryOutput;
  Object.defineProperty(AbstractTDigest, 'Companion', {
    get: AbstractTDigest$Companion_getInstance
  });
  var package$tdunning = package$com.tdunning || (package$com.tdunning = {});
  var package$math = package$tdunning.math || (package$tdunning.math = {});
  var package$stats = package$math.stats || (package$math.stats = {});
  package$stats.AbstractTDigest = AbstractTDigest;
  package$stats.AVLGroupTree = AVLGroupTree;
  Object.defineProperty(AVLTreeDigest, 'Companion', {
    get: AVLTreeDigest$Companion_getInstance
  });
  package$stats.AVLTreeDigest_init_14dthe$ = AVLTreeDigest_init;
  package$stats.AVLTreeDigest = AVLTreeDigest;
  Object.defineProperty(Centroid, 'Companion', {
    get: Centroid$Companion_getInstance
  });
  package$stats.Centroid_init_14dthe$ = Centroid_init;
  package$stats.Centroid_init_12fank$ = Centroid_init_0;
  package$stats.Centroid_init_mqu1mq$ = Centroid_init_1;
  package$stats.Centroid_init_87xbef$ = Centroid_init_2;
  package$stats.Centroid_init_scvtgg$ = Centroid_init_3;
  package$stats.Centroid = Centroid;
  Object.defineProperty(package$stats, 'Dist', {
    get: Dist_getInstance
  });
  $$importsForInline$$['tdigest-kt'] = _;
  Object.defineProperty(IntAVLTree, 'Companion', {
    get: IntAVLTree$Companion_getInstance
  });
  package$stats.IntAVLTree = IntAVLTree;
  Object.defineProperty(MergingDigest$Encoding, 'VERBOSE_ENCODING', {
    get: MergingDigest$Encoding$VERBOSE_ENCODING_getInstance
  });
  Object.defineProperty(MergingDigest$Encoding, 'SMALL_ENCODING', {
    get: MergingDigest$Encoding$SMALL_ENCODING_getInstance
  });
  MergingDigest.Encoding = MergingDigest$Encoding;
  Object.defineProperty(MergingDigest, 'Companion', {
    get: MergingDigest$Companion_getInstance
  });
  package$stats.MergingDigest = MergingDigest;
  Object.defineProperty(ScaleFunction, 'K_0', {
    get: ScaleFunction$K_0_getInstance
  });
  Object.defineProperty(ScaleFunction, 'K_1', {
    get: ScaleFunction$K_1_getInstance
  });
  Object.defineProperty(ScaleFunction, 'K_1_FAST', {
    get: ScaleFunction$K_1_FAST_getInstance
  });
  Object.defineProperty(ScaleFunction, 'K_2', {
    get: ScaleFunction$K_2_getInstance
  });
  Object.defineProperty(ScaleFunction, 'K_3', {
    get: ScaleFunction$K_3_getInstance
  });
  Object.defineProperty(ScaleFunction, 'K_2_NO_NORM', {
    get: ScaleFunction$K_2_NO_NORM_getInstance
  });
  Object.defineProperty(ScaleFunction, 'K_3_NO_NORM', {
    get: ScaleFunction$K_3_NO_NORM_getInstance
  });
  Object.defineProperty(ScaleFunction, 'Companion', {
    get: ScaleFunction$Companion_getInstance
  });
  package$stats.ScaleFunction = ScaleFunction;
  Object.defineProperty(package$stats, 'Sort', {
    get: Sort_getInstance
  });
  Object.defineProperty(TDigest, 'Companion', {
    get: TDigest$Companion_getInstance
  });
  package$stats.TDigest = TDigest;
  Object.defineProperty(package$stats, 'Utils', {
    get: Utils_getInstance
  });
  package$stats.mpassert_6taknv$ = mpassert;
  var package$sample = _.sample || (_.sample = {});
  package$sample.hello = hello;
  package$basicio.KotlinLongFromBits_vux9f0$ = KotlinLongFromBits;
  package$basicio.BinaryInputFromByteBuffer = BinaryInputFromByteBuffer;
  package$basicio.toBinaryInput = toBinaryInput;
  package$basicio.buildBinaryInputFromB64_61zpoe$ = buildBinaryInputFromB64;
  package$basicio.BinaryOutputFromByteBuffer = BinaryOutputFromByteBuffer;
  package$basicio.toBinaryOutput = toBinaryOutput;
  package$basicio.buildBinaryOutput_pfnpwk$ = buildBinaryOutput;
  package$stats.mpassert_4ina18$ = mpassert_0;
  package$sample.Sample = Sample;
  Object.defineProperty(package$sample, 'Platform', {
    get: Platform_getInstance
  });
  BinaryInputFromByteBuffer.prototype.readBoolean = BinaryInput.prototype.readBoolean;
  BinaryOutputFromByteBuffer.prototype.writeBoolean_6taknv$ = BinaryOutput.prototype.writeBoolean_6taknv$;
  Kotlin.defineModule('tdigest-kt', _);
  return _;
}(module.exports, require('kotlin'), require('kotlinx-atomicfu'), require('bytebuffer'), require('long')));

//# sourceMappingURL=tdigest-kt.js.map
