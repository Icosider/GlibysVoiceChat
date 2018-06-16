package org.xiph.speex.spi;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;

public class SpeexFormatConvertionProvider extends FormatConversionProvider {

   public static final Encoding[] NO_ENCODING = new Encoding[0];
   public static final Encoding[] PCM_ENCODING = new Encoding[]{Encoding.PCM_SIGNED};
   public static final Encoding[] SPEEX_ENCODING = new Encoding[]{SpeexEncoding.SPEEX};
   public static final Encoding[] BOTH_ENCODINGS = new Encoding[]{SpeexEncoding.SPEEX, Encoding.PCM_SIGNED};
   public static final AudioFormat[] NO_FORMAT = new AudioFormat[0];


   public Encoding[] getSourceEncodings() {
      Encoding[] var1 = new Encoding[]{SpeexEncoding.SPEEX, Encoding.PCM_SIGNED};
      return var1;
   }

   public Encoding[] getTargetEncodings() {
      Encoding[] var1 = new Encoding[]{SpeexEncoding.SPEEX_Q0, SpeexEncoding.SPEEX_Q1, SpeexEncoding.SPEEX_Q2, SpeexEncoding.SPEEX_Q3, SpeexEncoding.SPEEX_Q4, SpeexEncoding.SPEEX_Q5, SpeexEncoding.SPEEX_Q6, SpeexEncoding.SPEEX_Q7, SpeexEncoding.SPEEX_Q8, SpeexEncoding.SPEEX_Q9, SpeexEncoding.SPEEX_Q10, SpeexEncoding.SPEEX_VBR0, SpeexEncoding.SPEEX_VBR1, SpeexEncoding.SPEEX_VBR2, SpeexEncoding.SPEEX_VBR3, SpeexEncoding.SPEEX_VBR4, SpeexEncoding.SPEEX_VBR5, SpeexEncoding.SPEEX_VBR6, SpeexEncoding.SPEEX_VBR7, SpeexEncoding.SPEEX_VBR8, SpeexEncoding.SPEEX_VBR9, SpeexEncoding.SPEEX_VBR10, Encoding.PCM_SIGNED};
      return var1;
   }

   public Encoding[] getTargetEncodings(AudioFormat var1) {
      Encoding[] var2;
      if(var1.getEncoding().equals(Encoding.PCM_SIGNED)) {
         var2 = new Encoding[]{SpeexEncoding.SPEEX_Q0, SpeexEncoding.SPEEX_Q1, SpeexEncoding.SPEEX_Q2, SpeexEncoding.SPEEX_Q3, SpeexEncoding.SPEEX_Q4, SpeexEncoding.SPEEX_Q5, SpeexEncoding.SPEEX_Q6, SpeexEncoding.SPEEX_Q7, SpeexEncoding.SPEEX_Q8, SpeexEncoding.SPEEX_Q9, SpeexEncoding.SPEEX_Q10, SpeexEncoding.SPEEX_VBR0, SpeexEncoding.SPEEX_VBR1, SpeexEncoding.SPEEX_VBR2, SpeexEncoding.SPEEX_VBR3, SpeexEncoding.SPEEX_VBR4, SpeexEncoding.SPEEX_VBR5, SpeexEncoding.SPEEX_VBR6, SpeexEncoding.SPEEX_VBR7, SpeexEncoding.SPEEX_VBR8, SpeexEncoding.SPEEX_VBR9, SpeexEncoding.SPEEX_VBR10};
         return var2;
      } else if(var1.getEncoding() instanceof SpeexEncoding) {
         var2 = new Encoding[]{Encoding.PCM_SIGNED};
         return var2;
      } else {
         var2 = new Encoding[0];
         return var2;
      }
   }

   public AudioFormat[] getTargetFormats(Encoding var1, AudioFormat var2) {
      AudioFormat[] var3;
      if(var2.getEncoding().equals(Encoding.PCM_SIGNED) && var1 instanceof SpeexEncoding) {
         if(var2.getChannels() <= 2 && var2.getChannels() > 0 && !var2.isBigEndian()) {
            var3 = new AudioFormat[]{new AudioFormat(var1, var2.getSampleRate(), -1, var2.getChannels(), -1, -1.0F, false)};
            return var3;
         } else {
            var3 = new AudioFormat[0];
            return var3;
         }
      } else if(var2.getEncoding() instanceof SpeexEncoding && var1.equals(Encoding.PCM_SIGNED)) {
         var3 = new AudioFormat[]{new AudioFormat(var2.getSampleRate(), 16, var2.getChannels(), true, false)};
         return var3;
      } else {
         var3 = new AudioFormat[0];
         return var3;
      }
   }

   public AudioInputStream getAudioInputStream(Encoding var1, AudioInputStream var2) {
      if(this.isConversionSupported(var1, var2.getFormat())) {
         AudioFormat[] var3 = this.getTargetFormats(var1, var2.getFormat());
         if(var3 != null && var3.length > 0) {
            AudioFormat var4 = var2.getFormat();
            AudioFormat var5 = var3[0];
            if(var4.equals(var5)) {
               return var2;
            } else if(var4.getEncoding() instanceof SpeexEncoding && var5.getEncoding().equals(Encoding.PCM_SIGNED)) {
               return new Speex2PcmAudioInputStream(var2, var5, -1L);
            } else if(var4.getEncoding().equals(Encoding.PCM_SIGNED) && var5.getEncoding() instanceof SpeexEncoding) {
               return new Pcm2SpeexAudioInputStream(var2, var5, -1L);
            } else {
               throw new IllegalArgumentException("unable to convert " + var4.toString() + " to " + var5.toString());
            }
         } else {
            throw new IllegalArgumentException("target format not found");
         }
      } else {
         throw new IllegalArgumentException("conversion not supported");
      }
   }

   public AudioInputStream getAudioInputStream(AudioFormat var1, AudioInputStream var2) {
      if(this.isConversionSupported(var1, var2.getFormat())) {
         AudioFormat[] var3 = this.getTargetFormats(var1.getEncoding(), var2.getFormat());
         if(var3 != null && var3.length > 0) {
            AudioFormat var4 = var2.getFormat();
            if(var4.equals(var1)) {
               return var2;
            } else if(var4.getEncoding() instanceof SpeexEncoding && var1.getEncoding().equals(Encoding.PCM_SIGNED)) {
               return new Speex2PcmAudioInputStream(var2, var1, -1L);
            } else if(var4.getEncoding().equals(Encoding.PCM_SIGNED) && var1.getEncoding() instanceof SpeexEncoding) {
               return new Pcm2SpeexAudioInputStream(var2, var1, -1L);
            } else {
               throw new IllegalArgumentException("unable to convert " + var4.toString() + " to " + var1.toString());
            }
         } else {
            throw new IllegalArgumentException("target format not found");
         }
      } else {
         throw new IllegalArgumentException("conversion not supported");
      }
   }

}
