package org.xiph.speex;


public class Vbr {

   public static final int VBR_MEMORY_SIZE = 5;
   public static final int MIN_ENERGY = 6000;
   public static final float NOISE_POW = 0.3F;
   public static final float[][] nb_thresh = new float[][]{{-1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F}, {3.5F, 2.5F, 2.0F, 1.2F, 0.5F, 0.0F, -0.5F, -0.7F, -0.8F, -0.9F, -1.0F}, {10.0F, 6.5F, 5.2F, 4.5F, 3.9F, 3.5F, 3.0F, 2.5F, 2.3F, 1.8F, 1.0F}, {11.0F, 8.8F, 7.5F, 6.5F, 5.0F, 3.9F, 3.9F, 3.9F, 3.5F, 3.0F, 1.0F}, {11.0F, 11.0F, 9.9F, 9.0F, 8.0F, 7.0F, 6.5F, 6.0F, 5.0F, 4.0F, 2.0F}, {11.0F, 11.0F, 11.0F, 11.0F, 9.5F, 9.0F, 8.0F, 7.0F, 6.5F, 5.0F, 3.0F}, {11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 9.5F, 8.5F, 8.0F, 6.5F, 4.0F}, {11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 9.8F, 7.5F, 5.5F}, {8.0F, 5.0F, 3.7F, 3.0F, 2.5F, 2.0F, 1.8F, 1.5F, 1.0F, 0.0F, 0.0F}};
   public static final float[][] hb_thresh = new float[][]{{-1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F}, {-1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F}, {11.0F, 11.0F, 9.5F, 8.5F, 7.5F, 6.0F, 5.0F, 3.9F, 3.0F, 2.0F, 1.0F}, {11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 9.5F, 8.7F, 7.8F, 7.0F, 6.5F, 4.0F}, {11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 11.0F, 9.8F, 7.5F, 5.5F}};
   public static final float[][] uhb_thresh = new float[][]{{-1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F, -1.0F}, {3.9F, 2.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F}};
   private float energy_alpha = 0.1F;
   private float average_energy = 0.0F;
   private float last_energy = 1.0F;
   private float[] last_log_energy;
   private float accum_sum = 0.0F;
   private float last_pitch_coef = 0.0F;
   private float soft_pitch = 0.0F;
   private float last_quality = 0.0F;
   private float noise_level;
   private float noise_accum = (float)(0.05D * Math.pow(6000.0D, 0.30000001192092896D));
   private float noise_accum_count = 0.05F;
   private int consec_noise;


   public Vbr() {
      this.noise_level = this.noise_accum / this.noise_accum_count;
      this.consec_noise = 0;
      this.last_log_energy = new float[5];

      for(int var1 = 0; var1 < 5; ++var1) {
         this.last_log_energy[var1] = (float)Math.log(6000.0D);
      }

   }

   public float analysis(float[] var1, int var2, int var3, float var4) {
      float var6 = 0.0F;
      float var7 = 0.0F;
      float var8 = 0.0F;
      float var9 = 7.0F;
      float var12 = 0.0F;

      int var5;
      for(var5 = 0; var5 < var2 >> 1; ++var5) {
         var7 += var1[var5] * var1[var5];
      }

      for(var5 = var2 >> 1; var5 < var2; ++var5) {
         var8 += var1[var5] * var1[var5];
      }

      var6 = var7 + var8;
      float var11 = (float)Math.log((double)(var6 + 6000.0F));

      for(var5 = 0; var5 < 5; ++var5) {
         var12 += (var11 - this.last_log_energy[var5]) * (var11 - this.last_log_energy[var5]);
      }

      var12 /= 150.0F;
      if(var12 > 1.0F) {
         var12 = 1.0F;
      }

      float var13 = 3.0F * (var4 - 0.4F) * Math.abs(var4 - 0.4F);
      this.average_energy = (1.0F - this.energy_alpha) * this.average_energy + this.energy_alpha * var6;
      this.noise_level = this.noise_accum / this.noise_accum_count;
      float var14 = (float)Math.pow((double)var6, 0.30000001192092896D);
      if(this.noise_accum_count < 0.06F && var6 > 6000.0F) {
         this.noise_accum = 0.05F * var14;
      }

      boolean var10;
      float var15;
      if((var13 >= 0.3F || var12 >= 0.2F || var14 >= 1.2F * this.noise_level) && (var13 >= 0.3F || var12 >= 0.05F || var14 >= 1.5F * this.noise_level) && (var13 >= 0.4F || var12 >= 0.05F || var14 >= 1.2F * this.noise_level) && (var13 >= 0.0F || var12 >= 0.05F)) {
         var10 = true;
         this.consec_noise = 0;
      } else {
         var10 = false;
         ++this.consec_noise;
         if(var14 > 3.0F * this.noise_level) {
            var15 = 3.0F * this.noise_level;
         } else {
            var15 = var14;
         }

         if(this.consec_noise >= 4) {
            this.noise_accum = 0.95F * this.noise_accum + 0.05F * var15;
            this.noise_accum_count = 0.95F * this.noise_accum_count + 0.05F;
         }
      }

      if(var14 < this.noise_level && var6 > 6000.0F) {
         this.noise_accum = 0.95F * this.noise_accum + 0.05F * var14;
         this.noise_accum_count = 0.95F * this.noise_accum_count + 0.05F;
      }

      if(var6 < 30000.0F) {
         var9 -= 0.7F;
         if(var6 < 10000.0F) {
            var9 -= 0.7F;
         }

         if(var6 < 3000.0F) {
            var9 -= 0.7F;
         }
      } else {
         var15 = (float)Math.log((double)((var6 + 1.0F) / (1.0F + this.last_energy)));
         float var16 = (float)Math.log((double)((var6 + 1.0F) / (1.0F + this.average_energy)));
         if(var16 < -5.0F) {
            var16 = -5.0F;
         }

         if(var16 > 2.0F) {
            var16 = 2.0F;
         }

         if(var16 > 0.0F) {
            var9 += 0.6F * var16;
         }

         if(var16 < 0.0F) {
            var9 += 0.5F * var16;
         }

         if(var15 > 0.0F) {
            if(var15 > 5.0F) {
               var15 = 5.0F;
            }

            var9 += 0.5F * var15;
         }

         if(var8 > 1.6F * var7) {
            var9 += 0.5F;
         }
      }

      this.last_energy = var6;
      this.soft_pitch = 0.6F * this.soft_pitch + 0.4F * var4;
      var9 = (float)((double)var9 + 2.200000047683716D * ((double)var4 - 0.4D + ((double)this.soft_pitch - 0.4D)));
      if(var9 < this.last_quality) {
         var9 = 0.5F * var9 + 0.5F * this.last_quality;
      }

      if(var9 < 4.0F) {
         var9 = 4.0F;
      }

      if(var9 > 10.0F) {
         var9 = 10.0F;
      }

      if(this.consec_noise >= 3) {
         var9 = 4.0F;
      }

      if(this.consec_noise != 0) {
         var9 -= (float)(1.0D * (Math.log(3.0D + (double)this.consec_noise) - Math.log(3.0D)));
      }

      if(var9 < 0.0F) {
         var9 = 0.0F;
      }

      if(var6 < 60000.0F) {
         if(this.consec_noise > 2) {
            var9 -= (float)(0.5D * (Math.log(3.0D + (double)this.consec_noise) - Math.log(3.0D)));
         }

         if(var6 < 10000.0F && this.consec_noise > 2) {
            var9 -= (float)(0.5D * (Math.log(3.0D + (double)this.consec_noise) - Math.log(3.0D)));
         }

         if(var9 < 0.0F) {
            var9 = 0.0F;
         }

         var9 += (float)(0.3D * Math.log((double)var6 / 60000.0D));
      }

      if(var9 < -1.0F) {
         var9 = -1.0F;
      }

      this.last_pitch_coef = var4;
      this.last_quality = var9;

      for(var5 = 4; var5 > 0; --var5) {
         this.last_log_energy[var5] = this.last_log_energy[var5 - 1];
      }

      this.last_log_energy[0] = var11;
      return var9;
   }

}
