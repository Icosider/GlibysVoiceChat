package org.xiph.speex;

public class NbCodec implements Codebook {

   public static final float VERY_SMALL = 0.0F;
   public static final int[] NB_FRAME_SIZE = new int[]{5, 43, 119, 160, 220, 300, 364, 492, 79, 1, 1, 1, 1, 1, 1, 1};
   public static final int NB_SUBMODES = 16;
   public static final int NB_SUBMODE_BITS = 4;
   public static final float[] exc_gain_quant_scal1 = new float[]{-0.35F, 0.05F};
   public static final float[] exc_gain_quant_scal3 = new float[]{-2.79475F, -1.81066F, -1.16985F, -0.848119F, -0.58719F, -0.329818F, -0.063266F, 0.282826F};
   protected Lsp m_lsp = new Lsp();
   protected Filters filters = new Filters();
   protected SubMode[] submodes;
   protected int submodeID;
   protected int first;
   protected int frameSize;
   protected int subframeSize;
   protected int nbSubframes;
   protected int windowSize;
   protected int lpcSize;
   protected int bufSize;
   protected int min_pitch;
   protected int max_pitch;
   protected float gamma1;
   protected float gamma2;
   protected float lag_factor;
   protected float lpc_floor;
   protected float preemph;
   protected float pre_mem;
   protected float[] frmBuf;
   protected int frmIdx;
   protected float[] excBuf;
   protected int excIdx;
   protected float[] innov;
   protected float[] lpc;
   protected float[] qlsp;
   protected float[] old_qlsp;
   protected float[] interp_qlsp;
   protected float[] interp_qlpc;
   protected float[] mem_sp;
   protected float[] pi_gain;
   protected float[] awk1;
   protected float[] awk2;
   protected float[] awk3;
   protected float voc_m1;
   protected float voc_m2;
   protected float voc_mean;
   protected int voc_offset;
   protected int dtx_enabled;


   public void nbinit() {
      this.submodes = buildNbSubModes();
      this.submodeID = 5;
      this.init(160, 40, 10, 640);
   }

   protected void init(int var1, int var2, int var3, int var4) {
      this.first = 1;
      this.frameSize = var1;
      this.windowSize = var1 * 3 / 2;
      this.subframeSize = var2;
      this.nbSubframes = var1 / var2;
      this.lpcSize = var3;
      this.bufSize = var4;
      this.min_pitch = 17;
      this.max_pitch = 144;
      this.preemph = 0.0F;
      this.pre_mem = 0.0F;
      this.gamma1 = 0.9F;
      this.gamma2 = 0.6F;
      this.lag_factor = 0.01F;
      this.lpc_floor = 1.0001F;
      this.frmBuf = new float[var4];
      this.frmIdx = var4 - this.windowSize;
      this.excBuf = new float[var4];
      this.excIdx = var4 - this.windowSize;
      this.innov = new float[var1];
      this.lpc = new float[var3 + 1];
      this.qlsp = new float[var3];
      this.old_qlsp = new float[var3];
      this.interp_qlsp = new float[var3];
      this.interp_qlpc = new float[var3 + 1];
      this.mem_sp = new float[5 * var3];
      this.pi_gain = new float[this.nbSubframes];
      this.awk1 = new float[var3 + 1];
      this.awk2 = new float[var3 + 1];
      this.awk3 = new float[var3 + 1];
      this.voc_m1 = this.voc_m2 = this.voc_mean = 0.0F;
      this.voc_offset = 0;
      this.dtx_enabled = 0;
   }

   private static SubMode[] buildNbSubModes() {
      Ltp3Tap var0 = new Ltp3Tap(Codebook.gain_cdbk_nb, 7, 7);
      Ltp3Tap var1 = new Ltp3Tap(Codebook.gain_cdbk_lbr, 5, 0);
      Ltp3Tap var2 = new Ltp3Tap(Codebook.gain_cdbk_lbr, 5, 7);
      Ltp3Tap var3 = new Ltp3Tap(Codebook.gain_cdbk_lbr, 5, 7);
      LtpForcedPitch var4 = new LtpForcedPitch();
      NoiseSearch var5 = new NoiseSearch();
      SplitShapeSearch var6 = new SplitShapeSearch(40, 10, 4, Codebook.exc_10_16_table, 4, 0);
      SplitShapeSearch var7 = new SplitShapeSearch(40, 10, 4, Codebook.exc_10_32_table, 5, 0);
      SplitShapeSearch var8 = new SplitShapeSearch(40, 5, 8, Codebook.exc_5_64_table, 6, 0);
      SplitShapeSearch var9 = new SplitShapeSearch(40, 8, 5, Codebook.exc_8_128_table, 7, 0);
      SplitShapeSearch var10 = new SplitShapeSearch(40, 5, 8, Codebook.exc_5_256_table, 8, 0);
      SplitShapeSearch var11 = new SplitShapeSearch(40, 20, 2, Codebook.exc_20_32_table, 5, 0);
      NbLspQuant var12 = new NbLspQuant();
      LbrLspQuant var13 = new LbrLspQuant();
      SubMode[] var14 = new SubMode[16];
      var14[1] = new SubMode(0, 1, 0, 0, var13, var4, var5, 0.7F, 0.7F, -1.0F, 43);
      var14[2] = new SubMode(0, 0, 0, 0, var13, var1, var6, 0.7F, 0.5F, 0.55F, 119);
      var14[3] = new SubMode(-1, 0, 1, 0, var13, var2, var7, 0.7F, 0.55F, 0.45F, 160);
      var14[4] = new SubMode(-1, 0, 1, 0, var13, var3, var9, 0.7F, 0.63F, 0.35F, 220);
      var14[5] = new SubMode(-1, 0, 3, 0, var12, var0, var8, 0.7F, 0.65F, 0.25F, 300);
      var14[6] = new SubMode(-1, 0, 3, 0, var12, var0, var10, 0.68F, 0.65F, 0.1F, 364);
      var14[7] = new SubMode(-1, 0, 3, 1, var12, var0, var8, 0.65F, 0.65F, -1.0F, 492);
      var14[8] = new SubMode(0, 1, 0, 0, var13, var4, var11, 0.7F, 0.5F, 0.65F, 79);
      return var14;
   }

   public int getFrameSize() {
      return this.frameSize;
   }

   public boolean getDtx() {
      return this.dtx_enabled != 0;
   }

   public float[] getPiGain() {
      return this.pi_gain;
   }

   public float[] getExc() {
      float[] var1 = new float[this.frameSize];
      System.arraycopy(this.excBuf, this.excIdx, var1, 0, this.frameSize);
      return var1;
   }

   public float[] getInnov() {
      return this.innov;
   }

}
