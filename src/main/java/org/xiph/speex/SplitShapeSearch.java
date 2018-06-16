package org.xiph.speex;

public class SplitShapeSearch extends CbSearch {

   public static final int MAX_COMPLEXITY = 10;
   private int subframesize;
   private int subvect_size;
   private int nb_subvect;
   private int[] shape_cb;
   private int shape_cb_size;
   private int shape_bits;
   private int have_sign;
   private int[] ind;
   private int[] signs;
   private float[] t;
   private float[] e;
   private float[] E;
   private float[] r2;
   private float[][] ot;
   private float[][] nt;
   private int[][] nind;
   private int[][] oind;


   public SplitShapeSearch(int var1, int var2, int var3, int[] var4, int var5, int var6) {
      this.subframesize = var1;
      this.subvect_size = var2;
      this.nb_subvect = var3;
      this.shape_cb = var4;
      this.shape_bits = var5;
      this.have_sign = var6;
      this.ind = new int[var3];
      this.signs = new int[var3];
      this.shape_cb_size = 1 << var5;
      this.ot = new float[10][var1];
      this.nt = new float[10][var1];
      this.oind = new int[10][var3];
      this.nind = new int[10][var3];
      this.t = new float[var1];
      this.e = new float[var1];
      this.r2 = new float[var1];
      this.E = new float[this.shape_cb_size];
   }

   public final void quant(float[] var1, float[] var2, float[] var3, float[] var4, int var5, int var6, float[] var7, int var8, float[] var9, Bits var10, int var11) {
      int var23 = var11;
      if(var11 > 10) {
         var23 = 10;
      }

      float[] var18 = new float[this.shape_cb_size * this.subvect_size];
      int[] var21 = new int[var23];
      float[] var22 = new float[var23];
      float[] var19 = new float[var23];
      float[] var20 = new float[var23];

      int var12;
      int var13;
      for(var12 = 0; var12 < var23; ++var12) {
         for(var13 = 0; var13 < this.nb_subvect; ++var13) {
            this.nind[var12][var13] = this.oind[var12][var13] = -1;
         }
      }

      for(var13 = 0; var13 < var23; ++var13) {
         for(var12 = 0; var12 < var6; ++var12) {
            this.ot[var13][var12] = var1[var12];
         }
      }

      int var14;
      int var24;
      for(var12 = 0; var12 < this.shape_cb_size; ++var12) {
         var24 = var12 * this.subvect_size;
         int var25 = var12 * this.subvect_size;

         for(var13 = 0; var13 < this.subvect_size; ++var13) {
            var18[var24 + var13] = 0.0F;

            for(var14 = 0; var14 <= var13; ++var14) {
               var18[var24 + var13] = (float)((double)var18[var24 + var13] + 0.03125D * (double)this.shape_cb[var25 + var14] * (double)var9[var13 - var14]);
            }
         }

         this.E[var12] = 0.0F;

         for(var13 = 0; var13 < this.subvect_size; ++var13) {
            this.E[var12] += var18[var24 + var13] * var18[var24 + var13];
         }
      }

      for(var13 = 0; var13 < var23; ++var13) {
         var20[var13] = 0.0F;
      }

      for(var12 = 0; var12 < this.nb_subvect; ++var12) {
         var24 = var12 * this.subvect_size;

         for(var13 = 0; var13 < var23; ++var13) {
            var19[var13] = -1.0F;
         }

         int var15;
         for(var13 = 0; var13 < var23; ++var13) {
            if(this.have_sign != 0) {
               VQ.nbest_sign(this.ot[var13], var24, var18, this.subvect_size, this.shape_cb_size, this.E, var23, var21, var22);
            } else {
               VQ.nbest(this.ot[var13], var24, var18, this.subvect_size, this.shape_cb_size, this.E, var23, var21, var22);
            }

            for(var14 = 0; var14 < var23; ++var14) {
               float var26 = 0.0F;
               float[] var30 = this.ot[var13];

               for(var15 = var24; var15 < var24 + this.subvect_size; ++var15) {
                  this.t[var15] = var30[var15];
               }

               float var29 = 1.0F;
               int var27 = var21[var14];
               if(var27 >= this.shape_cb_size) {
                  var29 = -1.0F;
                  var27 -= this.shape_cb_size;
               }

               int var28 = var27 * this.subvect_size;
               if(var29 > 0.0F) {
                  for(var15 = 0; var15 < this.subvect_size; ++var15) {
                     this.t[var24 + var15] -= var18[var28 + var15];
                  }
               } else {
                  for(var15 = 0; var15 < this.subvect_size; ++var15) {
                     this.t[var24 + var15] += var18[var28 + var15];
                  }
               }

               var26 = var20[var13];

               for(var15 = var24; var15 < var24 + this.subvect_size; ++var15) {
                  var26 += this.t[var15] * this.t[var15];
               }

               if(var26 < var19[var23 - 1] || (double)var19[var23 - 1] < -0.5D) {
                  for(var15 = var24 + this.subvect_size; var15 < var6; ++var15) {
                     this.t[var15] = var30[var15];
                  }

                  int var16;
                  int var17;
                  for(var15 = 0; var15 < this.subvect_size; ++var15) {
                     var29 = 1.0F;
                     var28 = var21[var14];
                     if(var28 >= this.shape_cb_size) {
                        var29 = -1.0F;
                        var28 -= this.shape_cb_size;
                     }

                     float var33 = var29 * 0.03125F * (float)this.shape_cb[var28 * this.subvect_size + var15];
                     var17 = this.subvect_size - var15;

                     for(var16 = var24 + this.subvect_size; var16 < var6; ++var17) {
                        this.t[var16] -= var33 * var9[var17];
                        ++var16;
                     }
                  }

                  for(var15 = 0; var15 < var23; ++var15) {
                     if(var26 < var19[var15] || (double)var19[var15] < -0.5D) {
                        for(var16 = var23 - 1; var16 > var15; --var16) {
                           for(var17 = var24 + this.subvect_size; var17 < var6; ++var17) {
                              this.nt[var16][var17] = this.nt[var16 - 1][var17];
                           }

                           for(var17 = 0; var17 < this.nb_subvect; ++var17) {
                              this.nind[var16][var17] = this.nind[var16 - 1][var17];
                           }

                           var19[var16] = var19[var16 - 1];
                        }

                        for(var17 = var24 + this.subvect_size; var17 < var6; ++var17) {
                           this.nt[var15][var17] = this.t[var17];
                        }

                        for(var17 = 0; var17 < this.nb_subvect; ++var17) {
                           this.nind[var15][var17] = this.oind[var13][var17];
                        }

                        this.nind[var15][var12] = var21[var14];
                        var19[var15] = var26;
                        break;
                     }
                  }
               }
            }

            if(var12 == 0) {
               break;
            }
         }

         float[][] var31 = this.ot;
         this.ot = this.nt;
         this.nt = var31;

         for(var13 = 0; var13 < var23; ++var13) {
            for(var15 = 0; var15 < this.nb_subvect; ++var15) {
               this.oind[var13][var15] = this.nind[var13][var15];
            }
         }

         for(var13 = 0; var13 < var23; ++var13) {
            var20[var13] = var19[var13];
         }
      }

      for(var12 = 0; var12 < this.nb_subvect; ++var12) {
         this.ind[var12] = this.nind[0][var12];
         var10.pack(this.ind[var12], this.shape_bits + this.have_sign);
      }

      for(var12 = 0; var12 < this.nb_subvect; ++var12) {
         float var32 = 1.0F;
         var24 = this.ind[var12];
         if(var24 >= this.shape_cb_size) {
            var32 = -1.0F;
            var24 -= this.shape_cb_size;
         }

         for(var13 = 0; var13 < this.subvect_size; ++var13) {
            this.e[this.subvect_size * var12 + var13] = var32 * 0.03125F * (float)this.shape_cb[var24 * this.subvect_size + var13];
         }
      }

      for(var13 = 0; var13 < var6; ++var13) {
         var7[var8 + var13] += this.e[var13];
      }

      Filters.syn_percep_zero(this.e, 0, var2, var3, var4, this.r2, var6, var5);

      for(var13 = 0; var13 < var6; ++var13) {
         var1[var13] -= this.r2[var13];
      }

   }

   public final void unquant(float[] var1, int var2, int var3, Bits var4) {
      int var5;
      for(var5 = 0; var5 < this.nb_subvect; ++var5) {
         if(this.have_sign != 0) {
            this.signs[var5] = var4.unpack(1);
         } else {
            this.signs[var5] = 0;
         }

         this.ind[var5] = var4.unpack(this.shape_bits);
      }

      for(var5 = 0; var5 < this.nb_subvect; ++var5) {
         float var7 = 1.0F;
         if(this.signs[var5] != 0) {
            var7 = -1.0F;
         }

         for(int var6 = 0; var6 < this.subvect_size; ++var6) {
            var1[var2 + this.subvect_size * var5 + var6] += var7 * 0.03125F * (float)this.shape_cb[this.ind[var5] * this.subvect_size + var6];
         }
      }

   }
}
