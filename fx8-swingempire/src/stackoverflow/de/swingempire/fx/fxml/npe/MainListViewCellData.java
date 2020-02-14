/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import java.util.List;
import javafx.collections.FXCollections;

/**
 *
 * @author blj0011
 */
public class MainListViewCellData
{
    private int id;
    private String title;
    private String username;
    private String password;
    private String url;
    private String bibId;
    private String poNumber;
    private boolean statsOnly;
    private boolean hasCounter;
    private boolean hasOtherStats;
    private List<Note> notes;

    public MainListViewCellData(int id, String title, String username, String password, String url, String bibId, String poNumber, boolean statsOnly, boolean hasCounter, boolean hasOtherStats, List<Note> notes)
    {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.url = url;
        this.bibId = bibId;
        this.poNumber = poNumber;
        this.statsOnly = statsOnly;
        this.hasCounter = hasCounter;
        this.hasOtherStats = hasOtherStats;
        this.notes = notes;
    }

    public MainListViewCellData(int id, String title, String username, String password, String url, String bibId, String poNumber, boolean statsOnly, boolean hasCounter, boolean hasOtherStats)
    {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.url = url;
        this.bibId = bibId;
        this.poNumber = poNumber;
        this.statsOnly = statsOnly;
        this.hasCounter = hasCounter;
        this.hasOtherStats = hasOtherStats;
        this.notes = FXCollections.observableArrayList();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getBibId()
    {
        return bibId;
    }

    public void setBibId(String bibId)
    {
        this.bibId = bibId;
    }

    public String getPoNumber()
    {
        return poNumber;
    }

    public void setPoNumber(String poNumber)
    {
        this.poNumber = poNumber;
    }

    public boolean isStatsOnly()
    {
        return statsOnly;
    }

    public void setStatsOnly(boolean statsOnly)
    {
        this.statsOnly = statsOnly;
    }

    public boolean isHasCounter()
    {
        return hasCounter;
    }

    public void setHasCounter(boolean hasCounter)
    {
        this.hasCounter = hasCounter;
    }

    public boolean isHasOtherStats()
    {
        return hasOtherStats;
    }

    public void setHasOtherStats(boolean hasOtherStats)
    {
        this.hasOtherStats = hasOtherStats;
    }

    public List<Note> getNotes()
    {
        return notes;
    }

    public void setNotes(List<Note> notes)
    {
        this.notes = notes;
    }

    @Override
    public String toString()
    {
        return "MainListViewCellData{" + "id=" + id + ", title=" + title + ", username=" + username + ", password=" + password + ", url=" + url + ", bibId=" + bibId + ", poNumber=" + poNumber + ", statsOnly=" + statsOnly + ", hasCounter=" + hasCounter + ", hasOtherStats=" + hasOtherStats + ", notes=" + notes + '}';
    }
}

