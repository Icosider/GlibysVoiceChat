package org.xiph.speex;


public class Lpc {

   public static float wld(float[] var0, float[] var1, float[] var2, int var3) {
      float var7 = var1[0];
      int var4;
      if(var1[0] == 0.0F) {
         for(var4 = 0; var4 < var3; ++var4) {
            var2[var4] = 0.0F;
         }

         return 0.0F;
      } else {
         for(var4 = 0; var4 < var3; ++var4) {
            float var6 = -var1[var4 + 1];

            int var5;
            for(var5 = 0; var5 < var4; ++var5) {
               var6 -= var0[var5] * var1[var4 - var5];
            }

            var2[var4] = var6 /= var7;
            var0[var4] = var6;

            for(var5 = 0; var5 < var4 / 2; ++var5) {
               float var8 = var0[var5];
               var0[var5] += var6 * var0[var4 - 1 - var5];
               var0[var4 - 1 - var5] += var6 * var8;
            }

            if(var4 % 2 != 0) {
               var0[var5] += var0[var5] * var6;
            }

            var7 = (float)((double)var7 * (1.0D - (double)(var6 * var6)));
         }

         return var7;
      }
   }

   public static void autocorr(float[] var0, float[] var1, int var2, int var3) {
      while(var2-- > 0) {
         int var5 = var2;

         float var4;
         for(var4 = 0.0F; var5 < var3; ++var5) {
            var4 += var0[var5] * var0[var5 - var2];
         }

         var1[var2] = var4;
      }

   }
}
