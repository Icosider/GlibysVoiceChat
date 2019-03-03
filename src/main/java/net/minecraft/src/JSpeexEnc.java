package net.minecraft.src;

import org.xiph.speex.*;

import java.io.*;

public class JSpeexEnc {
    public static final String VERSION = "Java Speex Command Line Encoder v0.9.7 ($Revision: 1.5 $)";
    public static final String COPYRIGHT = "Copyright (C) 2002-2004 Wimba S.A.";
    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;
    public static final int FILE_FORMAT_RAW = 0;
    public static final int FILE_FORMAT_OGG = 1;
    public static final int FILE_FORMAT_WAVE = 2;
    protected int printlevel = 1;
    protected int srcFormat = 1;
    protected int destFormat = 2;
    protected int mode = -1;
    protected int quality = 8;
    protected int complexity = 3;
    protected int nframes = 1;
    protected int bitrate = -1;
    protected int sampleRate = -1;
    protected int channels = 1;
    protected float vbr_quality = -1.0F;
    protected boolean vbr = false;
    protected boolean vad = false;
    protected boolean dtx = false;
    protected String srcFile;
    protected String destFile;


    public static void main(String[] var0) throws IOException {
        JSpeexEnc var1 = new JSpeexEnc();
        if (var1.parseArgs(var0)) {
            var1.encode();
        }
    }

    public static void usage() {
        version();
        System.out.println(" ");
        System.out.println("Usage: JSpeexEnc [options] input_file output_file");
        System.out.println("Where:");
        System.out.println("  input_file can be:");
        System.out.println("    filename.wav  a PCM wav file");
        System.out.println("    filename.*    a raw PCM file (any extension other than .wav)");
        System.out.println("  output_file can be:");
        System.out.println("    filename.spx  an Ogg Speex file");
        System.out.println("    filename.wav  a Wave Speex file (beta!!!)");
        System.out.println("    filename.*    a raw Speex file");
        System.out.println("Options: -h, --help     This help");
        System.out.println("         -v, --version  Version information");
        System.out.println("         --verbose      Print detailed information");
        System.out.println("         --quiet        Print minimal information");
        System.out.println("         -n, -nb        Consider input as Narrowband (8kHz)");
        System.out.println("         -w, -wb        Consider input as Wideband (16kHz)");
        System.out.println("         -u, -uwb       Consider input as Ultra-Wideband (32kHz)");
        System.out.println("         --quality n    Encoding quality (0-10) default 8");
        System.out.println("         --complexity n Encoding complexity (0-10) default 3");
        System.out.println("         --nframes n    Number of frames per Ogg packet, default 1");
        System.out.println("         --vbr          Enable varible bit-rate (VBR)");
        System.out.println("         --vad          Enable voice activity detection (VAD)");
        System.out.println("         --dtx          Enable file based discontinuous transmission (DTX)");
        System.out.println("         if the input file is raw PCM (not a Wave file)");
        System.out.println("         --rate n       Sampling rate for raw input");
        System.out.println("         --stereo       Consider input as stereo");
        System.out.println("More information is available from: http://jspeex.sourceforge.net/");
        System.out.println("This code is a Java port of the Speex codec: http://www.speex.org/");
    }

    public static void version() {
        System.out.println("Java Speex Command Line Encoder v0.9.7 ($Revision: 1.5 $)");
        System.out.println("using Java Speex Encoder v0.9.7 ($Revision: 1.6 $)");
        System.out.println("Copyright (C) 2002-2004 Wimba S.A.");
    }

    protected static int readInt(byte[] var0, int var1) {
        return var0[var1] & 255 | (var0[var1 + 1] & 255) << 8 | (var0[var1 + 2] & 255) << 16 | var0[var1 + 3] << 24;
    }

    protected static int readShort(byte[] var0, int var1) {
        return var0[var1] & 255 | var0[var1 + 1] << 8;
    }

    public boolean parseArgs(String[] var1) {
        if (var1.length < 2) {
            if (var1.length == 1 && (var1[0].equalsIgnoreCase("-v") || var1[0].equalsIgnoreCase("--version"))) {
                version();
                return false;
            } else {
                usage();
                return false;
            }
        } else {
            this.srcFile = var1[var1.length - 2];
            this.destFile = var1[var1.length - 1];
            if (this.srcFile.toLowerCase().endsWith(".wav")) {
                this.srcFormat = 2;
            } else {
                this.srcFormat = 0;
            }

            if (this.destFile.toLowerCase().endsWith(".spx")) {
                this.destFormat = 1;
            } else if (this.destFile.toLowerCase().endsWith(".wav")) {
                this.destFormat = 2;
            } else {
                this.destFormat = 0;
            }

            int var2 = 0;

            while (var2 < var1.length - 2) {
                if (!var1[var2].equalsIgnoreCase("-h") && !var1[var2].equalsIgnoreCase("--help")) {
                    if (!var1[var2].equalsIgnoreCase("-v") && !var1[var2].equalsIgnoreCase("--version")) {
                        if (var1[var2].equalsIgnoreCase("--verbose")) {
                            this.printlevel = 0;
                        } else if (var1[var2].equalsIgnoreCase("--quiet")) {
                            this.printlevel = 2;
                        } else if (!var1[var2].equalsIgnoreCase("-n") && !var1[var2].equalsIgnoreCase("-nb") && !var1[var2].equalsIgnoreCase("--narrowband")) {
                            if (!var1[var2].equalsIgnoreCase("-w") && !var1[var2].equalsIgnoreCase("-wb") && !var1[var2].equalsIgnoreCase("--wideband")) {
                                if (!var1[var2].equalsIgnoreCase("-u") && !var1[var2].equalsIgnoreCase("-uwb") && !var1[var2].equalsIgnoreCase("--ultra-wideband")) {
                                    if (!var1[var2].equalsIgnoreCase("-q") && !var1[var2].equalsIgnoreCase("--quality")) {
                                        if (var1[var2].equalsIgnoreCase("--complexity")) {
                                            try {
                                                ++var2;
                                                this.complexity = Integer.parseInt(var1[var2]);
                                            } catch (NumberFormatException var6) {
                                                usage();
                                                return false;
                                            }
                                        } else if (var1[var2].equalsIgnoreCase("--nframes")) {
                                            try {
                                                ++var2;
                                                this.nframes = Integer.parseInt(var1[var2]);
                                            } catch (NumberFormatException var5) {
                                                usage();
                                                return false;
                                            }
                                        } else if (var1[var2].equalsIgnoreCase("--vbr")) {
                                            this.vbr = true;
                                        } else if (var1[var2].equalsIgnoreCase("--vad")) {
                                            this.vad = true;
                                        } else if (var1[var2].equalsIgnoreCase("--dtx")) {
                                            this.dtx = true;
                                        } else if (var1[var2].equalsIgnoreCase("--rate")) {
                                            try {
                                                ++var2;
                                                this.sampleRate = Integer.parseInt(var1[var2]);
                                            } catch (NumberFormatException var4) {
                                                usage();
                                                return false;
                                            }
                                        } else {
                                            if (!var1[var2].equalsIgnoreCase("--stereo")) {
                                                usage();
                                                return false;
                                            }

                                            this.channels = 2;
                                        }
                                    } else {
                                        try {
                                            ++var2;
                                            this.vbr_quality = Float.parseFloat(var1[var2]);
                                            this.quality = (int) this.vbr_quality;
                                        } catch (NumberFormatException var7) {
                                            usage();
                                            return false;
                                        }
                                    }
                                } else {
                                    this.mode = 2;
                                }
                            } else {
                                this.mode = 1;
                            }
                        } else {
                            this.mode = 0;
                        }

                        ++var2;
                        continue;
                    }

                    version();
                    return false;
                }

                usage();
                return false;
            }

            return true;
        }
    }

    public void encode() throws IOException {
        this.encode(new File(this.srcFile), new File(this.destFile));
    }

    public void encode(File var1, File var2) throws IOException {
        byte[] var3 = new byte[2560];
        if (this.printlevel <= 1) {
            version();
        }

        if (this.printlevel <= 0) {
            System.out.println("");
        }

        if (this.printlevel <= 0) {
            System.out.println("Input File: " + var1);
        }

        DataInputStream var10 = new DataInputStream(new FileInputStream(var1));
        if (this.srcFormat == 2) {
            var10.readFully(var3, 0, 12);
            if (!"RIFF".equals(new String(var3, 0, 4)) && !"WAVE".equals(new String(var3, 8, 4))) {
                System.err.println("Not a WAVE file");
                return;
            }

            var10.readFully(var3, 0, 8);
            String var11 = new String(var3, 0, 4);

            int var12;
            for (var12 = readInt(var3, 4); !var11.equals("data"); var12 = readInt(var3, 4)) {
                var10.readFully(var3, 0, var12);
                if (var11.equals("fmt ")) {
                    if (readShort(var3, 0) != 1) {
                        System.err.println("Not a PCM file");
                        return;
                    }

                    this.channels = readShort(var3, 2);
                    this.sampleRate = readInt(var3, 4);
                    if (readShort(var3, 14) != 16) {
                        System.err.println("Not a 16 bit file " + readShort(var3, 18));
                        return;
                    }

                    if (this.printlevel <= 0) {
                        System.out.println("File Format: PCM wave");
                        System.out.println("Sample Rate: " + this.sampleRate);
                        System.out.println("Channels: " + this.channels);
                    }
                }

                var10.readFully(var3, 0, 8);
                var11 = new String(var3, 0, 4);
            }

            if (this.printlevel <= 0) {
                System.out.println("Data size: " + var12);
            }
        } else {
            if (this.sampleRate < 0) {
                switch (this.mode) {
                    case 0:
                        this.sampleRate = 8000;
                        break;
                    case 1:
                        this.sampleRate = 16000;
                        break;
                    case 2:
                        this.sampleRate = 32000;
                        break;
                    default:
                        this.sampleRate = 8000;
                }
            }

            if (this.printlevel <= 0) {
                System.out.println("File format: Raw audio");
                System.out.println("Sample rate: " + this.sampleRate);
                System.out.println("Channels: " + this.channels);
                System.out.println("Data size: " + var1.length());
            }
        }

        if (this.mode < 0) {
            if (this.sampleRate < 100) {
                this.sampleRate *= 1000;
            }

            if (this.sampleRate < 12000) {
                this.mode = 0;
            } else if (this.sampleRate < 24000) {
                this.mode = 1;
            } else {
                this.mode = 2;
            }
        }

        SpeexEncoder var16 = new SpeexEncoder();
        var16.init(this.mode, this.quality, this.sampleRate, this.channels);
        if (this.complexity > 0) {
            var16.getEncoder().setComplexity(this.complexity);
        }

        if (this.bitrate > 0) {
            var16.getEncoder().setBitRate(this.bitrate);
        }

        if (this.vbr) {
            var16.getEncoder().setVbr(this.vbr);
            if (this.vbr_quality > 0.0F) {
                var16.getEncoder().setVbrQuality(this.vbr_quality);
            }
        }

        if (this.vad) {
            var16.getEncoder().setVad(this.vad);
        }

        if (this.dtx) {
            var16.getEncoder().setDtx(this.dtx);
        }

        if (this.printlevel <= 0) {
            System.out.println(" ");
            System.out.println("Output File: " + var2);
            System.out.println("File format: Ogg Speex");
            System.out.println("Encoder mode: " + (this.mode == 0 ? "Narrowband" : (this.mode == 1 ? "Wideband" : "UltraWideband")));
            System.out.println("Quality: " + (this.vbr ? this.vbr_quality : (float) this.quality));
            System.out.println("Complexity: " + this.complexity);
            System.out.println("Frames per packet: " + this.nframes);
            System.out.println("Varible bitrate: " + this.vbr);
            System.out.println("Voice activity detection: " + this.vad);
            System.out.println("Discontinouous Transmission: " + this.dtx);
        }

        AudioFileWriter var17;
        if (this.destFormat == 1) {
            var17 = new OggSpeexWriter(this.mode, this.sampleRate, this.channels, this.nframes, this.vbr);
        } else if (this.destFormat == 2) {
            this.nframes = PcmWaveWriter.WAVE_FRAME_SIZES[this.mode - 1][this.channels - 1][this.quality];
            var17 = new PcmWaveWriter(this.mode, this.quality, this.sampleRate, this.channels, this.nframes, this.vbr);
        } else {
            var17 = new RawWriter();
        }

        (var17).open(var2);
        (var17).writeHeader("Encoded with: Java Speex Command Line Encoder v0.9.7 ($Revision: 1.5 $)");
        int var13 = 2 * this.channels * var16.getFrameSize();

        try {
            while (true) {
                var10.readFully(var3, 0, this.nframes * var13);

                int var14;
                for (var14 = 0; var14 < this.nframes; ++var14) {
                    var16.processData(var3, var14 * var13, var13);
                }

                var14 = var16.getProcessedData(var3, 0);
                if (var14 > 0) {
                    (var17).writePacket(var3, 0, var14);
                }
            }
        } catch (EOFException var15) {
            (var17).close();
            var10.close();
        }
    }
}