package org.acme;

import java.util.ArrayList;
import java.util.List;

public class SearchVarietalResponse {
    public List<Varietal> Varietais = new ArrayList<>();
    public long TotalVarietais;
    public int TotalPages;
    public boolean HasMore;
    public String NextPage;
}