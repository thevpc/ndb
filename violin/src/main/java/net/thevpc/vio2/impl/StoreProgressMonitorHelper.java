package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.StoreProgressMonitor;

import java.util.ArrayList;
import java.util.List;

public class StoreProgressMonitorHelper implements StoreProgressMonitor {
    public static final StoreProgressMonitor SILIENT = new StoreProgressMonitor() {
        @Override
        public void onProgress(double progress, String message) {

        }
    };
    private StoreProgressMonitor mon = SILIENT;
    private List<StoreProgressMonitor> mons=new ArrayList<>();

    public void addProgressMonitor(StoreProgressMonitor m){
        if(m!=null){
            mons.add(m);
        }
        if(mons.isEmpty()){
            mon =SILIENT;
        }else if(mons.size()==1){
            mon =mons.get(0);
        }else{
            mon =new StoreProgressMonitor() {
                @Override
                public void onProgress(double progress, String message) {
                    for (StoreProgressMonitor mon : mons) {
                        mon.onProgress(progress, message);
                    }
                }
            };
        }
    }

    @Override
    public void onProgress(double progress, String message) {
        mon.onProgress(progress, message);
    }
}
