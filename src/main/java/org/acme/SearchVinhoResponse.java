package org.acme;

import java.util.ArrayList;
import java.util.List;

public class SearchVinhoResponse {
    public List<Vinho> Vinhos = new ArrayList<>();
    public long TotalVinhos;
    public int TotalPages;
    public boolean HasMore;
    public String NextPage;
}