package org.xiph.speex.spi;

import javax.sound.sampled.AudioFormat.Encoding;

public class SpeexEncoding extends Encoding {

   public static final SpeexEncoding SPEEX = new SpeexEncoding("SPEEX");
   public static final SpeexEncoding SPEEX_Q0 = new SpeexEncoding("SPEEX_quality_0", 0, false);
   public static final SpeexEncoding SPEEX_Q1 = new SpeexEncoding("SPEEX_quality_1", 1, false);
   public static final SpeexEncoding SPEEX_Q2 = new SpeexEncoding("SPEEX_quality_2", 2, false);
   public static final SpeexEncoding SPEEX_Q3 = new SpeexEncoding("SPEEX_quality_3", 3, false);
   public static final SpeexEncoding SPEEX_Q4 = new SpeexEncoding("SPEEX_quality_4", 4, false);
   public static final SpeexEncoding SPEEX_Q5 = new SpeexEncoding("SPEEX_quality_5", 5, false);
   public static final SpeexEncoding SPEEX_Q6 = new SpeexEncoding("SPEEX_quality_6", 6, false);
   public static final SpeexEncoding SPEEX_Q7 = new SpeexEncoding("SPEEX_quality_7", 7, false);
   public static final SpeexEncoding SPEEX_Q8 = new SpeexEncoding("SPEEX_quality_8", 8, false);
   public static final SpeexEncoding SPEEX_Q9 = new SpeexEncoding("SPEEX_quality_9", 9, false);
   public static final SpeexEncoding SPEEX_Q10 = new SpeexEncoding("SPEEX_quality_10", 10, false);
   public static final SpeexEncoding SPEEX_VBR0 = new SpeexEncoding("SPEEX_VBR_quality_0", 0, true);
   public static final SpeexEncoding SPEEX_VBR1 = new SpeexEncoding("SPEEX_VBR_quality_1", 1, true);
   public static final SpeexEncoding SPEEX_VBR2 = new SpeexEncoding("SPEEX_VBR_quality_2", 2, true);
   public static final SpeexEncoding SPEEX_VBR3 = new SpeexEncoding("SPEEX_VBR_quality_3", 3, true);
   public static final SpeexEncoding SPEEX_VBR4 = new SpeexEncoding("SPEEX_VBR_quality_4", 4, true);
   public static final SpeexEncoding SPEEX_VBR5 = new SpeexEncoding("SPEEX_VBR_quality_5", 5, true);
   public static final SpeexEncoding SPEEX_VBR6 = new SpeexEncoding("SPEEX_VBR_quality_6", 6, true);
   public static final SpeexEncoding SPEEX_VBR7 = new SpeexEncoding("SPEEX_VBR_quality_7", 7, true);
   public static final SpeexEncoding SPEEX_VBR8 = new SpeexEncoding("SPEEX_VBR_quality_8", 8, true);
   public static final SpeexEncoding SPEEX_VBR9 = new SpeexEncoding("SPEEX_VBR_quality_9", 9, true);
   public static final SpeexEncoding SPEEX_VBR10 = new SpeexEncoding("SPEEX_VBR_quality_10", 10, true);
   public static final int DEFAULT_QUALITY = 3;
   public static final boolean DEFAULT_VBR = false;
   protected int quality;
   protected boolean vbr;


   public SpeexEncoding(String var1, int var2, boolean var3) {
      super(var1);
      this.quality = var2;
      this.vbr = var3;
   }

   public SpeexEncoding(String var1) {
      this(var1, 3, false);
   }

   public int getQuality() {
      return this.quality;
   }

   public boolean isVBR() {
      return this.vbr;
   }

}
