package com.codedead.advancedportchecker.domain.interfaces;

import com.codedead.advancedportchecker.domain.object.ScanProgress;

public interface AsyncResponse {
    void scanComplete();

    void scanCancelled();

    void update(final ScanProgress scanProgress);
}
