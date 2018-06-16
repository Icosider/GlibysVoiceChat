package net.gliby.gman;

import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class GMan
{
    public static void launchMod(Logger logger, ModInfo modInfo, String minecraftVersion, String modVersion)
    {
        String url = "https://gitlab.com/Ivasik78/GlibysVoiceChat/blob/master/gvc_updates.json";
        Gson gson = new Gson();

        try (InputStreamReader reader = new InputStreamReader((new URL(url)).openStream()))
        {
            final ModInfo externalInfo = gson.fromJson(reader, ModInfo.class);
            modInfo.donateURL = externalInfo.donateURL;
            modInfo.updateURL = externalInfo.updateURL;
            modInfo.versions = externalInfo.versions;
            modInfo.determineUpdate(modVersion, minecraftVersion);
            logger.info(modInfo.isUpdated()?"Mod is up-to-date.":"Mod is outdated, download latest at " + modInfo.updateURL);
        }
        catch (MalformedURLException urlE)
        {
            urlE.printStackTrace();
        }
        catch (IOException io)
        {
            logger.info("Failed to retrieve mod info, either mod doesn\'t exist or host(" + url + ") is down?");
        }
    }
}