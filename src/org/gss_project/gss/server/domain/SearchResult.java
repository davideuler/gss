package org.gss_project.gss.server.domain;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: chstath Date: 5/26/11 Time: 2:26 PM To change this template use File | Settings |
 * File Templates.
 */
public class SearchResult {
    /*
     * The total number of results
     */
    private long total;

    /*
     * The results returned
     */
    private List<FileHeader> results;

    public SearchResult(long total, List<FileHeader> results) {
        this.total = total;
        this.results = results;
    }

    public long getTotal() {
        return total;
    }

    public List<FileHeader> getResults() {
        return results;
    }
}
