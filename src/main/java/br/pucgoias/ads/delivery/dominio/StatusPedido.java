package br.pucgoias.ads.delivery.dominio;

import br.pucgoias.ads.delivery.excecao.TransicaoInvalidaException;

/** Ciclo de vida do pedido (ver diagrama de estados do material). */
public enum StatusPedido {
    RECEBIDO,
    EM_PREPARO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO;

    /**
     * TODO (Etapa 2): retornar o status seguinte conforme o diagrama de estados.
     * ENTREGUE e CANCELADO sao estados finais: lancar TransicaoInvalidaException.
     */
    public StatusPedido proximo() {
        throw new UnsupportedOperationException("TODO: implementar StatusPedido.proximo()");
    }

    public boolean permiteCancelamento() {
        return this == RECEBIDO;
    }
}
