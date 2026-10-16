package br.pucgoias.ads.delivery.dominio;

import br.pucgoias.ads.delivery.excecao.TransicaoInvalidaException;

/** Ciclo de vida do pedido (ver diagrama de estados do material). */
public enum StatusPedido {
    RECEBIDO,
    EM_PREPARO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO;

    /** Retorna o status seguinte; ENTREGUE e CANCELADO sao estados finais. */
    public StatusPedido proximo() {
        return switch (this) {
            case RECEBIDO -> EM_PREPARO;
            case EM_PREPARO -> SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA -> ENTREGUE;
            case ENTREGUE, CANCELADO -> throw new TransicaoInvalidaException(this, "avancar");
        };
    }

    public boolean permiteCancelamento() {
        return this == RECEBIDO;
    }
}