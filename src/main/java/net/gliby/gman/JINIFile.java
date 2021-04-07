package net.gliby.gman;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JINIFile extends ArrayList<String> {
    private final File userFileName;

    public JINIFile(File file) throws IOException {
        this.clear();
        this.userFileName = file;

        if (this.userFileName.exists()) {
            for (String text : Files.readLines(this.userFileName, StandardCharsets.UTF_8)) {
                if (!text.startsWith(";"))
                    this.add(text);
            }
        } else
            file.createNewFile();
    }

    private void addToList(String Section, String key, String value) {
        if (this.SectionExist(Section)) {
            if (this.ValueExist(Section, key)) {
                final int pos = this.ValuePosition(Section, key);
                this.remove(pos);
                this.add(pos, value);
            } else
                this.add(this.SectionPosition(Section) + 1, value);
        } else {
            this.add("[" + Section + "]");
            this.add(value);
        }
    }

    public void DeleteKey(String Section, String key) {
        if (this.ValuePosition(Section, key) > 0)
            this.remove(this.ValuePosition(Section, key));
    }

    public void EraseSection(String Section) {
        final int start = this.SectionPosition(Section) + 1;

        if (this.SectionPosition(Section) > -1) {
            for (int i = start; i < this.size(); ++i) {
                final String s = this.get(i);

                if (s.startsWith("[") && s.endsWith("]")) {
                    break;
                }
                this.remove(i);
                --i;
            }
            this.remove(this.SectionPosition(Section));
        }
    }

    public boolean ReadBool(String Section, String key, boolean defaultValue) throws JINIFile.JINIReadException {
        final String s = this.get(this.ValuePosition(Section, key)).substring(key.length() + 1);

        if (this.ValuePosition(Section, key) > 0)
            return Boolean.parseBoolean(s);
        else
            throw new JINIFile.JINIReadException("ReadBool operation failed: " + s);
    }

    public Float ReadFloat(String Section, String key, Float defaultValue) throws JINIFile.JINIReadException {
        if (this.ValuePosition(Section, key) > 0) {
            final int strLen = key.length() + 1;
            return Float.valueOf(this.get(this.ValuePosition(Section, key)).substring(strLen));
        } else
            throw new JINIFile.JINIReadException("ReadFloat operation failed.");
    }

    public int ReadInteger(String Section, String key, int defaultValue) throws JINIFile.JINIReadException {
        if (this.ValuePosition(Section, key) > 0) {
            final int strLen = key.length() + 1;
            return Integer.parseInt(this.get(this.ValuePosition(Section, key)).substring(strLen));
        } else
            throw new JINIFile.JINIReadException("ReadInteger operation failed.");
    }

    public List<String> ReadSection(String Section) {
        final List<String> myList = new ArrayList<>();
        final int start = this.SectionPosition(Section) + 1;

        if (this.SectionPosition(Section) > -1) {
            for (int i = start; i < this.size(); ++i) {
                final String s = this.get(i);

                if (s.startsWith("[") && s.endsWith("]"))
                    break;
                myList.add(s.substring(0, s.indexOf("=")));
            }
        }
        return myList;
    }

    public List<String> ReadSections() {
        final List<String> list = new ArrayList<>();

        for (int i = 0; i < this.size(); ++i) {
            final String s = this.get(i);

            if (s.startsWith("[") && s.endsWith("]"))
                list.add(s.substring(1, s.length() - 1));
        }
        return list;
    }

    public List<String> ReadSectionValues(String Section) {
        final List<String> myList = new ArrayList<>();
        final int start = this.SectionPosition(Section) + 1;

        if (this.SectionPosition(Section) > -1) {
            for (int i = start; i < this.size(); ++i) {
                final String s = this.get(i).substring(this.get(i).indexOf("=") + 1);

                if (s.startsWith("[") && s.endsWith("]"))
                    break;
                myList.add(s);
            }
        }
        return myList;
    }

    public String ReadString(String Section, String key, String defaultValue) {
        String value = defaultValue;

        if (this.ValuePosition(Section, key) > 0) {
            final int e = key.length() + 1;
            value = this.get(this.ValuePosition(Section, key)).substring(e);
        } else {
            try {
                throw new Exception("Failed to parse");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    private boolean SectionExist(String Section) {
        boolean val = false;

        for (String s : this) {
            if (s.equals("[" + Section + "]")) {
                val = true;
                break;
            }
        }
        return val;
    }

    private int SectionPosition(String Section) {
        int pos = -1;

        for (int i = 0; i < this.size(); ++i) {
            final String s = this.get(i);

            if (s.equals("[" + Section + "]")) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public boolean UpdateFile() {
        try {
            final String data = String.join("\n", this);
            FileUtils.writeStringToFile(this.userFileName, data, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean ValueExist(String Section, String key) {
        final int start = this.SectionPosition(Section);
        boolean val = false;

        for (int i = start + 1; i < this.size(); ++i) {
            String s = this.get(i);

            if (s.startsWith(key + "=")) {
                val = true;
                break;
            }

            if (s.startsWith("[") && s.endsWith("]"))
                break;
        }
        return val;
    }

    private int ValuePosition(String Section, String key) {
        final int start = this.SectionPosition(Section);
        int pos = -1;

        for (int i = start + 1; i < this.size(); ++i) {
            String s = this.get(i);

            if (s.startsWith(key + "=")) {
                pos = i;
                break;
            }

            if (s.startsWith("[") && s.endsWith("]"))
                break;
        }
        return pos;
    }

    public void WriteBool(String Section, String key, boolean value) {
        final String s = key + "=" + value;
        this.addToList(Section, key, s);
    }

    public void WriteComment(String Section, String comment) {
        if (this.SectionExist(Section))
            this.add(this.SectionPosition(Section) + 1, "; " + comment);
    }

    public void WriteFloat(String Section, String key, float value) {
        final String s = key + "=" + value;
        this.addToList(Section, key, s);
    }

    public void WriteInteger(String Section, String key, int value) {
        final String s = key + "=" + value;
        this.addToList(Section, key, s);
    }

    public void WriteString(String Section, String key, String value) {
        final String s = key + "=" + value;
        this.addToList(Section, key, s);
    }

    public static class JINIReadException extends Exception {
        JINIReadException(String string) {
            super(string);
        }
    }
}
