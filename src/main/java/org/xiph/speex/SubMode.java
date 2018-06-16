package org.xiph.speex;

public class SubMode {

   public int lbr_pitch;
   public int forced_pitch_gain;
   public int have_subframe_gain;
   public int double_codebook;
   public LspQuant lsqQuant;
   public Ltp ltp;
   public CbSearch innovation;
   public float lpc_enh_k1;
   public float lpc_enh_k2;
   public float comb_gain;
   public int bits_per_frame;


   public SubMode(int var1, int var2, int var3, int var4, LspQuant var5, Ltp var6, CbSearch var7, float var8, float var9, float var10, int var11) {
      this.lbr_pitch = var1;
      this.forced_pitch_gain = var2;
      this.have_subframe_gain = var3;
      this.double_codebook = var4;
      this.lsqQuant = var5;
      this.ltp = var6;
      this.innovation = var7;
      this.lpc_enh_k1 = var8;
      this.lpc_enh_k2 = var9;
      this.comb_gain = var10;
      this.bits_per_frame = var11;
   }
}
