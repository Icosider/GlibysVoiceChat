package org.xiph.speex;

public abstract class Ltp {

   public abstract int quant(float[] var1, float[] var2, int var3, float[] var4, float[] var5, float[] var6, float[] var7, int var8, int var9, int var10, float var11, int var12, int var13, Bits var14, float[] var15, int var16, float[] var17, int var18);

   public abstract int unquant(float[] var1, int var2, int var3, float var4, int var5, float[] var6, Bits var7, int var8, int var9, float var10);

   protected static float inner_prod(float[] var0, int var1, float[] var2, int var3, int var4) {
      float var6 = 0.0F;
      float var7 = 0.0F;
      float var8 = 0.0F;
      float var9 = 0.0F;

      for(int var5 = 0; var5 < var4; var5 += 4) {
         var6 += var0[var1 + var5] * var2[var3 + var5];
         var7 += var0[var1 + var5 + 1] * var2[var3 + var5 + 1];
         var8 += var0[var1 + var5 + 2] * var2[var3 + var5 + 2];
         var9 += var0[var1 + var5 + 3] * var2[var3 + var5 + 3];
      }

      return var6 + var7 + var8 + var9;
   }

   protected static void open_loop_nbest_pitch(float[] var0, int var1, int var2, int var3, int var4, int[] var5, float[] var6, int var7) {
      float[] var11 = new float[var7];
      float[] var13 = new float[var3 - var2 + 1];
      float[] var14 = new float[var3 - var2 + 2];
      float[] var15 = new float[var3 - var2 + 1];

      int var8;
      for(var8 = 0; var8 < var7; ++var8) {
         var11[var8] = -1.0F;
         var6[var8] = 0.0F;
         var5[var8] = var2;
      }

      var14[0] = inner_prod(var0, var1 - var2, var0, var1 - var2, var4);
      float var12 = inner_prod(var0, var1, var0, var1, var4);

      for(var8 = var2; var8 <= var3; ++var8) {
         var14[var8 - var2 + 1] = var14[var8 - var2] + var0[var1 - var8 - 1] * var0[var1 - var8 - 1] - var0[var1 - var8 + var4 - 1] * var0[var1 - var8 + var4 - 1];
         if(var14[var8 - var2 + 1] < 1.0F) {
            var14[var8 - var2 + 1] = 1.0F;
         }
      }

      for(var8 = var2; var8 <= var3; ++var8) {
         var13[var8 - var2] = 0.0F;
         var15[var8 - var2] = 0.0F;
      }

      for(var8 = var2; var8 <= var3; ++var8) {
         var13[var8 - var2] = inner_prod(var0, var1, var0, var1 - var8, var4);
         var15[var8 - var2] = var13[var8 - var2] * var13[var8 - var2] / (var14[var8 - var2] + 1.0F);
      }

      for(var8 = var2; var8 <= var3; ++var8) {
         if(var15[var8 - var2] > var11[var7 - 1]) {
            float var16 = var13[var8 - var2] / (var14[var8 - var2] + 10.0F);
            float var17 = (float)Math.sqrt((double)(var16 * var13[var8 - var2] / (var12 + 10.0F)));
            if(var17 > var16) {
               var17 = var16;
            }

            if(var17 < 0.0F) {
               var17 = 0.0F;
            }

            for(int var9 = 0; var9 < var7; ++var9) {
               if(var15[var8 - var2] > var11[var9]) {
                  for(int var10 = var7 - 1; var10 > var9; --var10) {
                     var11[var10] = var11[var10 - 1];
                     var5[var10] = var5[var10 - 1];
                     var6[var10] = var6[var10 - 1];
                  }

                  var11[var9] = var15[var8 - var2];
                  var5[var9] = var8;
                  var6[var9] = var17;
                  break;
               }
            }
         }
      }

   }
}
