package org.xiph.speex;

public interface Encoder {

   int encode(Bits var1, float[] var2);

   int getEncodedFrameSize();

   int getFrameSize();

   void setQuality(int var1);

   int getBitRate();

   float[] getPiGain();

   float[] getExc();

   float[] getInnov();

   void setMode(int var1);

   int getMode();

   void setBitRate(int var1);

   void setVbr(boolean var1);

   boolean getVbr();

   void setVad(boolean var1);

   boolean getVad();

   void setDtx(boolean var1);

   boolean getDtx();

   int getAbr();

   void setAbr(int var1);

   void setVbrQuality(float var1);

   float getVbrQuality();

   void setComplexity(int var1);

   int getComplexity();

   void setSamplingRate(int var1);

   int getSamplingRate();

   int getLookAhead();

   float getRelativeQuality();
}
