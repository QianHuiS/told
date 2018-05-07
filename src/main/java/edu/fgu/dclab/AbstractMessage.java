package edu.fgu.dclab;

public abstract class AbstractMessage implements Message {
    protected String source = "MurMur"; //預設來源是"系統".

    public String getSource() {
        return this.source;
    }
}
