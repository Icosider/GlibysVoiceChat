package org.xiph.speex;

public class LbrLspQuant extends LspQuant {

   public final void quant(float[] var1, float[] var2, int var3, Bits var4) {
      float[] var9 = new float[20];

      int var5;
      for(var5 = 0; var5 < var3; ++var5) {
         var2[var5] = var1[var5];
      }

      var9[0] = 1.0F / (var2[1] - var2[0]);
      var9[var3 - 1] = 1.0F / (var2[var3 - 1] - var2[var3 - 2]);

      for(var5 = 1; var5 < var3 - 1; ++var5) {
         float var6 = 1.0F / ((0.15F + var2[var5] - var2[var5 - 1]) * (0.15F + var2[var5] - var2[var5 - 1]));
         float var7 = 1.0F / ((0.15F + var2[var5 + 1] - var2[var5]) * (0.15F + var2[var5 + 1] - var2[var5]));
         var9[var5] = var6 > var7?var6:var7;
      }

      for(var5 = 0; var5 < var3; ++var5) {
         var2[var5] = (float)((double)var2[var5] - (0.25D * (double)var5 + 0.25D));
      }

      for(var5 = 0; var5 < var3; ++var5) {
         var2[var5] *= 256.0F;
      }

      int var8 = lsp_quant(var2, 0, Codebook.cdbk_nb, 64, var3);
      var4.pack(var8, 6);

      for(var5 = 0; var5 < var3; ++var5) {
         var2[var5] *= 2.0F;
      }

      var8 = lsp_weight_quant(var2, 0, var9, 0, Codebook.cdbk_nb_low1, 64, 5);
      var4.pack(var8, 6);
      var8 = lsp_weight_quant(var2, 5, var9, 5, Codebook.cdbk_nb_high1, 64, 5);
      var4.pack(var8, 6);

      for(var5 = 0; var5 < var3; ++var5) {
         var2[var5] = (float)((double)var2[var5] * 0.0019531D);
      }

      for(var5 = 0; var5 < var3; ++var5) {
         var2[var5] = var1[var5] - var2[var5];
      }

   }

   public final void unquant(float[] var1, int var2, Bits var3) {
      for(int var4 = 0; var4 < var2; ++var4) {
         var1[var4] = 0.25F * (float)var4 + 0.25F;
      }

      this.unpackPlus(var1, Codebook.cdbk_nb, var3, 0.0039062F, 10, 0);
      this.unpackPlus(var1, Codebook.cdbk_nb_low1, var3, 0.0019531F, 5, 0);
      this.unpackPlus(var1, Codebook.cdbk_nb_high1, var3, 0.0019531F, 5, 5);
   }
}
