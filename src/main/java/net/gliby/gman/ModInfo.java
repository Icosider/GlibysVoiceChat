package net.gliby.gman;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModInfo
{
    @SerializedName("DonateURL")
    public String donateURL;

    @SerializedName("UpdateURL")
    public String updateURL;

    @SerializedName("Versions")
    public List<String> versions;

    private boolean updated = true;
    public final String modId;

    public ModInfo(String modId, String updateURL)
    {
        this.updateURL = updateURL;
        this.donateURL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PBXHJ67N62ZRW";
        this.modId = modId;
    }

    void determineUpdate(String currentModVersion, String currentMinecraftVersion)
    {
        for (String version : this.versions)
        {
            if (version.startsWith(currentMinecraftVersion))
            {
                this.updated = version.split(":")[1].trim().equals(currentModVersion);
                break;
            }
        }
    }

    public final String getUpdateSite()
    {
        return this.updateURL;
    }

    final boolean isUpdated()
    {
        return this.updated;
    }

    public String toString()
    {
        return "[" + this.modId + "]" + "; Up to date? " + (this.isUpdated() ? "Yes" : "No");
    }

    public final boolean updateNeeded()
    {
        return !this.updated;
    }
}