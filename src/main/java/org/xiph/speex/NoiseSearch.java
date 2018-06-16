package org.xiph.speex;

public class NoiseSearch extends CbSearch {

   public final void quant(float[] var1, float[] var2, float[] var3, float[] var4, int var5, int var6, float[] var7, int var8, float[] var9, Bits var10, int var11) {
      float[] var13 = new float[var6];
      Filters.residue_percep_zero(var1, 0, var2, var3, var4, var13, var6, var5);

      int var12;
      for(var12 = 0; var12 < var6; ++var12) {
         var7[var8 + var12] += var13[var12];
      }

      for(var12 = 0; var12 < var6; ++var12) {
         var1[var12] = 0.0F;
      }

   }

   public final void unquant(float[] var1, int var2, int var3, Bits var4) {
      for(int var5 = 0; var5 < var3; ++var5) {
         var1[var2 + var5] += (float)(3.0D * (Math.random() - 0.5D));
      }

   }
}
