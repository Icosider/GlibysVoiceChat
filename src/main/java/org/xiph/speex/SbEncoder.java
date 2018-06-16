package org.xiph.speex;

public class SbEncoder extends SbCodec implements Encoder {

   public static final int[] NB_QUALITY_MAP = new int[]{1, 8, 2, 3, 4, 5, 5, 6, 6, 7, 7};
   public static final int[] WB_QUALITY_MAP = new int[]{1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4};
   public static final int[] UWB_QUALITY_MAP = new int[]{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
   protected Encoder lowenc;
   private float[] x1d;
   private float[] h0_mem;
   private float[] buf;
   private float[] swBuf;
   private float[] res;
   private float[] target;
   private float[] window;
   private float[] lagWindow;
   private float[] rc;
   private float[] autocorr;
   private float[] lsp;
   private float[] old_lsp;
   private float[] interp_lsp;
   private float[] interp_lpc;
   private float[] bw_lpc1;
   private float[] bw_lpc2;
   private float[] mem_sp2;
   private float[] mem_sw;
   protected int nb_modes;
   private boolean uwb;
   protected int complexity;
   protected int vbr_enabled;
   protected int vad_enabled;
   protected int abr_enabled;
   protected float vbr_quality;
   protected float relative_quality;
   protected float abr_drift;
   protected float abr_drift2;
   protected float abr_count;
   protected int sampling_rate;
   protected int submodeSelect;


   public void wbinit() {
      this.lowenc = new NbEncoder();
      ((NbEncoder)this.lowenc).nbinit();
      super.wbinit();
      this.init(160, 40, 8, 640, 0.9F);
      this.uwb = false;
      this.nb_modes = 5;
      this.sampling_rate = 16000;
   }

   public void uwbinit() {
      this.lowenc = new SbEncoder();
      ((SbEncoder)this.lowenc).wbinit();
      super.uwbinit();
      this.init(320, 80, 8, 1280, 0.7F);
      this.uwb = true;
      this.nb_modes = 2;
      this.sampling_rate = 32000;
   }

   public void init(int var1, int var2, int var3, int var4, float var5) {
      super.init(var1, var2, var3, var4, var5);
      this.complexity = 3;
      this.vbr_enabled = 0;
      this.vad_enabled = 0;
      this.abr_enabled = 0;
      this.vbr_quality = 8.0F;
      this.submodeSelect = super.submodeID;
      this.x1d = new float[var1];
      this.h0_mem = new float[64];
      this.buf = new float[super.windowSize];
      this.swBuf = new float[var1];
      this.res = new float[var1];
      this.target = new float[var2];
      this.window = Misc.window(super.windowSize, var2);
      this.lagWindow = Misc.lagWindow(var3, super.lag_factor);
      this.rc = new float[var3];
      this.autocorr = new float[var3 + 1];
      this.lsp = new float[var3];
      this.old_lsp = new float[var3];
      this.interp_lsp = new float[var3];
      this.interp_lpc = new float[var3 + 1];
      this.bw_lpc1 = new float[var3 + 1];
      this.bw_lpc2 = new float[var3 + 1];
      this.mem_sp2 = new float[var3];
      this.mem_sw = new float[var3];
      this.abr_count = 0.0F;
   }

   public int encode(Bits var1, float[] var2) {
      Filters.qmf_decomp(var2, Codebook.h0, super.x0d, this.x1d, super.fullFrameSize, 64, this.h0_mem);
      this.lowenc.encode(var1, super.x0d);

      int var3;
      for(var3 = 0; var3 < super.windowSize - super.frameSize; ++var3) {
         super.high[var3] = super.high[super.frameSize + var3];
      }

      for(var3 = 0; var3 < super.frameSize; ++var3) {
         super.high[super.windowSize - super.frameSize + var3] = this.x1d[var3];
      }

      System.arraycopy(super.excBuf, super.frameSize, super.excBuf, 0, super.bufSize - super.frameSize);
      float[] var7 = this.lowenc.getPiGain();
      float[] var8 = this.lowenc.getExc();
      float[] var9 = this.lowenc.getInnov();
      int var11 = this.lowenc.getMode();
      boolean var10;
      if(var11 == 0) {
         var10 = true;
      } else {
         var10 = false;
      }

      for(var3 = 0; var3 < super.windowSize; ++var3) {
         this.buf[var3] = super.high[var3] * this.window[var3];
      }

      Lpc.autocorr(this.buf, this.autocorr, super.lpcSize + 1, super.windowSize);
      ++this.autocorr[0];
      this.autocorr[0] *= super.lpc_floor;

      for(var3 = 0; var3 < super.lpcSize + 1; ++var3) {
         this.autocorr[var3] *= this.lagWindow[var3];
      }

      Lpc.wld(super.lpc, this.autocorr, this.rc, super.lpcSize);
      System.arraycopy(super.lpc, 0, super.lpc, 1, super.lpcSize);
      super.lpc[0] = 1.0F;
      int var12 = Lsp.lpc2lsp(super.lpc, super.lpcSize, this.lsp, 15, 0.2F);
      if(var12 != super.lpcSize) {
         var12 = Lsp.lpc2lsp(super.lpc, super.lpcSize, this.lsp, 11, 0.02F);
         if(var12 != super.lpcSize) {
            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.lsp[var3] = (float)Math.cos(3.141592653589793D * (double)((float)(var3 + 1)) / (double)(super.lpcSize + 1));
            }
         }
      }

      for(var3 = 0; var3 < super.lpcSize; ++var3) {
         this.lsp[var3] = (float)Math.acos((double)this.lsp[var3]);
      }

      float var13 = 0.0F;

      for(var3 = 0; var3 < super.lpcSize; ++var3) {
         var13 += (this.old_lsp[var3] - this.lsp[var3]) * (this.old_lsp[var3] - this.lsp[var3]);
      }

      float var15;
      float var16;
      int var32;
      if((this.vbr_enabled != 0 || this.vad_enabled != 0) && !var10) {
         float var14 = 0.0F;
         var15 = 0.0F;
         if(this.abr_enabled != 0) {
            float var17 = 0.0F;
            if(this.abr_drift2 * this.abr_drift > 0.0F) {
               var17 = -1.0E-5F * this.abr_drift / (1.0F + this.abr_count);
               if(var17 > 0.1F) {
                  var17 = 0.1F;
               }

               if(var17 < -0.1F) {
                  var17 = -0.1F;
               }
            }

            this.vbr_quality += var17;
            if(this.vbr_quality > 10.0F) {
               this.vbr_quality = 10.0F;
            }

            if(this.vbr_quality < 0.0F) {
               this.vbr_quality = 0.0F;
            }
         }

         for(var3 = 0; var3 < super.frameSize; ++var3) {
            var14 += super.x0d[var3] * super.x0d[var3];
            var15 += super.high[var3] * super.high[var3];
         }

         var16 = (float)Math.log((double)((1.0F + var15) / (1.0F + var14)));
         this.relative_quality = this.lowenc.getRelativeQuality();
         if(var16 < -4.0F) {
            var16 = -4.0F;
         }

         if(var16 > 2.0F) {
            var16 = 2.0F;
         }

         if(this.vbr_enabled != 0) {
            var32 = this.nb_modes - 1;
            this.relative_quality = (float)((double)this.relative_quality + 1.0D * (double)(var16 + 2.0F));
            if(this.relative_quality < -1.0F) {
               this.relative_quality = -1.0F;
            }

            int var18;
            while(var32 != 0) {
               var18 = (int)Math.floor((double)this.vbr_quality);
               float var19;
               if(var18 == 10) {
                  var19 = Vbr.hb_thresh[var32][var18];
               } else {
                  var19 = (this.vbr_quality - (float)var18) * Vbr.hb_thresh[var32][var18 + 1] + ((float)(1 + var18) - this.vbr_quality) * Vbr.hb_thresh[var32][var18];
               }

               if(this.relative_quality >= var19) {
                  break;
               }

               --var32;
            }

            this.setMode(var32);
            if(this.abr_enabled != 0) {
               var18 = this.getBitRate();
               this.abr_drift += (float)(var18 - this.abr_enabled);
               this.abr_drift2 = 0.95F * this.abr_drift2 + 0.05F * (float)(var18 - this.abr_enabled);
               this.abr_count = (float)((double)this.abr_count + 1.0D);
            }
         } else {
            if((double)this.relative_quality < 2.0D) {
               var32 = 1;
            } else {
               var32 = this.submodeSelect;
            }

            super.submodeID = var32;
         }
      }

      var1.pack(1, 1);
      if(var10) {
         var1.pack(0, 3);
      } else {
         var1.pack(super.submodeID, 3);
      }

      if(!var10 && super.submodes[super.submodeID] != null) {
         super.submodes[super.submodeID].lsqQuant.quant(this.lsp, super.qlsp, super.lpcSize, var1);
         if(super.first != 0) {
            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.old_lsp[var3] = this.lsp[var3];
            }

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.old_qlsp[var3] = super.qlsp[var3];
            }
         }

         float[] var4 = new float[super.lpcSize];
         float[] var6 = new float[super.subframeSize];
         float[] var5 = new float[super.subframeSize];

         for(int var31 = 0; var31 < super.nbSubframes; ++var31) {
            float var24 = 0.0F;
            float var25 = 0.0F;
            int var21 = super.subframeSize * var31;
            var32 = super.excIdx + var21;
            int var20 = var21;
            int var33 = var21;
            var15 = (1.0F + (float)var31) / (float)super.nbSubframes;

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.interp_lsp[var3] = (1.0F - var15) * this.old_lsp[var3] + var15 * this.lsp[var3];
            }

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.interp_qlsp[var3] = (1.0F - var15) * super.old_qlsp[var3] + var15 * super.qlsp[var3];
            }

            Lsp.enforce_margin(this.interp_lsp, super.lpcSize, 0.05F);
            Lsp.enforce_margin(super.interp_qlsp, super.lpcSize, 0.05F);

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.interp_lsp[var3] = (float)Math.cos((double)this.interp_lsp[var3]);
            }

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.interp_qlsp[var3] = (float)Math.cos((double)super.interp_qlsp[var3]);
            }

            super.m_lsp.lsp2lpc(this.interp_lsp, this.interp_lpc, super.lpcSize);
            super.m_lsp.lsp2lpc(super.interp_qlsp, super.interp_qlpc, super.lpcSize);
            Filters.bw_lpc(super.gamma1, this.interp_lpc, this.bw_lpc1, super.lpcSize);
            Filters.bw_lpc(super.gamma2, this.interp_lpc, this.bw_lpc2, super.lpcSize);
            float var23 = 0.0F;
            float var22 = 0.0F;
            var15 = 1.0F;
            super.pi_gain[var31] = 0.0F;

            for(var3 = 0; var3 <= super.lpcSize; ++var3) {
               var23 += var15 * super.interp_qlpc[var3];
               var15 = -var15;
               super.pi_gain[var31] += super.interp_qlpc[var3];
            }

            var22 = var7[var31];
            var22 = 1.0F / (Math.abs(var22) + 0.01F);
            var23 = 1.0F / (Math.abs(var23) + 0.01F);
            var16 = Math.abs(0.01F + var23) / (0.01F + Math.abs(var22));
            boolean var26 = var16 < 5.0F;
            var26 = false;
            Filters.fir_mem2(super.high, var21, super.interp_qlpc, super.excBuf, var32, super.subframeSize, super.lpcSize, this.mem_sp2);

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               var24 += super.excBuf[var32 + var3] * super.excBuf[var32 + var3];
            }

            float var27;
            if(super.submodes[super.submodeID].innovation == null) {
               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  var25 += var9[var21 + var3] * var9[var21 + var3];
               }

               var27 = var24 / (0.01F + var25);
               var27 = (float)Math.sqrt((double)var27);
               var27 *= var16;
               int var28 = (int)Math.floor(10.5D + 8.0D * Math.log((double)var27 + 1.0E-4D));
               if(var28 < 0) {
                  var28 = 0;
               }

               if(var28 > 31) {
                  var28 = 31;
               }

               var1.pack(var28, 5);
               var27 = (float)(0.1D * Math.exp((double)var28 / 9.4D));
               float var10000 = var27 / var16;
            } else {
               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  var25 += var8[var21 + var3] * var8[var21 + var3];
               }

               var27 = (float)(Math.sqrt((double)(1.0F + var24)) * (double)var16 / Math.sqrt((double)((1.0F + var25) * (float)super.subframeSize)));
               int var30 = (int)Math.floor(0.5D + 3.7D * (Math.log((double)var27) + 2.0D));
               if(var30 < 0) {
                  var30 = 0;
               }

               if(var30 > 15) {
                  var30 = 15;
               }

               var1.pack(var30, 4);
               var27 = (float)Math.exp(0.27027027027027023D * (double)var30 - 2.0D);
               float var34 = var27 * (float)Math.sqrt((double)(1.0F + var25)) / var16;
               float var29 = 1.0F / var34;

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  super.excBuf[var32 + var3] = 0.0F;
               }

               super.excBuf[var32] = 1.0F;
               Filters.syn_percep_zero(super.excBuf, var32, super.interp_qlpc, this.bw_lpc1, this.bw_lpc2, var6, super.subframeSize, super.lpcSize);

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  super.excBuf[var32 + var3] = 0.0F;
               }

               for(var3 = 0; var3 < super.lpcSize; ++var3) {
                  var4[var3] = super.mem_sp[var3];
               }

               Filters.iir_mem2(super.excBuf, var32, super.interp_qlpc, super.excBuf, var32, super.subframeSize, super.lpcSize, var4);

               for(var3 = 0; var3 < super.lpcSize; ++var3) {
                  var4[var3] = this.mem_sw[var3];
               }

               Filters.filter_mem2(super.excBuf, var32, this.bw_lpc1, this.bw_lpc2, this.res, var21, super.subframeSize, super.lpcSize, var4, 0);

               for(var3 = 0; var3 < super.lpcSize; ++var3) {
                  var4[var3] = this.mem_sw[var3];
               }

               Filters.filter_mem2(super.high, var21, this.bw_lpc1, this.bw_lpc2, this.swBuf, var21, super.subframeSize, super.lpcSize, var4, 0);

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  this.target[var3] = this.swBuf[var33 + var3] - this.res[var20 + var3];
               }

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  super.excBuf[var32 + var3] = 0.0F;
               }

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  this.target[var3] *= var29;
               }

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  var5[var3] = 0.0F;
               }

               super.submodes[super.submodeID].innovation.quant(this.target, super.interp_qlpc, this.bw_lpc1, this.bw_lpc2, super.lpcSize, super.subframeSize, var5, 0, var6, var1, this.complexity + 1 >> 1);

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  super.excBuf[var32 + var3] += var5[var3] * var34;
               }

               if(super.submodes[super.submodeID].double_codebook != 0) {
                  float[] var35 = new float[super.subframeSize];

                  for(var3 = 0; var3 < super.subframeSize; ++var3) {
                     var35[var3] = 0.0F;
                  }

                  for(var3 = 0; var3 < super.subframeSize; ++var3) {
                     this.target[var3] = (float)((double)this.target[var3] * 2.5D);
                  }

                  super.submodes[super.submodeID].innovation.quant(this.target, super.interp_qlpc, this.bw_lpc1, this.bw_lpc2, super.lpcSize, super.subframeSize, var35, 0, var6, var1, this.complexity + 1 >> 1);

                  for(var3 = 0; var3 < super.subframeSize; ++var3) {
                     var35[var3] = (float)((double)var35[var3] * (double)var34 * 0.4D);
                  }

                  for(var3 = 0; var3 < super.subframeSize; ++var3) {
                     super.excBuf[var32 + var3] += var35[var3];
                  }
               }
            }

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               var4[var3] = super.mem_sp[var3];
            }

            Filters.iir_mem2(super.excBuf, var32, super.interp_qlpc, super.high, var21, super.subframeSize, super.lpcSize, super.mem_sp);
            Filters.filter_mem2(super.high, var21, this.bw_lpc1, this.bw_lpc2, this.swBuf, var33, super.subframeSize, super.lpcSize, this.mem_sw, 0);
         }

         super.filters.fir_mem_up(super.x0d, Codebook.h0, super.y0, super.fullFrameSize, 64, super.g0_mem);
         super.filters.fir_mem_up(super.high, Codebook.h1, super.y1, super.fullFrameSize, 64, super.g1_mem);

         for(var3 = 0; var3 < super.fullFrameSize; ++var3) {
            var2[var3] = 2.0F * (super.y0[var3] - super.y1[var3]);
         }

         for(var3 = 0; var3 < super.lpcSize; ++var3) {
            this.old_lsp[var3] = this.lsp[var3];
         }

         for(var3 = 0; var3 < super.lpcSize; ++var3) {
            super.old_qlsp[var3] = super.qlsp[var3];
         }

         super.first = 0;
         return 1;
      } else {
         for(var3 = 0; var3 < super.frameSize; ++var3) {
            super.excBuf[super.excIdx + var3] = this.swBuf[var3] = 0.0F;
         }

         for(var3 = 0; var3 < super.lpcSize; ++var3) {
            this.mem_sw[var3] = 0.0F;
         }

         super.first = 1;
         Filters.iir_mem2(super.excBuf, super.excIdx, super.interp_qlpc, super.high, 0, super.subframeSize, super.lpcSize, super.mem_sp);
         super.filters.fir_mem_up(super.x0d, Codebook.h0, super.y0, super.fullFrameSize, 64, super.g0_mem);
         super.filters.fir_mem_up(super.high, Codebook.h1, super.y1, super.fullFrameSize, 64, super.g1_mem);

         for(var3 = 0; var3 < super.fullFrameSize; ++var3) {
            var2[var3] = 2.0F * (super.y0[var3] - super.y1[var3]);
         }

         return var10?0:1;
      }
   }

   public int getEncodedFrameSize() {
      int var1 = SbCodec.SB_FRAME_SIZE[super.submodeID];
      var1 += this.lowenc.getEncodedFrameSize();
      return var1;
   }

   public void setQuality(int var1) {
      if(var1 < 0) {
         var1 = 0;
      }

      if(var1 > 10) {
         var1 = 10;
      }

      if(this.uwb) {
         this.lowenc.setQuality(var1);
         this.setMode(UWB_QUALITY_MAP[var1]);
      } else {
         this.lowenc.setMode(NB_QUALITY_MAP[var1]);
         this.setMode(WB_QUALITY_MAP[var1]);
      }

   }

   public void setVbrQuality(float var1) {
      this.vbr_quality = var1;
      float var2 = var1 + 0.6F;
      if(var2 > 10.0F) {
         var2 = 10.0F;
      }

      this.lowenc.setVbrQuality(var2);
      int var3 = (int)Math.floor(0.5D + (double)var1);
      if(var3 > 10) {
         var3 = 10;
      }

      this.setQuality(var3);
   }

   public void setVbr(boolean var1) {
      this.vbr_enabled = var1?1:0;
      this.lowenc.setVbr(var1);
   }

   public void setAbr(int var1) {
      this.lowenc.setVbr(true);
      this.abr_enabled = var1 != 0?1:0;
      this.vbr_enabled = 1;
      int var2 = 10;

      for(int var4 = var1; var2 >= 0; --var2) {
         this.setQuality(var2);
         int var3 = this.getBitRate();
         if(var3 <= var4) {
            break;
         }
      }

      float var5 = (float)var2;
      if(var5 < 0.0F) {
         var5 = 0.0F;
      }

      this.setVbrQuality(var5);
      this.abr_count = 0.0F;
      this.abr_drift = 0.0F;
      this.abr_drift2 = 0.0F;
   }

   public int getBitRate() {
      return super.submodes[super.submodeID] != null?this.lowenc.getBitRate() + this.sampling_rate * super.submodes[super.submodeID].bits_per_frame / super.frameSize:this.lowenc.getBitRate() + this.sampling_rate * 4 / super.frameSize;
   }

   public void setSamplingRate(int var1) {
      this.sampling_rate = var1;
      this.lowenc.setSamplingRate(var1);
   }

   public int getLookAhead() {
      return 2 * this.lowenc.getLookAhead() + 64 - 1;
   }

   public void setMode(int var1) {
      if(var1 < 0) {
         var1 = 0;
      }

      super.submodeID = this.submodeSelect = var1;
   }

   public int getMode() {
      return super.submodeID;
   }

   public void setBitRate(int var1) {
      for(int var2 = 10; var2 >= 0; --var2) {
         this.setQuality(var2);
         if(this.getBitRate() <= var1) {
            return;
         }
      }

   }

   public boolean getVbr() {
      return this.vbr_enabled != 0;
   }

   public void setVad(boolean var1) {
      this.vad_enabled = var1?1:0;
   }

   public boolean getVad() {
      return this.vad_enabled != 0;
   }

   public void setDtx(boolean var1) {
      super.dtx_enabled = var1?1:0;
   }

   public int getAbr() {
      return this.abr_enabled;
   }

   public float getVbrQuality() {
      return this.vbr_quality;
   }

   public void setComplexity(int var1) {
      if(var1 < 0) {
         var1 = 0;
      }

      if(var1 > 10) {
         var1 = 10;
      }

      this.complexity = var1;
   }

   public int getComplexity() {
      return this.complexity;
   }

   public int getSamplingRate() {
      return this.sampling_rate;
   }

   public float getRelativeQuality() {
      return this.relative_quality;
   }

}
