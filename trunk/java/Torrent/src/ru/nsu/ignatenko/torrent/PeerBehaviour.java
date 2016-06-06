package ru.nsu.ignatenko.torrent;

public class PeerBehaviour
{
    private boolean isLeecher;
    private boolean isSeeder;
    private boolean isCreator;
    private String pathToFile;
    private String pathToTorrent;

    public void setLeecher(boolean leecher)
    {
        isLeecher = leecher;
    }

    public void setSeeder(boolean seeder)
    {
        isSeeder = seeder;
    }

    public void setCreator(boolean creator)
    {
        isCreator = creator;
    }

    public void setPathToFile(String pathToFile)
    {
        this.pathToFile = pathToFile;
    }

    public void setPathToTorrent(String pathToTorrent)
    {
        this.pathToTorrent = pathToTorrent;
    }

    public boolean isLeecher()
    {
        return isLeecher;
    }

    public boolean isSeeder()
    {
        return isSeeder;
    }

    public boolean isCreator()
    {
        return isCreator;
    }

    public String getPathToTorrent()
    {
        return pathToTorrent;
    }

    public String getPathToFile()
    {
        return pathToFile;
    }
}
