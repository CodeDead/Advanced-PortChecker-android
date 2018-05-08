package com.codedead.advancedportchecker.interfaces;

import com.codedead.advancedportchecker.domain.ScanProgress;

public interface AsyncResponse {
    void scanComplete();
    void scanCancelled();
    void update(ScanProgress scanProgress);
}
