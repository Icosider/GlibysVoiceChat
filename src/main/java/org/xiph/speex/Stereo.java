package org.xiph.speex;

public class Stereo {

   public static final int SPEEX_INBAND_STEREO = 9;
   public static final float[] e_ratio_quant = new float[]{0.25F, 0.315F, 0.397F, 0.5F};
   private float balance = 1.0F;
   private float e_ratio = 0.5F;
   private float smooth_left = 1.0F;
   private float smooth_right = 1.0F;


   public static void encode(Bits var0, float[] var1, int var2) {
      float var5 = 0.0F;
      float var6 = 0.0F;
      float var7 = 0.0F;

      for(int var3 = 0; var3 < var2; ++var3) {
         var5 += var1[2 * var3] * var1[2 * var3];
         var6 += var1[2 * var3 + 1] * var1[2 * var3 + 1];
         var1[var3] = 0.5F * (var1[2 * var3] + var1[2 * var3 + 1]);
         var7 += var1[var3] * var1[var3];
      }

      float var8 = (var5 + 1.0F) / (var6 + 1.0F);
      float var9 = var7 / (1.0F + var5 + var6);
      var0.pack(14, 5);
      var0.pack(9, 4);
      var8 = (float)(4.0D * Math.log((double)var8));
      if(var8 > 0.0F) {
         var0.pack(0, 1);
      } else {
         var0.pack(1, 1);
      }

      var8 = (float)Math.floor((double)(0.5F + Math.abs(var8)));
      if(var8 > 30.0F) {
         var8 = 31.0F;
      }

      var0.pack((int)var8, 5);
      int var4 = VQ.index(var9, e_ratio_quant, 4);
      var0.pack(var4, 2);
   }

   public void decode(float[] var1, int var2) {
      float var4 = 0.0F;

      int var3;
      for(var3 = var2 - 1; var3 >= 0; --var3) {
         var4 += var1[var3] * var1[var3];
      }

      float var7 = var4 / this.e_ratio;
      float var5 = var7 * this.balance / (1.0F + this.balance);
      float var6 = var7 - var5;
      var5 = (float)Math.sqrt((double)(var5 / (var4 + 0.01F)));
      var6 = (float)Math.sqrt((double)(var6 / (var4 + 0.01F)));

      for(var3 = var2 - 1; var3 >= 0; --var3) {
         float var8 = var1[var3];
         this.smooth_left = 0.98F * this.smooth_left + 0.02F * var5;
         this.smooth_right = 0.98F * this.smooth_right + 0.02F * var6;
         var1[2 * var3] = this.smooth_left * var8;
         var1[2 * var3 + 1] = this.smooth_right * var8;
      }

   }

   public void init(Bits var1) {
      float var2 = 1.0F;
      if(var1.unpack(1) != 0) {
         var2 = -1.0F;
      }

      int var3 = var1.unpack(5);
      this.balance = (float)Math.exp((double)var2 * 0.25D * (double)var3);
      var3 = var1.unpack(2);
      this.e_ratio = e_ratio_quant[var3];
   }

}
