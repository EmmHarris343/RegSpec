package net.yarr.regspec.Database;

public class DBNodes {
	// This declares the columns for the table RSpecPermNodes 
    private long id;
    private String region;
    private String type;
    private String node;
    private String world;
    
    public long getID() {
        return id;
    }

    public String getREGION() {
        return region;
    }

    public String getTYPE() {
        return type;
    }

    public String getNODE() {
        return node;
    }
    public String getWORLD() {
        return world;
    }
    public void setID(long id) {
        this.id = id;
    }

    public void setREGION(String region) {
        this.region = region;
    }

    public void setTYPE(String type) {
        this.type = type;
    }

    public void setNODE(String node) {
        this.node = node;
    }
    public void setWORLD(String world) {
        this.world = world;
    }
}
