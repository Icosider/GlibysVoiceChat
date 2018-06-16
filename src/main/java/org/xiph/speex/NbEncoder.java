package org.xiph.speex;

public class NbEncoder extends NbCodec implements Encoder {

   public static final int[] NB_QUALITY_MAP = new int[]{1, 8, 2, 3, 3, 4, 4, 5, 5, 6, 7};
   private int bounded_pitch;
   private int[] pitch;
   private float pre_mem2;
   private float[] exc2Buf;
   private int exc2Idx;
   private float[] swBuf;
   private int swIdx;
   private float[] window;
   private float[] buf2;
   private float[] autocorr;
   private float[] lagWindow;
   private float[] lsp;
   private float[] old_lsp;
   private float[] interp_lsp;
   private float[] interp_lpc;
   private float[] bw_lpc1;
   private float[] bw_lpc2;
   private float[] rc;
   private float[] mem_sw;
   private float[] mem_sw_whole;
   private float[] mem_exc;
   private Vbr vbr;
   private int dtx_count;
   private float[] innov2;
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


   public void init(int var1, int var2, int var3, int var4) {
      super.init(var1, var2, var3, var4);
      this.complexity = 3;
      this.vbr_enabled = 0;
      this.vad_enabled = 0;
      this.abr_enabled = 0;
      this.vbr_quality = 8.0F;
      this.submodeSelect = 5;
      this.pre_mem2 = 0.0F;
      this.bounded_pitch = 1;
      this.exc2Buf = new float[var4];
      this.exc2Idx = var4 - super.windowSize;
      this.swBuf = new float[var4];
      this.swIdx = var4 - super.windowSize;
      this.window = Misc.window(super.windowSize, var2);
      this.lagWindow = Misc.lagWindow(var3, super.lag_factor);
      this.autocorr = new float[var3 + 1];
      this.buf2 = new float[super.windowSize];
      this.interp_lpc = new float[var3 + 1];
      super.interp_qlpc = new float[var3 + 1];
      this.bw_lpc1 = new float[var3 + 1];
      this.bw_lpc2 = new float[var3 + 1];
      this.lsp = new float[var3];
      super.qlsp = new float[var3];
      this.old_lsp = new float[var3];
      super.old_qlsp = new float[var3];
      this.interp_lsp = new float[var3];
      super.interp_qlsp = new float[var3];
      this.rc = new float[var3];
      super.mem_sp = new float[var3];
      this.mem_sw = new float[var3];
      this.mem_sw_whole = new float[var3];
      this.mem_exc = new float[var3];
      this.vbr = new Vbr();
      this.dtx_count = 0;
      this.abr_count = 0.0F;
      this.sampling_rate = 8000;
      super.awk1 = new float[var3 + 1];
      super.awk2 = new float[var3 + 1];
      super.awk3 = new float[var3 + 1];
      this.innov2 = new float[40];
      super.filters.init();
      this.pitch = new int[super.nbSubframes];
   }

   public int encode(Bits var1, float[] var2) {
      System.arraycopy(super.frmBuf, super.frameSize, super.frmBuf, 0, super.bufSize - super.frameSize);
      super.frmBuf[super.bufSize - super.frameSize] = var2[0] - super.preemph * super.pre_mem;

      int var3;
      for(var3 = 1; var3 < super.frameSize; ++var3) {
         super.frmBuf[super.bufSize - super.frameSize + var3] = var2[var3] - super.preemph * var2[var3 - 1];
      }

      super.pre_mem = var2[super.frameSize - 1];
      System.arraycopy(this.exc2Buf, super.frameSize, this.exc2Buf, 0, super.bufSize - super.frameSize);
      System.arraycopy(super.excBuf, super.frameSize, super.excBuf, 0, super.bufSize - super.frameSize);
      System.arraycopy(this.swBuf, super.frameSize, this.swBuf, 0, super.bufSize - super.frameSize);

      for(var3 = 0; var3 < super.windowSize; ++var3) {
         this.buf2[var3] = super.frmBuf[var3 + super.frmIdx] * this.window[var3];
      }

      Lpc.autocorr(this.buf2, this.autocorr, super.lpcSize + 1, super.windowSize);
      this.autocorr[0] += 10.0F;
      this.autocorr[0] *= super.lpc_floor;

      for(var3 = 0; var3 < super.lpcSize + 1; ++var3) {
         this.autocorr[var3] *= this.lagWindow[var3];
      }

      Lpc.wld(super.lpc, this.autocorr, this.rc, super.lpcSize);
      System.arraycopy(super.lpc, 0, super.lpc, 1, super.lpcSize);
      super.lpc[0] = 1.0F;
      int var9 = Lsp.lpc2lsp(super.lpc, super.lpcSize, this.lsp, 15, 0.2F);
      if(var9 == super.lpcSize) {
         for(var3 = 0; var3 < super.lpcSize; ++var3) {
            this.lsp[var3] = (float)Math.acos((double)this.lsp[var3]);
         }
      } else {
         if(this.complexity > 1) {
            var9 = Lsp.lpc2lsp(super.lpc, super.lpcSize, this.lsp, 11, 0.05F);
         }

         if(var9 == super.lpcSize) {
            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.lsp[var3] = (float)Math.acos((double)this.lsp[var3]);
            }
         } else {
            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.lsp[var3] = this.old_lsp[var3];
            }
         }
      }

      float var10 = 0.0F;

      for(var3 = 0; var3 < super.lpcSize; ++var3) {
         var10 += (this.old_lsp[var3] - this.lsp[var3]) * (this.old_lsp[var3] - this.lsp[var3]);
      }

      if(super.first != 0) {
         for(var3 = 0; var3 < super.lpcSize; ++var3) {
            this.interp_lsp[var3] = this.lsp[var3];
         }
      } else {
         for(var3 = 0; var3 < super.lpcSize; ++var3) {
            this.interp_lsp[var3] = 0.375F * this.old_lsp[var3] + 0.625F * this.lsp[var3];
         }
      }

      Lsp.enforce_margin(this.interp_lsp, super.lpcSize, 0.002F);

      for(var3 = 0; var3 < super.lpcSize; ++var3) {
         this.interp_lsp[var3] = (float)Math.cos((double)this.interp_lsp[var3]);
      }

      super.m_lsp.lsp2lpc(this.interp_lsp, this.interp_lpc, super.lpcSize);
      int var12;
      float var13;
      if(super.submodes[super.submodeID] != null && this.vbr_enabled == 0 && this.vad_enabled == 0 && super.submodes[super.submodeID].forced_pitch_gain == 0 && super.submodes[super.submodeID].lbr_pitch == -1) {
         var12 = 0;
         var13 = 0.0F;
      } else {
         int[] var14 = new int[6];
         float[] var15 = new float[6];
         Filters.bw_lpc(super.gamma1, this.interp_lpc, this.bw_lpc1, super.lpcSize);
         Filters.bw_lpc(super.gamma2, this.interp_lpc, this.bw_lpc2, super.lpcSize);
         Filters.filter_mem2(super.frmBuf, super.frmIdx, this.bw_lpc1, this.bw_lpc2, this.swBuf, this.swIdx, super.frameSize, super.lpcSize, this.mem_sw_whole, 0);
         Ltp.open_loop_nbest_pitch(this.swBuf, this.swIdx, super.min_pitch, super.max_pitch, super.frameSize, var14, var15, 6);
         var12 = var14[0];
         var13 = var15[0];

         for(var3 = 1; var3 < 6; ++var3) {
            if((double)var15[var3] > 0.85D * (double)var13 && (Math.abs((double)var14[var3] - (double)var12 / 2.0D) <= 1.0D || Math.abs((double)var14[var3] - (double)var12 / 3.0D) <= 1.0D || Math.abs((double)var14[var3] - (double)var12 / 4.0D) <= 1.0D || Math.abs((double)var14[var3] - (double)var12 / 5.0D) <= 1.0D)) {
               var12 = var14[var3];
            }
         }
      }

      Filters.fir_mem2(super.frmBuf, super.frmIdx, this.interp_lpc, super.excBuf, super.excIdx, super.frameSize, super.lpcSize, this.mem_exc);
      float var11 = 0.0F;

      for(var3 = 0; var3 < super.frameSize; ++var3) {
         var11 += super.excBuf[super.excIdx + var3] * super.excBuf[super.excIdx + var3];
      }

      var11 = (float)Math.sqrt((double)(1.0F + var11 / (float)super.frameSize));
      float var16;
      int var17;
      float var28;
      int var31;
      if(this.vbr != null && (this.vbr_enabled != 0 || this.vad_enabled != 0)) {
         if(this.abr_enabled != 0) {
            var28 = 0.0F;
            if(this.abr_drift2 * this.abr_drift > 0.0F) {
               var28 = -1.0E-5F * this.abr_drift / (1.0F + this.abr_count);
               if(var28 > 0.05F) {
                  var28 = 0.05F;
               }

               if(var28 < -0.05F) {
                  var28 = -0.05F;
               }
            }

            this.vbr_quality += var28;
            if(this.vbr_quality > 10.0F) {
               this.vbr_quality = 10.0F;
            }

            if(this.vbr_quality < 0.0F) {
               this.vbr_quality = 0.0F;
            }
         }

         this.relative_quality = this.vbr.analysis(var2, super.frameSize, var12, var13);
         if(this.vbr_enabled != 0) {
            int var29 = 0;
            var16 = 100.0F;

            for(var31 = 8; var31 > 0; --var31) {
               var17 = (int)Math.floor((double)this.vbr_quality);
               float var18;
               if(var17 == 10) {
                  var18 = Vbr.nb_thresh[var31][var17];
               } else {
                  var18 = (this.vbr_quality - (float)var17) * Vbr.nb_thresh[var31][var17 + 1] + ((float)(1 + var17) - this.vbr_quality) * Vbr.nb_thresh[var31][var17];
               }

               if(this.relative_quality > var18 && this.relative_quality - var18 < var16) {
                  var29 = var31;
                  var16 = this.relative_quality - var18;
               }
            }

            var31 = var29;
            if(var29 == 0) {
               if(this.dtx_count != 0 && (double)var10 <= 0.05D && super.dtx_enabled != 0 && this.dtx_count <= 20) {
                  var31 = 0;
                  ++this.dtx_count;
               } else {
                  var31 = 1;
                  this.dtx_count = 1;
               }
            } else {
               this.dtx_count = 0;
            }

            this.setMode(var31);
            if(this.abr_enabled != 0) {
               var17 = this.getBitRate();
               this.abr_drift += (float)(var17 - this.abr_enabled);
               this.abr_drift2 = 0.95F * this.abr_drift2 + 0.05F * (float)(var17 - this.abr_enabled);
               this.abr_count = (float)((double)this.abr_count + 1.0D);
            }
         } else {
            if(this.relative_quality < 2.0F) {
               if(this.dtx_count != 0 && (double)var10 <= 0.05D && super.dtx_enabled != 0 && this.dtx_count <= 20) {
                  var31 = 0;
                  ++this.dtx_count;
               } else {
                  this.dtx_count = 1;
                  var31 = 1;
               }
            } else {
               this.dtx_count = 0;
               var31 = this.submodeSelect;
            }

            super.submodeID = var31;
         }
      } else {
         this.relative_quality = -1.0F;
      }

      var1.pack(0, 1);
      var1.pack(super.submodeID, 4);
      if(super.submodes[super.submodeID] == null) {
         for(var3 = 0; var3 < super.frameSize; ++var3) {
            super.excBuf[super.excIdx + var3] = this.exc2Buf[this.exc2Idx + var3] = this.swBuf[this.swIdx + var3] = 0.0F;
         }

         for(var3 = 0; var3 < super.lpcSize; ++var3) {
            this.mem_sw[var3] = 0.0F;
         }

         super.first = 1;
         this.bounded_pitch = 1;
         Filters.iir_mem2(super.excBuf, super.excIdx, super.interp_qlpc, super.frmBuf, super.frmIdx, super.frameSize, super.lpcSize, super.mem_sp);
         var2[0] = super.frmBuf[super.frmIdx] + super.preemph * this.pre_mem2;

         for(var3 = 1; var3 < super.frameSize; ++var3) {
            var2[var3] = super.frmBuf[super.frmIdx = var3] + super.preemph * var2[var3 - 1];
         }

         this.pre_mem2 = var2[super.frameSize - 1];
         return 0;
      } else {
         if(super.first != 0) {
            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.old_lsp[var3] = this.lsp[var3];
            }
         }

         super.submodes[super.submodeID].lsqQuant.quant(this.lsp, super.qlsp, super.lpcSize, var1);
         if(super.submodes[super.submodeID].lbr_pitch != -1) {
            var1.pack(var12 - super.min_pitch, 7);
         }

         if(super.submodes[super.submodeID].forced_pitch_gain != 0) {
            var31 = (int)Math.floor(0.5D + (double)(15.0F * var13));
            if(var31 > 15) {
               var31 = 15;
            }

            if(var31 < 0) {
               var31 = 0;
            }

            var1.pack(var31, 4);
            var13 = 0.066667F * (float)var31;
         }

         var31 = (int)Math.floor(0.5D + 3.5D * Math.log((double)var11));
         if(var31 < 0) {
            var31 = 0;
         }

         if(var31 > 31) {
            var31 = 31;
         }

         var11 = (float)Math.exp((double)var31 / 3.5D);
         var1.pack(var31, 5);
         if(super.first != 0) {
            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.old_qlsp[var3] = super.qlsp[var3];
            }
         }

         float[] var4 = new float[super.subframeSize];
         float[] var5 = new float[super.subframeSize];
         float[] var7 = new float[super.subframeSize];
         float[] var6 = new float[super.lpcSize];
         float[] var8 = new float[super.frameSize];

         for(var3 = 0; var3 < super.frameSize; ++var3) {
            var8[var3] = super.frmBuf[super.frmIdx + var3];
         }

         float var30;
         for(var31 = 0; var31 < super.nbSubframes; ++var31) {
            int var32 = super.subframeSize * var31;
            var17 = super.frmIdx + var32;
            int var19 = super.excIdx + var32;
            int var33 = this.swIdx + var32;
            int var20 = this.exc2Idx + var32;
            var30 = (float)(1.0D + (double)var31) / (float)super.nbSubframes;

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.interp_lsp[var3] = (1.0F - var30) * this.old_lsp[var3] + var30 * this.lsp[var3];
            }

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.interp_qlsp[var3] = (1.0F - var30) * super.old_qlsp[var3] + var30 * super.qlsp[var3];
            }

            Lsp.enforce_margin(this.interp_lsp, super.lpcSize, 0.002F);
            Lsp.enforce_margin(super.interp_qlsp, super.lpcSize, 0.002F);

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.interp_lsp[var3] = (float)Math.cos((double)this.interp_lsp[var3]);
            }

            super.m_lsp.lsp2lpc(this.interp_lsp, this.interp_lpc, super.lpcSize);

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.interp_qlsp[var3] = (float)Math.cos((double)super.interp_qlsp[var3]);
            }

            super.m_lsp.lsp2lpc(super.interp_qlsp, super.interp_qlpc, super.lpcSize);
            var30 = 1.0F;
            super.pi_gain[var31] = 0.0F;

            for(var3 = 0; var3 <= super.lpcSize; ++var3) {
               super.pi_gain[var31] += var30 * super.interp_qlpc[var3];
               var30 = -var30;
            }

            Filters.bw_lpc(super.gamma1, this.interp_lpc, this.bw_lpc1, super.lpcSize);
            if(super.gamma2 >= 0.0F) {
               Filters.bw_lpc(super.gamma2, this.interp_lpc, this.bw_lpc2, super.lpcSize);
            } else {
               this.bw_lpc2[0] = 1.0F;
               this.bw_lpc2[1] = -super.preemph;

               for(var3 = 2; var3 <= super.lpcSize; ++var3) {
                  this.bw_lpc2[var3] = 0.0F;
               }
            }

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               super.excBuf[var19 + var3] = 0.0F;
            }

            super.excBuf[var19] = 1.0F;
            Filters.syn_percep_zero(super.excBuf, var19, super.interp_qlpc, this.bw_lpc1, this.bw_lpc2, var7, super.subframeSize, super.lpcSize);

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               super.excBuf[var19 + var3] = 0.0F;
            }

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               this.exc2Buf[var20 + var3] = 0.0F;
            }

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               var6[var3] = super.mem_sp[var3];
            }

            Filters.iir_mem2(super.excBuf, var19, super.interp_qlpc, super.excBuf, var19, super.subframeSize, super.lpcSize, var6);

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               var6[var3] = this.mem_sw[var3];
            }

            Filters.filter_mem2(super.excBuf, var19, this.bw_lpc1, this.bw_lpc2, var4, 0, super.subframeSize, super.lpcSize, var6, 0);

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               var6[var3] = this.mem_sw[var3];
            }

            Filters.filter_mem2(super.frmBuf, var17, this.bw_lpc1, this.bw_lpc2, this.swBuf, var33, super.subframeSize, super.lpcSize, var6, 0);

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               var5[var3] = this.swBuf[var33 + var3] - var4[var3];
            }

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               super.excBuf[var19 + var3] = this.exc2Buf[var20 + var3] = 0.0F;
            }

            int var22;
            int var23;
            int var24;
            if(super.submodes[super.submodeID].lbr_pitch != -1) {
               var24 = super.submodes[super.submodeID].lbr_pitch;
               if(var24 != 0) {
                  if(var12 < super.min_pitch + var24 - 1) {
                     var12 = super.min_pitch + var24 - 1;
                  }

                  if(var12 > super.max_pitch - var24) {
                     var12 = super.max_pitch - var24;
                  }

                  var22 = var12 - var24 + 1;
                  var23 = var12 + var24;
               } else {
                  var23 = var12;
                  var22 = var12;
               }
            } else {
               var22 = super.min_pitch;
               var23 = super.max_pitch;
            }

            if(this.bounded_pitch != 0 && var23 > var32) {
               var23 = var32;
            }

            int var21 = super.submodes[super.submodeID].ltp.quant(var5, this.swBuf, var33, super.interp_qlpc, this.bw_lpc1, this.bw_lpc2, super.excBuf, var19, var22, var23, var13, super.lpcSize, super.subframeSize, var1, this.exc2Buf, var20, var7, this.complexity);
            this.pitch[var31] = var21;
            Filters.syn_percep_zero(super.excBuf, var19, super.interp_qlpc, this.bw_lpc1, this.bw_lpc2, var4, super.subframeSize, super.lpcSize);

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               var5[var3] -= var4[var3];
            }

            float var25 = 0.0F;
            var24 = var31 * super.subframeSize;

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               super.innov[var24 + var3] = 0.0F;
            }

            Filters.residue_percep_zero(var5, 0, super.interp_qlpc, this.bw_lpc1, this.bw_lpc2, this.buf2, super.subframeSize, super.lpcSize);

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               var25 += this.buf2[var3] * this.buf2[var3];
            }

            var25 = (float)Math.sqrt((double)(0.1F + var25 / (float)super.subframeSize));
            var25 /= var11;
            if(super.submodes[super.submodeID].have_subframe_gain != 0) {
               var25 = (float)Math.log((double)var25);
               int var27;
               if(super.submodes[super.submodeID].have_subframe_gain == 3) {
                  var27 = VQ.index(var25, NbCodec.exc_gain_quant_scal3, 8);
                  var1.pack(var27, 3);
                  var25 = NbCodec.exc_gain_quant_scal3[var27];
               } else {
                  var27 = VQ.index(var25, NbCodec.exc_gain_quant_scal1, 2);
                  var1.pack(var27, 1);
                  var25 = NbCodec.exc_gain_quant_scal1[var27];
               }

               var25 = (float)Math.exp((double)var25);
            } else {
               var25 = 1.0F;
            }

            var25 *= var11;
            float var26 = 1.0F / var25;

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               var5[var3] *= var26;
            }

            super.submodes[super.submodeID].innovation.quant(var5, super.interp_qlpc, this.bw_lpc1, this.bw_lpc2, super.lpcSize, super.subframeSize, super.innov, var24, var7, var1, this.complexity);

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               super.innov[var24 + var3] *= var25;
            }

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               super.excBuf[var19 + var3] += super.innov[var24 + var3];
            }

            if(super.submodes[super.submodeID].double_codebook != 0) {
               float[] var34 = new float[super.subframeSize];

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  var5[var3] = (float)((double)var5[var3] * 2.2D);
               }

               super.submodes[super.submodeID].innovation.quant(var5, super.interp_qlpc, this.bw_lpc1, this.bw_lpc2, super.lpcSize, super.subframeSize, var34, 0, var7, var1, this.complexity);

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  var34[var3] = (float)((double)var34[var3] * (double)var25 * 0.45454545454545453D);
               }

               for(var3 = 0; var3 < super.subframeSize; ++var3) {
                  super.excBuf[var19 + var3] += var34[var3];
               }
            }

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               var5[var3] *= var25;
            }

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               var6[var3] = super.mem_sp[var3];
            }

            Filters.iir_mem2(super.excBuf, var19, super.interp_qlpc, super.frmBuf, var17, super.subframeSize, super.lpcSize, super.mem_sp);
            Filters.filter_mem2(super.frmBuf, var17, this.bw_lpc1, this.bw_lpc2, this.swBuf, var33, super.subframeSize, super.lpcSize, this.mem_sw, 0);

            for(var3 = 0; var3 < super.subframeSize; ++var3) {
               this.exc2Buf[var20 + var3] = super.excBuf[var19 + var3];
            }
         }

         if(super.submodeID >= 1) {
            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               this.old_lsp[var3] = this.lsp[var3];
            }

            for(var3 = 0; var3 < super.lpcSize; ++var3) {
               super.old_qlsp[var3] = super.qlsp[var3];
            }
         }

         if(super.submodeID == 1) {
            if(this.dtx_count != 0) {
               var1.pack(15, 4);
            } else {
               var1.pack(0, 4);
            }
         }

         super.first = 0;
         var28 = 0.0F;
         var30 = 0.0F;

         for(var3 = 0; var3 < super.frameSize; ++var3) {
            var28 += super.frmBuf[super.frmIdx + var3] * super.frmBuf[super.frmIdx + var3];
            var30 += (super.frmBuf[super.frmIdx + var3] - var8[var3]) * (super.frmBuf[super.frmIdx + var3] - var8[var3]);
         }

         var16 = (float)(10.0D * Math.log((double)((var28 + 1.0F) / (var30 + 1.0F))));
         var2[0] = super.frmBuf[super.frmIdx] + super.preemph * this.pre_mem2;

         for(var3 = 1; var3 < super.frameSize; ++var3) {
            var2[var3] = super.frmBuf[super.frmIdx + var3] + super.preemph * var2[var3 - 1];
         }

         this.pre_mem2 = var2[super.frameSize - 1];
         if(!(super.submodes[super.submodeID].innovation instanceof NoiseSearch) && super.submodeID != 0) {
            this.bounded_pitch = 0;
         } else {
            this.bounded_pitch = 1;
         }

         return 1;
      }
   }

   public int getEncodedFrameSize() {
      return NbCodec.NB_FRAME_SIZE[super.submodeID];
   }

   public void setQuality(int var1) {
      if(var1 < 0) {
         var1 = 0;
      }

      if(var1 > 10) {
         var1 = 10;
      }

      super.submodeID = this.submodeSelect = NB_QUALITY_MAP[var1];
   }

   public int getBitRate() {
      return super.submodes[super.submodeID] != null?this.sampling_rate * super.submodes[super.submodeID].bits_per_frame / super.frameSize:this.sampling_rate * 5 / super.frameSize;
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

   public void setVbr(boolean var1) {
      this.vbr_enabled = var1?1:0;
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

   public void setAbr(int var1) {
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

   public void setVbrQuality(float var1) {
      if(var1 < 0.0F) {
         var1 = 0.0F;
      }

      if(var1 > 10.0F) {
         var1 = 10.0F;
      }

      this.vbr_quality = var1;
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

   public void setSamplingRate(int var1) {
      this.sampling_rate = var1;
   }

   public int getSamplingRate() {
      return this.sampling_rate;
   }

   public int getLookAhead() {
      return super.windowSize - super.frameSize;
   }

   public float getRelativeQuality() {
      return this.relative_quality;
   }

}
