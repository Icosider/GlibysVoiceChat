package org.xiph.speex;

import java.io.StreamCorruptedException;
import java.util.Random;

public class NbDecoder extends NbCodec implements Decoder {

   private float[] innov2;
   private int count_lost;
   private int last_pitch;
   private float last_pitch_gain;
   private float[] pitch_gain_buf;
   private int pitch_gain_buf_idx;
   private float last_ol_gain;
   protected Random random = new Random();
   protected Stereo stereo = new Stereo();
   protected Inband inband;
   protected boolean enhanced;


   public NbDecoder() {
      this.inband = new Inband(this.stereo);
      this.enhanced = true;
   }

   public void init(int var1, int var2, int var3, int var4) {
      super.init(var1, var2, var3, var4);
      super.filters.init();
      this.innov2 = new float[40];
      this.count_lost = 0;
      this.last_pitch = 40;
      this.last_pitch_gain = 0.0F;
      this.pitch_gain_buf = new float[3];
      this.pitch_gain_buf_idx = 0;
      this.last_ol_gain = 0.0F;
   }

   public int decode(Bits var1, float[] var2) throws StreamCorruptedException {
      int var6 = 0;
      float[] var8 = new float[3];
      float var9 = 0.0F;
      float var10 = 0.0F;
      int var11 = 40;
      float var12 = 0.0F;
      float var13 = 0.0F;
      int var14;
      if(var1 == null && super.dtx_enabled != 0) {
         super.submodeID = 0;
      } else {
         if(var1 == null) {
            this.decodeLost(var2);
            return 0;
         }

         int var7;
         do {
            if(var1.unpack(1) != 0) {
               var7 = var1.unpack(3);
               var14 = SbCodec.SB_FRAME_SIZE[var7];
               if(var14 < 0) {
                  throw new StreamCorruptedException("Invalid sideband mode encountered (1st sideband): " + var7);
               }

               var14 -= 4;
               var1.advance(var14);
               if(var1.unpack(1) != 0) {
                  var7 = var1.unpack(3);
                  var14 = SbCodec.SB_FRAME_SIZE[var7];
                  if(var14 < 0) {
                     throw new StreamCorruptedException("Invalid sideband mode encountered. (2nd sideband): " + var7);
                  }

                  var14 -= 4;
                  var1.advance(var14);
                  if(var1.unpack(1) != 0) {
                     throw new StreamCorruptedException("More than two sideband layers found");
                  }
               }
            }

            var7 = var1.unpack(4);
            if(var7 == 15) {
               return 1;
            }

            if(var7 == 14) {
               this.inband.speexInbandRequest(var1);
            } else if(var7 == 13) {
               this.inband.userInbandRequest(var1);
            } else if(var7 > 8) {
               throw new StreamCorruptedException("Invalid mode encountered: " + var7);
            }
         } while(var7 > 8);

         super.submodeID = var7;
      }

      System.arraycopy(super.frmBuf, super.frameSize, super.frmBuf, 0, super.bufSize - super.frameSize);
      System.arraycopy(super.excBuf, super.frameSize, super.excBuf, 0, super.bufSize - super.frameSize);
      int var3;
      float var26;
      if(super.submodes[super.submodeID] == null) {
         Filters.bw_lpc(0.93F, super.interp_qlpc, super.lpc, 10);
         var26 = 0.0F;

         for(var3 = 0; var3 < super.frameSize; ++var3) {
            var26 += super.innov[var3] * super.innov[var3];
         }

         var26 = (float)Math.sqrt((double)(var26 / (float)super.frameSize));

         for(var3 = super.excIdx; var3 < super.excIdx + super.frameSize; ++var3) {
            super.excBuf[var3] = 3.0F * var26 * (this.random.nextFloat() - 0.5F);
         }

         super.first = 1;
         Filters.iir_mem2(super.excBuf, super.excIdx, super.lpc, super.frmBuf, super.frmIdx, super.frameSize, super.lpcSize, super.mem_sp);
         var2[0] = super.frmBuf[super.frmIdx] + super.preemph * super.pre_mem;

         for(var3 = 1; var3 < super.frameSize; ++var3) {
            var2[var3] = super.frmBuf[super.frmIdx + var3] + super.preemph * var2[var3 - 1];
         }

         super.pre_mem = var2[super.frameSize - 1];
         this.count_lost = 0;
         return 0;
      } else {
         super.submodes[super.submodeID].lsqQuant.unquant(super.qlsp, super.lpcSize, var1);
         if(this.count_lost != 0) {
            var26 = 0.0F;

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               var26 += Math.abs(super.old_qlsp[var3] - super.qlsp[var3]);
            }

            float var15 = (float)(0.6D * Math.exp(-0.2D * (double)var26));

            for(var3 = 0; var3 < 2 * super.lpcSize; ++var3) {
               super.mem_sp[var3] *= var15;
            }
         }

         if(super.first != 0 || this.count_lost != 0) {
            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.old_qlsp[var3] = super.qlsp[var3];
            }
         }

         if(super.submodes[super.submodeID].lbr_pitch != -1) {
            var6 = super.min_pitch + var1.unpack(7);
         }

         if(super.submodes[super.submodeID].forced_pitch_gain != 0) {
            var14 = var1.unpack(4);
            var10 = 0.066667F * (float)var14;
         }

         var14 = var1.unpack(5);
         var9 = (float)Math.exp((double)var14 / 3.5D);
         int var27;
         if(super.submodeID == 1) {
            var27 = var1.unpack(4);
            if(var27 == 15) {
               super.dtx_enabled = 1;
            } else {
               super.dtx_enabled = 0;
            }
         }

         if(super.submodeID > 1) {
            super.dtx_enabled = 0;
         }

         for(int var4 = 0; var4 < super.nbSubframes; ++var4) {
            var27 = super.subframeSize * var4;
            int var16 = super.frmIdx + var27;
            int var17 = super.excIdx + var27;
            float var18 = (1.0F + (float)var4) / (float)super.nbSubframes;

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.interp_qlsp[var3] = (1.0F - var18) * super.old_qlsp[var3] + var18 * super.qlsp[var3];
            }

            Lsp.enforce_margin(super.interp_qlsp, super.lpcSize, 0.002F);

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.interp_qlsp[var3] = (float)Math.cos((double)super.interp_qlsp[var3]);
            }

            super.m_lsp.lsp2lpc(super.interp_qlsp, super.interp_qlpc, super.lpcSize);
            float var21;
            if(this.enhanced) {
               float var19 = 0.9F;
               float var20 = super.submodes[super.submodeID].lpc_enh_k1;
               var21 = super.submodes[super.submodeID].lpc_enh_k2;
               float var22 = (1.0F - (1.0F - var19 * var20) / (1.0F - var19 * var21)) / var19;
               Filters.bw_lpc(var20, super.interp_qlpc, super.awk1, super.lpcSize);
               Filters.bw_lpc(var21, super.interp_qlpc, super.awk2, super.lpcSize);
               Filters.bw_lpc(var22, super.interp_qlpc, super.awk3, super.lpcSize);
            }

            var18 = 1.0F;
            super.pi_gain[var4] = 0.0F;

            for(var3 = 0; var3 <= super.lpcSize; ++var3) {
               super.pi_gain[var4] += var18 * super.interp_qlpc[var3];
               var18 = -var18;
            }

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               super.excBuf[var17 + var3] = 0.0F;
            }

            int var28;
            int var29;
            int var30;
            if(super.submodes[super.submodeID].lbr_pitch != -1) {
               var30 = super.submodes[super.submodeID].lbr_pitch;
               if(var30 != 0) {
                  var28 = var6 - var30 + 1;
                  if(var28 < super.min_pitch) {
                     var28 = super.min_pitch;
                  }

                  var29 = var6 + var30;
                  if(var29 > super.max_pitch) {
                     var29 = super.max_pitch;
                  }
               } else {
                  var28 = var6;
               }
            } else {
               var28 = super.min_pitch;
               var29 = super.max_pitch;
            }

            int var5 = super.submodes[super.submodeID].ltp.unquant(super.excBuf, var17, var28, var10, super.subframeSize, var8, var1, this.count_lost, var27, this.last_pitch_gain);
            if(this.count_lost != 0 && var9 < this.last_ol_gain) {
               var21 = var9 / (this.last_ol_gain + 1.0F);

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  super.excBuf[super.excIdx + var3] *= var21;
               }
            }

            Math.abs(var8[0] + var8[1] + var8[2]);
            var18 = Math.abs(var8[1]);
            if(var8[0] > 0.0F) {
               var18 += var8[0];
            } else {
               var18 = (float)((double)var18 - 0.5D * (double)var8[0]);
            }

            if(var8[2] > 0.0F) {
               var18 += var8[2];
            } else {
               var18 = (float)((double)var18 - 0.5D * (double)var8[0]);
            }

            var13 += var18;
            if(var18 > var12) {
               var11 = var5;
               var12 = var18;
            }

            int var31 = var4 * super.subframeSize;

            for(var3 = var31; var3 < var31 + super.subframeSize; ++var3) {
               super.innov[var3] = 0.0F;
            }

            float var23;
            if(super.submodes[super.submodeID].have_subframe_gain == 3) {
               var30 = var1.unpack(3);
               var23 = (float)((double)var9 * Math.exp((double)NbCodec.exc_gain_quant_scal3[var30]));
            } else if(super.submodes[super.submodeID].have_subframe_gain == 1) {
               var30 = var1.unpack(1);
               var23 = (float)((double)var9 * Math.exp((double)NbCodec.exc_gain_quant_scal1[var30]));
            } else {
               var23 = var9;
            }

            if(super.submodes[super.submodeID].innovation != null) {
               super.submodes[super.submodeID].innovation.unquant(super.innov, var31, super.subframeSize, var1);
            }

            for(var3 = var31; var3 < var31 + super.subframeSize; ++var3) {
               super.innov[var3] *= var23;
            }

            if(super.submodeID == 1) {
               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  super.excBuf[var17 + var3] = 0.0F;
               }

               for(; super.voc_offset < super.subframeSize; super.voc_offset += var6) {
                  if(super.voc_offset >= 0) {
                     super.excBuf[var17 + super.voc_offset] = (float)Math.sqrt((double)(1.0F * (float)var6));
                  }
               }

               super.voc_offset -= super.subframeSize;
               float var24 = 0.5F + 2.0F * (var10 - 0.6F);
               if(var24 < 0.0F) {
                  var24 = 0.0F;
               }

               if(var24 > 1.0F) {
                  var24 = 1.0F;
               }

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  float var25 = super.excBuf[var17 + var3];
                  super.excBuf[var17 + var3] = 0.8F * var24 * super.excBuf[var17 + var3] * var9 + 0.6F * var24 * super.voc_m1 * var9 + 0.5F * var24 * super.innov[var31 + var3] - 0.5F * var24 * super.voc_m2 + (1.0F - var24) * super.innov[var31 + var3];
                  super.voc_m1 = var25;
                  super.voc_m2 = super.innov[var31 + var3];
                  super.voc_mean = 0.95F * super.voc_mean + 0.05F * super.excBuf[var17 + var3];
                  super.excBuf[var17 + var3] -= super.voc_mean;
               }
            } else {
               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  super.excBuf[var17 + var3] += super.innov[var31 + var3];
               }
            }

            if(super.submodes[super.submodeID].double_codebook != 0) {
               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  this.innov2[var3] = 0.0F;
               }

               super.submodes[super.submodeID].innovation.unquant(this.innov2, 0, super.subframeSize, var1);

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  this.innov2[var3] = (float)((double)this.innov2[var3] * (double)var23 * 0.45454545454545453D);
               }

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  super.excBuf[var17 + var3] += this.innov2[var3];
               }
            }

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               super.frmBuf[var16 + var3] = super.excBuf[var17 + var3];
            }

            if(this.enhanced && super.submodes[super.submodeID].comb_gain > 0.0F) {
               super.filters.comb_filter(super.excBuf, var17, super.frmBuf, var16, super.subframeSize, var5, var8, super.submodes[super.submodeID].comb_gain);
            }

            if(this.enhanced) {
               Filters.filter_mem2(super.frmBuf, var16, super.awk2, super.awk1, super.subframeSize, super.lpcSize, super.mem_sp, super.lpcSize);
               Filters.filter_mem2(super.frmBuf, var16, super.awk3, super.interp_qlpc, super.subframeSize, super.lpcSize, super.mem_sp, 0);
            } else {
               for(var3 = 0; var3 < super.lpcSize; ++var3) {
                  super.mem_sp[super.lpcSize + var3] = 0.0F;
               }

               Filters.iir_mem2(super.frmBuf, var16, super.interp_qlpc, super.frmBuf, var16, super.subframeSize, super.lpcSize, super.mem_sp);
            }
         }

         var2[0] = super.frmBuf[super.frmIdx] + super.preemph * super.pre_mem;

         for(var3 = 1; var3 < super.frameSize; ++var3) {
            var2[var3] = super.frmBuf[super.frmIdx + var3] + super.preemph * var2[var3 - 1];
         }

         super.pre_mem = var2[super.frameSize - 1];

         for(var3 = 0; var3 < super.lpcSize; ++var3) {
            super.old_qlsp[var3] = super.qlsp[var3];
         }

         super.first = 0;
         this.count_lost = 0;
         this.last_pitch = var11;
         this.last_pitch_gain = 0.25F * var13;
         this.pitch_gain_buf[this.pitch_gain_buf_idx++] = this.last_pitch_gain;
         if(this.pitch_gain_buf_idx > 2) {
            this.pitch_gain_buf_idx = 0;
         }

         this.last_ol_gain = var9;
         return 0;
      }
   }

   public int decodeLost(float[] var1) {
      float var4 = (float)Math.exp(-0.04D * (double)this.count_lost * (double)this.count_lost);
      float var5 = this.pitch_gain_buf[0] < this.pitch_gain_buf[1]?(this.pitch_gain_buf[1] < this.pitch_gain_buf[2]?this.pitch_gain_buf[1]:(this.pitch_gain_buf[0] < this.pitch_gain_buf[2]?this.pitch_gain_buf[2]:this.pitch_gain_buf[0])):(this.pitch_gain_buf[2] < this.pitch_gain_buf[1]?this.pitch_gain_buf[1]:(this.pitch_gain_buf[2] < this.pitch_gain_buf[0]?this.pitch_gain_buf[2]:this.pitch_gain_buf[0]));
      if(var5 < this.last_pitch_gain) {
         this.last_pitch_gain = var5;
      }

      float var3 = this.last_pitch_gain;
      if(var3 > 0.95F) {
         var3 = 0.95F;
      }

      var3 *= var4;
      System.arraycopy(super.frmBuf, super.frameSize, super.frmBuf, 0, super.bufSize - super.frameSize);
      System.arraycopy(super.excBuf, super.frameSize, super.excBuf, 0, super.bufSize - super.frameSize);

      int var2;
      for(int var6 = 0; var6 < super.nbSubframes; ++var6) {
         int var7 = super.subframeSize * var6;
         int var8 = super.frmIdx + var7;
         int var9 = super.excIdx + var7;
         float var10;
         if(this.enhanced) {
            var10 = 0.9F;
            float var11;
            float var12;
            if(super.submodes[super.submodeID] != null) {
               var11 = super.submodes[super.submodeID].lpc_enh_k1;
               var12 = super.submodes[super.submodeID].lpc_enh_k2;
            } else {
               var12 = 0.7F;
               var11 = 0.7F;
            }

            float var13 = (1.0F - (1.0F - var10 * var11) / (1.0F - var10 * var12)) / var10;
            Filters.bw_lpc(var11, super.interp_qlpc, super.awk1, super.lpcSize);
            Filters.bw_lpc(var12, super.interp_qlpc, super.awk2, super.lpcSize);
            Filters.bw_lpc(var13, super.interp_qlpc, super.awk3, super.lpcSize);
         }

         var10 = 0.0F;

         for(var2 = 0; var2 < super.frameSize; ++var2) {
            var10 += super.innov[var2] * super.innov[var2];
         }

         var10 = (float)Math.sqrt((double)(var10 / (float)super.frameSize));

         for(var2 = 0; var2 < super.subframeSize; ++var2) {
            super.excBuf[var9 + var2] = var3 * super.excBuf[var9 + var2 - this.last_pitch] + var4 * (float)Math.sqrt((double)(1.0F - var3)) * 3.0F * var10 * (this.random.nextFloat() - 0.5F);
         }

         for(var2 = 0; var2 < super.subframeSize; ++var2) {
            super.frmBuf[var8 + var2] = super.excBuf[var9 + var2];
         }

         if(this.enhanced) {
            Filters.filter_mem2(super.frmBuf, var8, super.awk2, super.awk1, super.subframeSize, super.lpcSize, super.mem_sp, super.lpcSize);
            Filters.filter_mem2(super.frmBuf, var8, super.awk3, super.interp_qlpc, super.subframeSize, super.lpcSize, super.mem_sp, 0);
         } else {
            for(var2 = 0; var2 < super.lpcSize; ++var2) {
               super.mem_sp[super.lpcSize + var2] = 0.0F;
            }

            Filters.iir_mem2(super.frmBuf, var8, super.interp_qlpc, super.frmBuf, var8, super.subframeSize, super.lpcSize, super.mem_sp);
         }
      }

      var1[0] = super.frmBuf[0] + super.preemph * super.pre_mem;

      for(var2 = 1; var2 < super.frameSize; ++var2) {
         var1[var2] = super.frmBuf[var2] + super.preemph * var1[var2 - 1];
      }

      super.pre_mem = var1[super.frameSize - 1];
      super.first = 0;
      ++this.count_lost;
      this.pitch_gain_buf[this.pitch_gain_buf_idx++] = var3;
      if(this.pitch_gain_buf_idx > 2) {
         this.pitch_gain_buf_idx = 0;
      }

      return 0;
   }

   public void decodeStereo(float[] var1, int var2) {
      this.stereo.decode(var1, var2);
   }

   public void setPerceptualEnhancement(boolean var1) {
      this.enhanced = var1;
   }

   public boolean getPerceptualEnhancement() {
      return this.enhanced;
   }
}
