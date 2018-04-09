package com.xk.io;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xiaokang on 2017-05-13.
 */
public class FileMonitor {
    private Timer timer_;
    private HashMap<File, Long> files_;
    private Collection<WeakReference<FileListener>> listeners_;

    public FileMonitor(long pollingInterval)
    {
        this.files_ = new HashMap();

        this.listeners_ = new ArrayList();

        this.timer_ = new Timer(true);
        this.timer_.schedule(new FileMonitorNotifier(), 0L, pollingInterval);
    }

    public void stop()
    {
        this.timer_.cancel();
    }

    public void addFile(File file)
    {
        if (!this.files_.containsKey(file)) {
            long modifiedTime = file.exists() ? file.lastModified() : -1L;
            this.files_.put(file, new Long(modifiedTime));
        }
    }

    public void removeFile(File file)
    {
        this.files_.remove(file);
    }

    public void addListener(FileListener fileListener)
    {
        Iterator i = this.listeners_.iterator();
        while (i.hasNext()) {
            WeakReference reference = (WeakReference) i.next();
            FileListener listener = (FileListener) reference.get();
            if (listener == fileListener) return;

        }

        this.listeners_.add(new WeakReference(fileListener));
    }

    public void removeListener(FileListener fileListener)
    {
        Iterator i = this.listeners_.iterator();
        while (i.hasNext()) {
            WeakReference reference = (WeakReference) i.next();
            FileListener listener = (FileListener) reference.get();
            if (listener == fileListener) {
                i.remove();
                break;
            }
        }
    }

    private class FileMonitorNotifier extends TimerTask {
        private FileMonitorNotifier()
        {
        }

        public void run()
        {
            Collection files = new ArrayList(FileMonitor.this.files_.keySet());

            for (Iterator i = files.iterator(); i.hasNext(); ) {
                File file = (File) i.next();
                long lastModifiedTime = ((Long) FileMonitor.this.files_.get(file)).longValue();
                long newModifiedTime = file.exists() ? file.lastModified() : -1L;

                if (newModifiedTime != lastModifiedTime) {
                    FileMonitor.this.files_.put(file, new Long(newModifiedTime));

                    Iterator j = FileMonitor.this.listeners_.iterator();
                    while (j.hasNext()) {
                        WeakReference reference = (WeakReference) j.next();
                        FileListener listener = (FileListener) reference.get();

                        if (listener == null)
                            j.remove();
                        else
                            listener.fileChanged(file);
                    }
                }
            }
        }
    }
}
