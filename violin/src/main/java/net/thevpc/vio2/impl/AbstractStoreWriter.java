package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.StoreProgressMonitor;
import net.thevpc.vio2.api.StoreWriter;
import net.thevpc.vio2.model.StoreStructId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public abstract class AbstractStoreWriter implements StoreWriter {
    private boolean compress = false;
    private boolean data = true;
    private long maxRows = -1;
    private LinkedHashSet<StoreStructId> structs = new LinkedHashSet<>();
    private StoreProgressMonitorHelper mon = new StoreProgressMonitorHelper();

    public boolean isCompress() {
        return compress;
    }
    public void addProgressMonitor(StoreProgressMonitor m) {
        mon.addProgressMonitor(m);
    }

    public StoreWriter setCompress(boolean compress) {
        this.compress = compress;
        return this;
    }

    public boolean isData() {
        return data;
    }

    public StoreWriter setData(boolean data) {
        this.data = data;
        return this;
    }

    public long getMaxRows() {
        return maxRows;
    }

    public StoreWriter setMaxRows(long maxRows) {
        this.maxRows = maxRows;
        return this;
    }

    public StoreWriter addStructs(StoreStructId... pred) {
        structs.addAll(pred == null ? new ArrayList<>() :
                Arrays.stream(pred).filter(x -> x != null).collect(Collectors.toList())
        );
        return this;
    }

    @Override
    public StoreWriter addStructs(Collection<StoreStructId> pred) {
        structs.addAll(pred == null ? new ArrayList<>() :
                pred.stream().filter(x -> x != null).collect(Collectors.toList())
        );
        return this;
    }

    protected void incProgress(long[] indexHolder, long max, String message) {
        indexHolder[0]++;
        mon.onProgress(indexHolder[0] * 100.0 / max, message);
    }

    protected LinkedHashSet<StoreStructId> getStructs() {
        return structs;
    }
}
