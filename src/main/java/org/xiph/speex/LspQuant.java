package org.xiph.speex;

public abstract class LspQuant implements Codebook {

   public static final int MAX_LSP_SIZE = 20;


   public abstract void quant(float[] var1, float[] var2, int var3, Bits var4);

   public abstract void unquant(float[] var1, int var2, Bits var3);

   protected void unpackPlus(float[] var1, int[] var2, Bits var3, float var4, int var5, int var6) {
      int var7 = var3.unpack(6);

      for(int var8 = 0; var8 < var5; ++var8) {
         var1[var8 + var6] += var4 * (float)var2[var7 * var5 + var8];
      }

   }

   protected static int lsp_quant(float[] var0, int var1, int[] var2, int var3, int var4) {
      float var9 = 0.0F;
      int var10 = 0;
      int var11 = 0;

      int var6;
      for(int var5 = 0; var5 < var3; ++var5) {
         float var7 = 0.0F;

         for(var6 = 0; var6 < var4; ++var6) {
            float var8 = var0[var1 + var6] - (float)var2[var11++];
            var7 += var8 * var8;
         }

         if(var7 < var9 || var5 == 0) {
            var9 = var7;
            var10 = var5;
         }
      }

      for(var6 = 0; var6 < var4; ++var6) {
         var0[var1 + var6] -= (float)var2[var10 * var4 + var6];
      }

      return var10;
   }

   protected static int lsp_weight_quant(float[] var0, int var1, float[] var2, int var3, int[] var4, int var5, int var6) {
      float var11 = 0.0F;
      int var12 = 0;
      int var13 = 0;

      int var8;
      for(int var7 = 0; var7 < var5; ++var7) {
         float var9 = 0.0F;

         for(var8 = 0; var8 < var6; ++var8) {
            float var10 = var0[var1 + var8] - (float)var4[var13++];
            var9 += var2[var3 + var8] * var10 * var10;
         }

         if(var9 < var11 || var7 == 0) {
            var11 = var9;
            var12 = var7;
         }
      }

      for(var8 = 0; var8 < var6; ++var8) {
         var0[var1 + var8] -= (float)var4[var12 * var6 + var8];
      }

      return var12;
   }
}
