package net.gliby.gman;

import java.io.*;
import java.util.ArrayList;

public class JINIFile extends ArrayList<String>
{
    private final File userFileName;

    public JINIFile(File file) throws IOException
    {
        this.clear();
        this.userFileName = file;
        BufferedReader inbuf;

        if (this.userFileName.exists())
        {
            inbuf = new BufferedReader(new FileReader(this.userFileName));

            while (true)
            {
                String s = inbuf.readLine();

                if (s == null)
                {
                    inbuf.close();
                    break;
                }

                if (!s.startsWith(";"))
                {
                    this.add(s);
                }
            }
        }
        else {
            file.createNewFile();
            inbuf = new BufferedReader(new FileReader(this.userFileName));
            inbuf.close();
        }
    }

    private void addToList(String Section, String key, String value)
    {
        if (this.SectionExist(Section))
        {
            if (this.ValueExist(Section, key))
            {
                int pos = this.ValuePosition(Section, key);
                this.remove(pos);
                this.add(pos, value);
            }
            else {
                this.add(this.SectionPosition(Section) + 1, value);
            }
        }
        else {
            this.add("[" + Section + "]");
            this.add(value);
        }
    }

    public void DeleteKey(String Section, String key)
    {
        if (this.ValuePosition(Section, key) > 0)
        {
            this.remove(this.ValuePosition(Section, key));
        }
    }

    public void EraseSection(String Section)
    {
        int start = this.SectionPosition(Section) + 1;

        if (this.SectionPosition(Section) > -1)
        {
            for (int i = start; i < this.size(); ++i)
            {
                String s = this.get(i);

                if (s.startsWith("[") && s.endsWith("]"))
                {
                    break;
                }
                this.remove(i);
                --i;
            }
            this.remove(this.SectionPosition(Section));
        }
    }

    public boolean ReadBool(String Section, String key, boolean defaultValue) throws JINIFile.JINIReadException
    {
        String s = this.get(this.ValuePosition(Section, key)).substring(key.length() + 1, this.get(this.ValuePosition(Section, key)).length());

        if (this.ValuePosition(Section, key) > 0)
        {
            return Boolean.parseBoolean(s);
        }
        else {
            throw new JINIFile.JINIReadException("ReadBool operation failed: " + s);
        }
    }

    public Float ReadFloat(String Section, String key, Float defaultValue) throws JINIFile.JINIReadException
    {
        new Float(0.0F);

        if (this.ValuePosition(Section, key) > 0)
        {
            int strLen = key.length() + 1;
            return Float.valueOf(this.get(this.ValuePosition(Section, key)).substring(strLen, this.get(this.ValuePosition(Section, key)).length()));
        }
        else {
            throw new JINIFile.JINIReadException("ReadFloat operation failed.");
        }
    }

    public int ReadInteger(String Section, String key, int defaultValue) throws JINIFile.JINIReadException
    {
        if (this.ValuePosition(Section, key) > 0)
        {
            int strLen = key.length() + 1;
            return Integer.parseInt(this.get(this.ValuePosition(Section, key)).substring(strLen, this.get(this.ValuePosition(Section, key)).length()));
        }
        else {
            throw new JINIFile.JINIReadException("ReadInteger operation failed.");
        }
    }

    public ArrayList ReadSection(String Section)
    {
        ArrayList<String> myList = new ArrayList<>();
        int start = this.SectionPosition(Section) + 1;

        if (this.SectionPosition(Section) > -1)
        {
            for (int i = start; i < this.size(); ++i)
            {
                String s = this.get(i);

                if (s.startsWith("[") && s.endsWith("]"))
                {
                    break;
                }
                myList.add(s.substring(0, s.indexOf("=")));
            }
        }
        return myList;
    }

    public ArrayList ReadSections()
    {
        ArrayList<String> list = new ArrayList<>();

        for (int i = 0; i < this.size(); ++i)
        {
            String s = this.get(i);

            if (s.startsWith("[") && s.endsWith("]"))
            {
                list.add(s.substring(1, s.length() - 1));
            }
        }
        return list;
    }

    public ArrayList ReadSectionValues(String Section)
    {
        ArrayList<String> myList = new ArrayList<>();
        int start = this.SectionPosition(Section) + 1;

        if (this.SectionPosition(Section) > -1)
        {
            for (int i = start; i < this.size(); ++i)
            {
                String s = this.get(i).substring(this.get(i).indexOf("=") + 1, this.get(i).length());

                if (s.startsWith("[") && s.endsWith("]"))
                {
                    break;
                }
                myList.add(s);
            }
        }
        return myList;
    }

    public String ReadString(String Section, String key, String defaultValue)
    {
        String value = defaultValue;

        if (this.ValuePosition(Section, key) > 0)
        {
            int e = key.length() + 1;
            value = this.get(this.ValuePosition(Section, key)).substring(e, this.get(this.ValuePosition(Section, key)).length());
        }
        else {
            try
            {
                throw new Exception("Failed to parse");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return value;
    }

    private boolean SectionExist(String Section)
    {
        boolean val = false;

        for (String s : this)
        {
            if (s.equals("[" + Section + "]"))
            {
                val = true;
                break;
            }
        }
        return val;
    }

    private int SectionPosition(String Section)
    {
        int pos = -1;

        for (int i = 0; i < this.size(); ++i)
        {
            String s = this.get(i);

            if (s.equals("[" + Section + "]"))
            {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public boolean UpdateFile()
    {
        try (BufferedWriter ioe = new BufferedWriter(new FileWriter(this.userFileName, false)))
        {
            for (String s : this)
            {
                if (s == null)
                {
                    break;
                }
                ioe.write(s);
                ioe.newLine();
            }
            ioe.close();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private boolean ValueExist(String Section, String key)
    {
        int start = this.SectionPosition(Section);
        boolean val = false;

        for (int i = start + 1; i < this.size(); ++i)
        {
            String s = this.get(i);

            if (s.startsWith(key + "="))
            {
                val = true;
                break;
            }

            if (s.startsWith("[") && s.endsWith("]"))
            {
                break;
            }
        }
        return val;
    }

    private int ValuePosition(String Section, String key)
    {
        int start = this.SectionPosition(Section);
        int pos = -1;

        for (int i = start + 1; i < this.size(); ++i)
        {
            String s = this.get(i);

            if (s.startsWith(key + "="))
            {
                pos = i;
                break;
            }

            if (s.startsWith("[") && s.endsWith("]"))
            {
                break;
            }
        }
        return pos;
    }

    public void WriteBool(String Section, String key, boolean value)
    {
        String s = key + "=" + Boolean.toString(value);
        this.addToList(Section, key, s);
    }

    public void WriteComment(String Section, String comment)
    {
        if (this.SectionExist(Section))
        {
            this.add(this.SectionPosition(Section) + 1, "; " + comment);
        }
    }

    public void WriteFloat(String Section, String key, float value)
    {
        String s = key + "=" + Float.toString(value);
        this.addToList(Section, key, s);
    }

    public void WriteInteger(String Section, String key, int value)
    {
        String s = key + "=" + Integer.toString(value);
        this.addToList(Section, key, s);
    }

    public void WriteString(String Section, String key, String value)
    {
        String s = key + "=" + value;
        this.addToList(Section, key, s);
    }

    public static class JINIReadException extends Exception
    {
        JINIReadException(String string)
        {
            super(string);
        }
    }
}