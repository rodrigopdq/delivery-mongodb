package br.pucgoias.ads.delivery.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Raiz do agregado Pedido. O restaurante e referenciado pelo identificador;
 * cliente e itens sao incorporados.
 *
 * TODO (Etapa 2): mapear a classe para a colecao "pedidos":
 *   - identificador gerado pelo MongoDB;
 *   - indice simples em restauranteId;
 *   - total gravado como Decimal128;
 *   - campo de versao para controle otimista de concorrencia.
 */
public class Pedido {

    private String id;

    private String restauranteId;

    private Cliente cliente;

    private List<ItemPedido> itens;

    private StatusPedido status;

    private BigDecimal total;

    private Instant criadoEm;

    private Long versao;

    protected Pedido() {
    }

    /** TODO (Etapa 2): status inicial RECEBIDO; total = soma dos subtotais. */
    public Pedido(String restauranteId, Cliente cliente, List<ItemPedido> itens) {
        this.restauranteId = restauranteId;
        this.cliente = cliente;
        this.itens = List.copyOf(itens);
        this.criadoEm = Instant.now();
    }

    /** TODO (Etapa 2): delegar ao StatusPedido. */
    public void avancarStatus() {
        throw new UnsupportedOperationException("TODO: implementar Pedido.avancarStatus()");
    }

    /** TODO (Etapa 2): somente pedidos RECEBIDO podem ser cancelados. */
    public void cancelar() {
        throw new UnsupportedOperationException("TODO: implementar Pedido.cancelar()");
    }

    public String getId() { return id; }
    public String getRestauranteId() { return restauranteId; }
    public Cliente getCliente() { return cliente; }
    public List<ItemPedido> getItens() { return itens; }
    public StatusPedido getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public Instant getCriadoEm() { return criadoEm; }
    public Long getVersao() { return versao; }
}
