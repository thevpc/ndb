package net.thevpc.vio2.util;

public class Param {
    private String arg;
    private String key;
    private String value;

    public Param(String n) {
        this.arg=n;
        int x=n.indexOf('=');
        if(x>=0) {
            this.key = n.substring(0,x);
            this.value = n.substring(x+1);
        }else{
            this.key = n;
        }
    }

    public boolean isPair() {
        return value !=null;
    }

    public boolean isNonOption() {
        return !isOption();
    }
    public boolean isOption() {
        return key.startsWith("-");
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(arg);
    }
}
