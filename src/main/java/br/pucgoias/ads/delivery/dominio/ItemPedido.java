package br.pucgoias.ads.delivery.dominio;

import java.math.BigDecimal;

/**
 * Item do pedido. Nome e preco sao copiados do cardapio no momento da compra
 * (padrao de referencia estendida).
 *
 * TODO (Etapa 2): garantir que precoUnitario seja gravado como Decimal128.
 */
public record ItemPedido(
        String codigo,
        String nome,
        BigDecimal precoUnitario,
        int quantidade) {

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
