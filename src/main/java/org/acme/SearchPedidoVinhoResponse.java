package org.acme;

import java.util.ArrayList;
import java.util.List;

public class SearchPedidoVinhoResponse {
    public List<PedidoVinho> Pedidos = new ArrayList<>();
    public long TotalPedidos;
    public int TotalPages;
    public boolean HasMore;
    public String NextPage;
}