package org.xiph.speex;

public class SbCodec extends NbCodec {

   public static final int[] SB_FRAME_SIZE = new int[]{4, 36, 112, 192, 352, -1, -1, -1};
   public static final int SB_SUBMODES = 8;
   public static final int SB_SUBMODE_BITS = 3;
   public static final int QMF_ORDER = 64;
   protected int fullFrameSize;
   protected float foldingGain;
   protected float[] high;
   protected float[] y0;
   protected float[] y1;
   protected float[] x0d;
   protected float[] g0_mem;
   protected float[] g1_mem;


   public void wbinit() {
      super.submodes = buildWbSubModes();
      super.submodeID = 3;
   }

   public void uwbinit() {
      super.submodes = buildUwbSubModes();
      super.submodeID = 1;
   }

   protected void init(int var1, int var2, int var3, int var4, float var5) {
      super.init(var1, var2, var3, var4);
      this.fullFrameSize = 2 * var1;
      this.foldingGain = var5;
      super.lag_factor = 0.002F;
      this.high = new float[this.fullFrameSize];
      this.y0 = new float[this.fullFrameSize];
      this.y1 = new float[this.fullFrameSize];
      this.x0d = new float[var1];
      this.g0_mem = new float[64];
      this.g1_mem = new float[64];
   }

   protected static SubMode[] buildWbSubModes() {
      HighLspQuant var0 = new HighLspQuant();
      SplitShapeSearch var1 = new SplitShapeSearch(40, 10, 4, Codebook.hexc_10_32_table, 5, 0);
      SplitShapeSearch var2 = new SplitShapeSearch(40, 8, 5, Codebook.hexc_table, 7, 1);
      SubMode[] var3 = new SubMode[8];
      var3[1] = new SubMode(0, 0, 1, 0, var0, (Ltp)null, (CbSearch)null, 0.75F, 0.75F, -1.0F, 36);
      var3[2] = new SubMode(0, 0, 1, 0, var0, (Ltp)null, var1, 0.85F, 0.6F, -1.0F, 112);
      var3[3] = new SubMode(0, 0, 1, 0, var0, (Ltp)null, var2, 0.75F, 0.7F, -1.0F, 192);
      var3[4] = new SubMode(0, 0, 1, 1, var0, (Ltp)null, var2, 0.75F, 0.75F, -1.0F, 352);
      return var3;
   }

   protected static SubMode[] buildUwbSubModes() {
      HighLspQuant var0 = new HighLspQuant();
      SubMode[] var1 = new SubMode[8];
      var1[1] = new SubMode(0, 0, 1, 0, var0, (Ltp)null, (CbSearch)null, 0.75F, 0.75F, -1.0F, 2);
      return var1;
   }

   public int getFrameSize() {
      return this.fullFrameSize;
   }

   public boolean getDtx() {
      return super.dtx_enabled != 0;
   }

   public float[] getExc() {
      float[] var2 = new float[this.fullFrameSize];

      for(int var1 = 0; var1 < super.frameSize; ++var1) {
         var2[2 * var1] = 2.0F * super.excBuf[super.excIdx + var1];
      }

      return var2;
   }

   public float[] getInnov() {
      return this.getExc();
   }

}
