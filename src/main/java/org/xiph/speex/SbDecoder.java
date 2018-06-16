package org.xiph.speex;

import java.io.StreamCorruptedException;

public class SbDecoder extends SbCodec implements Decoder {

   protected Decoder lowdec;
   protected Stereo stereo = new Stereo();
   protected boolean enhanced = true;
   private float[] innov2;


   public void wbinit() {
      this.lowdec = new NbDecoder();
      ((NbDecoder)this.lowdec).nbinit();
      this.lowdec.setPerceptualEnhancement(this.enhanced);
      super.wbinit();
      this.init(160, 40, 8, 640, 0.7F);
   }

   public void uwbinit() {
      this.lowdec = new SbDecoder();
      ((SbDecoder)this.lowdec).wbinit();
      this.lowdec.setPerceptualEnhancement(this.enhanced);
      super.uwbinit();
      this.init(320, 80, 8, 1280, 0.5F);
   }

   public void init(int var1, int var2, int var3, int var4, float var5) {
      super.init(var1, var2, var3, var4, var5);
      super.excIdx = 0;
      this.innov2 = new float[var2];
   }

   public int decode(Bits var1, float[] var2) throws StreamCorruptedException {
      int var6 = this.lowdec.decode(var1, super.x0d);
      if(var6 != 0) {
         return var6;
      } else {
         boolean var10 = this.lowdec.getDtx();
         if(var1 == null) {
            this.decodeLost(var2, var10);
            return 0;
         } else {
            int var5 = var1.peek();
            if(var5 != 0) {
               var5 = var1.unpack(1);
               super.submodeID = var1.unpack(3);
            } else {
               super.submodeID = 0;
            }

            int var3;
            for(var3 = 0; var3 < super.frameSize; ++var3) {
               super.excBuf[var3] = 0.0F;
            }

            if(super.submodes[super.submodeID] == null) {
               if(var10) {
                  this.decodeLost(var2, true);
                  return 0;
               } else {
                  for(var3 = 0; var3 < super.frameSize; ++var3) {
                     super.excBuf[var3] = 0.0F;
                  }

                  super.first = 1;
                  Filters.iir_mem2(super.excBuf, super.excIdx, super.interp_qlpc, super.high, 0, super.frameSize, super.lpcSize, super.mem_sp);
                  super.filters.fir_mem_up(super.x0d, Codebook.h0, super.y0, super.fullFrameSize, 64, super.g0_mem);
                  super.filters.fir_mem_up(super.high, Codebook.h1, super.y1, super.fullFrameSize, 64, super.g1_mem);

                  for(var3 = 0; var3 < super.fullFrameSize; ++var3) {
                     var2[var3] = 2.0F * (super.y0[var3] - super.y1[var3]);
                  }

                  return 0;
               }
            } else {
               float[] var7 = this.lowdec.getPiGain();
               float[] var8 = this.lowdec.getExc();
               float[] var9 = this.lowdec.getInnov();
               super.submodes[super.submodeID].lsqQuant.unquant(super.qlsp, super.lpcSize, var1);
               if(super.first != 0) {
                  for(var3 = 0; var3 < super.lpcSize; ++var3) {
                     super.old_qlsp[var3] = super.qlsp[var3];
                  }
               }

               for(int var4 = 0; var4 < super.nbSubframes; ++var4) {
                  float var13 = 0.0F;
                  float var14 = 0.0F;
                  float var15 = 0.0F;
                  int var16 = super.subframeSize * var4;
                  float var11 = (1.0F + (float)var4) / (float)super.nbSubframes;

                  for(var3 = 0; var3 < super.lpcSize; ++var3) {
                     super.interp_qlsp[var3] = (1.0F - var11) * super.old_qlsp[var3] + var11 * super.qlsp[var3];
                  }

                  Lsp.enforce_margin(super.interp_qlsp, super.lpcSize, 0.05F);

                  for(var3 = 0; var3 < super.lpcSize; ++var3) {
                     super.interp_qlsp[var3] = (float)Math.cos((double)super.interp_qlsp[var3]);
                  }

                  super.m_lsp.lsp2lpc(super.interp_qlsp, super.interp_qlpc, super.lpcSize);
                  float var17;
                  float var18;
                  if(this.enhanced) {
                     var17 = super.submodes[super.submodeID].lpc_enh_k1;
                     var18 = super.submodes[super.submodeID].lpc_enh_k2;
                     float var19 = var17 - var18;
                     Filters.bw_lpc(var17, super.interp_qlpc, super.awk1, super.lpcSize);
                     Filters.bw_lpc(var18, super.interp_qlpc, super.awk2, super.lpcSize);
                     Filters.bw_lpc(var19, super.interp_qlpc, super.awk3, super.lpcSize);
                  }

                  var11 = 1.0F;
                  super.pi_gain[var4] = 0.0F;

                  for(var3 = 0; var3 <= super.lpcSize; ++var3) {
                     var15 += var11 * super.interp_qlpc[var3];
                     var11 = -var11;
                     super.pi_gain[var4] += super.interp_qlpc[var3];
                  }

                  var14 = var7[var4];
                  var14 = 1.0F / (Math.abs(var14) + 0.01F);
                  var15 = 1.0F / (Math.abs(var15) + 0.01F);
                  float var12 = Math.abs(0.01F + var15) / (0.01F + Math.abs(var14));

                  for(var3 = var16; var3 < var16 + super.subframeSize; ++var3) {
                     super.excBuf[var3] = 0.0F;
                  }

                  if(super.submodes[super.submodeID].innovation == null) {
                     int var20 = var1.unpack(5);
                     var17 = (float)Math.exp(((double)var20 - 10.0D) / 8.0D);
                     var17 /= var12;

                     for(var3 = var16; var3 < var16 + super.subframeSize; ++var3) {
                        super.excBuf[var3] = super.foldingGain * var17 * var9[var3];
                     }
                  } else {
                     int var21 = var1.unpack(4);

                     for(var3 = var16; var3 < var16 + super.subframeSize; ++var3) {
                        var13 += var8[var3] * var8[var3];
                     }

                     var17 = (float)Math.exp((double)(0.27027026F * (float)var21 - 2.0F));
                     var18 = var17 * (float)Math.sqrt((double)(1.0F + var13)) / var12;
                     super.submodes[super.submodeID].innovation.unquant(super.excBuf, var16, super.subframeSize, var1);

                     for(var3 = var16; var3 < var16 + super.subframeSize; ++var3) {
                        super.excBuf[var3] *= var18;
                     }

                     if(super.submodes[super.submodeID].double_codebook != 0) {
                        for(var3 = 0; var3 < super.subframeSize; ++var3) {
                           this.innov2[var3] = 0.0F;
                        }

                        super.submodes[super.submodeID].innovation.unquant(this.innov2, 0, super.subframeSize, var1);

                        for(var3 = 0; var3 < super.subframeSize; ++var3) {
                           this.innov2[var3] *= var18 * 0.4F;
                        }

                        for(var3 = 0; var3 < super.subframeSize; ++var3) {
                           super.excBuf[var16 + var3] += this.innov2[var3];
                        }
                     }
                  }

                  for(var3 = var16; var3 < var16 + super.subframeSize; ++var3) {
                     super.high[var3] = super.excBuf[var3];
                  }

                  if(this.enhanced) {
                     Filters.filter_mem2(super.high, var16, super.awk2, super.awk1, super.subframeSize, super.lpcSize, super.mem_sp, super.lpcSize);
                     Filters.filter_mem2(super.high, var16, super.awk3, super.interp_qlpc, super.subframeSize, super.lpcSize, super.mem_sp, 0);
                  } else {
                     for(var3 = 0; var3 < super.lpcSize; ++var3) {
                        super.mem_sp[super.lpcSize + var3] = 0.0F;
                     }

                     Filters.iir_mem2(super.high, var16, super.interp_qlpc, super.high, var16, super.subframeSize, super.lpcSize, super.mem_sp);
                  }
               }

               super.filters.fir_mem_up(super.x0d, Codebook.h0, super.y0, super.fullFrameSize, 64, super.g0_mem);
               super.filters.fir_mem_up(super.high, Codebook.h1, super.y1, super.fullFrameSize, 64, super.g1_mem);

               for(var3 = 0; var3 < super.fullFrameSize; ++var3) {
                  var2[var3] = 2.0F * (super.y0[var3] - super.y1[var3]);
               }

               for(var3 = 0; var3 < super.lpcSize; ++var3) {
                  super.old_qlsp[var3] = super.qlsp[var3];
               }

               super.first = 0;
               return 0;
            }
         }
      }
   }

   public int decodeLost(float[] var1, boolean var2) {
      int var4 = 0;
      if(var2) {
         var4 = super.submodeID;
         super.submodeID = 1;
      } else {
         Filters.bw_lpc(0.99F, super.interp_qlpc, super.interp_qlpc, super.lpcSize);
      }

      super.first = 1;
      super.awk1 = new float[super.lpcSize + 1];
      super.awk2 = new float[super.lpcSize + 1];
      super.awk3 = new float[super.lpcSize + 1];
      if(this.enhanced) {
         float var5;
         float var6;
         if(super.submodes[super.submodeID] != null) {
            var5 = super.submodes[super.submodeID].lpc_enh_k1;
            var6 = super.submodes[super.submodeID].lpc_enh_k2;
         } else {
            var6 = 0.7F;
            var5 = 0.7F;
         }

         float var7 = var5 - var6;
         Filters.bw_lpc(var5, super.interp_qlpc, super.awk1, super.lpcSize);
         Filters.bw_lpc(var6, super.interp_qlpc, super.awk2, super.lpcSize);
         Filters.bw_lpc(var7, super.interp_qlpc, super.awk3, super.lpcSize);
      }

      int var3;
      if(!var2) {
         for(var3 = 0; var3 < super.frameSize; ++var3) {
            super.excBuf[super.excIdx + var3] = (float)((double)super.excBuf[super.excIdx + var3] * 0.9D);
         }
      }

      for(var3 = 0; var3 < super.frameSize; ++var3) {
         super.high[var3] = super.excBuf[super.excIdx + var3];
      }

      if(this.enhanced) {
         Filters.filter_mem2(super.high, 0, super.awk2, super.awk1, super.high, 0, super.frameSize, super.lpcSize, super.mem_sp, super.lpcSize);
         Filters.filter_mem2(super.high, 0, super.awk3, super.interp_qlpc, super.high, 0, super.frameSize, super.lpcSize, super.mem_sp, 0);
      } else {
         for(var3 = 0; var3 < super.lpcSize; ++var3) {
            super.mem_sp[super.lpcSize + var3] = 0.0F;
         }

         Filters.iir_mem2(super.high, 0, super.interp_qlpc, super.high, 0, super.frameSize, super.lpcSize, super.mem_sp);
      }

      super.filters.fir_mem_up(super.x0d, Codebook.h0, super.y0, super.fullFrameSize, 64, super.g0_mem);
      super.filters.fir_mem_up(super.high, Codebook.h1, super.y1, super.fullFrameSize, 64, super.g1_mem);

      for(var3 = 0; var3 < super.fullFrameSize; ++var3) {
         var1[var3] = 2.0F * (super.y0[var3] - super.y1[var3]);
      }

      if(var2) {
         super.submodeID = var4;
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
