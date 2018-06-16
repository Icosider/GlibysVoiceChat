package org.xiph.speex;


public class Misc {

   public static float[] window(int var0, int var1) {
      int var3 = var1 * 7 / 2;
      int var4 = var1 * 5 / 2;
      float[] var5 = new float[var0];

      int var2;
      for(var2 = 0; var2 < var3; ++var2) {
         var5[var2] = (float)(0.54D - 0.46D * Math.cos(3.141592653589793D * (double)var2 / (double)var3));
      }

      for(var2 = 0; var2 < var4; ++var2) {
         var5[var3 + var2] = (float)(0.54D + 0.46D * Math.cos(3.141592653589793D * (double)var2 / (double)var4));
      }

      return var5;
   }

   public static float[] lagWindow(int var0, float var1) {
      float[] var2 = new float[var0 + 1];

      for(int var3 = 0; var3 < var0 + 1; ++var3) {
         var2[var3] = (float)Math.exp(-0.5D * 6.283185307179586D * (double)var1 * (double)var3 * 6.283185307179586D * (double)var1 * (double)var3);
      }

      return var2;
   }
}
